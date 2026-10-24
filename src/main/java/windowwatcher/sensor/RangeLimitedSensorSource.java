package windowwatcher.sensor;

import java.util.Optional;
import windowwatcher.model.SensorReading;

/**
 * DECORATOR PATTERN: wraps another SensorSource and ignores anything farther away than maxDistanceMm,
 * so trees blowing in the wind across the street don't count as people.
 *
 * It is a SensorSource itself, so the rest of the program can't tell it's there.
 */
public class RangeLimitedSensorSource implements SensorSource {
    private final SensorSource inner;
    private final int maxDistanceMm;

    public RangeLimitedSensorSource(SensorSource inner, int maxDistanceMm) {
        this.inner = inner;
        this.maxDistanceMm = maxDistanceMm;
    }

    @Override
    public Optional<SensorReading> readNext() {
        return inner.readNext().map(this::ignoreIfTooFar);
    }

    @Override
    public void close() {
        inner.close();
    }

    private SensorReading ignoreIfTooFar(SensorReading reading) {
        // Straight-line distance from the sensor (Pythagoras)
        double distanceMm = Math.hypot(reading.xMm(), reading.yMm());
        if (reading.personDetected() && distanceMm > maxDistanceMm) {
            return SensorReading.nobody(reading.timeMillis());
        }
        return reading;
    }
}
