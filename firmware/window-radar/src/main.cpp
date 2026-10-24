// WindowWatcher radar firmware (ESP32-C3 + HLK-LD2450).
//
// Reads the radar's 30-byte frames (about 10 a second) and prints one line per frame over USB:
//   "x_mm,y_mm"  e.g. "-820,1450"  when someone is in view
//   "none"                         when nobody is in view
// The Java app on the Raspberry Pi reads these lines (see SensorLineParser.java).
//
// Frame layout: [AA FF 03 00] [target 1: 8 bytes] [target 2] [target 3] [55 CC]
//
// Wiring (same as garage-radar): sensor TX -> GPIO20, sensor RX -> GPIO21, 5V -> 5V, GND -> GND

#include <Arduino.h>
#include <WiFi.h>

const int RADAR_RX_PIN = 20;
const int RADAR_TX_PIN = 21;
const uint32_t RADAR_BAUD = 256000;
const uint32_t USB_BAUD = 115200;

const uint8_t FRAME_HEAD[4] = {0xAA, 0xFF, 0x03, 0x00};
const uint8_t FRAME_TAIL[2] = {0x55, 0xCC};
const int FRAME_LEN = 30;
const int TARGET_LEN = 8;
const int TARGET_COUNT = 3;

// The LD2450 stores the sign in the top bit: 1 means positive, 0 means negative.
int decodeSigned(uint8_t low, uint8_t high) {
  uint16_t raw = low | (high << 8);
  int value = raw & 0x7FFF;
  return (raw & 0x8000) ? value : -value;
}

// Prints the first target the radar is tracking, or "none".
// (The radar can track 3 people; the Java app only needs one at a time for now.)
void printFrame(const uint8_t* frame) {
  for (int t = 0; t < TARGET_COUNT; t++) {
    const uint8_t* target = frame + 4 + t * TARGET_LEN;
    int x = decodeSigned(target[0], target[1]);
    int y = decodeSigned(target[2], target[3]);
    if (x != 0 || y != 0) {  // x == 0 and y == 0 means this slot is empty
      Serial.printf("%d,%d\n", x, y);
      return;
    }
  }
  Serial.println("none");
}

// Reads bytes from the radar one at a time until a full, valid frame has arrived.
void readRadar() {
  static uint8_t frame[FRAME_LEN];
  static int have = 0;

  while (Serial1.available()) {
    uint8_t b = Serial1.read();

    // Wait until the 4 header bytes arrive in a row
    if (have < 4) {
      if (b == FRAME_HEAD[have]) {
        frame[have++] = b;
      } else {
        have = (b == FRAME_HEAD[0]) ? 1 : 0;
        frame[0] = b;
      }
      continue;
    }

    frame[have++] = b;
    if (have == FRAME_LEN) {
      have = 0;
      if (frame[FRAME_LEN - 2] == FRAME_TAIL[0] && frame[FRAME_LEN - 1] == FRAME_TAIL[1]) {
        printFrame(frame);
      }
    }
  }
}

void setup() {
  // Keep the chip cool for 24/7 use: WiFi off (the Pi talks to us over USB) and a slower CPU.
  // 80 MHz is still far more than enough for 10 small frames a second.
  WiFi.mode(WIFI_OFF);
  setCpuFrequencyMhz(80);

  Serial.begin(USB_BAUD);
  Serial1.begin(RADAR_BAUD, SERIAL_8N1, RADAR_RX_PIN, RADAR_TX_PIN);
}

void loop() {
  readRadar();
  delay(5);  // let the CPU idle; the radar's UART buffer easily holds 5 ms of data
}
