package windowwatcher;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** All the settings for the app, loaded from config.properties. */
public record AppConfig(
        String sensorSource,
        String serialPort,
        int baudRate,
        int simulatedWalks,
        Path csvFile,
        boolean positiveXMeansTowardComplex,
        int minMovementMm,
        long minCrossingMillis) {

    public static AppConfig load(Path file) {
        Properties settings = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            settings.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }

        // "mvn exec:java -Dsource=serial" overrides the sensor.source setting
        String sensorSource = System.getProperty("source", settings.getProperty("sensor.source"));

        return new AppConfig(
                sensorSource,
                settings.getProperty("serial.port"),
                Integer.parseInt(settings.getProperty("serial.baudRate")),
                Integer.parseInt(settings.getProperty("simulated.walks")),
                Path.of(settings.getProperty("csv.file")),
                Boolean.parseBoolean(settings.getProperty("direction.positiveXMeansTowardComplex")),
                Integer.parseInt(settings.getProperty("detection.minMovementMm")),
                Long.parseLong(settings.getProperty("detection.minCrossingMillis")));
    }
}
