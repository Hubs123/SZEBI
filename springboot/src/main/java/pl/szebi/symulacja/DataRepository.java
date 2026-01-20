package pl.szebi.symulacja;

import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Klasa odpowiedzialna za zapis i odczyt danych symulacji z bazy danych.
 */
public class DataRepository {
    
    /**
     * Zapisuje rekord symulacji do bazy danych.
     * 
     * @param record rekord symulacji do zapisania
     * @return true jeśli zapis się powiódł, false w przeciwnym razie
     */
    public Boolean save(SimulationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Rekord nie może być null");
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            if (record.getId() == null) {
                // Wstawianie nowego rekordu
                String sql = "INSERT INTO simulation_records " +
                        "(simulation_date, period_number, period_start, period_end, " +
                        "sunlight_intensity, pv_production, " +
                        "energy_stored, battery_level, grid_consumption, grid_feed_in, " +
                        "panel_power, battery_capacity) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                
                ps = Db.conn.prepareStatement(sql);
                ps.setDate(1, java.sql.Date.valueOf(record.getSimulationDate()));
                ps.setInt(2, record.getPeriodNumber());
                ps.setTimestamp(3, Timestamp.valueOf(record.getPeriodStart()));
                ps.setTimestamp(4, Timestamp.valueOf(record.getPeriodEnd()));
                ps.setDouble(5, record.getSunlightIntensity());
                ps.setDouble(6, record.getPvProduction());
                ps.setDouble(7, record.getEnergyStored());
                ps.setDouble(8, record.getBatteryLevel());
                ps.setDouble(9, record.getGridConsumption());
                ps.setDouble(10, record.getGridFeedIn());
                ps.setDouble(11, record.getPanelPower());
                ps.setDouble(12, record.getBatteryCapacity());
                
                rs = ps.executeQuery();
                if (rs.next()) {
                    record.setId(rs.getInt("id"));
                }
                return true;
            } else {
                // Aktualizacja istniejącego rekordu
                String sql = "UPDATE simulation_records SET " +
                        "simulation_date = ?, period_number = ?, period_start = ?, period_end = ?, " +
                        "sunlight_intensity = ?, pv_production = ?, " +
                        "energy_stored = ?, battery_level = ?, grid_consumption = ?, grid_feed_in = ?, " +
                        "panel_power = ?, battery_capacity = ? " +
                        "WHERE id = ?";
                
                ps = Db.conn.prepareStatement(sql);
                ps.setDate(1, java.sql.Date.valueOf(record.getSimulationDate()));
                ps.setInt(2, record.getPeriodNumber());
                ps.setTimestamp(3, Timestamp.valueOf(record.getPeriodStart()));
                ps.setTimestamp(4, Timestamp.valueOf(record.getPeriodEnd()));
                ps.setDouble(5, record.getSunlightIntensity());
                ps.setDouble(6, record.getPvProduction());
                ps.setDouble(7, record.getEnergyStored());
                ps.setDouble(8, record.getBatteryLevel());
                ps.setDouble(9, record.getGridConsumption());
                ps.setDouble(10, record.getGridFeedIn());
                ps.setDouble(11, record.getPanelPower());
                ps.setDouble(12, record.getBatteryCapacity());
                ps.setInt(13, record.getId());
                
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
    
    /**
     * Zapisuje listę rekordów symulacji do bazy danych.
     * 
     * @param records lista rekordów do zapisania
     * @return true jeśli wszystkie zapisy się powiodły, false w przeciwnym razie
     */
    public Boolean saveAll(List<SimulationRecord> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }

        LocalDate date = records.get(0).getSimulationDate();
        deleteByDate(date);

        boolean allSuccess = true;
        for (SimulationRecord record : records) {
            if (!save(record)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }
    
    /**
     * Pobiera wszystkie rekordy symulacji dla podanej daty.
     * 
     * @param date data symulacji
     * @return lista rekordów symulacji dla danej daty, posortowana według numeru okresu
     */
    public List<SimulationRecord> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data nie może być null");
        }
        
        List<SimulationRecord> records = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT * FROM simulation_records " +
                        "WHERE simulation_date = ? " +
                        "ORDER BY period_number";
            
            ps = Db.conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(date));
            rs = ps.executeQuery();
            
            while (rs.next()) {
                SimulationRecord record = mapResultSetToRecord(rs);
                records.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
        
        return records;
    }
    
    /**
     * Pobiera rekord symulacji po ID.
     * 
     * @param id identyfikator rekordu
     * @return rekord symulacji lub null jeśli nie znaleziono
     */
    public SimulationRecord findById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID nie może być null");
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT * FROM simulation_records WHERE id = ?";
            ps = Db.conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToRecord(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
        
        return null;
    }
    
    /**
     * Usuwa wszystkie rekordy symulacji dla podanej daty.
     * 
     * @param date data symulacji
     * @return true jeśli usunięcie się powiodło, false w przeciwnym razie
     */
    public Boolean deleteByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Data nie może być null");
        }
        
        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM simulation_records WHERE simulation_date = ?";
            ps = Db.conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(date));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Mapuje wynik zapytania SQL na obiekt SimulationRecord.
     */
    private SimulationRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        SimulationRecord record = new SimulationRecord();
        
        // Pobierz dostępne kolumny
        ResultSetMetaData metaData = rs.getMetaData();
        Set<String> columns = new HashSet<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columns.add(metaData.getColumnName(i).toLowerCase());
        }
        
        record.setId(rs.getInt("id"));
        record.setSimulationDate(rs.getDate("simulation_date").toLocalDate());
        record.setPeriodNumber(rs.getInt("period_number"));
        
        Timestamp periodStart = rs.getTimestamp("period_start");
        if (periodStart != null) {
            record.setPeriodStart(periodStart.toLocalDateTime());
        }
        
        Timestamp periodEnd = rs.getTimestamp("period_end");
        if (periodEnd != null) {
            record.setPeriodEnd(periodEnd.toLocalDateTime());
        }
        
        record.setSunlightIntensity(rs.getDouble("sunlight_intensity"));
        record.setPvProduction(rs.getDouble("pv_production"));
        record.setEnergyStored(rs.getDouble("energy_stored"));
        record.setBatteryLevel(rs.getDouble("battery_level"));
        
        // Grid consumption i feed-in - mogą nie istnieć w starych tabelach
        if (columns.contains("grid_consumption")) {
            record.setGridConsumption(rs.getDouble("grid_consumption"));
        } else {
            record.setGridConsumption(0.0); // Domyślnie 0 jeśli kolumna nie istnieje
        }
        
        if (columns.contains("grid_feed_in")) {
            record.setGridFeedIn(rs.getDouble("grid_feed_in"));
        } else {
            record.setGridFeedIn(0.0); // Domyślnie 0 jeśli kolumna nie istnieje
        }
        
        record.setPanelPower(rs.getDouble("panel_power"));
        record.setBatteryCapacity(rs.getDouble("battery_capacity"));
        
        return record;
    }

    public Boolean saveToEnergyStats(SimulationRecord record) {
        if (record == null) {
            return false;
        }

        String sql = "insert into energy_stats (device_id, start_time, end_time, avg_power_w, daily_kwh, annual_kwh, min_power_w, max_power_w)"
                    + "values (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = Db.conn.prepareStatement(sql)) {
            ps.setInt(1, 71);
            ps.setTimestamp(2, Timestamp.valueOf(record.getPeriodStart()));
            ps.setTimestamp(3, Timestamp.valueOf(record.getPeriodEnd()));

            double avgPowerW = (record.getGridConsumption() / 4.0) * 1000.0;
            ps.setDouble(4, avgPowerW);

            ps.setDouble(5, record.getGridConsumption());

            ps.setDouble(6, record.getGridConsumption());
            ps.setDouble(7, avgPowerW * 0.9);
            ps.setDouble(8, avgPowerW * 1.1);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Błąd zapisu do energy_stats: " + e.getMessage());
            return false;
        }
    }

    public Boolean saveToMeasurements(SimulationRecord record) {
        if (record == null) {
            return false;
        }

        String sql = "insert into measurements (device_id, timestamp, power_output_w, grid_feed_in_kwh, grid_consumption_kwh) " +
                "values (?, ?, ?, ?, ?)";


        try (PreparedStatement ps = Db.conn.prepareStatement(sql)) {
            ps.setInt(1, 71);
            ps.setTimestamp(2, Timestamp.valueOf(record.getPeriodStart()));

            double powerW = (record.getPvProduction() / 4.0) * 1000.0;
            ps.setDouble(3, powerW);

            ps.setDouble(4, record.getGridFeedIn());
            ps.setDouble(5, record.getGridConsumption());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Błąd zapisu do measurements: " + e.getMessage());
            return false;
        }
    }
}
