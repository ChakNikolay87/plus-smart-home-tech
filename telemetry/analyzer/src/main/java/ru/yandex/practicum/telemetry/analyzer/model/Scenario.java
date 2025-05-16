package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
        name = Scenario.TABLE_NAME,
        uniqueConstraints = {@UniqueConstraint(columnNames = {Scenario.HUB_ID, Scenario.NAME})}
)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class Scenario {
    public static final String TABLE_NAME = "scenarios";
    public static final String ID = "id";
    public static final String HUB_ID = "hub_id";
    public static final String NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID)
    Long id;

    @Column(name = HUB_ID)
    String hubId;

    @Column(name = NAME)
    String name;
}
