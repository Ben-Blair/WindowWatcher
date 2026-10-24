package windowwatcher.detection;

import windowwatcher.model.SensorReading;

/**
 * Someone is in front of the window. We remember where they first and last appeared
 * so that when they leave, the detector can work out how long they took and which way they went.
 */
public class PersonPresentState implements DetectorState {
    private final SensorReading firstReading;
    private SensorReading lastReading;

    public PersonPresentState(SensorReading firstReading) {
        this.firstReading = firstReading;
        this.lastReading = firstReading;
    }

    @Override
    public DetectorState handle(SensorReading reading, CrossingDetector detector) {
        if (reading.personDetected()) {
            lastReading = reading;
            return this;
        }

        detector.personLeft(firstReading, lastReading);
        return new EmptyState();
    }
}
