<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0"><i class="bi bi-person-gear me-2"></i>Utilisateurs</h1>
    <a class="btn btn-success" href="${pageContext.request.contextPath}/users/create">
        <i class="bi bi-person-plus me-1"></i>Nouvel utilisateur
    </a>
</div>

<div class="card">
    <div class="card-body">

        <form method="get" action="${pageContext.request.contextPath}/users" class="row g-2 mb-3">
            <div class="col-md-5">
                <input type="text" class="form-control" name="search" value="${recherche}"
                       placeholder="Nom, prénom ou login...">
            </div>
            <div class="col-md-3">
                <select name="statut" class="form-select">
                    <option value="">Tous les statuts</option>
                    <option value="ACTIF" ${statutFiltre == 'ACTIF' ? 'selected' : ''}>Actif</option>
                    <option value="INACTIF" ${statutFiltre == 'INACTIF' ? 'selected' : ''}>Désactivé</option>
                </select>
            </div>
            <div class="col-md-auto">
                <button type="submit" class="btn btn-primary"><i class="bi bi-search me-1"></i>Rechercher</button>
                <a href="${pageContext.request.contextPath}/users" class="btn btn-outline-secondary">Réinitialiser</a>
            </div>
        </form>

        <!-- Les mots de passe ne sont jamais affichés dans les JSP (contrainte du sujet). -->
        <table class="table table-microbank table-hover align-middle">
            <thead>
            <tr>
                <th>Nom complet</th>
                <th>Login</th>
                <th>Rôle</th>
                <th>Statut</th>
                <th>Créé le</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="utilisateur" items="${resultat.items}">
                <tr class="${utilisateur.statut == 'INACTIF' ? 'table-light text-muted' : ''}">
                    <td>${utilisateur.nomComplet}</td>
                    <td><strong>${utilisateur.login}</strong></td>
                    <td>
                        <span class="badge ${utilisateur.role == 'ADMIN' ? 'bg-dark' : 'bg-secondary'}">${utilisateur.role}</span>
                    </td>
                    <td>
                        <span class="badge ${utilisateur.statut == 'ACTIF' ? 'bg-success' : 'bg-danger'}">${utilisateur.statut}</span>
                    </td>
                    <td>${f:dateFr(utilisateur.dateCreation.toLocalDate())}</td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-secondary me-1"
                           href="${pageContext.request.contextPath}/users/edit?id=${utilisateur.id}"
                           title="Modifier">
                            <i class="bi bi-pencil"></i>
                        </a>
                        <a class="btn btn-sm ${utilisateur.statut == 'ACTIF' ? 'btn-outline-danger' : 'btn-outline-success'}"
                           href="${pageContext.request.contextPath}/users/toggle?id=${utilisateur.id}"
                           title="${utilisateur.statut == 'ACTIF' ? 'Désactiver' : 'Activer'}">
                            <i class="bi bi-${utilisateur.statut == 'ACTIF' ? 'lock' : 'unlock'}"></i>
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty resultat.items}">
                <tr><td colspan="6" class="text-center text-muted py-4">Aucun utilisateur trouvé.</td></tr>
            </c:if>
            </tbody>
        </table>

        <c:if test="${resultat.totalPages > 1}">
            <nav aria-label="Pagination des utilisateurs"
                 class="d-flex justify-content-between align-items-center mt-3">
                <span class="small text-muted">${resultat.total} utilisateur(s) — page ${resultat.page + 1} / ${resultat.totalPages}</span>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${resultat.hasPrevious() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&statut=${statutFiltre}&page=${resultat.page - 1}&size=${resultat.size}">&laquo;</a>
                    </li>
                    <c:forEach begin="0" end="${resultat.totalPages - 1}" var="numeroPage">
                        <li class="page-item ${numeroPage == resultat.page ? 'active' : ''}">
                            <a class="page-link"
                               href="?search=${recherche}&statut=${statutFiltre}&page=${numeroPage}&size=${resultat.size}">${numeroPage + 1}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${resultat.hasNext() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&statut=${statutFiltre}&page=${resultat.page + 1}&size=${resultat.size}">&raquo;</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
