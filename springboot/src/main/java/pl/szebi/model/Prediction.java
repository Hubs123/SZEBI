package pl.szebi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prediction {
    private Integer id;
    private Integer sensorId;
    private LocalDateTime timestamp;
    private LocalDate predictedForDate;
    private Double value;
    private Integer modelId;

    public Prediction() {
    }

    public Prediction(Integer id, Integer sensorId, LocalDateTime timestamp, LocalDate predictedForDate, Double value, Integer modelId) {
        this.id = id;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.predictedForDate = predictedForDate;
        this.value = value;
        this.modelId = modelId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDate getPredictedForDate() {
        return predictedForDate;
    }

    public void setPredictedForDate(LocalDate predictedForDate) {
        this.predictedForDate = predictedForDate;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }
}

