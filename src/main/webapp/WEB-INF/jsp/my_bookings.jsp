<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8"%>  
   
<%-- 
    Page: my_bookings.jsp
    Rôle: afficher toutes les réservations du PASSAGER connecté.
    - Uniquement les réservations de l'utilisateur courant (via MyBookingsServlet)
    - Pour chaque réservation : trajet, date, nombre de places, prix total, statut, lien ticket.
 --%> 

<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%
    request.setAttribute("pageTitle", "Mes réservations - Covoiturage");
	request.setAttribute("activePage", "myBookings"); // rendre active (surblimage) la page
%>

<%@ include file="layout.jspf" %>

<div class="row">
    <div class="col-md-10 offset-md-1">

        <h2 class="mb-4">Mes réservations</h2>

        <%-- Message d'erreur général --%>
        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                ${error}
            </div>
        </c:if>
        
        <%-- Cas où l'utilisateur n'a encore aucune réservation --%>
        <c:choose>
            <c:when test="${empty bookingsWithRide}">
                <div class="alert alert-info">
                    Vous n'avez encore aucune réservation.
                </div>
            </c:when>

            <%-- Cas où il y a au moins une réservation à afficher --%>
            <c:otherwise>
                <div class="card shadow-sm">
                    <div class="card-body">
                        <table class="table table-striped align-middle">
                            <thead>
                            <tr>
                                <th>Trajet</th>
                                <th>Date/heure</th>
                                <th>Places réservées</th>
                                <th>Prix total</th>
                                <th>Réservé le</th>
                                <th>Statut </th>
                                <th>Ticket</th>
                            </tr>
                            </thead>
                            <tbody>
                            
                            <%-- On parcourt chaque réservation de l'utilisateur --%>
                            <c:forEach var="item" items="${bookingsWithRide}">
                                <tr>
                                    <%-- Colonne Trajet et Parcours réservé --%>
                                    <td>
                                        <c:choose>           
                                            <%-- Si le trajet existe encore en base --%>
                                            <c:when test="${not empty item.ride}">
                                                ${item.ride.departureCityDisplay} &rarr; ${item.ride.arrivalCityDisplay}
                                            </c:when>
                                            <%-- Si le trajet a été supprimé côté conducteur --%>
                                            <c:otherwise>
                                                (Trajet supprimé)
                                            </c:otherwise>
                                        </c:choose>

                                        <div class="small text-muted mt-1">
                                            <strong>Parcours réservé :</strong>
                                            <c:choose>
                                                <c:when test="${not empty item.booking.fromCity and not empty item.booking.toCity}">
                                                    ${item.booking.fromCity} &rarr; ${item.booking.toCity}
                                                </c:when>
                                                <c:when test="${not empty item.ride}">
                                                    ${item.ride.departureCityDisplay} &rarr; ${item.ride.arrivalCityDisplay}
                                                </c:when>
                                                <c:otherwise>
                                                    (information trajet indisponible)
                                                </c:otherwise>
                                            </c:choose>

                                            <%-- nombre d'arrêts intermédiaires sur le sous-trajet --%>
                                            <c:if test="${item.booking.toIndex > item.booking.fromIndex}">
                                                <c:set var="segmentStopsCount"
                                                       value="${item.booking.toIndex - item.booking.fromIndex - 1}" />
                                                <c:if test="${segmentStopsCount > 0}">
                                                    <span class="ms-1">
                                                        · ${segmentStopsCount} arrêt(s) intermédiaire(s)
                                                    </span>
                                                </c:if>
                                            </c:if>
                                        </div>
                                    </td>
                                    
                                    <%-- Date/heure du trajet  --%>
                                    <td>
                                        <c:if test="${not empty item.ride}">
                                            ${item.ride.departureDateTimeFormatted}
                                        </c:if>
                                    </td>
                                    
                                    <%-- Nombre de places réservées --%>
                                    <td>${item.booking.seats}</td>

                                    <%-- Prix total pour CE booking (sous-trajet ou complet) --%>
                                    <td>
                                        <%-- Calcul du prix unitaire --%>
                                        <c:choose>
                                            <c:when test="${not empty item.ride}">
                                                <c:set var="unitPrice"
                                                       value="${item.booking.pricePerSeat > 0 
                                                               ? item.booking.pricePerSeat 
                                                               : item.ride.pricePerSeat}" />
                                            </c:when>
                                            <c:otherwise>
                                                <%-- Trajet supprimé: on prend ce qui est dans la réservation si disponible --%>
                                                <c:set var="unitPrice"
                                                       value="${item.booking.pricePerSeat}" />
                                            </c:otherwise>
                                        </c:choose>

                                        <%-- Calcul du total --%>
                                        <c:choose>
                                            <c:when test="${item.booking.totalPrice > 0}">
                                                <c:set var="totalPrice" value="${item.booking.totalPrice}" />
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="totalPrice" value="${item.booking.seats * unitPrice}" />
                                            </c:otherwise>
                                        </c:choose>

                                        <c:if test="${unitPrice > 0}">
                                            ${totalPrice} €
                                            <div class="small text-muted">
                                                (${unitPrice} € / place)
                                            </div>
                                        </c:if>
                                        <c:if test="${unitPrice <= 0}">
                                            N/A
                                        </c:if>
                                    </td>

                                    <%-- Date de réservation --%>
                                    <td>${item.booking.createdAtFormatted}</td>

                                    <%-- Statut avec badge --%>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.booking.status == 'CONFIRMED'}">
                                                <span class="badge bg-success">${item.booking.statusLabel}</span>
                                            </c:when>
                                            <c:when test="${item.booking.status == 'PENDING'}">
                                                <span class="badge bg-warning text-dark">${item.booking.statusLabel}</span>
                                            </c:when>
                                            <c:when test="${item.booking.status == 'REJECTED'}">
                                                <span class="badge bg-danger">${item.booking.statusLabel}</span>
                                            </c:when>
                                            <c:otherwise>
                                                ${item.booking.statusLabel}
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
									
                                    <%-- Lien vers le ticket (QR Code, détails) si la réservation est confirmée --%>
                                    <td>
                                        <c:if test="${item.booking.status == 'CONFIRMED'}">
                                            <a href="${pageContext.request.contextPath}/ticket?bookingId=${item.booking.id}"
                                               class="btn btn-sm btn-outline-primary">
                                                Voir le ticket
                                            </a>
                                        </c:if>
                                    </td>
							    </tr>
							</c:forEach>
							</tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<%@ include file="footer.jspf" %>
