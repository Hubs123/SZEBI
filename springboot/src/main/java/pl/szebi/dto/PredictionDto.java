package pl.szebi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class PredictionDto {
    private Integer id;
    private Integer sensorId;

    private OffsetDateTime timestamp;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate predictedForDate;

    private Double value;
    private Integer modelId;

    public PredictionDto() {
    }

    public PredictionDto(Integer id, Integer sensorId, OffsetDateTime timestamp, LocalDate predictedForDate, Double value, Integer modelId) {
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

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
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
