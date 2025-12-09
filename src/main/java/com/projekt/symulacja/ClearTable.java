package com.projekt.symulacja;

import com.projekt.db.Db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Klasa do usunięcia i ponownego utworzenia tabeli simulation_records
 * z aktualnymi kolumnami (włącznie z grid_consumption i grid_feed_in).
 */
public class RecreateSimulationTable {
    
    public static void main(String[] args) {
        try {
            // Użycie istniejącego połączenia z klasy Db
            Connection conn = Db.conn;
            
            System.out.println("=".repeat(50));
            System.out.println("Odtwarzanie tabeli simulation_records");
            System.out.println("=".repeat(50));
            
            // Usunięcie tabeli, jeśli istnieje
            dropTable(conn);
            
            // Utworzenie tabeli z aktualnymi kolumnami
            createTable(conn);
            
            System.out.println("\n✓ Tabela simulation_records została pomyślnie odtworzona!");
            System.out.println("  Kolumny: id, simulation_date, period_number, period_start, period_end,");
            System.out.println("           sunlight_intensity, pv_production, energy_stored, battery_level,");
            System.out.println("           grid_consumption, grid_feed_in, panel_power, battery_capacity");
            
        } catch (Exception e) {
            System.err.println("\n✗ Błąd podczas odtwarzania tabeli: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Usuwa tabelę simulation_records, jeśli istnieje.
     * 
     * @param conn połączenie z bazą danych
     */
    private static void dropTable(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute("DROP TABLE IF EXISTS simulation_records CASCADE");
            System.out.println("✓ Usunięto tabelę simulation_records (jeśli istniała)");
        } catch (SQLException e) {
            System.err.println("Błąd podczas usuwania tabeli: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // Ignoruj błędy zamykania
                }
            }
        }
    }
    
    /**
     * Tworzy tabelę simulation_records z aktualnymi kolumnami.
     * 
     * @param conn połączenie z bazą danych
     * @throws SQLException jeśli wystąpi błąd SQL
     */
    private static void createTable(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            
            // Tworzenie tabeli z wszystkimi kolumnami
            String createTableSQL = 
                "CREATE TABLE simulation_records (" +
                "    id SERIAL PRIMARY KEY, " +
                "    simulation_date DATE NOT NULL, " +
                "    period_number INTEGER NOT NULL CHECK (period_number >= 1 AND period_number <= 6), " +
                "    period_start TIMESTAMP NOT NULL, " +
                "    period_end TIMESTAMP NOT NULL, " +
                "    sunlight_intensity DOUBLE PRECISION NOT NULL CHECK (sunlight_intensity >= 0.0 AND sunlight_intensity <= 1.0), " +
                "    pv_production DOUBLE PRECISION NOT NULL CHECK (pv_production >= 0.0), " +
                "    energy_stored DOUBLE PRECISION NOT NULL CHECK (energy_stored >= 0.0), " +
                "    battery_level DOUBLE PRECISION NOT NULL CHECK (battery_level >= 0.0), " +
                "    grid_consumption DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (grid_consumption >= 0.0), " +
                "    grid_feed_in DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (grid_feed_in >= 0.0), " +
                "    panel_power DOUBLE PRECISION NOT NULL CHECK (panel_power > 0.0), " +
                "    battery_capacity DOUBLE PRECISION NOT NULL CHECK (battery_capacity > 0.0), " +
                "    UNIQUE(simulation_date, period_number)" +
                ")";
            
            stmt.execute(createTableSQL);
            System.out.println("✓ Utworzono tabelę simulation_records");
            
            // Tworzenie indeksów
            String createIndex1SQL = "CREATE INDEX idx_simulation_date ON simulation_records(simulation_date)";
            stmt.execute(createIndex1SQL);
            System.out.println("✓ Utworzono indeks idx_simulation_date");
            
            String createIndex2SQL = "CREATE INDEX idx_simulation_date_period ON simulation_records(simulation_date, period_number)";
            stmt.execute(createIndex2SQL);
            System.out.println("✓ Utworzono indeks idx_simulation_date_period");
            
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Błąd podczas zamykania Statement: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Metoda pomocnicza do wywołania z innej klasy.
     * Usuwa i odtwarza tabelę simulation_records.
     * 
     * @param conn połączenie z bazą danych
     * @return true jeśli operacja się powiodła, false w przeciwnym razie
     */
    public static boolean recreateTable(Connection conn) {
        try {
            dropTable(conn);
            createTable(conn);
            return true;
        } catch (Exception e) {
            System.err.println("Błąd podczas odtwarzania tabeli: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

