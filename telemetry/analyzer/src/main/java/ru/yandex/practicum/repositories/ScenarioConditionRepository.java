package ru.yandex.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.entities.ScenarioConditions;

@Repository
public interface ScenarioConditionRepository extends JpaRepository<ScenarioConditions, Long> {

    void deleteBySensorId(String sensorId);

}