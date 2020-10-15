#include <SoftwareSerial.h>
#include <LiquidCrystal.h>

SoftwareSerial BT(10, 11); //Rx, Tx
LiquidCrystal lcd(7, 6, 5, 4, 3, 2); //RS E D4 D5 D6 D7

#define PIN_RELAY 9

String text, SMSnumber, SMStext; 

void setup()
{
  //serial monitor and bt serial open
  Serial.begin(9600);
  BT.begin(9600);

  //relee
  pinMode(PIN_RELAY, OUTPUT);
  digitalWrite(PIN_RELAY, LOW); //relee off
  
  //test msgs
  BT.println("bt module is ready");
  Serial.println("arduino monitor is ready");
  
  lcd.begin(16,2);
  
  lcd.setCursor(0, 1);
  lcd.print("Number");
  lcd.setCursor(0, 0);
  lcd.print("S6num!");
}
 
void loop()
{
  //keep reading from bt module and send to arduino serial monitor
  if (BT.available())
  {
    text = BT.readString();
    SMSnumber = text.substring(0, (text.indexOf('\n'))); 
    SMStext = text.substring((text.indexOf('\n')+1), text.length()); 
    Serial.println("number: " + SMSnumber); 
    Serial.println("text:   " + SMStext);

    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print(SMSnumber);
    lcd.setCursor(0, 0);
    lcd.print(SMStext);

    if (SMStext.equals("marko")){
      BT.println("polo");
    }
    else if(SMStext.equals("ma")){
      BT.println("mama");
    }
    else if(SMStext.equals("relee_on")){
      BT.println("relee on");
      digitalWrite(PIN_RELAY, HIGH); //relee on
    }
    else if(SMStext.equals("relee_off")){
      BT.println("relee off");
      digitalWrite(PIN_RELAY, LOW); // relee off
    }
    else{
      BT.println("error operator");
    }
    
    
    //Serial.write(BT.read());
  }

  //keep reading from arduino serial monitor and send to bt module
  //if (Serial.available())
  //{
  //  BT.write(Serial.read());
  //}
}
