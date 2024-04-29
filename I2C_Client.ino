// I2C client

#include <Wire.h>

byte x;

void setup() {
  Wire.begin(8);                // join i2c bus with address #8
  Wire.onReceive(receiveEvent); // register event
  Serial.begin(9600);           // start serial for output
  Serial.println("I2C peripheral Arduino Uno started ...");
}

void loop() {
  delay(100);
  pinMode(7, OUTPUT); // It's very IMPORTANT for relay
  digitalWrite(7, HIGH);    // Turn ON relay that is responsible for the load in general
}

// function that executes whenever data is received from master
// this function is registered as an event, see setup()
void receiveEvent(int howMany)
{
  x = Wire.read();    // receives byte: flag0          counterLights: we send 2)two) portions of data - 4 bytes each
  Serial.println(x);      // print the integer
  x = Wire.read();    // receives three bytes of k0
  Serial.print(x);      // print the integer
  x = Wire.read();    // receives byte
  Serial.print(x);      // print the integer
  x = Wire.read();    // receives byte
  Serial.println(x);      // print the integer
}
