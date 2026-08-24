<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Erreur - MicroBank</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
</head>
<body class="bg-light d-flex align-items-center justify-content-center" style="min-height:100vh">
<div class="text-center">
    <h1 class="display-1 fw-bold text-danger">500</h1>
    <p class="lead">Une erreur interne est survenue.</p>
    <p class="text-muted small">Vérifiez que la base de données est démarrée, puis réessayez.</p>
    <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-success">Retour au tableau de bord</a>
</div>
</body>
</html>
