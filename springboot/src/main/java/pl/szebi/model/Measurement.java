package pl.szebi.model;

import java.time.LocalDateTime;

public class Measurement {
    private Integer id;
    private LocalDateTime timestamp;
    private Double powerOutput;
    private Double gridFeedIn;
    private Double gridConsumption;

    public Measurement() {
    }

    public Measurement(Integer id, LocalDateTime timestamp, Double powerOutput, Double gridFeedIn, Double gridConsumption) {
        this.id = id;
        this.timestamp = timestamp;
        this.powerOutput = powerOutput;
        this.gridFeedIn = gridFeedIn;
        this.gridConsumption = gridConsumption;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(Double powerOutput) {
        this.powerOutput = powerOutput;
    }

    public Double getGridFeedIn() {
        return gridFeedIn;
    }

    public void setGridFeedIn(Double gridFeedIn) {
        this.gridFeedIn = gridFeedIn;
    }

    public Double getGridConsumption() {
        return gridConsumption;
    }

    public void setGridConsumption(Double gridConsumption) {
        this.gridConsumption = gridConsumption;
    }
}

