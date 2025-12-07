package com.projekt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {

    private static final String URL_BASE = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    private static final String SCHEMA = System.getenv("DB_SCHEMA");

    private static final String FINAL_URL;

    static {
        FINAL_URL = String.format("%s?user=%s&password=%s&sslmode=require&currentSchema=%s",
                URL_BASE, USER, PASSWORD, SCHEMA);
    }

    public static Connection getConnection() throws SQLException {
        if (URL_BASE == null || USER == null || PASSWORD == null) {
            throw new IllegalStateException();
        }

        return DriverManager.getConnection(FINAL_URL);
    }

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