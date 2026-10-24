package windowwatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import windowwatcher.model.CrossingEvent;
import windowwatcher.model.Direction;
import windowwatcher.output.CsvEventStore;

class CsvEventStoreTest {

    @TempDir
    Path tempFolder;

    @Test
    void savedEventsCanBeLoadedBack() {
        CsvEventStore store = new CsvEventStore(tempFolder.resolve("events.csv"));
        CrossingEvent first = new CrossingEvent(Instant.parse("2026-10-24T18:00:00Z"), 2100, Direction.TOWARD_COMPLEX);
        CrossingEvent second = new CrossingEvent(Instant.parse("2026-10-24T18:05:00Z"), 1800, Direction.AWAY_FROM_COMPLEX);

        store.onCrossing(first);
        store.onCrossing(second);

        assertEquals(List.of(first, second), store.loadAll());
    }

    @Test
    void headerIsWrittenOnlyOnce() throws IOException {
        Path file = tempFolder.resolve("events.csv");
        CsvEventStore store = new CsvEventStore(file);

        store.onCrossing(new CrossingEvent(Instant.EPOCH, 1000, Direction.UNKNOWN));
        store.onCrossing(new CrossingEvent(Instant.EPOCH, 1000, Direction.UNKNOWN));

        List<String> lines = Files.readAllLines(file);
        assertEquals(3, lines.size());
        assertEquals(CsvEventStore.HEADER, lines.get(0));
    }

    @Test
    void missingFileMeansNoEvents() {
        CsvEventStore store = new CsvEventStore(tempFolder.resolve("does-not-exist.csv"));

        assertTrue(store.loadAll().isEmpty());
    }
}
