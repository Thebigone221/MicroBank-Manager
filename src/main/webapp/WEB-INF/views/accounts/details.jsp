<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
    <h1 class="h4 mb-0">
        <i class="bi bi-wallet2 me-2"></i>Compte ${compte.numeroCompte}
        <span class="badge bg-${compte.statut == 'ACTIF' ? 'success' : (compte.statut == 'BLOQUE' ? 'warning text-dark' : 'secondary')} ms-2">${compte.statut}</span>
    </h1>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-danger"
           href="${pageContext.request.contextPath}/accounts/statut?id=${compte.id}&statut=BLOQUE">
            <i class="bi bi-lock me-1"></i>Bloquer
        </a>
        <a class="btn btn-outline-secondary"
           href="${pageContext.request.contextPath}/accounts/statut?id=${compte.id}&statut=CLOTURE"
           onclick="return confirm('Clôturer ce compte ? Cette action est définitive.');">
            Clôturer
        </a>
        <c:if test="${compte.statut != 'ACTIF'}">
            <a class="btn btn-outline-success"
               href="${pageContext.request.contextPath}/accounts/statut?id=${compte.id}&statut=ACTIF">
                Réactiver
            </a>
        </c:if>
    </div>
</div>

<div class="row g-3 mb-3">
    <!-- Informations du compte -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold"><i class="bi bi-info-circle me-2"></i>Informations</div>
            <ul class="list-group list-group-flush">
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Titulaire</span>
                    <a href="${pageContext.request.contextPath}/clients/details?id=${compte.client.id}">
                        <strong>${compte.client.nomComplet}</strong>
                    </a>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Type</span><strong>${compte.type}</strong>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Date d'ouverture</span>${f:dateFr(compte.dateOuverture)}
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Agence</span>${not empty compte.agency ? compte.agency.nom : "—"}
                </li>
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <span class="text-muted">Solde</span>
                    <span class="fs-5 fw-bold text-success">${f:fcfa(compte.solde)}</span>
                </li>
            </ul>
        </div>

        <!-- Relevé PDF / Export CSV -->
        <div class="card mt-3">
            <div class="card-header bg-white fw-semibold"><i class="bi bi-file-earmark-text me-2"></i>Relevés</div>
            <div class="card-body">
                <form method="get" action="${pageContext.request.contextPath}/statements/pdf" id="formReleve"
                      class="row g-2 align-items-end">
                    <input type="hidden" name="accountId" value="${compte.id}">
                    <div class="col-6">
                        <label class="form-label small text-muted">Du</label>
                        <input type="date" name="du" class="form-control form-control-sm" value="${duParam}">
                    </div>
                    <div class="col-6">
                        <label class="form-label small text-muted">Au</label>
                        <input type="date" name="au" class="form-control form-control-sm" value="${auParam}">
                    </div>
                </form>
                <div class="d-flex gap-2 mt-3">
                    <button type="submit" form="formReleve" class="btn btn-primary btn-sm w-100">
                        <i class="bi bi-file-earmark-pdf me-1"></i>Télécharger le relevé PDF
                    </button>
                    <button type="submit" form="formReleve" class="btn btn-outline-dark btn-sm w-100"
                            formaction="${pageContext.request.contextPath}/statements/csv">
                        <i class="bi bi-filetype-csv me-1"></i>Exporter CSV
                    </button>
                    <button type="submit" form="formReleve" class="btn btn-outline-dark btn-sm w-100"
                            formaction="${pageContext.request.contextPath}/statements/print" target="_blank">
                        <i class="bi bi-printer me-1"></i>Imprimer
                    </button>
                </div>
                <p class="small text-muted mt-2 mb-0">Sans période sélectionnée : historique complet.</p>
            </div>
        </div>
    </div>

    <!-- Dernières opérations + raccourcis -->
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-clock-history me-2"></i>Dernières opérations</span>
                <a class="btn btn-sm btn-outline-secondary"
                   href="${pageContext.request.contextPath}/operations?accountId=${compte.id}">
                    Tout l'historique
                </a>
            </div>
            <table class="table table-microbank table-hover mb-0 align-middle">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Référence</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th class="text-end">Montant</th>
                    <th class="text-end">Solde</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="op" items="${dernieresOperations}">
                    <tr>
                        <td class="text-nowrap">${f:dateHeureFr(op.dateOperation)}</td>
                        <td>${op.reference}</td>
                        <td><span class="badge bg-secondary">${op.type}</span></td>
                        <td class="small">${op.description}</td>
                        <td class="text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                                ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)}
                        </td>
                        <td class="text-end">${f:nombre(compte.solde)}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty dernieresOperations}">
                    <tr><td colspan="6" class="text-center text-muted py-4">Aucune opération sur ce compte.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>

        <!-- Raccourcis opérations -->
        <div class="d-flex gap-2 mt-3">
            <a class="btn btn-success flex-fill ${compte.statut != 'ACTIF' ? 'disabled' : ''}"
               href="${pageContext.request.contextPath}/operations/deposit?accountId=${compte.id}">
                <i class="bi bi-cash-coin me-1"></i>Dépôt
            </a>
            <a class="btn btn-danger flex-fill ${compte.statut != 'ACTIF' ? 'disabled' : ''}"
               href="${pageContext.request.contextPath}/operations/withdraw?accountId=${compte.id}">
                <i class="bi bi-cash-stack me-1"></i>Retrait
            </a>
            <a class="btn btn-primary flex-fill ${compte.statut != 'ACTIF' ? 'disabled' : ''}"
               href="${pageContext.request.contextPath}/operations/transfer?compteSource=${compte.id}">
                <i class="bi bi-arrow-left-right me-1"></i>Virement
            </a>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
