package pl.szebi.dto;

public class PredictionResponse {
    private PredictionDto prediction;
    private String plotUrl;
    private Boolean saveSucceeded;

    public PredictionResponse() {
    }

    public PredictionResponse(PredictionDto prediction, String plotUrl, Boolean saveSucceeded) {
        this.prediction = prediction;
        this.plotUrl = plotUrl;
        this.saveSucceeded = saveSucceeded;
    }

    public PredictionDto getPrediction() {
        return prediction;
    }

    public void setPrediction(PredictionDto prediction) {
        this.prediction = prediction;
    }

    public String getPlotUrl() {
        return plotUrl;
    }

    public void setPlotUrl(String plotUrl) {
        this.plotUrl = plotUrl;
    }

    public Boolean getSaveSucceeded() {
        return saveSucceeded;
    }

    public void setSaveSucceeded(Boolean saveSucceeded) {
        this.saveSucceeded = saveSucceeded;
    }
}
