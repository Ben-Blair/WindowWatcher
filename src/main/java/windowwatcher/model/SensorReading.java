package windowwatcher.model;

/**
 * One measurement from the mmWave sensor.
 *
 * x is how far left/right of the sensor the person is, y is how far away they are.
 * Both are in millimeters. When nobody is in view, personDetected is false and x/y are 0.
 */
public record SensorReading(long timeMillis, boolean personDetected, int xMm, int yMm) {

    public static SensorReading person(long timeMillis, int xMm, int yMm) {
        return new SensorReading(timeMillis, true, xMm, yMm);
    }

    public static SensorReading nobody(long timeMillis) {
        return new SensorReading(timeMillis, false, 0, 0);
    }
}
