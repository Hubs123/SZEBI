package pl.szebi.repository;

import pl.szebi.model.Prediction;

import java.util.Optional;

public interface PredictionRepository {
    Prediction save(Prediction prediction);
    Optional<Prediction> get(Integer id);
}

