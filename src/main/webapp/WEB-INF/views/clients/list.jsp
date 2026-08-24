<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0"><i class="bi bi-people me-2"></i>Clients</h1>
    <a class="btn btn-success" href="${pageContext.request.contextPath}/clients/create">
        <i class="bi bi-person-plus me-1"></i>Nouveau client
    </a>
</div>

<div class="card">
    <div class="card-body">

        <form method="get" action="${pageContext.request.contextPath}/clients" class="row g-2 mb-3">
            <div class="col-md-5">
                <input type="text" class="form-control" name="search"
                       value="${recherche}" placeholder="Nom, prénom, téléphone ou n° de pièce...">
            </div>
            <div class="col-md-3">
                <select name="statut" class="form-select">
                    <option value="">Tous les statuts</option>
                    <option value="ACTIF" ${statutFiltre == 'ACTIF' ? 'selected' : ''}>Actif</option>
                    <option value="INACTIF" ${statutFiltre == 'INACTIF' ? 'selected' : ''}>Inactif</option>
                </select>
            </div>
            <div class="col-md-auto">
                <button type="submit" class="btn btn-primary"><i class="bi bi-search me-1"></i>Rechercher</button>
                <a href="${pageContext.request.contextPath}/clients" class="btn btn-outline-secondary">Réinitialiser</a>
            </div>
        </form>

        <table class="table table-microbank table-hover align-middle">
            <thead>
            <tr>
                <th>N°</th>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Téléphone</th>
                <th>Email</th>
                <th>N° pièce</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="client" items="${resultat.items}" varStatus="boucle">
                <tr>
                    <td>C${client.id < 100 ? (client.id < 10 ? '00' : '0') : ''}${client.id}</td>
                    <td class="fw-semibold">${client.nom}</td>
                    <td>${client.prenom}</td>
                    <td>${client.telephone}</td>
                    <td>${client.email}</td>
                    <td>${client.numeroPiece}</td>
                    <td>
                        <span class="badge ${client.statut == 'ACTIF' ? 'bg-success' : 'bg-danger'}">${client.statut}</span>
                    </td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary me-1"
                           href="${pageContext.request.contextPath}/clients/details?id=${client.id}"
                           title="Voir la fiche">
                            <i class="bi bi-eye"></i>
                        </a>
                        <a class="btn btn-sm btn-outline-secondary"
                           href="${pageContext.request.contextPath}/clients/edit?id=${client.id}"
                           title="Modifier">
                            <i class="bi bi-pencil"></i>
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty resultat.items}">
                <tr><td colspan="8" class="text-center text-muted py-4">Aucun client trouvé.</td></tr>
            </c:if>
            </tbody>
        </table>

        <c:if test="${resultat.totalPages > 1}">
            <nav aria-label="Pagination des clients" class="d-flex justify-content-between align-items-center mt-3">
                <span class="small text-muted">${resultat.total} client(s) - page ${resultat.page + 1} / ${resultat.totalPages}</span>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${resultat.hasPrevious() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&statut=${statutFiltre}&page=${resultat.page - 1}&size=${resultat.size}">&laquo; Précédent</a>
                    </li>
                    <c:forEach begin="0" end="${resultat.totalPages - 1}" var="numeroPage">
                        <li class="page-item ${numeroPage == resultat.page ? 'active' : ''}">
                            <a class="page-link"
                               href="?search=${recherche}&statut=${statutFiltre}&page=${numeroPage}&size=${resultat.size}">${numeroPage + 1}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${resultat.hasNext() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&statut=${statutFiltre}&page=${resultat.page + 1}&size=${resultat.size}">Suivant &raquo;</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
