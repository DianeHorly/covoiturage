<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8" %>
    
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>



<%
    request.setAttribute("pageTitle", "Trajets - Covoiturage");
	request.setAttribute("activePage", "rides");

%>

<%@ include file="layout.jspf" %>

<div class="row mb-4">
    <div class="col-md-10 offset-md-1">

        <div class="card shadow-sm">
            <div class="card-body">
                <h2 class="mb-3">Rechercher un trajet</h2>

                <form method="get" action="${pageContext.request.contextPath}/rides">
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Ville de départ</label>
                            <input class="form-control"
                                   type="text"
                                   name="departureCity"
                                   value="${departureCity}">
                        </div>

                        <div class="col-md-4 mb-3">
                            <label class="form-label">Ville d'arrivée</label>
                            <input class="form-control"
                                   type="text"
                                   name="arrivalCity"
                                   value="${arrivalCity}">
                        </div>

                        <div class="col-md-4 mb-3">
                            <label class="form-label">Date</label>
                            <input class="form-control"
                                   type="date"
                                   name="date"
                                   value="${date}">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary">
                        Chercher
                    </button>

                    <a href="${pageContext.request.contextPath}/rides"
                       class="btn btn-outline-secondary ms-2">
                        Réinitialiser
                    </a>
                </form>
            </div>
        </div>

    </div>
</div>

<!-- Bloc résultats -->
<div class="row">
    <div class="col-md-10 offset-md-1">

        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                ${error}
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty rides}">
                <div class="alert alert-info">
                    Aucun trajet trouvé pour ce filtre.
                </div>
            </c:when>

            <c:otherwise>
                 <%-- Grille de cartes --%>
                <div class="row row-cols-1 row-cols-md-2 g-3">
                    <c:forEach var="r" items="${rides}">
                        <div class="col">
                            <div class="card shadow-sm h-100 ride-card">
                                <div class="card-body d-flex flex-column">

                                    <%-- Ligne principale: villes et prix --%>
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <div>
                                            <h5 class="card-title mb-1">
                                                ${r.departureCityDisplay}
                                                <span class="text-muted"> &rarr;</span>
                                                ${r.arrivalCityDisplay}
                                            </h5>
                                            <div class="text-muted small">
                                                ${r.departureDateTimeFormatted}
                                            </div>
                                            <%-- Itinéraire avec les arrêts --%>
											<c:if test="${not empty r.fullPath}">
											    <div class="small text-muted mb-2">
											        <span class="fw-semibold">Itinéraire :</span>
											        ${r.stopsDisplay}
											        <span class="ms-2">· ${r.stopsCountLabel}</span>
											    </div>
											</c:if>
                                        </div>
                                        <div class="text-end">
                                            <div class="fw-bold fs-5">
                                                ${r.pricePerSeat} € <span class="fs-6 text-muted">/ place</span>
                                            </div>
                                            <div class="text-muted small">
                                                ${r.totalSeats} place(s) totales
                                            </div>
                                        </div>
                                    </div>

                                    <%-- Description éventuelle --%>
                                    <c:if test="${not empty r.description}">
                                        <p class="card-text small text-muted mb-2">
                                            ${r.description}
                                        </p>
                                    </c:if>

                                    <div class="mt-auto d-flex justify-content-between align-items-center">
                                        <span class="badge bg-light text-dark border">
                                            Trajet disponible
                                        </span>

                                        <c:choose>
                                            <%-- Utilisateur NON connecté --%>
                                            <c:when test="${empty sessionScope.user}">
                                                <a href="${pageContext.request.contextPath}/login"
                                                   class="btn btn-sm btn-outline-primary">
                                                    Se connecter pour réserver
                                                </a>
                                            </c:when>

                                            <%-- Utilisateur connecté --%>
                                            
                                            <c:otherwise>
											    <c:choose>
											        <%-- Cas où l'utilisateur est le conducteur du trajet --%>
											        <c:when test="${not empty r.driverId and not empty sessionScope.user 
											                       and r.driverId == sessionScope.user.id}">
											            <span class="badge bg-secondary">
											                C'est ton trajet
											            </span>
											        </c:when>
											
											        <%-- Si conducteur different de celui qui a Proposé -> on peut réserver --%>
											        <c:otherwise>
											            <%-- URL vers /book en conservant la recherche de l'utilisateur --%>
														<c:url var="bookUrl" value="/book">
														    <c:param name="rideId" value="${r.id}"/>
														
														    <%-- si l'utilisateur a saisi une ville de départ dans le formulaire --%>
														    <c:if test="${not empty param.departureCity}">
														        <c:param name="from" value="${param.departureCity}"/>
														    </c:if>
														
														    <%-- si l'utilisateur a saisi une ville d'arrivée dans le formulaire --%>
														    <c:if test="${not empty param.arrivalCity}">
														        <c:param name="to" value="${param.arrivalCity}"/>
														    </c:if>
														</c:url>
														
														<a href="${bookUrl}" class="btn btn-primary btn-sm">
														    Réserver ce trajet
														</a>

											        </c:otherwise>
											    </c:choose>
											</c:otherwise>

                                        </c:choose>
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