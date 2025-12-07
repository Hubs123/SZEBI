package com.projekt.sterowanie;

import com.projekt.db.Db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {
    private List<Room> rooms = new ArrayList<>();

    public Boolean add(Room room) {
        return rooms.add(room);
    }

    public Boolean save(Room room) throws SQLException {
        PreparedStatement ps = Db.conn.prepareStatement(
                "insert into rooms values (default, ?)");
        ps.setString(1, room.getName());
        return ps.executeUpdate() == 1;
    }

    public Boolean delete(int roomId) {
        return rooms.remove(roomId) != null;
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
        return rooms;
    }
}
