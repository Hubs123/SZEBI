package com.projekt.sterowanie;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DeviceRepository {
    private final List<Device> devices = Collections.synchronizedList(new ArrayList<>());

    public Boolean save(Device device) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // wstawia nowy rekord
            if (device.getId() == null) {
                String sql = "INSERT INTO devices (name, type, room_id) "
                        + "VALUES (?, ?, ?) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, device.getName());
                ps.setString(2, device.getType().toString());
                if (device.getRoomId() != null)
                    ps.setInt(3, device.getRoomId());
                else
                    ps.setNull(3, java.sql.Types.INTEGER);
                rs = ps.executeQuery();
                if (rs.next()) {
                    device.setId(rs.getInt("id"));
                }
                return true;
            } else {
                // update istniejący rekord
                String sql = "UPDATE devices "
                        + "SET name = ?, type = ?, room_id = ? "
                        + "WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, device.getName());
                ps.setString(2, device.getType().toString());
                if (device.getRoomId() != null)
                    ps.setInt(3, device.getRoomId());
                else
                    ps.setNull(3, java.sql.Types.INTEGER);
                ps.setInt(4, device.getId());
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

    public Boolean add(Device device) {
        if (device == null) return false;
        return devices.add(device);
    }

    public boolean delete(Integer deviceId) {
        synchronized (devices) {
            Iterator<Device> it = devices.iterator();
            while (it.hasNext()) {
                Device d = it.next();
                if (deviceId.equals(d.getId())) {
                    d.stopTicking();
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    public Device findById(Integer deviceId) {
        synchronized (devices) {
            for (Device d : devices) {
                if (d.getId() != null && d.getId().equals(deviceId)) {
                    return d;
                }
            }
        }
        return null;
    }

    public List<Device> findByRoom(Integer roomId) {
        if (roomId == null) return Collections.emptyList();

        List<Device> result = new ArrayList<>();
        synchronized (devices) {
            for (Device d : devices) {
                if (roomId.equals(d.getRoomId())) {
                    result.add(d);
                }
            }
        }
        return result;
    }

    public List<Device> findAll() {
        synchronized (devices) {
            return new ArrayList<>(devices);
        }
    }

    public List<Device> findByType(DeviceType type) {
        if (type == null) return Collections.emptyList();

        List<Device> result = new ArrayList<>();
        synchronized (devices) {
            for (Device d : devices) {
                if (type.equals(d.getType())) {
                    result.add(d);
                }
            }
        }
        return result;
    }
}
