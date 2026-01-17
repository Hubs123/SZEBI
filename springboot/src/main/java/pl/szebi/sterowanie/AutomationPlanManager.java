package pl.szebi.sterowanie;

import pl.szebi.time.TimeControl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

public class AutomationPlanManager {
    private final AutomationPlanRepository planRepo = new AutomationPlanRepository();
    private static volatile AutomationPlan currentPlan = null;
    private static final Object TW_LOCK = new Object();
    private static final Map<String, TimeWindowJob> activeTimeWindowJobs = new HashMap<>();

    // helperki
    private static void lockDevices(List<AutomationRule> rules) {
        for (AutomationRule rule : rules) {
            Device d = DeviceManager.getDevice(rule.getDeviceId());
            if (d != null) d.emergencyLock();
        }
    }

    private static List<Pair<Integer, Map<String, Float>>> toDeviceStates(List<AutomationRule> rules) {
        List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
        for (AutomationRule r : rules) {
            devicesStates.add(new Pair<>(r.getDeviceId(), r.getStates()));
        }
        return devicesStates;
    }

    private static List<Pair<Integer, Map<String, Float>>> toDeviceStates(Map<Integer, Map<String, Float>> snapshot) {
        List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Float>> e : snapshot.entrySet()) {
            devicesStates.add(new Pair<>(e.getKey(), e.getValue()));
        }
        return devicesStates;
    }

    private static Map<Integer, Map<String, Float>> snapshotBefore(List<AutomationRule> rules) {
        Map<Integer, Map<String, Float>> before = new HashMap<>();
        for (AutomationRule r : rules) {
            Device d = DeviceManager.getDevice(r.getDeviceId());
            if (d == null) continue;

            Map<String, Float> current = d.getStates();
            Map<String, Float> snap = new HashMap<>();
            for (String key : r.getStates().keySet()) {
                Float v = current.get(key);
                if (v != null) snap.put(key, v);
            }
            if (!snap.isEmpty()) before.put(r.getDeviceId(), snap);
        }
        return before;
    }

    private static void waitUntil(Instant target) {
        while (!Thread.currentThread().isInterrupted()) {
            Instant now = TimeControl.now();
            if (!now.isBefore(target)) return;
            long nanos = Duration.between(now, target).toNanos();
            LockSupport.parkNanos(Math.min(nanos, 50_000_000L)); // max ~50ms
        }
    }

    private static final DateTimeFormatter TW_FMT = DateTimeFormatter.ofPattern("H:mm");

    private static class TimeWindowJob {
        final String timeWindow;
        final List<AutomationRule> rules;
        final boolean emergency;

        volatile Thread thread;
        volatile boolean applied = false;
        volatile Map<Integer, Map<String, Float>> before = null;

        TimeWindowJob(String timeWindow, List<AutomationRule> rules, boolean emergency) {
            this.timeWindow = timeWindow;
            this.rules = rules;
            this.emergency = emergency;
        }

        void start() {
            Thread t = new Thread(this::run, "timeWindow-" + timeWindow);
            t.setDaemon(true);
            this.thread = t;
            t.start();
        }

        void cancel() {
            if (applied && before != null) {
                DeviceManager.applyCommands(toDeviceStates(before), true);
            }
            Thread t = thread;
            if (t != null) t.interrupt();
        }

        void run() {
            Instant startI;
            Instant endI;
            try {
                String[] p = timeWindow.split("-");
                LocalTime startT = LocalTime.parse(p[0].trim(), TW_FMT);
                LocalTime endT = LocalTime.parse(p[1].trim(), TW_FMT);

                ZoneId zone = ZoneId.systemDefault();
                ZonedDateTime nowZ = ZonedDateTime.ofInstant(TimeControl.now(), zone);

                startI = nowZ.withHour(startT.getHour()).withMinute(startT.getMinute()).withSecond(0).withNano(0).toInstant();
                endI = nowZ.withHour(endT.getHour()).withMinute(endT.getMinute()).withSecond(0).withNano(0).toInstant();
            } catch (Exception e) {
                return;
            }
            try {
                Instant now = TimeControl.now();

                if (!now.isBefore(endI)) return;

                if (now.isBefore(startI)) {
                    waitUntil(startI);
                }

                now = TimeControl.now();
                if (now.isBefore(endI)) {
                    before = snapshotBefore(rules);
                    boolean ok = DeviceManager.applyCommands(toDeviceStates(rules), emergency);
                    if (!ok) return;

                    applied = true;
                    if (emergency) lockDevices(rules);
                }

                waitUntil(endI);
                if (applied && before != null) {
                    DeviceManager.applyCommands(toDeviceStates(before));
                }
            } finally {
                synchronized (TW_LOCK) {
                    TimeWindowJob cur = activeTimeWindowJobs.get(timeWindow);
                    if (cur == this) {
                        activeTimeWindowJobs.remove(timeWindow);
                    }
                }
            }
        }
    }

    public Integer createPlan(String name, List<AutomationRule> rules) {
        if (rules == null) return null;
        AutomationPlan plan = new AutomationPlan(name, rules);
        boolean added = planRepo.add(plan);
        return added ? plan.getId() : null;
    }

    public Boolean removePlan(Integer planId) {
        return planRepo.delete(planId);
    }

    public AutomationPlan getCurrentPlan() {
        return currentPlan;
    }

    public Boolean activatePlan(Integer planId) {
        AutomationPlan plan = planRepo.findById(planId);
        if (plan == null) return false;
        List<AutomationRule> rules = plan.getRules();
        List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
        for (AutomationRule rule : rules) {
            devicesStates.add(new Pair<>(rule.getDeviceId(), rule.getStates()));
        }
        Boolean commandApplied = DeviceManager.applyCommands(devicesStates);
        if (commandApplied) {
            currentPlan = plan;
        }
        else {
            currentPlan = null;
        }
        return commandApplied;
    }

    public Boolean saveToDatabase(AutomationPlan plan) {
        return planRepo.save(plan);
    }

    static public Boolean applyModifications(List<AutomationRule> rules, Integer priority) {
        final boolean emergency = priority > 0;
        boolean hasTimeWindow = rules.get(0).getTimeWindow() != null;

        // bezterminowo
        if (!hasTimeWindow) {
            List<Pair<Integer, Map<String, Float>>> devicesStates = new ArrayList<>();
            for (AutomationRule rule : rules) {
                devicesStates.add(new Pair<>(rule.getDeviceId(), rule.getStates()));
            }
            if (currentPlan == null || currentPlan.getName().equals("temp")) {
                AutomationPlan tempPlan = new AutomationPlan("temp", rules);
                if (DeviceManager.applyCommands(devicesStates, emergency)) {
                    currentPlan = tempPlan;
                    if (emergency) lockDevices(rules);
                    return true;
                }
                // tymczasowy plan utworzony na bazie poleceń od modułu optymalizacji
            }
            boolean commandApplied = DeviceManager.applyCommands(devicesStates);
            if (commandApplied && emergency) lockDevices(rules);
            return commandApplied;
        }

        // obsługa timeWindow
        Map<String, List<AutomationRule>> groups = new HashMap<>();
        for (AutomationRule r : rules) {
            groups.computeIfAbsent(r.getTimeWindow(), k -> new ArrayList<>()).add(r);
        }

        List<TimeWindowJob> newJobs = new ArrayList<>();
        for (Map.Entry<String, List<AutomationRule>> e : groups.entrySet()) {
            newJobs.add(new TimeWindowJob(e.getKey(), e.getValue(), emergency));
        }

        // wątki przez prawie cały czas życia tylko czuwają, obciążenie nie jest duże
        synchronized (TW_LOCK) {
            for (TimeWindowJob j : activeTimeWindowJobs.values()) {
                j.cancel();
            }
            activeTimeWindowJobs.clear();
            for (TimeWindowJob j : newJobs) {
                activeTimeWindowJobs.put(j.timeWindow, j);
            }
        }
        for (TimeWindowJob j : newJobs) {
            j.start();
        }

        return true; // asynchronicznie
    }

    public List<AutomationPlan> listPlans() {
        return planRepo.findAll();
    }

    public AutomationPlan getPlan(Integer planId) {
        return planRepo.findById(planId);
    }

    public Boolean addRule(Integer planId, AutomationRule rule) {
        AutomationPlan plan = planRepo.findById(planId);
        if (plan == null || rule == null) return false;
        plan.getRules().add(rule);
        return planRepo.save(plan);
    }
}