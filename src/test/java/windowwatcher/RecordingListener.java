package windowwatcher;

import java.util.ArrayList;
import java.util.List;
import windowwatcher.model.CrossingEvent;
import windowwatcher.output.CrossingListener;

/** A fake listener for tests: it just remembers every event it was told about. */
public class RecordingListener implements CrossingListener {
    public final List<CrossingEvent> events = new ArrayList<>();

    @Override
    public void onCrossing(CrossingEvent event) {
        events.add(event);
    }
}
