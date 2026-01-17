package pl.szebi.sterowanie.api;

import pl.szebi.sterowanie.Device;
import pl.szebi.sterowanie.DeviceManager;
import pl.szebi.sterowanie.DeviceType;
import pl.szebi.sterowanie.Room;

import pl.szebi.sterowanie.api.ControlDtos.CreateDeviceRequest;
import pl.szebi.sterowanie.api.ControlDtos.UpdateDeviceStatesRequest;
import pl.szebi.sterowanie.api.ControlDtos.AssignRoomRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/control/devices")
@CrossOrigin
public class DevicesController {

    private final DeviceManager deviceManager;

    DevicesController() {
        deviceManager = new DeviceManager();
        deviceManager.loadDevicesFromDataBase();
        deviceManager.loadRoomsFromDataBase();
    }

    @GetMapping
    public List<Device> listDevices() {
        return deviceManager.listDevices();
    }

    @PostMapping
    public ResponseEntity<?> createDevice(@RequestBody CreateDeviceRequest req) {
        if (req == null || req.name == null || req.name.isBlank() || req.type == null) {
            return ResponseEntity.badRequest().body("Niepoprawne dane urządzenia.");
        }
        Device d = deviceManager.registerDevice(req.name, req.type, req.roomId);
        if (d == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się utworzyć urządzenia.");
        }
        if (!deviceManager.saveDeviceToDatabase(d)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nie udało się utworzyć urządzenia.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(d);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(@PathVariable Integer deviceId) {
        boolean ok = deviceManager.removeDevice(deviceId);
        return ok ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono urządzenia.");
    }

    @GetMapping("/{deviceId}/states")
    public ResponseEntity<?> getStates(@PathVariable Integer deviceId) {
        Map<String, Float> states = deviceManager.getStates(deviceId);
        if (states == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono urządzenia.");
        return ResponseEntity.ok(states);
    }

    @PutMapping("/{deviceId}/states")
    public ResponseEntity<?> updateStates(@PathVariable Integer deviceId, @RequestBody UpdateDeviceStatesRequest req) {
        if (req == null || req.states == null || req.states.isEmpty()) {
            return ResponseEntity.badRequest().body("Brak parametrów do ustawienia.");
        }
        boolean ok = deviceManager.sendCommand(deviceId, req.states);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Brak możliwości zmiany parametrów - parametry ustawione przez procedurę alarmową.");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{deviceId}/room")
    public ResponseEntity<?> assignRoom(@PathVariable Integer deviceId, @RequestBody AssignRoomRequest req) {
        Device d = DeviceManager.getDevice(deviceId);
        if (d == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono urządzenia.");

        Integer roomId = (req == null) ? null : req.roomId;
        if (roomId != null) {
            Room r = DeviceManager.getRoom(roomId);
            if (r == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Brak pokoi");
            }
        }

        d.setRoomId(roomId);
        deviceManager.saveDeviceToDatabase(d);

        return ResponseEntity.ok(d);
    }

    @GetMapping("/types")
    public DeviceType[] listTypes() {
        return DeviceType.values();
    }
}
