package ru.yandex.practicum.model;

public enum TopicType {
    SENSOR_EVENTS,
    HUBS_EVENTS,
    SNAPSHOTS_EVENTS;

    public static TopicType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "sensor-events" -> SENSOR_EVENTS;
            case "hubs-events" -> HUBS_EVENTS;
            case "snapshots-events" -> SNAPSHOTS_EVENTS;
            default -> throw new IllegalArgumentException("Unknown topic type: " + value);
        };
    }
}
