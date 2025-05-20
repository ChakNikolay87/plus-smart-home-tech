package ru.yandex.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entities.ScenarioAction;

@Repository
public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, Long> {

    void deleteBySensorId(String sensorId);

    void deleteByScenarioId(Long scenarioId);
}