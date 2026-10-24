package windowwatcher.output;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import windowwatcher.model.CrossingEvent;
import windowwatcher.model.Direction;

/**
 * Saves every crossing as one line in a CSV file (e.g. events.csv), like:
 *   started_at,duration_ms,direction
 *   2026-10-24T18:02:31.100Z,2100,TOWARD_COMPLEX
 */
public class CsvEventStore implements CrossingListener {
    public static final String HEADER = "started_at,duration_ms,direction";

    private final Path file;

    public CsvEventStore(Path file) {
        this.file = file;
    }

    @Override
    public void onCrossing(CrossingEvent event) {
        String line = event.startTime() + "," + event.durationMillis() + "," + event.direction();
        try {
            if (Files.notExists(file)) {
                Files.writeString(file, HEADER + System.lineSeparator());
            }
            Files.writeString(file, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save event to " + file, e);
        }
    }

    /** Reads every saved crossing back out of the file. */
    public List<CrossingEvent> loadAll() {
        if (Files.notExists(file)) {
            return List.of();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read events from " + file, e);
        }

        List<CrossingEvent> events = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) { // skip the header
            String[] parts = line.split(",");
            Instant startTime = Instant.parse(parts[0]);
            long durationMillis = Long.parseLong(parts[1]);
            Direction direction = Direction.valueOf(parts[2]);
            events.add(new CrossingEvent(startTime, durationMillis, direction));
        }
        return events;
    }
}
