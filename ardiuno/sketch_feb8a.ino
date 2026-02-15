#include <WiFi.h>
#include <HTTPClient.h>

#define TOUCH_PIN 27
#define LONG_PRESS_TIME 800
#define DOUBLE_TAP_TIME 400

// WiFi
const char* ssid = "RITHIKA";
const char* password = "rith1402";

const char* launchUrl =
  "http://10.223.35.140:8080/CampusConnect/launch";

bool wifiReady = false;

bool lastState = LOW;
unsigned long pressStart = 0;
bool longPressTriggered = false;

// Tap handling
unsigned long firstTapTime = 0;
bool waitingForSecondTap = false;

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(TOUCH_PIN, INPUT);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED &&
         millis() - start < 10000) {
    delay(500);
    Serial.print(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    wifiReady = true;
    Serial.println("\nWiFi connected");
    Serial.print("ESP32 IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nWiFi NOT connected");
  }

  Serial.println("ESP32 Ready");
}

void loop() {

  bool currentState = digitalRead(TOUCH_PIN);

  // ================= TOUCH START =================
  if (currentState == HIGH && lastState == LOW) {
    pressStart = millis();
    longPressTriggered = false;
  }

  // ================= LONG PRESS =================
  if (currentState == HIGH && !longPressTriggered) {
    if (millis() - pressStart >= LONG_PRESS_TIME) {
      if (wifiReady) {
        callLaunchServlet();
      }
      longPressTriggered = true;
      waitingForSecondTap = false;  // cancel tap detection
    }
  }

  // ================= TOUCH RELEASE =================
  if (currentState == LOW && lastState == HIGH) {

    unsigned long pressDuration = millis() - pressStart;

    if (!longPressTriggered && pressDuration < LONG_PRESS_TIME) {

      if (!waitingForSecondTap) {
        // First tap
        waitingForSecondTap = true;
        firstTapTime = millis();
      } 
      else {
        // Second tap
        if (millis() - firstTapTime <= DOUBLE_TAP_TIME) {
          Serial.println("R");   // 🔁 Double tap
        }
        waitingForSecondTap = false;
      }
    }
  }

  // ================= SINGLE TAP CONFIRM =================
  if (waitingForSecondTap &&
      millis() - firstTapTime > DOUBLE_TAP_TIME) {

    Serial.println("M");  // 🎤 Single tap
    waitingForSecondTap = false;
  }

  lastState = currentState;
  delay(10);
}

void callLaunchServlet() {
  HTTPClient http;
  http.begin(launchUrl);
  int code = http.GET();
  Serial.print("Launch HTTP status: ");
  Serial.println(code);
  http.end();
}
