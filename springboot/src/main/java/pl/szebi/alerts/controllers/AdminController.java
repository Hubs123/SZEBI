package pl.szebi.alerts.controllers;

import pl.szebi.alerts.AutomaticReaction;
import pl.szebi.alerts.DeviceGroup;
import pl.szebi.alerts.DeviceGroupRepository;
import pl.szebi.alerts.Threshold;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
@CrossOrigin(origins = "*")
public class AdminController {

    private final DeviceGroupRepository deviceGroupRepository = new DeviceGroupRepository();

    private DeviceGroup findGroup(Integer groupId) {
        return deviceGroupRepository.getAll().stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/groups")
    public List<DeviceGroup> getAllGroups() {
        return deviceGroupRepository.getAll();
    }

    @GetMapping("/groups/{groupId}/thresholds")
    public List<Threshold> getThresholds(@PathVariable Integer groupId) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return List.of();
        return group.getAllThresholds();
    }

    @PostMapping("/groups/{groupId}/thresholds")
    public Boolean addThreshold(
            @PathVariable Integer groupId,
            @RequestBody Threshold threshold
    ) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return false;

        return group.addThreshold(threshold);
    }

    @PutMapping("/groups/{groupId}/thresholds/{thresholdId}")
    public Boolean updateThreshold(
            @PathVariable Integer groupId,
            @PathVariable Integer thresholdId,
            @RequestBody ThresholdUpdateRequest request
    ) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return false;

        Threshold threshold = group.getThresholdById(thresholdId);
        if (threshold == null) return false;

        // 1. Aktualizacja liczb
        boolean success = group.modifyThreshold(threshold, request.valueWarning, request.valueEmergency);

        // 2. Obsługa Reakcji z logiką Parzyste/Nieparzyste
        if (success) {
            if (request.reactionName != null && !request.reactionName.isEmpty()) {

                boolean isTurnOn = "turnOn".equals(request.reactionName);

                // Znajdź obecne maksymalne ID w grupie
                int maxId = group.getAllReactions().stream()
                        .mapToInt(AutomaticReaction::getId)
                        .max().orElse(0);

                // Szukamy następnego wolnego ID, które pasuje do reguły
                int nextId = maxId + 1;
                while (true) {
                    boolean isEven = (nextId % 2 == 0);

                    // Jeśli chcemy włączyć (parzyste) i mamy parzyste -> OK
                    // Jeśli chcemy wyłączyć (nieparzyste) i mamy nieparzyste -> OK
                    if ((isTurnOn && isEven) || (!isTurnOn && !isEven)) {
                        break;
                    }
                    nextId++;
                }

                // Tworzymy nową reakcję z wyliczonym ID
                AutomaticReaction newReaction = new AutomaticReaction(nextId, request.reactionName);
                group.addReaction(newReaction);
                threshold.setReactionId(nextId);

            } else if ("".equals(request.reactionName)) {
                // Pusty string = usunięcie reakcji
                threshold.setReactionId(null);
            }
        }

        return success;
    }

    static class ThresholdUpdateRequest {
        public Float valueWarning;
        public Float valueEmergency;
        public String reactionName;
    }

    @DeleteMapping("/groups/{groupId}/thresholds/{thresholdId}")
    public Boolean deleteThreshold(
            @PathVariable Integer groupId,
            @PathVariable Integer thresholdId
    ) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return false;

        Threshold threshold = group.getThresholdById(thresholdId);
        if (threshold == null) return false;

        return group.getAllThresholds().remove(threshold);
    }
}
