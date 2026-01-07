# 1. Covoiturage – Application Web Java / Jakarta / MongoDB

Application web de covoiturage réalisée en **Java 21**, **Jakarta Servlet/JSP**, **MongoDB** et **Maven**.

Elle permet :

- aux **conducteurs** :
  - de proposer un trajet avec des **arrêts intermédiaires** (stops),
  - de définir un **prix global par place** pour le trajet complet,
  - de définir un **prix par segment** (sous-trajet entre deux villes successives),
  - de **gérer les demandes de réservation** (accepter / refuser),
  - de générer un **ticket avec QR Code** pour les réservations confirmées.

- aux **passagers** :
  - de **chercher un trajet** (même en sous-trajet : ex. Cannes → Monaco sur un Nice → Milan),
  - de **réserver** un trajet ou un sous-trajet,
  - de consulter **Mes réservations**,
  - de voir le **prix du parcours réellement réservé** (et non juste le prix du trajet complet).

---

## 2 Stack technique

- **Langage** : Java 21
- **Web** :
  - Jakarta Servlet 6.0
  - JSP 3.1
  - JSTL 3.0
- **Serveur d’applications** : Apache Tomcat **10.x** (compatibles namespace `jakarta.*`)
- **Base de données** : MongoDB (driver `mongodb-driver-sync`)
- **Build** : Maven (`war`)
- **Tests** : JUnit 5 (Jupiter)
- **Sécurité mots de passe** : BCrypt (`org.mindrot.jbcrypt`)
- **QR Code** : ZXing (`com.google.zxing`)

---

## 3 Structure du projet

Projet Maven de type **WAR** (Webapp Java classique) :
\## 3. Architecture du code



\### Packages principaux



\- `com.covoit.covoiturage.config`

&nbsp; - `MongoManager` : gestion de la connexion MongoDB.



\- `com.covoit.covoiturage.model`

&nbsp; - `User` : utilisateur.

&nbsp; - `Ride` : trajet de covoiturage.

&nbsp;   - contient `stops` (liste de villes),

&nbsp;   - `segmentPrices` (liste de prix par tronçon),

&nbsp;   - méthodes utilitaires :

&nbsp;     - `getFullPath()` : reconstitue `\[départ, arrêts..., arrivée]`,

&nbsp;     - `getIntermediateStops()`, `getStopsCountLabel()`,

&nbsp;     - `getPricePerSeatForSegment(int from, int to)`,

&nbsp;     - `computeUnitPriceForSegment(int from, int to)`,

&nbsp;     - helpers sur les noms de villes.

&nbsp; - `Booking` : réservation.



\- `com.covoit.covoiturage.dao`

&nbsp; - `UserDao`

&nbsp; - `RideDao`

&nbsp;   - conversion `Document` ⇔ `Ride`,

&nbsp;   - `searchByCitiesAndDate(...)`,

&nbsp;   - `findLatest(...)`, `findByDriverId(...)`.

&nbsp; - `BookingDao`



\- `com.covoit.covoiturage.service`

&nbsp; - `RideService`

&nbsp;   - validation métier,

&nbsp;   - construction de la liste d’arrêts,

&nbsp;   - normalisation des villes,

&nbsp;   - alignement des `segmentPrices` sur les tronçons,

&nbsp;   - insertion via `RideDao`.

&nbsp; - `BookingService`

&nbsp;   - réservation “simple” (`bookRide`) et par sous-trajet (`createBookingForSegment`),

&nbsp;   - calcul des places restantes sur le trajet et sur un sous-trajet (`getRemainingSeatsForSegment`),

&nbsp;   - confirmation / refus des réservations,

&nbsp;   - génération du ticket.



\- `com.covoit.covoiturage.web.servlet`

&nbsp; - `LoginServlet`, `LogoutServlet`, `RegisterServlet`

&nbsp; - `HomeServlet` (page d’accueil après connexion)

&nbsp; - `RideListServlet` (`/rides`) : recherche \& liste de trajets.

&nbsp; - `RideCreateServlet` (`/rides/new`) : création de trajet.

&nbsp; - `BookingServlet` (`/book`) : détail + formulaire de réservation.

&nbsp; - `MyBookingsServlet` (`/myBookings`) : réservations du passager.

&nbsp; - `DriverBookingsServlet` (`/driver/bookings`) : demandes reçues.

&nbsp; - `DriverBookingActionServlet` : POST d’acceptation / refus.

&nbsp; - `TicketServlet` (`/ticket`) : affichage du ticket.

&nbsp; - `TicketQrServlet` (`/ticket/qr`) : génération du PNG du QR Code.



\- `com.covoit.covoiturage.util`

&nbsp; - `DateTimeUtils`

&nbsp; - `BusinessException` (exceptions métier contrôlées).



\### Vue (JSP)



\- `src/main/webapp/index.jsp` : redirige vers `/home` ou `/login`.

\- `src/main/webapp/WEB-INF/jsp/`

&nbsp; - `layout.jspf` / `footer.jspf` : layout commun + navbar.

&nbsp; - `home.jsp`

&nbsp; - `login.jsp`, `register.jsp`

&nbsp; - `rides\_list.jsp` : recherche \& résultats.

&nbsp; - `rides\_new.jsp` : formulaire de création de trajet.

&nbsp; - `booking.jsp` : détail + réservation.

&nbsp; - `my\_bookings.jsp`

&nbsp; - `driver\_bookings.jsp`

&nbsp; - `ticket.jsp`



\### Front-end



\- `src/main/webapp/css/` : styles (Bootstrap + perso).

\- `src/main/webapp/js/new\_ride.js` :

&nbsp; - écoute `DOMContentLoaded`,

&nbsp; - reconstruit la liste des segments dès que départ/arrivée/arrêts/prix global changent,

&nbsp; - crée un input `number` pour chaque tronçon,

&nbsp; - stocke les valeurs dans le champ caché `segmentPricesCsv` avant la soumission.



---



\ 4. Prérequis



\- Java \*\*21+\*\*

\- Maven \*\*3.8+\*\*

\- Tomcat \*\*10.1+\*\*

\- MongoDB (local ou distant)  

&nbsp; Exemple : `mongodb://localhost:27017/covoiturage`



---



\ 5. Configuration MongoDB



Par défaut, la classe `MongoManager` est configurée pour se connecter à :



```java

// exemple typique :

MongoClient client = MongoClients.create("mongodb://localhost:27017");

MongoDatabase db = client.getDatabase("covoiturage");


## Exécuter le projet avec Eclipse

### Prérequis

- Java 21 installé (JAVA_HOME configuré)
- Apache Tomcat 10.1+ (compatible Jakarta Servlet 6.0)
- MongoDB en local sur `mongodb://localhost:27017`
  - base utilisée : `covoiturage`
- Eclipse IDE avec support Maven (m2e)

### Import du projet Maven dans Eclipse

1. `File` -> `Import...`
2. `Maven` -> **Existing Maven Projects** -> `Next`
3. Choisir le dossier du projet `covoiturage`
4. Cocher le projet trouvé -> `Finish`

### Mise à jour Maven

Après toute modification de `pom.xml` :

1. Clic droit sur le projet -> `Maven` ->**Update Project…**
2. Cocher le projet -> `OK`

### Configuration de Tomcat dans Eclipse

1. `Window` -> `Show View` -> `Servers`
2. Dans la vue **Servers**, cliquer sur le lien *"No servers are available..."*
3. Choisir **Apache Tomcat v10.1** (ou version 10.x compatible Jakarta)
4. Indiquer le chemin d’installation de Tomcat -> `Finish`

Associer le projet au serveur :

1. Dans la vue *Servers*, double-cliquer sur `Tomcat v10.1 Server at localhost`
2. Cliquer sur **“Add and Remove…”**
3. Sélectionner le projet `covoiturage` dans la colonne de gauche
4. Cliquer sur **Add >** -> `Finish`

Cibler Tomcat comme runtime :

1. Clic droit sur le projet -> `Properties`
2. `Targeted Runtimes`
3. Cocher **Apache Tomcat v10.1** -> `Apply and Close`

### Lancer l'application

1. Dans la vue *Servers*, clic droit sur `Tomcat v10.1 Server at localhost`
2. `Start`

Une fois Tomcat démarré, ouvrir dans le navigateur :

- Page d'accueil :  
  `http://localhost:8080/covoiturage/home`

- Liste des trajets :  
  `http://localhost:8080/covoiturage/rides`

> Le context path `covoiturage` est défini par `<finalName>covoiturage</finalName>` dans le `pom.xml`.

### Lancer les tests unitaires

## Lancer le projet Via Maven
si vous n'utiliser pas eclipse vous pouvez passer directement à l'exécution du code via un terminal

Dans un terminal à la racine du projet :

- Compiler le projet : mvn clean package


Ça génère target/covoiturage.war.

- Copier le WAR dans le dossier webapps de Tomcat :

cp target/covoiturage.war /chemin/vers/tomcat/webapps/


- Démarrer Tomcat :

./bin/startup.sh    # Linux / macOS
bin\startup.bat     # Windows

- Accéder à :

http://localhost:8080/covoiturage/
