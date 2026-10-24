package windowwatcher.detection;

import windowwatcher.model.SensorReading;

/**
 * STATE PATTERN: the detector is always in one state ("nobody there" or "someone there").
 * Each state decides what a new reading means and which state comes next,
 * so the detector itself never needs an if/else about what state it's in.
 */
public interface DetectorState {

    /** Looks at one reading and returns the state the detector should be in next. */
    DetectorState handle(SensorReading reading, CrossingDetector detector);
}
