package com.covoit.covoiturage.dao;

import com.covoit.covoiturage.config.MongoManager;
import com.covoit.covoiturage.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;


import static com.mongodb.client.model.Filters.eq;

/**
 * DAO responsable de l'accès à la collection "users" dans MongoDB.
 * Ne contient QUE la logique de persistance (CRUD).
 * Le DAO ne fait que  parler à Mongo et mapper Document <--> User.
 */
public class UserDao {

    private final MongoCollection<Document> col;

    public UserDao() {
        MongoDatabase db = MongoManager.getDatabase();
        this.col = db.getCollection("users"); // collection "users"
    }

    //Convertit un Document MongoDB en objet User.
    private User toUser(Document doc) {
    	if (doc == null) return null;
    	User u = new User();
    	u.setId(doc.getObjectId("_id"));
    	u.setName(doc.getString("name"));
    	u.setEmail(doc.getString("email"));
    	u.setPasswordHash(doc.getString("passwordHash"));
    	return u;
    }

    //Trouve un utilisateur par son identifiant MongoDB.
    public User findById(ObjectId id) {
        //Document doc = col.find(eq("email", email)).first();
        Document doc = col.find(eq("_id", id)).first();
        return toUser(doc);
    }

    //Trouve un utilisateur par son email.
    public User findByEmail(String email) {
        Document doc = col.find(eq("email", email)).first();
        return toUser(doc);
    }
    
    /**
     * Insère un nouvel utilisateur en base.
     * L'id généré par MongoDB est réinjecté dans l'objet User.
     */
    public void insert(User user) {
        Document doc = new Document()
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("passwordHash", user.getPasswordHash());

        col.insertOne(doc);
        user.setId(doc.getObjectId("_id"));  // récupère l'id généré
    }
}
