<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="fr">
  <head>
    <meta charset="UTF-8" />
    <title>Covoiturage</title>
    <!-- Redirection immédiate côté navigateur au cas où -->
    <meta http-equiv="refresh" content="0; url=${pageContext.request.contextPath}/home" />
  </head>
  <body>
    <p>Redirection… Si rien ne se passe,
       <a href="${pageContext.request.contextPath}/home">clique ici</a>.</p>
  </body>
</html>
