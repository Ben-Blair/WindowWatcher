package windowwatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import windowwatcher.detection.CrossingDetector;
import windowwatcher.detection.DirectionDecider;
import windowwatcher.model.Direction;
import windowwatcher.model.SensorReading;

class CrossingDetectorTest {
    private CrossingDetector detector;
    private RecordingListener listener;

    @BeforeEach
    void setUp() {
        DirectionDecider directionDecider = new DirectionDecider(true, 500);
        detector = new CrossingDetector(directionDecider, 500);
        listener = new RecordingListener();
        detector.addListener(listener);
    }

    /** Sends readings of a person walking from startX to endX, one every 100 ms, then "nobody". */
    private void walk(long startTime, int startX, int endX, int steps) {
        for (int i = 0; i < steps; i++) {
            int x = startX + (endX - startX) * i / (steps - 1);
            detector.onReading(SensorReading.person(startTime + i * 100L, x, 1200));
        }
        detector.onReading(SensorReading.nobody(startTime + steps * 100L));
    }

    @Test
    void personWalkingPastCreatesOneEvent() {
        walk(0, -1500, 1500, 20);

        assertEquals(1, listener.events.size());
        assertEquals(1900, listener.events.get(0).durationMillis());
    }

    @Test
    void noEventWhileThePersonIsStillThere() {
        detector.onReading(SensorReading.person(0, -1500, 1200));
        detector.onReading(SensorReading.person(1000, 0, 1200));

        assertTrue(listener.events.isEmpty());
    }

    @Test
    void shortBlipIsIgnoredAsNoise() {
        walk(0, 0, 100, 3); // only 200 ms long

        assertTrue(listener.events.isEmpty());
    }

    @Test
    void walkingLeftToRightIsTowardComplex() {
        walk(0, -1500, 1500, 20);

        assertEquals(Direction.TOWARD_COMPLEX, listener.events.get(0).direction());
    }

    @Test
    void walkingRightToLeftIsAwayFromComplex() {
        walk(0, 1500, -1500, 20);

        assertEquals(Direction.AWAY_FROM_COMPLEX, listener.events.get(0).direction());
    }

    @Test
    void standingStillGivesUnknownDirection() {
        walk(0, 100, 200, 20);

        assertEquals(Direction.UNKNOWN, listener.events.get(0).direction());
    }

    @Test
    void everyListenerIsNotifiedExactlyOnce() {
        RecordingListener secondListener = new RecordingListener();
        detector.addListener(secondListener);

        walk(0, -1500, 1500, 20);

        assertEquals(1, listener.events.size());
        assertEquals(1, secondListener.events.size());
    }
}
