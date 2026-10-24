package windowwatcher.detection;

import windowwatcher.model.Direction;

/**
 * Works out which way someone walked from where they first and last appeared (their x position).
 *
 * Which side is "toward the complex" depends on how the sensor is mounted, so it's passed in from config.
 */
public class DirectionDecider {
    private final boolean positiveXMeansTowardComplex;
    private final int minMovementMm;

    public DirectionDecider(boolean positiveXMeansTowardComplex, int minMovementMm) {
        this.positiveXMeansTowardComplex = positiveXMeansTowardComplex;
        this.minMovementMm = minMovementMm;
    }

    public Direction decide(int startXMm, int endXMm) {
        int movement = endXMm - startXMm;
        if (Math.abs(movement) < minMovementMm) {
            return Direction.UNKNOWN;
        }

        boolean movedTowardPositiveX = movement > 0;
        if (movedTowardPositiveX == positiveXMeansTowardComplex) {
            return Direction.TOWARD_COMPLEX;
        }
        return Direction.AWAY_FROM_COMPLEX;
    }
}
