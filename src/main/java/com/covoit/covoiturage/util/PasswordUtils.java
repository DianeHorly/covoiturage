package com.covoit.covoiturage.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitaire pour gérer le hashage et la vérification des mots de passe.
 * On centralise l'usage de BCrypt ici.
 */
public final class PasswordUtils {

    private PasswordUtils() {
        // Constructeur privé 
    }

    /**
     * Hash un mot de passe en clair avec BCrypt.
     * @param rawPassword mot de passe en clair
     * @return hash BCrypt
    */
    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Vérifie si un mot de passe en clair correspond à un hash BCrypt.
     * @param rawPassword mot de passe en clair
     * @param hash hash stocké en base
     * @return true si ça correspond, sinon false
     */
    public static boolean checkPassword(String rawPassword, String hash) {
        if (rawPassword == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, hash);
    }
}
