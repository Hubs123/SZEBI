package pl.szebi.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.szebi.model.EnergyStats;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryEnergyStatsRepositoryTest {

    private InMemoryEnergyStatsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEnergyStatsRepository();
    }

    @Test
    void testSave_NewStats() {
        // Given
        EnergyStats stats = new EnergyStats(
            null, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            3.5, 84.0, 30660.0, 2.0, 5.0
        );

        // When
        EnergyStats saved = repository.save(stats);

        // Then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(1, saved.getId());
        assertEquals(1, saved.getSensorId());
    }

    @Test
    void testSave_ExistingStats() {
        // Given
        EnergyStats stats = new EnergyStats(
            1, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            3.5, 84.0, 30660.0, 2.0, 5.0
        );

        // When
        EnergyStats saved = repository.save(stats);

        // Then
        assertNotNull(saved);
        assertEquals(1, saved.getId());
    }

    @Test
    void testGet_ExistingId() {
        // Given
        EnergyStats stats = new EnergyStats(
            null, 1, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            3.5, 84.0, 30660.0, 2.0, 5.0
        );
        EnergyStats saved = repository.save(stats);

        // When
        Optional<EnergyStats> retrieved = repository.get(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals(saved.getId(), retrieved.get().getId());
    }

    @Test
    void testGet_NonExistingId() {
        // When
        Optional<EnergyStats> retrieved = repository.get(999);

        // Then
        assertFalse(retrieved.isPresent());
    }
}

