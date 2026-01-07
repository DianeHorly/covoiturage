<%@ page language="java" contentType="text/html;charset=UTF-8"
    pageEncoding="UTF-8"%>

<%-- Page: rides_new.jsp --%>

<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>    

<%
    request.setAttribute("pageTitle", "Nouveau trajet - Covoiturage");
    request.setAttribute("activePage", "rides_new");
%>

<%@ include file="layout.jspf" %>

<div class="row">
    <div class="col-md-6 offset-md-3">

        <div class="card shadow-sm">
            <div class="card-body">
                <h2 class="mb-4">Proposer un trajet</h2>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">
                        ${error}
                    </div>
                </c:if>

                <form method="post"
                      action="${pageContext.request.contextPath}/rides/new"
                      id="rideForm">

                    <div class="mb-3">
                        <label class="form-label">Ville de départ</label>
                        <input class="form-control"
                               type="text"
                               id="departureCity"
                               name="departureCity"
                               required
                               value="${departureCity}">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Ville d'arrivée</label>
                        <input class="form-control"
                               type="text"
                               id="arrivalCity"
                               name="arrivalCity"
                               required
                               value="${arrivalCity}">
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Date de départ</label>
                            <input class="form-control"
                                   type="date"
                                   name="date"
                                   required
                                   value="${date}">
                        </div>

                        <div class="col-md-6 mb-3">
                            <label class="form-label">Heure de départ</label>
                            <input class="form-control"
                                   type="time"
                                   name="time"
                                   required
                                   value="${time}">
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Nombre de places</label>
                            <input class="form-control"
                                   type="number"
                                   min="1"
                                   name="totalSeats"
                                   required
                                   value="${totalSeats}">
                        </div>

                        <div class="col-md-6 mb-3">
                            <label class="form-label">Prix par place (€) - trajet complet</label>
                            <input class="form-control"
                                   type="number"
                                   min="0"
                                   id="pricePerSeat"
                                   name="pricePerSeat"
                                   required
                                   value="${pricePerSeat}">
                        </div>
                    </div>
					    	
                    <div class="mb-3">
                        <label class="form-label">Commentaire (optionnel)</label>
                        <textarea class="form-control"
                                  name="description"
                                  rows="3">${description}</textarea>
                    </div>

                    <div class="mb-3">
					    <label class="form-label">
					        Étapes / arrêts du trajet
					        <span class="text-muted small d-block">
					            Une ville par ligne, dans l'ordre du trajet (entre départ et arrivée).
					        </span>
					    </label>
					    <textarea class="form-control"
					              id="stops"
					              name="stops"
					              rows="3"
					              placeholder="Monaco&#10;Roquebrune&#10;Menton">${stops}</textarea>
					</div>

                    <%-- Bloc prix par segment --%>
                    <div class="mb-3">
                        <label class="form-label">
                            Prix par segment (tronçon)
                            <span class="text-muted small d-block">
                                Pour chaque tronçon entre deux villes successives, vous pouvez définir un prix par place.
                                Si vous laissez vide, le prix du trajet complet sera utilisé.
                            </span>
                        </label>

                        <div id="segmentsContainer" class="border rounded p-2 bg-light">
                            <p class="text-muted small mb-0">
                                Saisissez d'abord la ville de départ, les arrêts et la ville d'arrivée
                                pour voir les segments.
                            </p>
                        </div>
                    </div>

                    <%-- Champ caché qui recevra la liste CSV des prix des segments --%>
                    <input type="hidden"
                           id="segmentPricesCsv"
                           name="segmentPricesCsv"
                           value="${segmentPricesCsv}"/>

                    <button type="submit" class="btn btn-success w-100">
                        Publier le trajet
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>

<%-- inclusion du fichier JS dans la page --%>
<script src="${pageContext.request.contextPath}/js/new_ride.js"></script>

<%@ include file="footer.jspf" %>
