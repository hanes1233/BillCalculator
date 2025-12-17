# TelephoneBillCalculator

## Popis 

Aplikace vypočítá částky k úhradě za telefonní účet podle výpisu volání.  
Metoda `TelephoneBillCalculator.calculate(phoneLog)` provede validaci vstupu a přemapuje vstup do seznamu objektů `CallRecord`.  
Následně vypočítá částky k úhradě pro každý `CallRecord` a vrátí finální částku.

## Validace
1. Validace vstupu – kontrola, zda není `null` ani prázdný (`blank`).
2. Pro mapování do `CallRecord`ů je použito `CsvToBeanBuilder` z balíčku `opencsv`; každé pole v objektu `CallRecord` má své pořadí (`CsvBindByPosition`).
3. Pro validaci čísla (`CallRecord.destination`) je implementován `PhoneNumberConverter`, který zkontroluje, zda číslo odpovídá požadavkům (začíná na 420 a má přesně 12 číslic).
4. Při nastavování datůmu a času začátku a konce hovoru se kontroluje, zda poskytnutý čas dává logický smysl (`!endTime.isBefore(startTime)`).

## Testy
Otestováno jednoduchými junit testy (TelephoneBillCalculatorTest) pomocí jupiter balíčku

## Instalace / Build
Projekt používá Maven.  
Pro zkompilování a spuštění testů:
```bash
mvn clean install
mvn test
