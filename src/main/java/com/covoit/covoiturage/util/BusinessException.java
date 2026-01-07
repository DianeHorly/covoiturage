package com.covoit.covoiturage.util;

/**
 * Exception "métier" pour signaler proprement les erreurs fonctionnelles
 * (exple: email déjà utilisé, mot de passe invalide, etc.)
 *
 * Les services lanceront des BusinessException que les servlets
 * pourront attraper pour afficher un message à l'utilisateur.
 */
public class BusinessException extends Exception {
    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }
}
