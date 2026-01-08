# Covoiturage – Application Web Java / Jakarta EE / JSP /MongoDB

Application web de covoiturage réalisée en **Java 21**, **Jakarta Servlet/JSP**, **MongoDB** et **Maven**.

Elle permet :

- aux **conducteurs** :
  - de proposer un trajet avec des **arrêts intermédiaires** (stops),
  - de définir un **prix global par place** pour le trajet complet,
  - de définir un **prix par segment** (sous-trajet entre deux villes successives),
  - de **gérer les demandes de réservation** (accepter / refuser),
  - de générer un **ticket avec QR Code** pour les réservations confirmées.

- aux **passagers** :
  - de **chercher un trajet** (même en sous-trajet: ex. Cannes -> Monaco sur un trajet Nice -> Milan),
  - de **réserver** un trajet ou un sous-trajet,
  - de consulter **Mes réservations**,
  - de voir le **prix du parcours réellement réservé** (et non juste le prix du trajet complet).
- Gérer un QR Code pour les réservations(ticket)

---

## 1. Stack technique

- **Langage** : Java 21
- **Framework Web** :
  - Jakarta Servlet 6.0
  - JSP 3.1
  - JSTL 3.0
  - JSTL (Jakarta EE 10)  
- **Serveur d’applications** : Apache Tomcat **10.x** (compatibles namespace `jakarta.*`)
- **Base de données** : MongoDB 
- **Build** : Maven (packaging `.war`)
- **Front :** JSP, Bootstrap, un peu de JavaScript (new_ride.js pour les prix par segment)  
- **Tests** : JUnit 5 (Jupiter)
- **Sécurité mots de passe** : BCrypt (`org.mindrot.jbcrypt`)
- **QR Code** : ZXing (`com.google.zxing`)
- **Conteneurisation :** Docker et Docker Compose


---

## 2. Structure du projet

Racine du projet :

- `pom.xml` – configuration Maven
- `src/main/java` – code Java (servlets, services, DAO, modèles)
- `src/main/webapp` – JSP, ressources web
  - `WEB-INF/jsp` – pages JSP (home, login, rides, booking, etc.)
  - `WEB-INF/web.xml` – configuration web
  - `js/new_ride.js` – gestion dynamique des prix par segment
- `src/test/java` – tests unitaires JUnit
- `Dockerfile` – image Docker de l'application (Tomcat + `.war`)
- `docker-compose.yml` – orchestration MongoDB et de l'app
- `README.md` – 

---


## 3. Configuration MongoDB

La connexion Mongo est gérée dans `com.covoit.covoiturage.config.MongoManager` ou `src/main/java/com/covoit/covoiturage/config/MongoManager.java`
:

```java
ConnectionString cs = new ConnectionString("mongodb://localhost:27017");
MongoDatabase database = client.getDatabase("covoiturage");
```
- Hôte: localhost
- Port: 27017
- Base: covoiturage
MongoDB crée automatiquement la base et les collections à la première insertion.


---

## 4. Lancer l'application avec Docker
### 4.1. Prérequis
- Installer Docker
- Installer Compose
- instller Maven

### 4.2. Conteneurs utilisés
- MongoDB (image mongo:8.0)
- Application Java (image construite à partir du Dockerfile -> Tomcat 10 et .war)

### 4.3. Fichiers importants
- Dockerfile (à la racine) : build Maven et déploiement sur Tomcat.
- docker-compose.yml (à la racine) :
 *. Lance MongoDB
 *. Lance l'appli Java
 *. Mappe les ports :
 *. MongoDB -> localhost:27017
 *. App -> localhost:8082 (vers Tomcat 8080 dans le conteneur)

### 4.4. Construction de l'image de l’application
Depuis la racine du projet :
```docker build -t covoiturage-app .```

## 5. Lancement avec Docker Compose

Toujours à la racine du projet :
```docker compose up -d```

Après ces étapes, l'application sera disponible sur: 
```http://localhost:8082/covoiturage```

---

## 6. Lancement de l'application sans Docker (local/Tomcat)
### 6.1. Prérequis

- Java 21 installé (et dans le PATH)

- Maven installé

- Tomcat 10 installé localement

- MongoDB installé localement ou via Docker (port 27017)

###  6.2. Lancer MongoDB

- Soit via Docker :```docker run -d --name covoiturage-mongo -p 27017:27017 mongo:8.0```
- Soit via l'installation native MongoDB.

### 6.3. Construire le .war

À la racine du projet :```mvn clean package```

Le .war sera généré dans :
**target/covoiturage.war**

### 6.4. Déployer sur Tomcat
- Copier target/covoiturage.war dans le dossier webapps de Tomcat
- Démarrer Tomcat
- Accéder à l'application :
  *http://localhost:8080/covoiturage*

---

## 7. Tests unitaires

Lancer les tests : ```mvn test```
Les tests couvrent notamment :
  - La logique métier des trajets (Ride, calcul des segments, prix par sous-trajet)
  - Les services (RideService, BookingService)
  - La gestion des réservations par segment

---

## 8. Fonctionnalités principales

- Création de trajet :
  - Villes de départ / arrivée
  - Date / heure
  - Nombre de places
  - Prix global par place
  - Arrêts intermédiaires (stops)
  - Prix par segment (via new_ride.js et champ caché segmentPricesCsv)

- Recherche de trajets :
  - Par ville de départ / arrivée / date
  - Gestion des sous-trajets (ex : réserver seulement Reims → Paris sur un trajet Reims → Paris → Lyon → Nice)

- Réservation :
  - Calcul des places restantes par segment
  - Prix ajusté au sous-trajet
  - Statuts : PENDING, CONFIRMED, REJECTED
  - QR Code pour le ticket (via ZXing) 

---

## 9. Exécuter le projet avec Eclipse

### Prérequis

- Java 21 installé (JAVA_HOME configuré)
- Apache Tomcat 10.1+ (compatible Jakarta Servlet 6.0)
- MongoDB en local sur *`mongodb://localhost:27017`*
  - base utilisée : `covoiturage`
- Eclipse IDE avec support Maven (m2e)

### Import du projet Maven dans Eclipse

  - `File` -> `Import...`
  - `Maven` -> **Existing Maven Projects** -> `Next`
  - Choisir le dossier du projet `covoiturage`
  - Cocher le projet trouvé -> `Finish`

### Mise à jour Maven

Après toute modification de `pom.xml` :
  - Clic droit sur le projet -> `Maven` ->**Update Project…**
  - Cocher le projet -> `OK`

### Configuration de Tomcat dans Eclipse et Import dans Eclipse

  - File -> Import folder -> Existing Maven Projects
  - Choisir le dossier du projet covoiturage
  - Vérifier :
      - Java 21 comme JDK
      - Tomcat 10 configuré dans l’onglet Servers

4. Déployer le projet sur Tomcat depuis Eclipse.

### Si Tomcat n'est pas configuré alors:
1. `Window` -> `Show View` -> `Servers`
2. Dans la vue **Servers**, cliquer sur le lien *"No servers are available..."*
3. Choisir **Apache Tomcat v10.1** (ou version 10.x compatible Jakarta)
4. Indiquer le chemin d’installation de Tomcat -> `Finish`

#### Associer le projet au serveur :

1. Dans la vue *Servers*, double-cliquer sur `Tomcat v10.1 Server at localhost`
2. Cliquer sur **“Add and Remove…”**
3. Sélectionner le projet `covoiturage` dans la colonne de gauche
4. Cliquer sur **Add >** -> `Finish`

#### Cibler Tomcat comme runtime :

1. Clic droit sur le projet -> `Properties`
2. `Targeted Runtimes`
3. Cocher **Apache Tomcat v10.1** -> `Apply and Close`

### Lancer l'application

1. Dans la vue *Servers*, clic droit sur `Tomcat v10.1 Server at localhost`
2. `Start`

Une fois Tomcat démarré, ouvrir dans le navigateur :

- Page d'accueil :  
  *`http://localhost:8080/covoiturage/home`*

- Liste des trajets :  
  *`http://localhost:8080/covoiturage/rides`*
 Le context path `covoiturage` est défini par `<finalName>covoiturage</finalName>` dans le `pom.xml`.

---

# Exécution de ce projet chez soi
## 1. Cloner le dépôt :

```git clone https://github.com/<ton-compte>/covoiturage.git```
```cd covoiturage```

## 2. Lancer via Docker :
- installer avant docker desktop, puis saissiser ces commandes: ```docker build -t covoiturage-app .
docker compose up -d```
Ça génère target/covoiturage.war.

- Copier le WAR dans le dossier webapps de Tomcat :```
cp target/covoiturage.war /chemin/vers/tomcat/webapps/```

- Démarrer Tomcat :
```./bin/startup.sh    # sur Linux / macOS```
```bin\startup.bat     # sur Windows```

- Accéder à l'app: *http://localhost:8080/covoiturage/*

## 3. Ouvrir le navigateur sur :
*http://localhost:8082/covoiturage*

---
