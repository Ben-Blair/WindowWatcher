package windowwatcher.sensor;

import java.util.Optional;
import windowwatcher.model.SensorReading;

/**
 * Somewhere sensor readings come from: the real ESP32 over USB, or a fake simulated sensor.
 * The rest of the program only knows about this interface, never which one it's using.
 */
public interface SensorSource extends AutoCloseable {

    /**
     * Waits for the next reading.
     * Returns an empty Optional when there are no more readings (e.g. the simulation is finished).
     */
    Optional<SensorReading> readNext();

    @Override
    void close();
}
