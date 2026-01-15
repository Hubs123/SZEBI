//package com.projekt.alerts.controllers;
//
//import com.projekt.alerts.Alert;
//import com.projekt.alerts.AlertManager;
//import com.projekt.alerts.Priority;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/alerts")
//@CrossOrigin(origins = "*")
//public class AlertController {
//
//    private final AlertManager alertManager = new AlertManager();
//
//    @GetMapping
//    public List<Alert> getAlertsByRole(@RequestParam String role) {
//
//        List<Alert> alerts = alertManager.listAlerts();
//
//        switch (role.toUpperCase()) {
//            case "RESIDENT":
//                return alerts;
//
//            case "ENGINEER":
//                return alerts.stream()
//                        .filter(a ->
//                                a.getPriority() == Priority.Warning ||
//                                        a.getPriority() == Priority.Emergency)
//                        .collect(Collectors.toList());
//
//            case "ADMIN":
//                return alerts.stream()
//                        .filter(a -> a.getPriority() == Priority.Information)
//                        .collect(Collectors.toList());
//
//            default:
//                return List.of();
//        }
//    }
//}
