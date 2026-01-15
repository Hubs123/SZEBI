package com.projekt.alerts.controllers;

import com.projekt.alerts.AutomaticReaction;
import com.projekt.alerts.DeviceGroup;
import com.projekt.alerts.DeviceGroupRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance/alerts")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final DeviceGroupRepository deviceGroupRepository = new DeviceGroupRepository();

    private DeviceGroup findGroup(Integer groupId) {
        return deviceGroupRepository.getAll().stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/groups/{groupId}/reactions")
    public List<AutomaticReaction> getReactions(@PathVariable Integer groupId) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return List.of();
        return group.getAllReactions();
    }

    @PostMapping("/groups/{groupId}/reactions")
    public Boolean addReaction(
            @PathVariable Integer groupId,
            @RequestBody AutomaticReaction reaction
    ) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return false;

        return group.addReaction(reaction);
    }

    @DeleteMapping("/groups/{groupId}/reactions/{reactionId}")
    public Boolean deleteReaction(
            @PathVariable Integer groupId,
            @PathVariable Integer reactionId
    ) {
        DeviceGroup group = findGroup(groupId);
        if (group == null) return false;

        AutomaticReaction reaction = group.getReactionById(reactionId);
        if (reaction == null) return false;

        return group.getAllReactions().remove(reaction);
    }
}
