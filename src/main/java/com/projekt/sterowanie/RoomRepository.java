package com.projekt.sterowanie;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomRepository {
    private final List<Room> rooms = new ArrayList<>();

    public Boolean add(Room room) {
        return rooms.add(room);
    }

    public Boolean save(Room room) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (room.getId() == null) {
                String sql = "INSERT INTO rooms (name) VALUES (?) RETURNING id";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, room.getName());
                rs = ps.executeQuery();
                if (rs.next()) {
                    room.setId(rs.getInt("id"));
                }
                return true;
            } else {
                String sql = "UPDATE rooms SET name = ? WHERE id = ?";
                ps = Db.conn.prepareStatement(sql);
                ps.setString(1, room.getName());
                ps.setInt(2, room.getId());
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

    public Boolean delete(int roomId) {
        return rooms.removeIf(r -> r.getId() == roomId);
    }

    public Room findById(int roomId) {
        for (Room r : rooms) {
            if (r.getId() != null && r.getId().equals(roomId)) {
                return r;
            }
        }
        return null;
    }

    public List<Room> findAll() {
        return Collections.unmodifiableList(rooms);
    }
}
