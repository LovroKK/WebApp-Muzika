## Pokretanje API

## provjera enviormenta (ako nema ide na Instalacija)

    java -version
    mvn -version

# Instalacija (koristi Chocolatey ako ga imaš)

    choco install openjdk17
    choco install maven

    maunalni install, preuzmite:
    - Java: https://www.oracle.com/java/technologies/downloads/
    - Maven: https://maven.apache.org/download.cgi 

    obavezno podesiti PATH (enviorment variables)

## pozicionirati se u ispravni folder

    cd backend

## pokrenuti springboot aplikaciju s mvn dependencies

    mvn spring-boot:run

