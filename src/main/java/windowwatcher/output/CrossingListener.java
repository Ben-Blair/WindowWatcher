package windowwatcher.output;

import windowwatcher.model.CrossingEvent;

/**
 * OBSERVER PATTERN: anything that wants to know when someone walks past implements this.
 * The CrossingDetector calls onCrossing() on every listener it was given.
 */
public interface CrossingListener {
    void onCrossing(CrossingEvent event);
}
