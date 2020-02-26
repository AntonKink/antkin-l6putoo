# IoT-seadme kondikava väljatöötamine bluetooth ja arduino abil  

## Arduino  
Kõik arduino skriptid asuvad kaustas Arduino. 

### Skriptid ja nende lühikirjeldus 
- MyBlink - Skript arduino testimisel. Lülitab LED sisse ja välja 1 sekundilise viivitusega.  
- Test1 - Esimene proovi script. Väljastab serial monitorile testmuutujad ja arv, mis   suurendatakse 1st kuni 50.  
- Test2 - Esimene script BT ühenduse loomiseks ja testimiseks. nüüd arduino väljastab serial  monitorile BT moodulile saadud sõnumeid.  
- Test3 - Script BT ühenduse loomiseks. Sõnumeid saadetakse formaadis: "SmsNumber\nSmsText"   
Sõnumeid, mis saadab teised seaded BT-moodulile arduino kirjutab serial monitorile.  

## SMSApp  
- loeb sisse tulevaid smsid, fail asub kaustas "APK" nimega "app-debug_v1.apk"

## Lühikirjeldus  
Kirjutada lihtne rakendus telefoni jaoks, mis saadab sissetulevad SMSid bluetoothi kaudu   
Arduinole ja see omakorda neid kuidagi kuvab (näiteks terminali kaudu nagu PUTTY või   
midagi taolist).Arduino lahendus võiks kasutada ka näiteks mingit on-board ekraani.

Tegu võiks olla mingi praktilise lahendusega, et see on ainult osa suuremast süsteemist.   
Näiteks sauna kerise mobiiliga juhtimine. Ühelt telefonilt saadetakse teisele info ning teine  
telefon edastab selle kontrollerile, mis siis omakorda juhib sauna kerist või mingit muud seadet

## ...  
Autor: Anton Kink 179284IACB  
Juhendaja: Priit Ruberg, PhD
