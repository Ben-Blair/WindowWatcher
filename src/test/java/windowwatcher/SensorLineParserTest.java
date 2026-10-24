package windowwatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import windowwatcher.model.SensorReading;
import windowwatcher.sensor.SensorLineParser;

class SensorLineParserTest {
    private final SensorLineParser parser = new SensorLineParser();

    @Test
    void parsesAPositionLine() {
        Optional<SensorReading> reading = parser.parse("-820,1450\r\n", 42);

        assertEquals(Optional.of(SensorReading.person(42, -820, 1450)), reading);
    }

    @Test
    void parsesNobodyThere() {
        Optional<SensorReading> reading = parser.parse("none", 42);

        assertTrue(reading.isPresent());
        assertFalse(reading.get().personDetected());
    }

    @Test
    void ignoresLinesThatAreNotReadings() {
        assertTrue(parser.parse("ets Jun  8 2016 00:22:57", 42).isEmpty());
        assertTrue(parser.parse("abc,def", 42).isEmpty());
        assertTrue(parser.parse("", 42).isEmpty());
    }
}
