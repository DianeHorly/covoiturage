<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- 
    Page: ticket.jsp
    Rôle: afficher le détail d'une réservation (ticket) pour un passager.
    - Affiche le statut (confirmée, en attente et refusée)
    - Affiche les infos du trajet et réservation
    - Si la réservation est CONFIRMÉE alors on affiche un QR Code et le code du ticket
 --%>

<%
    // Titre utilisé par layout.jspf pour <title> dans l'onglet du navigateur
    request.setAttribute("pageTitle", "Ticket de réservation - Covoiturage");
%>

<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%@ include file="layout.jspf" %>

<div class="row">
    <div class="col-md-8 offset-md-2">

        <%-- Si la servlet a posé un message d'erreur --%>
        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert">
                ${error}
            </div>
        </c:if>

        <%-- Si aucun "booking" n'a été trouvé ou fourni à la JSP --%>
        <c:if test="${empty booking}">
            <div class="alert alert-warning">
                Ticket introuvable.
            </div>
        </c:if>

        <%-- Cas où une réservation a bien été trouvée --%>
        <c:if test="${not empty booking}">
            <div class="card shadow-sm mb-4">
                <div class="card-body">

                    <h2 class="card-title mb-3">Ticket de réservation</h2>

                    <%-- Rappel du statut sous forme de texte lisible --%>
                    <p class="mb-1">
                        <strong>Statut: </strong> ${booking.statusLabel}
                    </p>

                    <%-- Message personnalisé saisi par le conducteur (optionnel) --%>
                    <c:if test="${not empty booking.driverMessage}">
                        <div class="alert alert-info mt-2">
                            Message du conducteur: ${booking.driverMessage}
                        </div>
                    </c:if>

                    <%-- Message selon statut + QR Code éventuel --%>
                    <c:choose>
                        <c:when test="${booking.status == 'CONFIRMED'}">
                            <div class="alert alert-success mt-3">
                                Votre réservation est confirmée par le conducteur.
                                <br>
                                Merci d'être à l'heure au point de départ.
                            </div>
                            <div class="text-center mt-4">
                                <p>Présentez ce QR Code au conducteur: </p>
                                <img
                                        src="${pageContext.request.contextPath}/ticket/qrcode?bookingId=${booking.id}"
                                        alt="QR Code du ticket"
                                        class="img-fluid"
                                        style="max-width: 220px;">
                            </div>
                        </c:when>

                        <c:when test="${booking.status == 'PENDING'}">
                            <div class="alert alert-warning mt-3">
                                Votre réservation est en attente de confirmation du conducteur.
                                <br>
                                Vous recevrez un message dès qu'elle sera acceptée ou refusée.
                            </div>
                        </c:when>

                        <c:when test="${booking.status == 'REJECTED'}">
                            <div class="alert alert-danger mt-3">
                                Votre réservation a été refusée par le conducteur.
                                <br>
                                Vous pouvez rechercher un autre trajet depuis la liste des trajets.
                            </div>
                        </c:when>
                    </c:choose>

                    <hr class="my-4">

                    <%-- Parcours effectivement réservé (sous-trajet) --%>
                    <p class="mb-1">
                        <strong>Parcours réservé: </strong>
                        <c:choose>
                            <c:when test="${not empty booking.fromCity and not empty booking.toCity}">
                                ${booking.fromCity} &rarr; ${booking.toCity}
                            </c:when>
                            <c:when test="${not empty ride}">
                                ${ride.departureCityDisplay} &rarr; ${ride.arrivalCityDisplay}
                            </c:when>
                            <c:otherwise>
                                (information trajet indisponible)
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <%-- Nombre d'arrêts intermédiaires sur le sous-trajet --%>
                    <c:if test="${booking.toIndex > booking.fromIndex}">
                        <c:set var="segmentStopsCount"
                               value="${booking.toIndex - booking.fromIndex - 1}" />
                        <c:if test="${segmentStopsCount > 0}">
                            <p class="mb-1 text-muted small">
                                Inclut ${segmentStopsCount} arrêt(s) intermédiaire(s)
                            </p>
                        </c:if>
                    </c:if>

                    <%-- Infos sur le trajet complet (si encore présent en base) --%>
                    <c:if test="${not empty ride}">
                        <p class="mb-1 mt-3">
                            <strong>Trajet complet: </strong>
                            ${ride.departureCityDisplay} &rarr; ${ride.arrivalCityDisplay}
                        </p>
                        <p class="mb-1">
                            <strong>Date/heure: </strong>
                            ${ride.departureDateTimeFormatted}
                        </p>
                        <p class="mb-3">
                            <strong>Prix par place (trajet complet): </strong>
                            ${ride.pricePerSeat} €
                        </p>
                    </c:if>

                    <%-- Calcul des prix pour CE ticket --%>
                    <c:set var="unitPrice" value="0" />
                    <c:choose>
                        <c:when test="${booking.pricePerSeat > 0}">
                            <c:set var="unitPrice" value="${booking.pricePerSeat}" />
                        </c:when>
                        <c:when test="${not empty ride}">
                            <c:set var="unitPrice" value="${ride.pricePerSeat}" />
                        </c:when>
                    </c:choose>

                    <c:set var="totalPrice" value="0" />
                    <c:choose>
                        <c:when test="${booking.totalPrice > 0}">
                            <c:set var="totalPrice" value="${booking.totalPrice}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="totalPrice" value="${booking.seats * unitPrice}" />
                        </c:otherwise>
                    </c:choose>

                    <%-- Infos liées à la réservation du passager --%>
                    <p class="mb-1">
                        <strong>Places réservées: </strong>
                        ${booking.seats}
                    </p>

                    <p class="mb-3">
                        <strong>Montant total: </strong>
                        <c:if test="${totalPrice > 0}">
                            ${totalPrice} €
                            <span class="text-muted small">
                                (${unitPrice} € / place)
                            </span>
                        </c:if>
                        <c:if test="${totalPrice <= 0}">
                            N/A
                        </c:if>
                    </p>

                    <%-- On n'affiche le code du ticket que s'il existe et que la réservation est confirmée --%>
                    <c:if test="${booking.status == 'CONFIRMED' && not empty booking.ticketCode}">
                        <p class="mb-3">
                            <strong>Code de ticket: </strong>
                            ${booking.ticketCode}
                        </p>
                    </c:if>

                    <div class="mt-4">
                        <a href="${pageContext.request.contextPath}/my_bookings"
                           class="btn btn-outline-secondary">
                            Retour à mes réservations
                        </a>
                    </div>
                </div>
            </div>
        </c:if>

    </div>
</div>

<%@ include file="footer.jspf" %>
