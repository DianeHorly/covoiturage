package com.covoit.covoiturage.model;

import org.bson.types.ObjectId;

/**
 * Représente un utilisateur de l'application.
 */
public class User {

    private ObjectId id;        // identifiant MongoDB
    private String name;        // nom complet
    private String email;       // email unique
    private String passwordHash; // mot de passe hashé (BCrypt)

    // -------     Getters et setters  --------------
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
