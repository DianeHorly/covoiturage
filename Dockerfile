# --- construction du .war avec Maven ---
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Dossier de travail dans le conteneur
WORKDIR /app

# Copie du pom.xml pour profiter du cache Maven
COPY pom.xml .

# Copie le code source
COPY src ./src

# Construire le projet qui génère target/covoiturage.war
RUN mvn -B -DskipTests package


# --- Image finale avec Tomcat et l'application ---
FROM tomcat:10.1-jdk21-temurin

# Nettoyage des applis par défaut de Tomcat (ROOT, docs, etc.)
RUN rm -rf /usr/local/tomcat/webapps/*

# Copie du war construit dans le build Maven
COPY --from=build /app/target/covoiturage.war /usr/local/tomcat/webapps/covoiturage.war

# Tomcat écoute sur 8080
EXPOSE 8080

# Lancer Tomcat
CMD ["catalina.sh", "run"]
