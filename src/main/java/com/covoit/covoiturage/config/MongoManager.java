package com.covoit.covoiturage.config;

import com.mongodb.ConnectionString;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/*
 * connexion à MongoDB
 * MongoDB créera automatiquement la base covoiturage 
 * et les collections à la première insertion. 
 * - En local (Tomcat/Eclipse) : utilise par défaut mongodb://localhost:27017/covoiturage
 * - En Docker : on peut surcharger via la variable d'environnement MONGO_URI
 *   par exple. MONGO_URI=mongodb://mongo:27017/covoiturage
 * */

public class MongoManager {

    private static MongoClient client;
    private static MongoDatabase database;

    static {
        // On essaie d'abord de lire l'URL dans la variable d'environnement MONGO_URI
        String uri = System.getenv("MONGO_URI");

        //  Si rien n'est défini, on utilise la valeur par défaut (dev local)
        if (uri == null || uri.isBlank()) {
            uri = "mongodb://localhost:27017/covoiturage";
        }

        //  Création de la ConnectionString à partir de l'URI
        ConnectionString cs = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();

        client = MongoClients.create(settings);

        //  Nom de la base :
        //    - si elle est présente dans l'URI ( /covoiturage), on la récupère
        //    - sinon on retombe sur "covoiturage"
        String dbName = cs.getDatabase();
        if (dbName == null || dbName.isBlank()) {
            dbName = "covoiturage";
        }

        database = client.getDatabase(dbName);
    }

    /**
     * Retourne l'instance de MongoDatabase.
     */
    public static MongoDatabase getDatabase() {
        return database;
    }
}