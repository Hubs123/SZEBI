package com.projekt.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    private static final String URL_BASE = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");
    private static final String SCHEMA = System.getenv("DB_SCHEMA");

    private static final String FINAL_URL;
    static {
        FINAL_URL = String.format("%s?user=%s&password=%s&sslmode=require&currentSchema=%s",
                URL_BASE, USER, PASSWORD, SCHEMA);
    }

    private static Connection getConnection() throws SQLException {
        if (URL_BASE == null || USER == null || PASSWORD == null) {

            throw new IllegalStateException("Brak konfiguracji bazy danych w zmiennych środowiskowych");
        }
        return DriverManager.getConnection(FINAL_URL);
    }

    public static Connection conn;
    static {
        try {
            conn = getConnection();
        } catch (SQLException e) {

            throw new RuntimeException("Nie można połączyć się z bazą danych", e);
        }
    }
}

