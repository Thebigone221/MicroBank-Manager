<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0"><i class="bi bi-wallet2 me-2"></i>Comptes</h1>
    <a class="btn btn-success" href="${pageContext.request.contextPath}/accounts/create">
        <i class="bi bi-plus-circle me-1"></i>Ouvrir un compte
    </a>
</div>

<div class="card">
    <div class="card-body">

        <form method="get" action="${pageContext.request.contextPath}/accounts" class="row g-2 mb-3">
            <div class="col-md-4">
                <input type="text" class="form-control" name="search" value="${recherche}"
                       placeholder="N° de compte ou nom du client...">
            </div>
            <div class="col-md-2">
                <select name="type" class="form-select">
                    <option value="">Tous types</option>
                    <option value="COURANT" ${typeFiltre == 'COURANT' ? 'selected' : ''}>Courant</option>
                    <option value="EPARGNE" ${typeFiltre == 'EPARGNE' ? 'selected' : ''}>Épargne</option>
                </select>
            </div>
            <div class="col-md-2">
                <select name="statut" class="form-select">
                    <option value="">Tous statuts</option>
                    <option value="ACTIF" ${statutFiltre == 'ACTIF' ? 'selected' : ''}>Actif</option>
                    <option value="BLOQUE" ${statutFiltre == 'BLOQUE' ? 'selected' : ''}>Bloqué</option>
                    <option value="CLOTURE" ${statutFiltre == 'CLOTURE' ? 'selected' : ''}>Clôturé</option>
                </select>
            </div>
            <div class="col-md-3">
                <c:if test="${empty clientIdFiltre}">
                    <select name="agenceId" class="form-select">
                        <option value="">Toutes agences</option>
                        <c:forEach var="agence" items="${agences}">
                            <option value="${agence.id}" ${agenceFiltre eq String.valueOf(agence.id) ? 'selected' : ''}>
                                    ${agence.nom}
                            </option>
                        </c:forEach>
                    </select>
                </c:if>
            </div>
            <div class="col-md-auto">
                <button type="submit" class="btn btn-primary"><i class="bi bi-search me-1"></i>Filtrer</button>
                <a href="${pageContext.request.contextPath}/accounts" class="btn btn-outline-secondary">Réinitialiser</a>
            </div>
        </form>

        <table class="table table-microbank table-hover align-middle">
            <thead>
            <tr>
                <th>Numéro</th>
                <th>Client</th>
                <th>Type</th>
                <th>Solde</th>
                <th>Agence</th>
                <th>Ouverture</th>
                <th>Statut</th>
                <th class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="compte" items="${resultat.items}">
                <tr>
                    <td class="fw-semibold">${compte.numeroCompte}</td>
                    <td>${compte.client.nomComplet}</td>
                    <td><span class="badge ${compte.type == 'COURANT' ? 'badge-courant' : 'badge-epargne'}">${compte.type}</span></td>
                    <td class="fw-semibold">${f:fcfa(compte.solde)}</td>
                    <td>${compte.agency.nom}</td>
                    <td>${f:dateFr(compte.dateOuverture)}</td>
                    <td><span class="badge bg-${compte.statut == 'ACTIF' ? 'success' : (compte.statut == 'BLOQUE' ? 'warning text-dark' : 'secondary')}">${compte.statut}</span></td>
                    <td class="text-end">
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/accounts/details?id=${compte.id}">
                            Détails
                        </a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty resultat.items}">
                <tr><td colspan="8" class="text-center text-muted py-4">Aucun compte trouvé.</td></tr>
            </c:if>
            </tbody>
        </table>

        <c:if test="${resultat.totalPages > 1}">
            <nav aria-label="Pagination des comptes"
                 class="d-flex justify-content-between align-items-center mt-3">
                <span class="small text-muted">${resultat.total} compte(s) - page ${resultat.page + 1} / ${resultat.totalPages}</span>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${resultat.hasPrevious() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&type=${typeFiltre}&statut=${statutFiltre}&agenceId=${agenceFiltre}&clientId=${clientIdFiltre}&page=${resultat.page - 1}&size=${resultat.size}">&laquo;</a>
                    </li>
                    <c:forEach begin="0" end="${resultat.totalPages - 1}" var="numeroPage">
                        <li class="page-item ${numeroPage == resultat.page ? 'active' : ''}">
                            <a class="page-link"
                               href="?search=${recherche}&type=${typeFiltre}&statut=${statutFiltre}&agenceId=${agenceFiltre}&clientId=${clientIdFiltre}&page=${numeroPage}&size=${resultat.size}">${numeroPage + 1}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${resultat.hasNext() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?search=${recherche}&type=${typeFiltre}&statut=${statutFiltre}&agenceId=${agenceFiltre}&clientId=${clientIdFiltre}&page=${resultat.page + 1}&size=${resultat.size}">&raquo;</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
