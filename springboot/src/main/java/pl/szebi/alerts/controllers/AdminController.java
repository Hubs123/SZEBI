package pl.szebi.alerts.controllers;

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
