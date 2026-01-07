<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%-- 
    Page: driver_bookings.jsp
    Rôle: afficher les demandes de réservation reçues par le CONDUCTEUR.
    Pour chaque demande:
      - on affiche le trajet, la date, le passager, le nombre de places
      - on montre le statut (PENDING / CONFIRMED / REJECTED)
      - si la demande est PENDING, on permet au conducteur de l'accepter ou de la refuser
 --%> 
   
<%
    //Titre de l'onglet, affiché via layout.jspf
    request.setAttribute("pageTitle", "Demandes de réservation - Covoiturage");
    request.setAttribute("activePage", "driverBookings"); // pour la mise en surbrillance dela page du menu 
%>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%@ include file="layout.jspf" %>

<div class="row">
    <div class="col-md-10 offset-md-1">

        <h2 class="mb-4">Demandes de réservation pour mes trajets</h2>

        <%-- 
            driverBookings est une liste de vues fabriquées par DriverBookingsServlet:
            - v.booking: la réservation
            - v.ride: le trajet associé
            - v.passenger: l'utilisateur passager
         --%>
        <c:choose>
        
            <%-- Le conducteur n'a encore aucune demande pour ses trajets --%>
            <c:when test="${empty driverBookings}">
                <div class="alert alert-info">
                    Vous n'avez encore aucune demande de réservation.
                </div>
            </c:when>

            <%-- Cas où il y a au moins une demande de réservation --%>
            <c:otherwise>
                <div class="row g-3">
                    <c:forEach var="v" items="${driverBookings}">
                        <div class="col-12">
                            <div class="card shadow-sm border-0 reservation-card">
                                <div class="card-body">

                                    <%-- Trajet complet, date et statut --%>
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <div>
                                            <h5 class="card-title mb-1">
                                                ${v.ride.departureCityDisplay}
                                                <span class="text-muted">&rarr;</span>
                                                ${v.ride.arrivalCityDisplay}
                                            </h5>
                                            <div class="text-muted small">
                                                Départ : ${v.ride.departureDateTimeFormatted}
                                            </div>
                                        </div>

                                        <div class="text-end">
                                            <%-- Badge statut --%>
                                            <c:choose>
                                                <c:when test="${v.booking.status == 'CONFIRMED'}">
                                                    <span class="badge bg-success">
                                                        ${v.booking.statusLabel}
                                                    </span>
                                                </c:when>
                                                <c:when test="${v.booking.status == 'PENDING'}">
                                                    <span class="badge bg-warning text-dark">
                                                        ${v.booking.statusLabel}
                                                    </span>
                                                </c:when>
                                                <c:when test="${v.booking.status == 'REJECTED'}">
                                                    <span class="badge bg-danger">
                                                        ${v.booking.statusLabel}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    ${v.booking.statusLabel}
                                                </c:otherwise>
                                            </c:choose>

                                            <div class="text-muted small mt-1">
                                                Demande : ${v.booking.createdAtFormatted}
                                            </div>
                                        </div>
                                    </div>

                                    <%-- Parcours réservé (sous-trajet) --%>
                                    <div class="small mb-2">
                                        <strong>Parcours réservé :</strong>
                                        <c:choose>
                                            <c:when test="${not empty v.booking.fromCity and not empty v.booking.toCity}">
                                                ${v.booking.fromCity} &rarr; ${v.booking.toCity}
                                            </c:when>
                                            <c:otherwise>
                                                ${v.ride.departureCityDisplay} &rarr; ${v.ride.arrivalCityDisplay}
                                            </c:otherwise>
                                        </c:choose>

                                        <%-- nombre d'arrêts intermédiaires sur le sous-trajet --%>
                                        <c:if test="${v.booking.toIndex > v.booking.fromIndex}">
                                            <c:set var="segmentStopsCount"
                                                   value="${v.booking.toIndex - v.booking.fromIndex - 1}" />
                                            <c:if test="${segmentStopsCount > 0}">
                                                <span class="text-muted ms-2">
                                                    · ${segmentStopsCount} arrêt(s) intermédiaire(s)
                                                </span>
                                            </c:if>
                                        </c:if>
                                    </div>

                                    <%-- Informations sur passager, places et prix --%>
                                    <div class="row mb-2">
                                        <div class="col-md-6">
                                            <div class="small">
                                                <strong>Passager :</strong>
                                                <c:if test="${not empty v.passenger}">
                                                    ${v.passenger.name} (${v.passenger.email})
                                                </c:if>
                                            </div>
                                            <div class="small">
                                                <strong>Places demandées :</strong>
                                                ${v.booking.seats}
                                            </div>
                                        </div>

                                        <div class="col-md-6 text-md-end">
                                            <c:if test="${not empty v.ride}">
                                                <%-- Prix unitaire et total pour CE booking (sous-trajet ou trajet complet) --%>
                                                <c:set var="unitPrice"
                                                       value="${v.booking.pricePerSeat > 0 
                                                               ? v.booking.pricePerSeat 
                                                               : v.ride.pricePerSeat}" />

                                                <c:set var="totalPrice"
                                                       value="${v.booking.totalPrice > 0 
                                                               ? v.booking.totalPrice 
                                                               : v.booking.seats * unitPrice}" />

                                                <div class="small">
                                                    <strong>Prix par place :</strong>
                                                    ${unitPrice} €
                                                </div>
                                                <div class="small">
                                                    <strong>Montant total :</strong>
                                                    ${totalPrice} €
                                                </div>
                                            </c:if>
                                        </div>
                                    </div>

                                    <%-- Message conducteur et actions associées --%>
                                    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mt-2 gap-2">

                                        <div class="flex-grow-1">
                                            <c:if test="${not empty v.booking.driverMessage}">
                                                <div class="small text-muted">
                                                    Votre message au passager :
                                                    <br>
                                                    <em>${v.booking.driverMessage}</em>
                                                </div>
                                            </c:if>
                                        </div>

                                        <%-- Zone actions: formulaires pour accepter ou refuser --%>
                                        <div class="text-md-end" style="min-width: 260px;">
                                            <c:choose>
                                                <c:when test="${v.booking.status == 'PENDING'}">
                                                    <%-- Formulaire accepter --%>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/driver/booking/action"
                                                          class="mb-2">
                                                        <input type="hidden" name="bookingId"
                                                               value="${v.booking.id}">
                                                        <input type="hidden" name="action"
                                                               value="confirm">
                                                        <input class="form-control form-control-sm mb-1"
                                                               type="text"
                                                               name="message"
                                                               placeholder="Message de confirmation (optionnel)">
                                                        <button type="submit"
                                                                class="btn btn-sm btn-success w-100">
                                                            Accepter la demande
                                                        </button>
                                                    </form>

                                                    <%-- Formulaire refuser --%>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/driver/booking/action">
                                                        <input type="hidden" name="bookingId"
                                                               value="${v.booking.id}">
                                                        <input type="hidden" name="action"
                                                               value="reject">
                                                        <input class="form-control form-control-sm mb-1"
                                                               type="text"
                                                               name="message"
                                                               placeholder="Message de refus (optionnel)">
                                                        <button type="submit"
                                                                class="btn btn-sm btn-outline-danger w-100">
                                                            Refuser la demande
                                                        </button>
                                                    </form>
                                                </c:when>
                                                
                                                <%-- Si ce n'est plus PENDING, on indique qu'il n'y a plus d'action possible --%>
                                                <c:otherwise>
                                                    <div class="small text-muted">
                                                        Aucune action possible sur cette demande.
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

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
