package ru.yandex.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table(name = "sensors")
@NoArgsConstructor
@AllArgsConstructor
public class Sensor {
    @Id
    private String id;

    private String hubId;
}
