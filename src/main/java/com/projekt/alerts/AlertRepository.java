package com.projekt.alerts;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertRepository {
    private List<Alert> alerts = new ArrayList<>();

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

    public Boolean add(Alert alert) {
        try {
            alerts.add(alert);}
        catch (Exception e) {
            return false;
        }
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

    public Boolean saveToDB(Alert alert) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (alert.getId() == null) {
                String sql = "INSERT INTO alerts (alert_date, anomaly_value, anomaly_type, device_id, priority) "
                        + "VALUES (?, ?, ?, ?, ?) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setTimestamp(1, new java.sql.Timestamp(alert.getAlertDate().getTime()));
                ps.setFloat(2, alert.getAnomalyValue());
                ps.setString(3, alert.getAnomalyType());
                if (alert.getDeviceId() != null) {
                    ps.setInt(4, alert.getDeviceId());
                }
                else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setString(5, alert.getPriority().toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    alert.setId(rs.getInt("id"));
                }
                return true;
            }
            else {
                String sql = "UPDATE alerts "
                        + "SET alert_date = ?, anomaly_value = ?, anomaly_type = ?, device_id = ?, priority = ? "
                        + "WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setTimestamp(1, new java.sql.Timestamp(alert.getAlertDate().getTime()));
                ps.setFloat(2, alert.getAnomalyValue());
                ps.setString(3, alert.getAnomalyType());
                ps.setInt(4, alert.getDeviceId());
                ps.setString(5, alert.getPriority().toString());
                ps.setInt(6, alert.getId());
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