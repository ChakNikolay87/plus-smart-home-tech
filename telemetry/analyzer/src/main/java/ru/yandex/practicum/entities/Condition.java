package ru.yandex.practicum.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.model.enums.ConditionOperation;
import ru.yandex.practicum.model.enums.ConditionType;

@Entity
@Table(name = "conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    private ConditionOperation operation;

    @Column(name = "value_int")
    private Integer valueInt;

    @Column(name = "value_bool")
    private Boolean valueBool;



    public boolean isValueFits(int value) {
        if (valueInt == null || operation == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> value == valueInt;
            case GREATER_THAN -> value > valueInt;
            case LOWER_THAN -> value < valueInt;
        };
    }

}