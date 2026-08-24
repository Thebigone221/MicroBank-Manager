<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Page introuvable - MicroBank</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
</head>
<body class="bg-light d-flex align-items-center justify-content-center" style="min-height:100vh">
<div class="text-center">
    <h1 class="display-1 fw-bold text-warning">404</h1>
    <p class="lead">Cette page n'existe pas.</p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-success">Retour au tableau de bord</a>
</div>
</body>
</html>
