package pl.szebi.optimization;

import pl.szebi.time.TimeControl;

public class OptimizationTests {
    public static void main(String[] args) {
        OptimizationManager optimizationManager = new OptimizationManager();
        OptimizationData optimizationData = new OptimizationData();
        optimizationData.loadFromDatabase(TimeControl.now());
    }
}
