package com.projekt.sterowanie;

public enum DeviceType {
    noSimulation(NoSimulation.class),
    thermometer(ThermometerSimulation.class),
    smokeDetector(SmokeDetectorSimulation.class);

    // enum przechowuje referencję do klasy symulacji odpowiadającej typowi urządzenia
    private final Class<? extends SimulationModel> modelClass;
    DeviceType(Class<? extends SimulationModel> modelClass) {
        this.modelClass = modelClass;
    }
    SimulationModel newModelInstance() {
        try {
            return modelClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate SimulationModel for " + this, e);
        }
    }
}