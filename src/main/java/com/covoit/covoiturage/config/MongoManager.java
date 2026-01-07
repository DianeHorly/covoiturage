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
 * */

public class MongoManager {

    private static MongoClient client;
    private static MongoDatabase database;

    static {
        // URL par défaut du serveur MongoDB local
        ConnectionString cs = new ConnectionString("mongodb://localhost:27017");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(cs)
                .build();

        client = MongoClients.create(settings);

        // Nom de la base: "covoiturage"
        database = client.getDatabase("covoiturage");
    }
    /**
     * Retourne l'instance de MongoDB pour la base "covoiturage".
    */

    public static MongoDatabase getDatabase() {
        return database;
    }
}
