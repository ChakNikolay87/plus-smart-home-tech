package ru.yandex.practicum.model.enums;

import ru.yandex.practicum.entities.Condition;

public enum ConditionType {
    MOTION {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Boolean)) return false;
            return Boolean.valueOf(condition.getValueBool()).equals(sensorValue);
        }
    },
    LUMINOSITY {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Integer)) return false;
            return condition.isValueFits((Integer) sensorValue);
        }
    },
    SWITCH {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Boolean)) return false;
            return Boolean.valueOf(condition.getValueBool()).equals(sensorValue);
        }
    },
    TEMPERATURE {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Integer)) return false;
            return condition.isValueFits((Integer) sensorValue);
        }
    },
    CO2LEVEL {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Integer)) return false;
            return condition.isValueFits((Integer) sensorValue);
        }
    },
    HUMIDITY {
        @Override
        public boolean matches(Condition condition, Object sensorValue) {
            if (!(sensorValue instanceof Integer)) return false;
            return condition.isValueFits((Integer) sensorValue);
        }
    };

    public abstract boolean matches(Condition condition, Object sensorValue);
}
