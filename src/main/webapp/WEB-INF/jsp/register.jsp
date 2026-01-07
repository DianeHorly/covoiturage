<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%
    request.setAttribute("pageTitle", "Inscription - Covoiturage");
    request.setAttribute("activePage", "register");
%>

<%@ include file="layout.jspf" %>

<div class="row">
    <div class="col-md-4 offset-md-4">

        <div class="card shadow-sm">
            <div class="card-body">
                <h2 class="mb-4"> Créer un compte</h2>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">
                        ${error}
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/register">

                    <div class="mb-3">
                        <label class="form-label">Nom complet</label>
                        <input class="form-control" type="text" name="name" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input class="form-control" type="email" name="email" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Mot de passe</label>
                        <input class="form-control" type="password" name="password" required>
                    </div>

                    <button type="submit" class="btn btn-success w-100">
                        Créer mon compte
                    </button>
                </form>

                <hr>

                <p class="text-center mb-0">
                    Déjà un compte ?
                    <a href="${pageContext.request.contextPath}/login">Se connecter</a>
                </p>
            </div>
        </div>

    </div>
</div>

<%@ include file="footer.jspf" %>