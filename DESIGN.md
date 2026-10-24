# WindowWatcher: Design

**Team:** Ben Blair, Ronald Jangam
**Language:** Java 17

## What it does
An HLK-LD2450 mmWave radar, wired to an ESP32-C3, faces a bedroom window. People often walk past it on their way
to or from the apartment complex. The ESP32 sends each radar reading (the person's x/y position) over USB to a
Raspberry Pi. On the Pi, the Java app turns those readings into **crossing events** (when someone walked past,
how long it took, and which direction they went) and saves each one to a CSV file. Motion farther than 5 feet
away (trees, cars) is ignored.

## Architecture

```
LD2450 -> ESP32 --USB--> SensorSource ---SensorReading---> CrossingDetector ---CrossingEvent---> CrossingListeners
                         (Serial or Simulated,             (state machine)                      ConsoleLogger
                          wrapped in RangeLimited)                                              CsvEventStore
```

| Folder | Job |
|---|---|
| `Main`, `AppConfig`, `WindowWatcher` | load settings, build and connect everything, run the main loop |
| `model/` | plain data: `SensorReading`, `CrossingEvent`, `Direction` |
| `sensor/` | where readings come from |
| `detection/` | turns readings into "someone walked by" |
| `output/` | what happens to each crossing (print it, save it) |

## Design patterns

### 1. Observer
**Where:** `output/CrossingListener` (observer interface) and `detection/CrossingDetector` (subject).

**How:** The detector keeps a list of `CrossingListener`s. When it sees a crossing, it calls `onCrossing(event)` on
each one. `ConsoleLogger` prints the event and `CsvEventStore` saves it. To add a new output (like a web page or a
notification), we write one new class and register it in `Main`. The detector itself doesn't change.

### 2. State
**Where:** `detection/DetectorState` with `EmptyState` and `PersonPresentState`.

**How:** The detector is always in one state. It hands each reading to its current state, and the state returns
the next one:
- `EmptyState`: if a person shows up, switch to `PersonPresentState`.
- `PersonPresentState`: remembers where the person first and last appeared. When they disappear, it tells the
  detector `personLeft(...)` and switches back to `EmptyState`.

Because of this, `CrossingDetector.onReading()` is one line (`state = state.handle(reading, this)`), with no
if/else or switch on "what mode am I in".

### 3. Factory
**Where:** `sensor/SensorSourceFactory`.

**How:** `create(config)` reads `sensor.source` from `config.properties` and builds either a `SerialSensorSource`
(the real ESP32) or a `SimulatedSensorSource` (fake people walking past, for development and demos without
hardware). This is the only place in the program that chooses. Everything else just uses a `SensorSource`.

### 4. Decorator
**Where:** `sensor/RangeLimitedSensorSource`.

**How:** It wraps any other `SensorSource` and is itself a `SensorSource`. Each reading passes through it, and
anything farther than `detection.maxDistanceMm` (5 feet by default) is turned into a "nobody there" reading.
The factory wraps every sensor in it, so the real and simulated sensors both get the filter, and neither of them
had to change.

### Bonus: Adapter
**Where:** `sensor/SensorLineParser` (used by `SerialSensorSource`).

**How:** The ESP32 speaks plain text (`"-820,1450"` or `"none"`). The adapter converts that text into
`SensorReading` objects, so hardware details never leak into the rest of the code.

## OO principles
- **Coding to abstractions:** The detector only knows `CrossingListener`. `WindowWatcher` only knows `SensorSource`.
- **Polymorphism:** Detector states, sensor sources and listeners are each swapped behind their interface.
- **Dependency injection:** Every dependency is passed in through a constructor (`CrossingDetector` receives a
  `DirectionDecider`, `SerialSensorSource` receives a parser and a `Clock`, and so on), and `Main` wires them all
  together. Nothing creates its own collaborators, which is why everything can be tested without hardware.

## Tests (`src/test/java`)
| Test class | What it checks |
|---|---|
| `CrossingDetectorTest` | one event per walk, no event while the person is still there, noise ignored, both directions, unknown direction, every listener notified once |
| `SensorLineParserTest` | position lines, "none", junk lines ignored |
| `RangeLimitedSensorSourceTest` | close readings kept, far readings (straight ahead or off to the side) ignored, end-of-data and close passed through |
| `CsvEventStoreTest` | save then load, header written once, missing file |
| `SensorSourceTest` | factory wraps the sensor in the range limit, rejects unknown types, full simulated run detects alternating directions |
