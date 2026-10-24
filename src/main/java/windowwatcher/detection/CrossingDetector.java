package windowwatcher.detection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import windowwatcher.model.CrossingEvent;
import windowwatcher.model.Direction;
import windowwatcher.model.SensorReading;
import windowwatcher.output.CrossingListener;

/**
 * Turns a stream of sensor readings into crossing events ("someone walked past").
 *
 * It uses the STATE PATTERN to track whether someone is there (see DetectorState)
 * and the OBSERVER PATTERN to tell every CrossingListener when a crossing happens.
 */
public class CrossingDetector {
    private final DirectionDecider directionDecider;
    private final long minCrossingMillis;
    private final List<CrossingListener> listeners = new ArrayList<>();
    private DetectorState state = new EmptyState();

    public CrossingDetector(DirectionDecider directionDecider, long minCrossingMillis) {
        this.directionDecider = directionDecider;
        this.minCrossingMillis = minCrossingMillis;
    }

    public void addListener(CrossingListener listener) {
        listeners.add(listener);
    }

    /** Call this with every reading from the sensor. */
    public void onReading(SensorReading reading) {
        state = state.handle(reading, this);
    }

    /** Called by PersonPresentState when the person is no longer detected. */
    void personLeft(SensorReading firstReading, SensorReading lastReading) {
        long durationMillis = lastReading.timeMillis() - firstReading.timeMillis();
        if (durationMillis < minCrossingMillis) {
            return; // Too short to be a real person walking by, probably sensor noise
        }

        Direction direction = directionDecider.decide(firstReading.xMm(), lastReading.xMm());
        Instant startTime = Instant.ofEpochMilli(firstReading.timeMillis());
        CrossingEvent event = new CrossingEvent(startTime, durationMillis, direction);

        for (CrossingListener listener : listeners) {
            listener.onCrossing(event);
        }
    }
}
