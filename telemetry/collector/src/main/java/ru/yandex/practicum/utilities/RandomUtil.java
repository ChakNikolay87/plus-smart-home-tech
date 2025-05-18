package ru.yandex.practicum.utilities;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
public class RandomUtil {
    private static final Map<String, Intervals> diapasonMap = Map.of(
            "temperature", new Intervals(-50, 60, 10),
            "voltage", new Intervals(0, 260, 30),
            "linkQuality", new Intervals(0, 100, 30),
            "luminosity", new Intervals(100, 1000, 200),
            "humidity", new Intervals(20, 70, 10),
            "co2Level", new Intervals(200, 1100, 200)
    );
    public static int getRandomValue(String keyString, int startValue) {
        Intervals bounds = diapasonMap.get(keyString);
        //страховка от опечаток при отладке
        if (bounds == null) {
            throw new IllegalArgumentException("Unknown key: " + keyString);
        }
        boolean needMake = true;
        int newValue = 0;
        while (needMake) {
            newValue = startValue + ThreadLocalRandom.current().nextInt( - bounds.delta, bounds.delta + 1);
            if (newValue > bounds.min && newValue < bounds.max)
                needMake = false;
        }
        return newValue;
    }
    private record Intervals(int min, int max, int delta) {
        //new value in [min, max], with a variation from the range [-delta, delta]
    }
}