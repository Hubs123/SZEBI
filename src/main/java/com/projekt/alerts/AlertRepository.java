package com.projekt.alerts;

import com.projekt.db.Db;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AlertRepository {
    private List<Alert> alerts;

    public List<Alert> getAll() {
        return alerts;
    }

    public Alert getById(int id) {
        for (Alert a : alerts) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }

    // docelowo createAlert, tworzy obiekt Alert i go dodaje
    public Boolean add(Alert alert) {
        try {
            alerts.add(alert);}
        catch (Exception e) {
            return false;
        }
        // find się zrobił w konstruktorze który tu będzie
        // teraz można wywołać helper wywołujący reakcję jeśli wyszło emergency
        return true;
    }

    public Boolean delete(int id) {
        for (Alert r : alerts) {
            if (r.getId() == id) {
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
    public Boolean saveAlertToDB(Alert alert) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (alert.getId() != null) {
                String sql = "INSERT INTO alerts (date, anomaly_value, anomaly_type, device_id)" + "VALUES (?, ?, ?, ?) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setDate(1, (Date) alert.getAlertDate());
                ps.setFloat(2, alert.getAnomalyValue());
                ps.setString(3, alert.getAnomalyType());
                if (alert.getDeviceId() != null) {
                    ps.setInt(4, alert.getDeviceId());
                }
                else {
                    ps.setNull(3, java.sql.Types.INTEGER);
                }
                rs = ps.executeQuery();
                if (rs.next()) {
                    alert.setId(rs.getInt("id"));
                }
                return true;
            }
            else {
                String sql = "UPDATE alerts" + "SET date = ?, anomaly_value = ?, anomaly_type = ?, device_id = ? WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setDate(1, (Date) alert.getAlertDate());
                ps.setFloat(2, alert.getAnomalyValue());
                ps.setString(3, alert.getAnomalyType());
                ps.setInt(4, alert.getDeviceId());
                ps.setInt(5, alert.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }
}