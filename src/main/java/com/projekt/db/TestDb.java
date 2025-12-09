package com.projekt.db;

import java.sql.Connection;
import java.sql.SQLException;
import static com.projekt.db.Db.getConnection;

public class TestDb {

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("polaczono z baza danych");

        } catch (SQLException e) {
            System.err.println("nie polaczono z baza danych");
            e.printStackTrace();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }
}