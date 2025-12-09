package pl.szebi.dto;

import java.time.OffsetDateTime;

public class EnergyStatsDto {
    private Integer id;
    private Integer sensorId;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private Double avg;
    private Double daily;
    private Double annual;
    private Double min;
    private Double max;

    public EnergyStatsDto() {
    }

    public EnergyStatsDto(Integer id, Integer sensorId, OffsetDateTime startTime, OffsetDateTime endTime, Double avg, Double daily, Double annual, Double min, Double max) {
        this.id = id;
        this.sensorId = sensorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.avg = avg;
        this.daily = daily;
        this.annual = annual;
        this.min = min;
        this.max = max;
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

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Double getDaily() {
        return daily;
    }

    public void setDaily(Double daily) {
        this.daily = daily;
    }

    public Double getAnnual() {
        return annual;
    }

    public void setAnnual(Double annual) {
        this.annual = annual;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }
}
