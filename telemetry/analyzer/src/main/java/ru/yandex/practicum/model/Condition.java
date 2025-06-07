package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conditions")
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    private OperationType operation;

    private int value;
}
