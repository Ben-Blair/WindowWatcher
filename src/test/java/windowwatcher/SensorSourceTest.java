package windowwatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import windowwatcher.detection.CrossingDetector;
import windowwatcher.detection.DirectionDecider;
import windowwatcher.model.Direction;
import windowwatcher.sensor.RangeLimitedSensorSource;
import windowwatcher.sensor.SensorSource;
import windowwatcher.sensor.SensorSourceFactory;
import windowwatcher.sensor.SimulatedSensorSource;

class SensorSourceTest {
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-10-24T18:00:00Z"), ZoneOffset.UTC);

    private AppConfig configWithSource(String source) {
        return new AppConfig(source, "/dev/ttyUSB0", 115200, 2, Path.of("events.csv"),
                true, 500, 500, 1524);
    }

    @Test
    void factoryWrapsTheSensorInARangeLimit() {
        SensorSourceFactory factory = new SensorSourceFactory(fixedClock);

        SensorSource sensor = factory.create(configWithSource("simulated"));

        assertInstanceOf(RangeLimitedSensorSource.class, sensor);
        assertTrue(sensor.readNext().isPresent());
    }

    @Test
    void factoryRejectsUnknownSensorType() {
        SensorSourceFactory factory = new SensorSourceFactory(fixedClock);

        assertThrows(IllegalArgumentException.class, () -> factory.create(configWithSource("telepathy")));
    }

    @Test
    void simulatedWalksAreDetectedInAlternatingDirections() {
        CrossingDetector detector = new CrossingDetector(new DirectionDecider(true, 500), 500);
        RecordingListener listener = new RecordingListener();
        detector.addListener(listener);

        new WindowWatcher(new SimulatedSensorSource(3, fixedClock, false), detector).run();

        assertEquals(3, listener.events.size());
        assertEquals(Direction.TOWARD_COMPLEX, listener.events.get(0).direction());
        assertEquals(Direction.AWAY_FROM_COMPLEX, listener.events.get(1).direction());
        assertEquals(Direction.TOWARD_COMPLEX, listener.events.get(2).direction());
    }
}
