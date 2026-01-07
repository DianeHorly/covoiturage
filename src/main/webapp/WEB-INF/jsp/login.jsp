<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c"   uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions" %>

<%
    request.setAttribute("pageTitle", "Connexion - Covoiturage");
	request.setAttribute("activePage", "login");
%>

<%@ include file="layout.jspf" %>
 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title> Page de Connexion</title>
</head>
<body>
	<div class="row">
    <div class="col-md-4 offset-md-4">

        <div class="card shadow-sm">
            <div class="card-body">
                <h2 class="mb-4">Connexion</h2>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">
                        ${error}
                    </div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input class="form-control" type="email" name="email" required>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Mot de passe</label>
                        <input class="form-control" type="password" name="password" required>
                    </div>

                    <button type="submit" class="btn btn-primary w-100">
                        Se connecter
                    </button>
                </form>

                <hr>

                <p class="text-center mb-0">
                    Pas encore de compte ?
                    <a href="${pageContext.request.contextPath}/register">Créer un compte</a>
                </p>
            </div>
        </div>

    </div>
</div>
	
</body>
</html>


<%@ include file="footer.jspf" %>