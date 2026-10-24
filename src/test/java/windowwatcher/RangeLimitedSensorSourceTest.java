package windowwatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import windowwatcher.model.SensorReading;
import windowwatcher.sensor.RangeLimitedSensorSource;
import windowwatcher.sensor.SensorSource;

class RangeLimitedSensorSourceTest {
    private static final int FIVE_FEET_MM = 1524;

    /** A fake sensor for tests that just hands out the readings it was given. */
    private static class ListSensorSource implements SensorSource {
        private final Iterator<SensorReading> readings;
        boolean closed = false;

        ListSensorSource(SensorReading... readings) {
            this.readings = List.of(readings).iterator();
        }

        @Override
        public Optional<SensorReading> readNext() {
            return readings.hasNext() ? Optional.of(readings.next()) : Optional.empty();
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    @Test
    void personCloseByIsKept() {
        SensorReading close = SensorReading.person(0, 300, 1000);
        SensorSource sensor = new RangeLimitedSensorSource(new ListSensorSource(close), FIVE_FEET_MM);

        assertEquals(Optional.of(close), sensor.readNext());
    }

    @Test
    void motionFarAwayBecomesNobody() {
        SensorReading tree = SensorReading.person(0, 500, 4000); // about 13 feet away
        SensorSource sensor = new RangeLimitedSensorSource(new ListSensorSource(tree), FIVE_FEET_MM);

        assertEquals(Optional.of(SensorReading.nobody(0)), sensor.readNext());
    }

    @Test
    void farToTheSideCountsAsFarAway() {
        SensorReading farLeft = SensorReading.person(0, -1500, 600); // close in y, but over 5 feet in a straight line
        SensorSource sensor = new RangeLimitedSensorSource(new ListSensorSource(farLeft), FIVE_FEET_MM);

        assertEquals(Optional.of(SensorReading.nobody(0)), sensor.readNext());
    }

    @Test
    void endOfReadingsAndCloseArePassedThrough() {
        ListSensorSource inner = new ListSensorSource();
        SensorSource sensor = new RangeLimitedSensorSource(inner, FIVE_FEET_MM);

        assertTrue(sensor.readNext().isEmpty());
        sensor.close();
        assertTrue(inner.closed);
    }
}
