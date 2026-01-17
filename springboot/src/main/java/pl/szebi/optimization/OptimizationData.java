package pl.szebi.optimization;

import java.util.Date;
import java.util.List;

public class OptimizationData {
    private Date timestamp;
    private List<Float> forecastConsumed;
    private List<Float> forecastSold;
    private List<Float> forecastStored;
    private List<Float> forecastGenerated;
    private List<Float> forecastTemperature;

    public boolean loadForecast(
            Date timestamp,
            List<Float> forecastConsumed,
            List<Float> forecastSold,
            List<Float> forecastStored,
            List<Float> forecastGenerated
    ) {
        try {
            this.timestamp = timestamp;
            this.forecastConsumed = forecastConsumed;
            this.forecastSold = forecastSold;
            this.forecastStored = forecastStored;
            this.forecastGenerated = forecastGenerated;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<Float> getForecastConsumed() {
        return forecastConsumed;
    }

    public List<Float> getForecastSold() {
        return forecastSold;
    }

    public List<Float> getForecastStored() {
        return forecastStored;
    }

    public List<Float> getForecastGenerated() {
        return forecastGenerated;
    }

    public List<Float> getForecastTemperature() {
        return forecastTemperature;
    }
}
