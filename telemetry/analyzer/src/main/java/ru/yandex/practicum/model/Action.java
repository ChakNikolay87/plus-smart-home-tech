package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "actions")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DeviceActionType type;

    private int value;
}
