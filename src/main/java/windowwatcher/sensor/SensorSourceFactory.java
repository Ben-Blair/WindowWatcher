package windowwatcher.sensor;

import java.time.Clock;
import windowwatcher.AppConfig;

/**
 * FACTORY PATTERN: the one place that decides whether we use the real sensor or the simulated one,
 * based on the "sensor.source" setting in config.properties.
 */
public class SensorSourceFactory {
    private final Clock clock;

    public SensorSourceFactory(Clock clock) {
        this.clock = clock;
    }

    public SensorSource create(AppConfig config) {
        return switch (config.sensorSource()) {
            case "simulated" -> new SimulatedSensorSource(config.simulatedWalks(), clock, true);
            case "serial" -> new SerialSensorSource(
                    config.serialPort(), config.baudRate(), new SensorLineParser(), clock);
            default -> throw new IllegalArgumentException(
                    "Unknown sensor.source '" + config.sensorSource() + "', use 'simulated' or 'serial'");
        };
    }
}
