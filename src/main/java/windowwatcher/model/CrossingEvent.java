package windowwatcher.model;

import java.time.Instant;

/** One person walking past the window. This is what gets saved. */
public record CrossingEvent(Instant startTime, long durationMillis, Direction direction) {
}
