package pl.szebi.optimization;

import pl.szebi.db.Db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OptimizationData {
    private Date timestamp;
    private List<Double> forecastConsumed;
    private List<Double> forecastSold;
    private List<Double> forecastStored;
    private List<Double> forecastGenerated;

    public boolean loadFromDatabase() {
        String sql = "SELECT period_start, " +
                "grid_consumption, " +
                "grid_feed_in, " +
                "battery_level, " +
                "pv_production " +
                "FROM simulation_records " +
                "ORDER BY simulation_date DESC, period_number ASC LIMIT 6";

        // POPRAWKA: Nie deklarujemy 'Connection conn' wewnątrz try(),
        // używamy Db.conn bezpośrednio w prepareStatement.
        // Dzięki temu connection NIE zostanie zamknięte po wykonaniu zapytania.
        try (PreparedStatement statement = Db.conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            this.forecastConsumed = new ArrayList<>();
            this.forecastSold = new ArrayList<>();
            this.forecastStored = new ArrayList<>();
            this.forecastGenerated = new ArrayList<>();

            int counter = 0;

            while (rs.next()) {
                if (counter == 0) {
                    // Bezpieczne pobieranie daty z SQL
                    Timestamp ts = rs.getTimestamp("period_start");
                    if (ts != null) {
                        this.timestamp = new Date(ts.getTime());
                    }
                }
                this.forecastConsumed.add(rs.getDouble("grid_consumption"));
                this.forecastSold.add(rs.getDouble("grid_feed_in"));
                this.forecastStored.add(rs.getDouble("battery_level"));
                this.forecastGenerated.add(rs.getDouble("pv_production"));
                counter++;
            }

            return !this.forecastConsumed.isEmpty();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Błąd w OptimizationData: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<Double> getForecastConsumed() {
        return forecastConsumed;
    }

    public List<Double> getForecastSold() {
        return forecastSold;
    }

    public List<Double> getForecastStored() {
        return forecastStored;
    }

    public List<Double> getForecastGenerated() {
        return forecastGenerated;
    }

    @Override
    public String toString() {
        return "OptimizationData{" +
                "timestamp=" + timestamp +
                ", forecastConsumed=" + forecastConsumed +
                ", forecastSold=" + forecastSold +
                ", forecastStored=" + forecastStored +
                ", forecastGenerated=" + forecastGenerated +
                '}';
    }
}