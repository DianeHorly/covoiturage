package com.covoit.covoiturage.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Filtre qui force l'encodage UTF-8 sur toutes les requêtes/réponses.
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        // Encodage pour la requête (paramètres)
        request.setCharacterEncoding("UTF-8");

        // Encodage pour la réponse (HTML, JSON, etc.)
        response.setCharacterEncoding("UTF-8");

        chain.doFilter(request, response);
    }
}
