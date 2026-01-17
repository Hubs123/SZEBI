package pl.szebi.sterowanie.api;

import pl.szebi.sterowanie.Device;
import pl.szebi.sterowanie.DeviceManager;
import pl.szebi.sterowanie.Room;

import pl.szebi.sterowanie.api.ControlDtos.CreateRoomRequest;
import pl.szebi.sterowanie.api.ControlDtos.GroupCommandRequest;
import pl.szebi.sterowanie.api.ControlDtos.GroupCommandResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/control/rooms")
@CrossOrigin
public class RoomsController {

    private final DeviceManager deviceManager = new DeviceManager();

    @GetMapping
    public List<Room> listRooms() {
        return deviceManager.getRooms();
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return ResponseEntity.badRequest().body("Niepoprawna nazwa pokoju.");
        }
        Room r = deviceManager.registerRoom(req.name, null);
        if (r == null) return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się utworzyć pokoju.");
        return ResponseEntity.status(HttpStatus.CREATED).body(r);
    }

    @GetMapping("/{roomId}/devices")
    public List<Device> listRoomDevices(@PathVariable Integer roomId) {
        return deviceManager.listRoomDevices(roomId);
    }

    /**
     * "Zastosowanie polecenia dla wszystkich urządzeń danego typu w pokoju"
     * Jeśli brak urządzeń w pokoju (dla danego typu) -> UI pokaże komunikat.
     */
    @PostMapping("/{roomId}/group-command")
    public ResponseEntity<?> applyGroupCommand(@PathVariable Integer roomId, @RequestBody GroupCommandRequest req) {
        if (req == null || req.type == null || req.states == null || req.states.isEmpty()) {
            return ResponseEntity.badRequest().body("Niepoprawne dane polecenia grupowego.");
        }

        List<Device> devices = deviceManager.listRoomDevices(roomId);
        if (devices == null) devices = Collections.emptyList();

        List<Device> targets = devices.stream()
                .filter(d -> d != null && d.getType() == req.type)
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            // brak urządzeń w pokoju danego typu
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Wyświetlenie komunikatu o braku urządzeń w pokoju");
        }

        List<Integer> applied = new ArrayList<>();
        List<Integer> locked = new ArrayList<>();
        List<Integer> missing = new ArrayList<>();

        for (Device d : targets) {
            if (d == null) continue;
            try {
                boolean ok = deviceManager.sendCommand(d.getId(), req.states);
                if (ok) applied.add(d.getId());
                else locked.add(d.getId()); // np. emergency/alarm lock
            } catch (Exception e) {
                missing.add(d.getId());
            }
        }

        GroupCommandResult res = new GroupCommandResult();
        res.appliedDeviceIds = applied;
        res.lockedDeviceIds = locked;
        res.missingDeviceIds = missing;

        return ResponseEntity.ok(res);
    }
}
