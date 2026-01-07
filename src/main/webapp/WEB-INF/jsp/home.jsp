<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8"%>
    
    
<%-- 
    Page: home.jsp
    Rôle: page d'accueil de l'application.
    - Présente le service de covoiturage (hero)
    - Explique comment ça marche
    - Affiche quelques trajets récents (latestRides)
 --%>

    
    
<%-- 
  On déclare les taglibs JSTL en "Jakarta", 
  indispensables avec Tomcat 10 et Servlet 6.0 
--%>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%
	// titre de la page utilisé par layout.jspf
    request.setAttribute("pageTitle", "Accueil - Covoiturage");
	//Indique à la navbar que la page active est "home"
	request.setAttribute("activePage", "home");
%>

<%-- 
  On réutilise la mise en page (header, navbar, container, CSS/JS Bootstrap, etc.)
  contenu dans layout.jspf qui est un fragment (.jspf) dans le même dossier : /WEB-INF/jsp/
--%>
<%@ include file="layout.jspf" %>

<%-- 
  CONTENU SPÉCIFIQUE À LA PAGE D'ACCUEIL
  - Si l'utilisateur est en session -> message personnalisé
  - Sinon -> call-to-action (Connexion / Inscription)
--%>


<%-- SECTION HERO (bandeau principal) --%>
<div class="row mb-5">
    <div class="col-12">
        <div class="hero-home rounded-4 p-4 p-md-5 mb-4 shadow-sm">
            <div class="row align-items-center">
                
                <%-- Colonne texte --%>
                <div class="col-md-7">
                    <h1 class="display-5 fw-bold mb-3 text-white">
                        Partage tes trajets simplement.
                    </h1>

                    <p class="lead text-light mb-4">
                        Trouve un covoiturage en quelques clics ou propose ton trajet
                        pour partager les frais et réduire ton impact carbone.
                    </p>

                    <div class="d-flex flex-wrap gap-2">
                        <%-- Bouton pour aller voir les trajets --%>
                        <a href="${pageContext.request.contextPath}/rides"
                           class="btn btn-light btn-lg">
                            Trouver un trajet
                        </a>

                        <%-- Si connecté alors redirige vers le bouton "Proposer un trajet", sinon "Créer un compte" --%>
                        <c:choose>
                            <c:when test="${not empty sessionScope.user}">
                                <a href="${pageContext.request.contextPath}/rides/new"
                                   class="btn btn-outline-light btn-lg">
                                    Proposer un trajet
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/register"
                                   class="btn btn-outline-light btn-lg">
                                    Créer un compte
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <%-- Colonne information complémentaire (message, avantages) --%>
                <div class="col-md-5 mt-4 mt-md-0">
                    <div class="bg-white bg-opacity-10 rounded-4 p-3 text-light small">
                        <p class="mb-2">
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty sessionScope.user}">
                                        Ravi de te revoir, ${sessionScope.user.name}
                                    </c:when>
                                    <c:otherwise>
                                        Prêt à partir ?
                                    </c:otherwise>
                                </c:choose>
                            </strong>
                        </p>
                        <p class="mb-1">
                        	<ul>
                             	<li> Réserve ta place en ligne.</li>
                            	<li> Reçois un ticket avec QR Code.</li>
                            	<li> Échange avec le conducteur en toute sécurité.</li>
                        	</ul>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- SECTION "COMMENT ÇA MARCHE" --%>
<div class="row mb-5">
    <div class="col-md-10 offset-md-1">
        <h2 class="h4 mb-4">Comment ça marche ?</h2>

        <div class="row g-3">
            <%-- Étape 1 --%>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0 step-card">
                    <div class="card-body">
                        <div class="step-number mb-2">1</div>
                        <h5 class="card-title">Cherche ou propose</h5>
                        <p class="card-text small text-muted">
                            Entre ton trajet (départ, arrivée, date) ou publie ton trajet
                            comme conducteur.
                        </p>
                    </div>
                </div>
            </div>

            <%-- Étape 2 --%>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0 step-card">
                    <div class="card-body">
                        <div class="step-number mb-2">2</div>
                        <h5 class="card-title">Réserve en ligne</h5>
                        <p class="card-text small text-muted">
                            Choisis le trajet qui te convient, réserve tes places
                            et attends la confirmation du conducteur.
                        </p>
                    </div>
                </div>
            </div>

            <%-- Étape 3 --%>
            <div class="col-md-4">
                <div class="card h-100 shadow-sm border-0 step-card">
                    <div class="card-body">
                        <div class="step-number mb-2">3</div>
                        <h5 class="card-title">Voyage serein</h5>
                        <p class="card-text small text-muted">
                            Reçois ton ticket avec QR Code et retrouve le conducteur
                            au point de départ le jour J.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%-- SECTION TRAJETS RÉCENTS (latestRides --%>
<div class="row mb-4">
    <div class="col-md-10 offset-md-1">

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h4 mb-0">Trajets récents</h2>
            <a href="${pageContext.request.contextPath}/rides"
               class="small text-decoration-none">
                Voir tous les trajets &rarr;
            </a>
            
        </div>

        <c:choose>
            <%-- Si aucun trajet en base  --%>
            <c:when test="${empty latestRides}">
                <div class="alert alert-light border">
                    Aucun trajet disponible pour le moment. Reviens un peu plus tard
                    ou sois le premier à proposer un trajet !
                </div>
            </c:when>
			<%-- Cas où on a des trajets récents à afficher --%>
            <c:otherwise>
                <div class="row row-cols-1 row-cols-md-3 g-3">
                    <%-- On parcourt la liste latestRides fournie par HomeServlet --%>
                    <c:forEach var="r" items="${latestRides}">
                        <div class="col">
                            <div class="card shadow-sm h-100 border-0 ride-card">
                                <div class="card-body d-flex flex-column">

                                    <h5 class="card-title mb-1">
                                        ${r.departureCityDisplay}
                                        <span class="text-muted">&rarr;</span>
                                        ${r.arrivalCityDisplay}
                                    </h5>
                                    <div class="text-muted small mb-2">
                                        ${r.departureDateTimeFormatted}
                                    </div>

                                    <div class="small text-muted">
									    Itinéraire: ${r.stopsDisplay}
									</div>
                                    <c:if test="${not empty r.description}">
                                        <p class="card-text small text-muted mb-2">
                                            ${r.description}
                                        </p>
                                    </c:if>

                                    <div class="mt-auto d-flex justify-content-between align-items-end">
                                        <div class="small">
                                            <div class="fw-bold">
                                                ${r.pricePerSeat} € / place
                                            </div>
                                            <div class="text-muted">
                                                ${r.totalSeats} place(s)
                                            </div>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/book?rideId=${r.id}"
                                           class="btn btn-sm btn-primary">
                                            Réserver
                                        </a>
                                    </div>

                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="footer.jspf" %>