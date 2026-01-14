package pl.szebi.repository;

import pl.szebi.model.Report;

import java.util.Optional;

public interface ReportRepository {
    Report save(Report report);
    Optional<Report> get(Integer id);
}

