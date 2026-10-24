package windowwatcher.output;

import java.io.PrintStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import windowwatcher.model.CrossingEvent;

/** Prints each crossing to the screen, e.g. "[14:02:31] Someone walked TOWARD_COMPLEX (2.1 s)". */
public class ConsoleLogger implements CrossingListener {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final PrintStream out;

    public ConsoleLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onCrossing(CrossingEvent event) {
        String time = TIME_FORMAT.format(event.startTime());
        double seconds = event.durationMillis() / 1000.0;
        out.printf("[%s] Someone walked %s (%.1f s)%n", time, event.direction(), seconds);
    }
}
