/*  Nimi: Test1
 *  
 *  Selgitus:
 *    Skript väljastab serial monitori tabeli ja loeb 0-st kuni 50-ni. Kui loendur on suurem kui 50, siis loendur = 0
 * 
 *  Autor: Anton Kink
 *  
 *  Kasutatud materjalid: 
 *    https://www.arduino.cc/reference/en/
 *  
 */
#include <SoftwareSerial.h> 
   
int loendur = 0; // ridade arv
int test_1 = 123; // testmuutuja 1
int test_2 = 456; // testmuutuja 2

void setup(){
  Serial.begin(9600); // andmekiirus 9600 bps 
  Serial.println("Test 1, Test 2, Num Rows"); // veergude pealkirjad
  }
  
void loop(){   
  loendur++; // rea number + 1   
  Serial.print(test_1);  // väljund
  Serial.print(",");  
  Serial.print(test_2);  
  Serial.print(",");  
  Serial.println(loendur);    
  
  // kui loendur on suurem kui 50, siis loemdur = 0
  if (loendur > 50){     
    loendur = 0;    
    Serial.println("NEW ROW");   
    }    
    delay(1000); // viivitus
} 
