package ru.yandex.practicum.telemetry.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity
@Table(name = Action.TABLE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
public class Action {
    public static final String TABLE_NAME = "actions";
    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID)
    Long id;

    @Column(name = TYPE)
    String type;

    @Column(name = VALUE)
    Integer value;
}
