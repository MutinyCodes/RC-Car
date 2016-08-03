#include <SoftwareSerial.h>// import the serial library
// Arduino receive communication on pin D12 and communicate with device on pin D13
SoftwareSerial plaviZub(12, 13); // RX, TX
// Variable which will store received data.
int receivedData;
/**
 * Class which manage output of 6 pins who then accordingly
 * manage 2 electromotors on car. 2 pins are for regulating power, and 4
 * are for electromotors, 2 for each for 2 directions.
 * 2 pins has to be PWM capable, and 4 regular output.
 * User can ask to move up/down/left/right and then class will
 * output power on corresponding pin.
 */
class Car
{
private:
  // Values for moving, higher value speed and turn are greater.
  int turn;
  int movement;
  // Pin for regulating power for speed.
  int pinMovement;
  // Pin for regulating power for turning.
  int pinTurn; 
  // When active, pinBack must be 0, then going forward.
  int pinForward;
  // When active, pinForward must be 0, then going backward.
  int pinBack;
  // When active, pinRight must be 0, then going left.
  int pinLeft;
  // When active, pinLeft must be 0, then going right.
  int pinRight;
public:
  // Increment value for turning, after each input car will turn for this value.
  const int angle = 255;
  // Increment value for speed, after each input car will (de)accelerate for this value.
  const int acceleration = 30;
  /**
   * Set initial speed on 0 and initial direction on strait.
   * Set selected pins for output.
   * @param pinMovemen Output pin for regulating movement.
   * @param pinTur Output pin for regulating turning.
   * @param pinForwar Output pin for moving forward.
   * @param pinBac Output pin for moving back.
   * @param pinLef Output pin for moving left.
   * @param pinRigh Output pin for moving right. 
   */
  Car(int pinMovemen, int pinTur, int pinForwar, int pinBac, int pinLef, int pinRigh)
  {
    turn = 0;
    movement = 0;
    pinMovement = pinMovemen;
    pinTurn = pinTur;
    pinForward = pinForwar;
    pinBack = pinBac;
    pinLeft = pinLef;
    pinRight = pinRigh;
    
    pinMode(pinMovement, OUTPUT);
    pinMode(pinTurn, OUTPUT);
    pinMode(pinForward, OUTPUT);
    pinMode(pinBack, OUTPUT);
    pinMode(pinLeft, OUTPUT);
    pinMode(pinRight, OUTPUT);
  }
  /**
   * Metod for updating output on current values.
   * Sign of movement determine if we move forward or back,
   * and absolut value determine how fast it will move.
   * Turning is analog to movement.
   */
  void update()
  {
    if(movement >= 0)
    {
      digitalWrite(pinBack, LOW);
      digitalWrite(pinForward, HIGH);
      analogWrite(pinMovement, movement);
    }
    else
    {
      digitalWrite(pinForward, LOW);
      digitalWrite(pinBack, HIGH);
      analogWrite(pinMovement, -movement);
    }
    if(turn >= 0)
    {
      digitalWrite(pinLeft, LOW);
      digitalWrite(pinRight, HIGH);
      analogWrite(5, turn);
    }
    else
    {
      digitalWrite(pinRight, LOW);
      digitalWrite(pinLeft, HIGH);
      analogWrite(pinTurn, -turn);  
    }  
    
    // Output for cheching current state.
    Serial.print("Smjer je: ");
    Serial.print(turn);
    Serial.print(", a brzina je: ");
    Serial.println(movement);
    plaviZub.print("Smjer je: ");
    plaviZub.print(turn);
    plaviZub.print(", a brzina je: ");
    plaviZub.println(movement);
}
  /**
   * Function for turning left (changing angle value).
   * @param value Value for which angle will change.
   */
  void move_left(int value)
  {
    turn -= value;
    if(turn < -255)
      turn = -255;
    update();
  }
  /**
   * Function for turning right (changing angle value).
   * @param value Value for which angle will change.
   */
  void move_right(int value)
  {
    turn += value;
    if(turn > 255)
      turn = 255;
    update();
  }
  /**
   * Function for accelerating (changing movement value).
   * @param value Value for which angle will change.
   */
  void move_up(int value)
  {
    movement += value;
    if(movement > 255)
      movement = 255;
    update();
  }
  /**
   * Function for decelerate (changing movement value).
   * @param value Value for which angle will change.
   */
  void move_down(int value)
  {
    movement -= value;
    if(movement < -255)
      movement = -255;
    update();
  }    
  /*
   * Function for stoping, sets values of movement and turn on 0.
   */
  void zero()
  {
    movement = 0;
    turn = 0;
    
    update();
  }
};
// Initializing car with pins D3, D5, D7, D8, D9 and D11.
// Pins D3 and D5 are PWM on arduino NANO.
// (https://www.arduino.cc/en/Tutorial/PWM)
// so they can adjust output voltage.
// On arduino NANO they are following.
Car autic(3, 5, 7, 8, 9, 11);
/**
 * Function acordingly of input change speed/direction
 * of moving.
 * @param in Code from which function order change.
 */
void input(int in)
{
  plaviZub.print("Poslan je: ");
  plaviZub.println(in);
  
  switch(in) {
  case '1':
    autic.move_up(autic.acceleration);
    break;
  case '2':
    autic.move_down(autic.acceleration);
    break;
  case '4':
    autic.move_left(autic.angle);
    break;
  case '3':
    autic.move_right(autic.angle);
    break;
  default:
    autic.zero();
    break;
  }  
}
/**
 * Setup code for arduino.
 * Initialize communication rate.
 * Initialize 2 ways of comunication:
 * 1) communication over USB (RX and TX)
 * 2) communication over pins D12 and D13 RX and TX respectively.
 */
void setup() 
{
  // Begining of communicating over TX and RX pins.
  // In order to communicate over those pins arduino 
  //   MUST BE DISCONECTED FROM USB/COMPUTER.
  // Else it communicate over USB.
  
  Serial.begin(9600);
  Serial.println("Bluetooth On please press 1 for forward, 2 back, 3 left and 4 right.");
  plaviZub.begin(9600);
  plaviZub.println("Bluetooth On please press 1 for forward, 2 back, 3 left and 4 right.");
  
}
/**
 * Check if something is recived and respond accordingly.
 */
void loop() 
{
  if (Serial.available() > 0) 
  {
    receivedData = Serial.read();
    //plaviZub.print(receivedData);
    Serial.print("Received from comp: ");
    Serial.println(receivedData);
    input(receivedData);  
  }
  
  if(plaviZub.available() > 0)
  {
    receivedData = plaviZub.read();
    Serial.print("Received form bluetooth: ");
    Serial.println(receivedData);
    input(receivedData); 
  }
  delay(5);// prepare for next data ...
}
