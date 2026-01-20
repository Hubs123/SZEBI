package pl.szebi.optimization;

import pl.szebi.time.TimeControl;

public class OptimizationTests {
    public static void main(String[] args) {
//        OptimizationManager optimizationManager = new OptimizationManager();
        OptimizationData optimizationData = new OptimizationData();
        System.out.println(optimizationData);
        optimizationData.loadFromDatabase();
        System.out.println(optimizationData);
    }
}
