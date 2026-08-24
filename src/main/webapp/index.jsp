<%@ page contentType="text/html; charset=UTF-8" %>
<%
    // Point d'entrée : redirige vers le tableau de bord si connecté, sinon vers la connexion.
    Object user = request.getSession(false) == null ? null : request.getSession(false).getAttribute("user");
    if (user != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
    } else {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
%>
