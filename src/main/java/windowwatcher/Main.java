package windowwatcher;

import java.nio.file.Path;
import java.time.Clock;
import windowwatcher.detection.CrossingDetector;
import windowwatcher.detection.DirectionDecider;
import windowwatcher.output.ConsoleLogger;
import windowwatcher.output.CsvEventStore;
import windowwatcher.sensor.SensorSource;
import windowwatcher.sensor.SensorSourceFactory;

/**
 * Builds every object and connects them together (DEPENDENCY INJECTION by hand).
 * No class creates its own dependencies; they're all passed in here through constructors.
 * Reading this file tells you how the whole app fits together.
 */
public class Main {

    public static void main(String[] args) {
        AppConfig config = AppConfig.load(Path.of("config.properties"));
        Clock clock = Clock.systemDefaultZone();

        // 1. Where readings come from: the real ESP32 or the simulated sensor
        SensorSource sensor = new SensorSourceFactory(clock).create(config);

        // 2. The detector that turns readings into "someone walked past" events
        DirectionDecider directionDecider =
                new DirectionDecider(config.positiveXMeansTowardComplex(), config.minMovementMm());
        CrossingDetector detector = new CrossingDetector(directionDecider, config.minCrossingMillis());

        // 3. Everyone who wants to hear about crossings
        CsvEventStore csvStore = new CsvEventStore(config.csvFile());
        detector.addListener(new ConsoleLogger(System.out));
        detector.addListener(csvStore);

        // 4. Run (for the real sensor this never ends; stop it with Ctrl+C)
        System.out.println("Watching the window using the '" + config.sensorSource() + "' sensor...");
        new WindowWatcher(sensor, detector).run();

        System.out.println("Saved " + csvStore.loadAll().size() + " crossings in total to " + config.csvFile());
    }
}
