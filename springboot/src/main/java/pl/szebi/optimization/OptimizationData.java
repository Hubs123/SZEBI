package pl.szebi.optimization;

import pl.szebi.db.Db;
import pl.szebi.time.TimeControl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OptimizationData {
    private Date timestamp;
    private List<Double> forecastConsumed;  // Odpowiada: grid_consumption
    private List<Double> forecastSold;      // Odpowiada: grid_feed_in
    private List<Double> forecastStored;    // Odpowiada: battery_level (stan naładowania)
    private List<Double> forecastGenerated; // Odpowiada: pv_production

    public boolean loadFromDatabase() {
        String sql = "SELECT " +
                "grid_consumption, " +
                "grid_feed_in, " +
                "battery_level, " +
                "pv_production " +
                "FROM simulation_records " +
                "ORDER BY simulation_date DESC, period_number ASC LIMIT 6";

        try (PreparedStatement statement = Db.conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            this.forecastConsumed = new ArrayList<>();
            this.forecastSold = new ArrayList<>();
            this.forecastStored = new ArrayList<>();
            this.forecastGenerated = new ArrayList<>();

            while (rs.next()) {
                this.forecastConsumed.add(rs.getDouble("grid_consumption"));
                this.forecastSold.add(rs.getDouble("grid_feed_in"));
                this.forecastStored.add(rs.getDouble("battery_level"));
                this.forecastGenerated.add(rs.getDouble("pv_production"));
            }

            // Zwracamy true tylko, jeśli faktycznie pobrano dane
            this.timestamp = Date.from(TimeControl.now());
            return !this.forecastConsumed.isEmpty();

        } catch (SQLException e) {
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
