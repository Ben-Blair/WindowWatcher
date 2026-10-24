package windowwatcher.sensor;

import java.util.Optional;
import windowwatcher.model.SensorReading;

/**
 * The ESP32 prints plain text lines like "-820,1450" (x,y in mm) or "none".
 * This class turns those lines into SensorReading objects that the rest of the program understands.
 */
public class SensorLineParser {

    /** Returns an empty Optional if the line isn't a reading (e.g. an ESP32 boot message). */
    public Optional<SensorReading> parse(String line, long timeMillis) {
        String text = line.trim();
        if (text.equals("none")) {
            return Optional.of(SensorReading.nobody(timeMillis));
        }

        String[] parts = text.split(",");
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            return Optional.of(SensorReading.person(timeMillis, x, y));
        } catch (NumberFormatException notANumber) {
            return Optional.empty();
        }
    }
}
