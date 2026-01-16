package pl.szebi.model;

public class PredictionConfig {
    private Integer movingAvgWindowDays = 7;
    private Integer historyDaysForLinear = 30;

    public PredictionConfig() {
    }

    public PredictionConfig(Integer movingAvgWindowDays, Integer historyDaysForLinear) {
        this.movingAvgWindowDays = movingAvgWindowDays;
        this.historyDaysForLinear = historyDaysForLinear;
    }

    public Integer getMovingAvgWindowDays() {
        return movingAvgWindowDays;
    }

    public void setMovingAvgWindowDays(Integer movingAvgWindowDays) {
        this.movingAvgWindowDays = movingAvgWindowDays;
    }

    public Integer getHistoryDaysForLinear() {
        return historyDaysForLinear;
    }

    public void setHistoryDaysForLinear(Integer historyDaysForLinear) {
        this.historyDaysForLinear = historyDaysForLinear;
    }
}

