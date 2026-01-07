package com.covoit.covoiturage.service;

import com.covoit.covoiturage.dao.UserDao;
import com.covoit.covoiturage.model.User;
import com.covoit.covoiturage.util.BusinessException;
import com.covoit.covoiturage.util.PasswordUtils;

/**
 * Ici on fait :
 *  - validation des données
 *  - règles fonctionnelles (unicité email, etc.)
 *  - hashage du mot de passe
 *
 * Les servlets utilisent UserService, pas directement UserDao.
 */


public class UserService {

    private final UserDao userDao = new UserDao();

    /**
     * Inscrit un nouvel utilisateur.
     * @throws BusinessException si les données sont invalides ou l'email déjà utilisé.
     */
    public User register(String name, String email, String rawPassword) throws BusinessException {

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Tous les champs sont obligatoires.");
        }

        // Vérifier que l'email n'est pas déjà pris
        if (userDao.findByEmail(email) != null) {
            throw new BusinessException("Cet email est déjà utilisé.");
        }

        // Hashage du mot de passe
        String hash = PasswordUtils.hashPassword(rawPassword);

        // Création de l'utilisateur
        User u = new User();
        u.setName(name.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPasswordHash(hash);

        // Persistance
        userDao.insert(u);

        return u;
    }

    /**
     * Tente de connecter un utilisateur.
     * @return l'utilisateur si OK
     * @throws BusinessException si email inconnu ou mot de passe invalide
     */
    public User login(String email, String rawPassword) throws BusinessException {

        if (email == null || email.isBlank()
                || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Email et mot de passe sont obligatoires.");
        }

        User user = userDao.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new BusinessException("Email ou mot de passe incorrect.");
        }

        if (!PasswordUtils.checkPassword(rawPassword, user.getPasswordHash())) {
            throw new BusinessException("Email ou mot de passe incorrect.");
        }

        return user;
    }
}
