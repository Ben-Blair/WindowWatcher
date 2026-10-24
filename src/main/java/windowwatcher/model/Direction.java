package windowwatcher.model;

/** Which way someone walked past the window. */
public enum Direction {
    TOWARD_COMPLEX,
    AWAY_FROM_COMPLEX,
    /** They didn't move far enough sideways to tell. */
    UNKNOWN
}
