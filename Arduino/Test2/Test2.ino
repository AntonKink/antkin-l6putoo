/*  Nimi: Test2
 *  
 *  Selgitus:
 *    esimene script BT ühenduse loomiseks ja testimiseks. sõnumeid, mis saadab teised seaded BT-moodulile arduino kirjutab serial monitorile
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

char BTData;

SoftwareSerial BT(10, 11); //Rx, Tx - UART-liides kontrolleriga suhtlemiseks
 
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
  //lugeda bt-moodulist ja saata arduino serial monitor'ile
  if (BT.available())
  {
    BTData = BT.read();
    Serial.print(BTData); // võib ka kasutada Serial.write(BTData);
  }
}
