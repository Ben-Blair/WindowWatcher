# WindowWatcher

Counts people walking past a window using an HLK-LD2450 mmWave radar + ESP32. It works out which way each person
went and saves every crossing to a CSV file. See [DESIGN.md](DESIGN.md) for the design patterns.

**Team:** Ben Blair, Ronald Jangam

## Run it without any hardware
Needs Java 17+ and Maven.

```bash
mvn test                 # run the tests
mvn compile exec:java    # simulated people walk past; events go into events.csv
```

## Code tour (start here)
1. `Main.java`: builds and connects everything
2. `WindowWatcher.java`: the main loop (sensor reading → detector)
3. `detection/`: turns readings into crossing events (State pattern)
4. `output/`: what happens to each event (Observer)
5. `sensor/`: real vs simulated sensor (Factory)

## Running on the Raspberry Pi with the real sensor
1. Flash the ESP32-C3 (PlatformIO; wiring is the same as garage-radar: sensor TX → GPIO20, sensor RX → GPIO21):
   ```bash
   cd firmware/window-radar
   pio run -t upload
   pio device monitor      # should print "none", or "x,y" when you walk in front of it
   ```
2. Plug the ESP32 into the Pi over USB. Find its port with `ls /dev/ttyUSB* /dev/ttyACM*`.
3. Install Java on the Pi: `sudo apt install openjdk-17-jre-headless`.
4. On your laptop run `mvn package`, then copy `target/window-watcher.jar` and `config.properties` to the Pi.
5. On the Pi, set `sensor.source=serial` and the right `serial.port` in `config.properties`, then run:
   ```bash
   java -jar window-watcher.jar
   ```
   If you get a permission error on the port, run `sudo usermod -aG dialout $USER` and log in again.
6. Walk past the window both ways. If the directions are backwards, flip
   `direction.positiveXMeansTowardComplex`.
