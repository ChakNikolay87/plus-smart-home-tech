package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = Sensor.TABLE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class Sensor {
    public static final String TABLE_NAME = "sensors";
    public static final String ID = "id";
    public static final String HUB_ID = "hub_id";

    @Id
    @Column(name = ID)
    String id;

    @Column(name = HUB_ID)
    String hubId;
}
