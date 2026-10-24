package windowwatcher.detection;

import windowwatcher.model.SensorReading;

/** Nobody is in front of the window. We're waiting for someone to show up. */
public class EmptyState implements DetectorState {

    @Override
    public DetectorState handle(SensorReading reading, CrossingDetector detector) {
        if (reading.personDetected()) {
            return new PersonPresentState(reading);
        }
        return this;
    }
}
