package pl.szebi.dto;

import jakarta.validation.constraints.NotNull;

public class PredictionRequest {
    @NotNull
    private Integer sensorId;

    @NotNull
    private Integer modelId;

    @NotNull
    private String modelType; // FastAPI oczekuje stringa: "SIMPLE_AVG", "MOVING_AVG", "LINEAR_TREND"

    private Integer historyDays;

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Integer getHistoryDays() {
        return historyDays;
    }

    public void setHistoryDays(Integer historyDays) {
        this.historyDays = historyDays;
    }
}

