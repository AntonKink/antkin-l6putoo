# IoT-seadme kondikava väljatöötamine bluetooth ja arduino abil  

## Arduino  
Kõik arduino skriptid asuvad kaustas Arduino. 

### Skriptid ja nende lühikirjeldus 
- MyBlink - Skript arduino testimisel. Lülitab LED sisse ja välja 1 sekundilise viivitusega.  
- SerialMonitorTest - Esimene proovi script. Väljastab serial monitorile testmuutujad ja arv, mis   suurendatakse 1st kuni 50.  
- BtToSMTest - Esimene script BT ühenduse loomiseks ja testimiseks. nüüd arduino väljastab serial  monitorile BT moodulile saadud sõnumeid.  
- BtToSM - Script BT ühenduse loomiseks. Sõnumeid saadetakse formaadis: "SmsNumber\nSmsText"   
Sõnumeid, mis saadab teised seaded BT-moodulile arduino kirjutab serial monitorile.  
- DisplayTest - Skript Ekraani testimiseks. Kuvab lähtestamisest möödunud sekundite arvu  
- BtToDisplay - kirjutab vastuvõetud sõnumid ekraanile ja serial monitorile.  Sõnumeid saadetakse formaadis: "SmsNumber\nSmsText"

## SMSApp  
- VERSIOON_1 - loeb sisse tulevaid sms, fail asub kaustas "APK" nimega "app-debug_v1.apk"  
- VERSIOON_2 - lülitab sisse/välja Bt, fail asub kaustas "APK" nimega "app-debug_v2.apk"  
- VERSIOON_3 - teeb ühendust ja saadab info Bt kaudu, fail asub kaustas "APK" nimega "app-debug_v3.apk"  
- VERSIOON_6 - filter, salvestab filter, sms vastus (tagasiside), arduino vastuse monitor, fail asub kaustas "APK" nimega "app-debug_v65.apk" 
   
## Lühikirjeldus  
Kirjutada lihtne rakendus telefoni jaoks, mis saadab sissetulevad SMSid bluetoothi kaudu   
Arduinole ja see omakorda neid kuidagi kuvab (näiteks terminali kaudu nagu PUTTY või   
midagi taolist).Arduino lahendus võiks kasutada ka näiteks mingit on-board ekraani.

Tegu võiks olla mingi praktilise lahendusega, et see on ainult osa suuremast süsteemist.   
Näiteks sauna kerise mobiiliga juhtimine. Ühelt telefonilt saadetakse teisele info ning teine  
telefon edastab selle kontrollerile, mis siis omakorda juhib sauna kerist või mingit muud seadet

## Autor  
Anton Kink 179284IACB  
Juhendaja: Priit Ruberg, PhD
