package pl.szebi.model;

import java.time.LocalDateTime;
import java.util.Map;

public class Report {
    private Integer id;
    private String type;
    private Integer sensorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer statsId;
    private Integer predictionId;
    private String textSummary;
    private Map<String, String> plotsPaths;
    private LocalDateTime createdAt;

    public Report() {
    }

    public Report(Integer id, String type, Integer sensorId, LocalDateTime startTime, LocalDateTime endTime, Integer statsId, Integer predictionId, String textSummary, Map<String, String> plotsPaths, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.sensorId = sensorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.statsId = statsId;
        this.predictionId = predictionId;
        this.textSummary = textSummary;
        this.plotsPaths = plotsPaths;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getStatsId() {
        return statsId;
    }

    public void setStatsId(Integer statsId) {
        this.statsId = statsId;
    }

    public Integer getPredictionId() {
        return predictionId;
    }

    public void setPredictionId(Integer predictionId) {
        this.predictionId = predictionId;
    }

    public String getTextSummary() {
        return textSummary;
    }

    public void setTextSummary(String textSummary) {
        this.textSummary = textSummary;
    }

    public Map<String, String> getPlotsPaths() {
        return plotsPaths;
    }

    public void setPlotsPaths(Map<String, String> plotsPaths) {
        this.plotsPaths = plotsPaths;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

