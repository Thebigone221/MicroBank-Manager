<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    String flashSuccess = (String) session.getAttribute("flashSuccess");
    String flashError = (String) session.getAttribute("flashError");
    if (flashSuccess != null) { session.removeAttribute("flashSuccess"); }
    if (flashError != null) { session.removeAttribute("flashError"); }
    request.setAttribute("flashSuccess", flashSuccess);
    request.setAttribute("flashError", flashError);
    String erreurLogin = (String) request.getAttribute("erreur");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connexion — MicroBank Manager</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="login-page d-flex align-items-center justify-content-center">

<div class="container">
    <div class="card login-card mx-auto shadow p-4">
        <div class="text-center mb-4">
            <i class="bi bi-bank2 text-success fs-1"></i>
            <h1 class="h3 mt-2 fw-bold">MICROBANK</h1>
            <p class="text-muted mb-0">Gestion de l'institution de microfinance</p>
        </div>

        <c:if test="${not empty flashSuccess}">
            <div class="alert alert-success py-2">${flashSuccess}</div>
        </c:if>
        <c:if test="${not empty erreurLogin}">
            <div class="alert alert-danger py-2">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>${erreurLogin}
            </div>
        </c:if>

        <!-- Formulaire envoyé à la servlet /login -->
        <form method="post" action="${pageContext.request.contextPath}/login">
            <div class="mb-3">
                <label for="login" class="form-label">Login</label>
                <input type="text" class="form-control" id="login" name="login"
                       value="${loginSaisi}" required autofocus
                       placeholder="Votre identifiant">
            </div>
            <div class="mb-4">
                <label for="motDePasse" class="form-label">Mot de passe</label>
                <input type="password" class="form-control" id="motDePasse"
                       name="motDePasse" required placeholder="••••••••">
            </div>
            <button type="submit" class="btn btn-success w-100">
                <i class="bi bi-box-arrow-in-right me-1"></i>Se connecter
            </button>
        </form>

        <hr>
        <p class="text-center small text-muted mb-0">
            Compte de test : admin / admin123 — agent / agent123
        </p>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
