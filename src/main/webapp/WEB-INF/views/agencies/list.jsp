<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0"><i class="bi bi-building me-2"></i>Agences</h1>
    <a class="btn btn-success" href="${pageContext.request.contextPath}/agencies/create">
        <i class="bi bi-plus-circle me-1"></i>Nouvelle agence
    </a>
</div>

<div class="card">
    <table class="table table-microbank table-hover align-middle mb-0">
        <thead>
        <tr>
            <th>Code</th>
            <th>Nom</th>
            <th>Ville</th>
            <th>Comptes rattachés</th>
            <th class="text-end">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="agence" items="${agences}">
            <tr>
                <td><span class="badge bg-dark">${fn:escapeXml(agence.code)}</span></td>
                <td class="fw-semibold">${fn:escapeXml(agence.nom)}</td>
                <td>${fn:escapeXml(agence.ville)}</td>
                <td>${agence.accounts.size()}</td>
                <td class="text-end text-nowrap">
                    <a class="btn btn-sm btn-outline-secondary me-1"
                       href="${pageContext.request.contextPath}/agencies/edit?id=${agence.id}">
                        <i class="bi bi-pencil"></i> Modifier
                    </a>
                    <a class="btn btn-sm btn-outline-danger"
                       href="${pageContext.request.contextPath}/agencies/delete?id=${agence.id}"
                       onclick="return confirm('Supprimer cette agence ?');">
                        <i class="bi bi-trash"></i>
                    </a>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty agences}">
            <tr><td colspan="5" class="text-center text-muted py-4">Aucune agence enregistrée.</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%@ include file="/includes/footer.jspf" %>
