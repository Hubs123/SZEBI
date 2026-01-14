package com.projekt.alerts.controllers;

import com.projekt.alerts.AutomaticReaction;
import com.projekt.alerts.DeviceGroup;
import com.projekt.alerts.DeviceGroupRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance/alerts")
@CrossOrigin(origins = "*")
public class MaintenanceController {
    // access for ENGINEER and ADMIN
    private final DeviceGroupRepository deviceGroupRepository = new DeviceGroupRepository();

    @PostMapping("/groups/{groupId}/reactions")
    public Boolean addReaction(
            @PathVariable Integer groupId,
            @RequestBody AutomaticReaction reaction
    ) {
        DeviceGroup group = deviceGroupRepository.getById(groupId);
        if (group == null) return false;

        return group.addReaction(reaction);
    }

    @DeleteMapping("/groups/{groupId}/reactions/{reactionId}")
    public Boolean removeReaction(
            @PathVariable Integer groupId,
            @PathVariable Integer reactionId
    ) {
        DeviceGroup group = deviceGroupRepository.getById(groupId);
        if (group == null) return false;

        return group.getReactions()
                .removeIf(r -> r.getId().equals(reactionId));
    }
}
