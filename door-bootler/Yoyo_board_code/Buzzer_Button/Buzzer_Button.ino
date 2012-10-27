/* 
 Button Control
 */

#define buttonPin      5     // the pin of the doorbell signal
#define OpenDoorServo  6     //pin where the servo that controls door opening is attached to

int buttonState;             // the current reading from the input pin
int lastButtonState = LOW;   // the previous reading from the input pin

long lastDebounceTime = 0;  // the last time the output pin was toggled
long debounceDelay = 50;    // the debounce time; increase if the output flickers

void setup() {
  Serial.begin(115200);
  pinMode(buttonPin, INPUT);

}

void loop() {
     
    readBuzzer();

}


void readBuzzer(){
    int reading = digitalRead(buttonPin);


  if (reading != lastButtonState) {
    lastDebounceTime = millis();
  } 
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    buttonState = reading;
  }

              Serial.println("Buzzer Activated!!");         
  //This is where we process communication to Android to say that the buzzer has been activated
  

  lastButtonState = reading;
}
