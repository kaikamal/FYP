// I2C controller
#include <Wire.h>
//#include <SPI.h>
#include <SD.h>

byte flag0, i, j, k, OffState=0, prevOffState, RelayState=0;
// OffState counts the number of OFF states; byte: 0 to 255
// RelayState=0 - relay is OFF
// flag0==0 if we didn't reduce the coefficient k0; flag0==1 if we reduced
File myFile;
float k0;
word valuePhoto; // Photoresistor: Store value from photoresistor (0-1023): word (unsigned number, 16 bit): 0 to 65535
float counterLights=0.0; // counter of measurements with value of photoresister larger than 1000: Floating-point numbers can be as large as 3.4028235E+38 and as low as -3.4028235E+38. They are stored as 32 bits (4 bytes) of information.

// https://ru.wikipedia.org/wiki/Долгота_дня
// Максимальная среднегодовая продолжительность дня достигается на северном и южном полярном круге — 12 часов 32 минуты и 12 часов 16 минут, соответственно.

void setup() {
  pinMode(7, OUTPUT); // It's very IMPORTANT for relay
  digitalWrite(7, LOW); // Turn OFF relay
  Serial.begin (9600);
//  Serial.println ("I2C controller");
  Wire.begin();
  Wire.setWireTimeout(500, true);

  if (!SD.begin(4)) {
    Serial.println("SdFAILED");
    while (1);
  } else{
    Serial.println("SdDONE");
    // Open the file for reading:
    myFile = SD.open("Data.TXT");
    if (myFile) {
//      Serial.println("Data.TXT");
      // read two values from the file:
      flag0=myFile.read()-48; // We read flag0 taking into consideration ASCII codes
      delay(100);
      k0=10*(myFile.read()-48); // We read tens, digits, and tenth taking into consideration ASCII codes      
      delay(100);
      k0=k0+myFile.read()-48;
      delay(100);
      k0=k0+float(myFile.read()-48)/10;
      delay(100);
      myFile.close(); // close the file:
    }
//  else Serial.println("Error Data.TXT"); // if the file didn't open, print an error:
  }
//  counterLights = 4000;
//  k0=0.5;
//  flag0=0;
//  WriteToFile();
}

void loop() {
  valuePhoto = analogRead(A0);
  Serial.print("Photoresistor: ");
  Serial.println(valuePhoto);
  Serial.print("counterLights: ");
  Serial.println(counterLights);
  if (valuePhoto>1002  && counterLights<9999  && OffState!=255) counterLights = counterLights + 1.0;  // we increase the number of loops with sunny light if the load isn't OFF
  // We say that number of loops with sunny light should be <= 9999. This is about 4.72 hours

  if (valuePhoto<300  && counterLights>0 && RelayState == 0) { // we start the load here
    digitalWrite(7, HIGH);    // Turn ON relay
    RelayState = 1; // State of the relay is ON
  }
  if (RelayState == 1) counterLights = counterLights - k0;
  if ((valuePhoto>350 || counterLights<=0)&& RelayState == 1) {
      digitalWrite(7, LOW); // Turn OFF relay
      RelayState = 0; // State of the relay is OFF
  }
  
  // Here, we send info to the peripheral Arduino board and check if it is online
  Wire.beginTransmission (8);
  Wire.write(flag0); // sends one byte - I2C
  delay(200);
  i=k0/10; // tens: we transmit k0
  Wire.write(i); // sends one byte - I2C
  delay(200);
  j=k0-i*10; // units
  Wire.write(j); // sends one byte - I2C
  delay(200);
  i=k0*10-j*10-i*100; // 1st decimal
  Wire.write(i); // sends one byte - I2C
  delay(200);
  prevOffState = OffState;
  if (Wire.endTransmission () == 0)
  {
    Serial.println ("8 ON"); // end of good response
    OffState=0; // Reset the number of OFF states
  } else {
    Serial.println ("8 OFF"); // end of bad response    
    if (OffState<255) OffState++; // Increase the number of OFF states
  }

  Wire.beginTransmission (8); // Here, we send other 4 bytes of data (kinda buffer): we assume counterLights<=9999
  if (counterLights>0) j=counterLights/1000; // thousands: we transmit counterLights
  else j=0;
  Wire.write(j); // sends one byte - I2C
  delay(200);
  if (counterLights>0) k=(counterLights-j*1000)/100; // hundreds: we transmit counterLights
  else k=0;
  Wire.write(k); // sends one byte - I2C
  delay(200);
  if (counterLights>0) i=(counterLights-j*1000-k*100)/10; // tens: we transmit counterLights
  else i=0;
  Wire.write(i); // sends one byte - I2C
  delay(200);
  if (counterLights>0) i=counterLights-j*1000-k*100-i*10; // units: we transmit counterLights
  else i=0;
  Wire.write(i); // sends one byte - I2C
  delay(200);
  Wire.endTransmission ();

  if (OffState==255) {
    if (RelayState == 1) {
      digitalWrite(7, LOW); // Turn OFF relay
      RelayState = 0; // State of the relay is OFF
    }
    if (prevOffState==254){
      k0=k0+k0; // We increase the coefficient to reduce the load on the battery and avoid blackouts
      flag0=1; // We specify that we reduced the coefficient, i.e., we apply the strategy to reduce the coefficient furthermore
      WriteToFile();
    }
    Serial.println("8 absolutely OFF"); // 255 loops is about 459sec, i.e., 7.6 min. This time is enough to identify disconnection of I2C client
    counterLights = 0.0; // We reset the number of loops with sunny light
  }
  Serial.print ("flag0 = "); Serial.println(flag0);
  Serial.print ("k0 = "); Serial.println (k0);
  delay(1000);
}

void WriteToFile(){
    // open the file. note that only one file can be open at a time, so you have to close this one before opening another.
  if (SD.exists("Data.TXT")) SD.remove("Data.TXT"); // removes the file
  myFile = SD.open("Data.TXT", FILE_WRITE);
  // if the file opened okay, write to it:
  if (myFile) {
    myFile.print(flag0); // writes one byte to file
    delay(100);
    i=k0/10; // tens
    myFile.print(i); // writes one byte to file
    delay(100);
    j=k0-i*10; // units
    myFile.print(j); // writes one byte to file
    delay(100);
    i=k0*10-j*10-i*100; // 1st decimal
    myFile.print(i); // writes one byte to file
    delay(100);
    myFile.close(); // close the file
    Serial.println("done");
  } else Serial.println("error Data.TXT"); // if the file didn't open, print an error
}
