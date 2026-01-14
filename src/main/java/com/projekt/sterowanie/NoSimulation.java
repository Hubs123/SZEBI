package com.projekt.sterowanie;

public class NoSimulation implements SimulationModel {
    @Override
    public void tick(Device device) {
        // rodzaj urządzenia noSimulation - nie ma stanów, które zmieniają się w czasie
        // stany mogą zostać zmienione tylko przez mieszkańca domu
        // na przykład: żarówki, "smart" okna, ale nie urządzenia z czujnikami (odczyty wymagają symulacji)
    }
}
