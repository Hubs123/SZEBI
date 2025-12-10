package pl.szebi.dto;

import java.util.Map;

public class AnalysisResponse {
    private EnergyStatsDto stats;
    private Integer reportId;
    private Map<String, String> plots;
    private Boolean saveSucceeded;

    public AnalysisResponse() {
    }

    public AnalysisResponse(EnergyStatsDto stats, Integer reportId, Map<String, String> plots, Boolean saveSucceeded) {
        this.stats = stats;
        this.reportId = reportId;
        this.plots = plots;
        this.saveSucceeded = saveSucceeded;
    }

    public EnergyStatsDto getStats() {
        return stats;
    }

    public void setStats(EnergyStatsDto stats) {
        this.stats = stats;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Map<String, String> getPlots() {
        return plots;
    }

    public void setPlots(Map<String, String> plots) {
        this.plots = plots;
    }

    public Boolean getSaveSucceeded() {
        return saveSucceeded;
    }

    public void setSaveSucceeded(Boolean saveSucceeded) {
        this.saveSucceeded = saveSucceeded;
    }
}
