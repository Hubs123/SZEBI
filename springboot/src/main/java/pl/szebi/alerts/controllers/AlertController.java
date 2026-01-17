package pl.szebi.alerts.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    // Spring automatycznie wstrzyknie tutaj wartości z application.properties
    @Value("${app.db.url}")
    private String dbUrl;

    @Value("${app.db.user}")
    private String dbUser;

    @Value("${app.db.password}")
    private String dbPassword;

    @GetMapping
    public List<SimpleAlert> getAlertsByRole(@RequestParam(defaultValue = "RESIDENT") String role) {
        List<SimpleAlert> alerts = new ArrayList<>();

        try {
            // Wymuszamy załadowanie sterownika PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Używamy zmiennych wczytanych z pliku konfiguracyjnego
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM alerts ORDER BY alert_date DESC")) {

                while (rs.next()) {
                    SimpleAlert alert = new SimpleAlert();
                    alert.setId(rs.getInt("id"));
                    alert.setAlertDate(rs.getTimestamp("alert_date"));
                    alert.setAnomalyValue(rs.getFloat("anomaly_value"));
                    alert.setAnomalyType(rs.getString("anomaly_type"));

                    String prio = rs.getString("priority");
                    alert.setPriority(prio != null ? prio : "Information");

                    int devId = rs.getInt("device_id");
                    alert.setDeviceId(rs.wasNull() ? null : devId);

                    // Tworzymy czytelną wiadomość dla frontendu
                    String msg = "Alert ID: " + alert.getId() +
                            "\nTyp: " + alert.getAnomalyType() +
                            "\nWartość: " + alert.getAnomalyValue() +
                            "\nPriorytet: " + alert.getPriority();
                    alert.setMessage(msg);

                    alerts.add(alert);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // W razie błędu konfiguracji wyświetlamy go na liście alertów
            SimpleAlert errorAlert = new SimpleAlert();
            errorAlert.setId(0);
            errorAlert.setAlertDate(new Date());
            errorAlert.setAnomalyType("BŁĄD KONFIGURACJI");
            errorAlert.setPriority("Emergency");
            errorAlert.setMessage("Błąd połączenia z bazą: " + e.getMessage() + "\nSprawdź zmienne środowiskowe w IntelliJ!");
            alerts.add(errorAlert);
        }

        return alerts;
    }

    // Prosta klasa do przesyłania danych (DTO), niezależna od reszty systemu
    public static class SimpleAlert {
        public Integer id;
        public Date alertDate;
        public Float anomalyValue;
        public String anomalyType;
        public String priority;
        public Integer deviceId;
        public String message;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Date getAlertDate() { return alertDate; }
        public void setAlertDate(Date alertDate) { this.alertDate = alertDate; }
        public Float getAnomalyValue() { return anomalyValue; }
        public void setAnomalyValue(Float anomalyValue) { this.anomalyValue = anomalyValue; }
        public String getAnomalyType() { return anomalyType; }
        public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public Integer getDeviceId() { return deviceId; }
        public void setDeviceId(Integer deviceId) { this.deviceId = deviceId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}