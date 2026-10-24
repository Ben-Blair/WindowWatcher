package windowwatcher.sensor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import windowwatcher.model.SensorReading;

/**
 * A fake sensor for testing and demos without the hardware.
 * It pretends people walk past the window, alternating left-to-right and right-to-left.
 */
public class SimulatedSensorSource implements SensorSource {
    private static final int READING_INTERVAL_MILLIS = 100; // the real LD2450 sends about 10 readings a second
    private static final int READINGS_PER_WALK = 20;        // so each walk takes about 2 seconds
    private static final int EMPTY_READINGS_BETWEEN_WALKS = 10;
    private static final int WALK_START_X_MM = -1000;
    private static final int WALK_END_X_MM = 1000;
    private static final int DISTANCE_FROM_WINDOW_MM = 900; // stays within the 5 ft range limit

    private final Iterator<SensorReading> readings;
    private final boolean runInRealTime;

    /**
     * @param runInRealTime true = wait between readings like the real sensor, false = go as fast as possible (tests)
     */
    public SimulatedSensorSource(int numberOfWalks, Clock clock, boolean runInRealTime) {
        this.readings = buildReadings(numberOfWalks, clock.millis()).iterator();
        this.runInRealTime = runInRealTime;
    }

    @Override
    public Optional<SensorReading> readNext() {
        if (!readings.hasNext()) {
            return Optional.empty();
        }
        if (runInRealTime) {
            sleep(READING_INTERVAL_MILLIS);
        }
        return Optional.of(readings.next());
    }

    @Override
    public void close() {
        // Nothing to close for a fake sensor
    }

    private static List<SensorReading> buildReadings(int numberOfWalks, long startTimeMillis) {
        List<SensorReading> list = new ArrayList<>();
        long time = startTimeMillis;

        for (int walk = 0; walk < numberOfWalks; walk++) {
            for (int i = 0; i < EMPTY_READINGS_BETWEEN_WALKS; i++) {
                list.add(SensorReading.nobody(time));
                time += READING_INTERVAL_MILLIS;
            }

            boolean leftToRight = walk % 2 == 0;
            for (int step = 0; step < READINGS_PER_WALK; step++) {
                int x = xPositionAt(step, leftToRight);
                list.add(SensorReading.person(time, x, DISTANCE_FROM_WINDOW_MM));
                time += READING_INTERVAL_MILLIS;
            }
        }

        list.add(SensorReading.nobody(time)); // the last person leaves
        return list;
    }

    private static int xPositionAt(int step, boolean leftToRight) {
        double progress = step / (double) (READINGS_PER_WALK - 1); // 0.0 at the start, 1.0 at the end
        int distanceWalked = (int) (progress * (WALK_END_X_MM - WALK_START_X_MM));
        if (leftToRight) {
            return WALK_START_X_MM + distanceWalked;
        }
        return WALK_END_X_MM - distanceWalked;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
