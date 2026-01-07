package com.covoit.covoiturage.util;

import java.time.*;
import java.util.Date;

/**
 * Utilitaire central pour gérer les conversions de dates/temps
 * entre la zone "métier" de l'application (Europe/Paris) et MongoDB (UTC).
 */
public final class DateTimeUtils {

    // Fuseau "officiel" de l'appli (heure affichée aux utilisateurs)
    public static final ZoneId APP_ZONE = ZoneId.of("Europe/Paris");

    private DateTimeUtils() { }

    /**
     * Convertit un LocalDateTime (dans le fuseau APP_ZONE) en java.util.Date (UTC)
     * pour stockage dans MongoDB.
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        ZonedDateTime zdt = localDateTime.atZone(APP_ZONE);
        return Date.from(zdt.toInstant());
    }

    /**
     * Convertit une Date (UTC depuis Mongo) en LocalDateTime dans APP_ZONE.
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, APP_ZONE);
    }

    /**
     * "Maintenant" dans le fuseau APP_ZONE (pour les règles métier).
     */
    public static LocalDateTime nowApp() {
        return LocalDateTime.now(APP_ZONE);
    }

    /**
     * Début de journée (00:00) dans APP_ZONE, converti en Date (UTC) pour requêtes Mongo.
     */
    public static Date startOfDay(LocalDate date) {
        LocalDateTime ldt = date.atStartOfDay();
        return toDate(ldt);
    }

    /**
     * Début de la journée suivante , pour faire des BETWEEN [start, end[.
     */
    public static Date startOfNextDay(LocalDate date) {
        LocalDateTime ldt = date.plusDays(1).atStartOfDay();
        return toDate(ldt);
    }
}
