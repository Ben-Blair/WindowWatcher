package windowwatcher.sensor;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Optional;
import windowwatcher.model.SensorReading;

/** Reads lines from the ESP32 plugged in over USB (e.g. /dev/ttyUSB0 on the Raspberry Pi). */
public class SerialSensorSource implements SensorSource {
    private final SerialPort port;
    private final BufferedReader reader;
    private final SensorLineParser parser;
    private final Clock clock;

    public SerialSensorSource(String portName, int baudRate, SensorLineParser parser, Clock clock) {
        this.parser = parser;
        this.clock = clock;

        port = SerialPort.getCommPort(portName);
        port.setBaudRate(baudRate);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0); // wait forever for data
        if (!port.openPort()) {
            throw new IllegalStateException("Could not open serial port " + portName);
        }
        reader = new BufferedReader(new InputStreamReader(port.getInputStream(), StandardCharsets.US_ASCII));
    }

    @Override
    public Optional<SensorReading> readNext() {
        try {
            String line = reader.readLine();
            while (line != null) {
                Optional<SensorReading> reading = parser.parse(line, clock.millis());
                if (reading.isPresent()) {
                    return reading;
                }
                line = reader.readLine(); // skip lines we don't understand
            }
            return Optional.empty(); // the port was closed
        } catch (IOException e) {
            throw new UncheckedIOException("Lost connection to the sensor", e);
        }
    }

    @Override
    public void close() {
        port.closePort();
    }
}
