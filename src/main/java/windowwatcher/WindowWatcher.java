package windowwatcher;

import java.util.Optional;
import windowwatcher.detection.CrossingDetector;
import windowwatcher.model.SensorReading;
import windowwatcher.sensor.SensorSource;

/** The main loop: take each reading from the sensor and give it to the detector. */
public class WindowWatcher {
    private final SensorSource sensor;
    private final CrossingDetector detector;

    public WindowWatcher(SensorSource sensor, CrossingDetector detector) {
        this.sensor = sensor;
        this.detector = detector;
    }

    /** Runs until the sensor has no more readings (forever, for the real sensor). */
    public void run() {
        try {
            Optional<SensorReading> reading = sensor.readNext();
            while (reading.isPresent()) {
                detector.onReading(reading.get());
                reading = sensor.readNext();
            }
        } finally {
            sensor.close();
        }
    }
}
