package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = ScenarioCondition.TABLE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@ToString(exclude = {"scenario", "sensor", "condition"})
@IdClass(ScenarioConditionId.class)
public class ScenarioCondition {
    public static final String TABLE_NAME = "scenario_conditions";
    public static final String SCENARIO_ID = "scenario_id";
    public static final String SENSOR_ID = "sensor_id";
    public static final String CONDITION_ID = "condition_id";

    @Id
    @ManyToOne
    @JoinColumn(name = SCENARIO_ID)
    @EqualsAndHashCode.Include
    Scenario scenario;

    @Id
    @ManyToOne
    @JoinColumn(name = SENSOR_ID)
    @EqualsAndHashCode.Include
    Sensor sensor;

    @Id
    @ManyToOne
    @JoinColumn(name = CONDITION_ID)
    @EqualsAndHashCode.Include
    Condition condition;
}
