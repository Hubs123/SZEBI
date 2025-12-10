package pl.szebi.repository;

import pl.szebi.model.EnergyStats;

import java.util.Optional;

public interface EnergyStatsRepository {
    EnergyStats save(EnergyStats stats);
    Optional<EnergyStats> get(Integer id);
}

