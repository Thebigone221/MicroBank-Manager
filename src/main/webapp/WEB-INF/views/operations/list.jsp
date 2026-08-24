<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0">
        <i class="bi bi-arrow-left-right me-2"></i>
            ${not empty compte ? "Historique du compte ".concat(compte.numeroCompte) : "Historique des opérations"}
    </h1>
    <c:if test="${not empty compte}">
        <a href="${pageContext.request.contextPath}/accounts/details?id=${compte.id}"
           class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left me-1"></i>Retour au compte
        </a>
    </c:if>
</div>

<div class="card mb-3">
    <div class="card-body">
        <form method="get" action="${pageContext.request.contextPath}/operations" class="row g-2 align-items-end">
            <input type="hidden" name="accountId" value="${param.accountId}">
            <div class="col-md-2">
                <label class="form-label small text-muted">Type</label>
                <select name="type" class="form-select form-select-sm">
                    <option value="">Tous</option>
                    <option value="DEPOT" ${filtreType == 'DEPOT' ? 'selected' : ''}>Dépôt</option>
                    <option value="RETRAIT" ${filtreType == 'RETRAIT' ? 'selected' : ''}>Retrait</option>
                    <option value="VIREMENT" ${filtreType == 'VIREMENT' ? 'selected' : ''}>Virement</option>
                </select>
            </div>
            <div class="col-md-2">
                <label class="form-label small text-muted">Du</label>
                <input type="date" name="du" class="form-control form-control-sm" value="${filtreDu}">
            </div>
            <div class="col-md-2">
                <label class="form-label small text-muted">Au</label>
                <input type="date" name="au" class="form-control form-control-sm" value="${filtreAu}">
            </div>
            <div class="col-md-2">
                <label class="form-label small text-muted">Montant min</label>
                <input type="number" min="0" name="min" class="form-control form-control-sm" value="${filtreMin}"
                       placeholder="0">
            </div>
            <div class="col-md-2">
                <label class="form-label small text-muted">Montant max</label>
                <input type="number" min="0" name="max" class="form-control form-control-sm" value="${filtreMax}"
                       placeholder="∞">
            </div>
            <div class="col-md-2 d-grid">
                <button type="submit" class="btn btn-primary btn-sm">
                    <i class="bi bi-search me-1"></i>Rechercher
                </button>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <table class="table table-microbank table-hover align-middle mb-0">
        <thead>
        <tr>
            <th>Date</th>
            <th>Référence</th>
            <th>Type</th>
            <th>Description</th>
            <th>Compte</th>
            <th class="text-end">Montant</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="op" items="${resultat.items}">
            <tr>
                <td class="mono text-nowrap pe-0">${f:dateHeureFr(op.dateOperation)}</td>
                <td><span class="ref-operation">${op.reference}</span></td>
                <td><span class="badge ${op.type == 'DEPOT' ? 'badge-op-depot' : op.type == 'RETRAIT' ? 'badge-op-retrait' : 'badge-op-virement'}">${op.type}</span></td>
                <td class="small">${fn:escapeXml(op.description)}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/accounts/details?id=${op.compte.id}"
                       class="fw-semibold">${op.compte.numeroCompte}</a>
                    <span class="small text-muted">(${fn:escapeXml(op.compte.client.prenom)} ${fn:escapeXml(op.compte.client.nom)})</span>
                </td>
                <td class="mono text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                        ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)} FCFA
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty resultat.items}">
            <tr><td colspan="6" class="text-center text-muted py-4">Aucune opération ne correspond aux critères.</td></tr>
        </c:if>
        </tbody>
    </table>

    <div class="card-body">
        <c:if test="${resultat.totalPages > 1}">
            <nav aria-label="Pagination des opérations"
                 class="d-flex justify-content-between align-items-center mt-2">
                <span class="small text-muted">${resultat.total} opération(s) - page ${resultat.page + 1} / ${resultat.totalPages}</span>
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item ${resultat.hasPrevious() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?accountId=${param.accountId}&type=${filtreType}&du=${filtreDu}&au=${filtreAu}&min=${filtreMin}&max=${filtreMax}&numeroCompte=${filtreNumeroCompte}&page=${resultat.page - 1}&size=${resultat.size}">&laquo; Précédent</a>
                    </li>
                    <c:forEach begin="0" end="${resultat.totalPages - 1}" var="numeroPage">
                        <li class="page-item ${numeroPage == resultat.page ? 'active' : ''}">
                            <a class="page-link"
                               href="?accountId=${param.accountId}&type=${filtreType}&du=${filtreDu}&au=${filtreAu}&min=${filtreMin}&max=${filtreMax}&numeroCompte=${filtreNumeroCompte}&page=${numeroPage}&size=${resultat.size}">${numeroPage + 1}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${resultat.hasNext() ? '' : 'disabled'}">
                        <a class="page-link"
                           href="?accountId=${param.accountId}&type=${filtreType}&du=${filtreDu}&au=${filtreAu}&min=${filtreMin}&max=${filtreMax}&numeroCompte=${filtreNumeroCompte}&page=${resultat.page + 1}&size=${resultat.size}">Suivant &raquo;</a>
                    </li>
                </ul>
            </nav>
        </c:if>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
