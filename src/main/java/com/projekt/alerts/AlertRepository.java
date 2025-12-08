package com.projekt.alerts;

import java.util.List;

public class AlertRepository {
    private List<Alert> alerts;

    public AlertRepository() {
    }

    public List<Alert> getAllAlerts() {
        return alerts;
    }

    public Alert getAlertById(int id) {
        for (Alert a : alerts) {
            if (a.getAlertId() == id) {
                return a;
            }
        }
        return null;
    }

    // docelowo createAlert, tworzy obiekt Alert i go dodaje
    public Boolean addAlert(Alert alert) {
        try {
            alerts.add(alert);}
        catch (Exception e) {
            return false;
        }
        // find się zrobił w konstruktorze który tu będzie
        // teraz można wywołać helper wywołujący reakcję jeśli wyszło emergency
        return true;
    }

    public Boolean deleteAlert(int id) {
        for (Alert r : alerts) {
            if (r.getAlertId() == id) {
                try {
                    alerts.remove(r);}
                catch (Exception e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

//    DO ZROBIENIA
//    public Boolean sendToDataBase(alerty.Alert alert) {}
}