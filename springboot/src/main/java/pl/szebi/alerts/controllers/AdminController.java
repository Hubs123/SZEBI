package pl.szebi.alerts.controllers;

import pl.szebi.alerts.DeviceGroup;
import pl.szebi.alerts.DeviceGroupRepository;
import pl.szebi.alerts.Threshold;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
// @CrossOrigin(origins = "*")
public class AdminController {

    private final DeviceGroupRepository deviceGroupRepository = new DeviceGroupRepository();

    /**
     * Dostęp:
     * - ADMIN
     */

    @GetMapping("/groups")
    public List<DeviceGroup> getAllGroups() {
        return deviceGroupRepository.getAll();
    }

    @PostMapping("/groups/{groupId}/thresholds")
    public Boolean addThreshold(
            @PathVariable Integer groupId,
            @RequestBody Threshold threshold
    ) {
        DeviceGroup group = deviceGroupRepository.getById(groupId);
        if (group == null) return false;

        return group.addThreshold(threshold);
    }

    @DeleteMapping("/groups/{groupId}/thresholds/{thresholdId}")
    public Boolean removeThreshold(
            @PathVariable Integer groupId,
            @PathVariable Integer thresholdId
    ) {
        DeviceGroup group = deviceGroupRepository.getById(groupId);
        if (group == null) return false;

        return group.getThresholds()
                .removeIf(t -> t.getId().equals(thresholdId));
    }
}
