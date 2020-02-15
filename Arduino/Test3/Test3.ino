/*  Nimi: Test3
 *  
 *  Selgitus:
 *    Script BT ühenduse loomiseks. Sõnumeid saadetakse formaadis: "SmsNumber\nSmsText" 
 *    Sõnumeid, mis saadab teised seaded BT-moodulile arduino kirjutab serial monitorile.
 * 
 * Ühendus:
 *  Arduino:    GND +5V 10  11  -
 *  BT module:  GND VCC TXD RXD KEY
 * 
 *  Autor: Anton Kink
 *  
 *  Kasutatud materjalid: 
 *    https://www.arduino.cc/reference/en/
 *    kood on võetud siin (ta on muudetud):
 *    https://robotclass.ru/articles/bluetooth-hc-05-06/4/
 *  
 */
#include <SoftwareSerial.h>

SoftwareSerial BT(10, 11); //Rx, Tx - UART-liides kontrolleriga suhtlemiseks

String text, SMSnumber, SMStext; 

void setup()
{
  //serial monitor and bt serial open
  Serial.begin(9600);
  BT.begin(9600);
  
  //test msgs
  BT.println("bt module is ready");
  Serial.println("arduino monitor is ready");
}
 
void loop()
{
  //lugeda bt-moodulist ja saata arduino serial monitori'le
  if (BT.available())
  {
    text = BT.readString();
    SMSnumber = text.substring(0, (text.indexOf('\n'))); 
    SMStext = text.substring((text.indexOf('\n')+1), text.length()); 
    Serial.println("number: " + SMSnumber); 
    Serial.println("text:   " + SMStext);
    
    //Serial.write(BT.read());
  }

  //lugeda arduino serial monitorist ja saata bt-mooduli'le 
  //if (Serial.available())
  //{
  //  BT.write(Serial.read());
  //}
}
