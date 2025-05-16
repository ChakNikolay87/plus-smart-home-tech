package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = ScenarioAction.TABLE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@ToString(exclude = {"scenario", "sensor", "action"})
@IdClass(ScenarioActionId.class)
public class ScenarioAction {
    public static final String TABLE_NAME = "scenario_actions";
    public static final String SCENARIO_ID = "scenario_id";
    public static final String SENSOR_ID = "sensor_id";
    public static final String ACTION_ID = "action_id";

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
    @JoinColumn(name = ACTION_ID)
    @EqualsAndHashCode.Include
    Action action;
}
