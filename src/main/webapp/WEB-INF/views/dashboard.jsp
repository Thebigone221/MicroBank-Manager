<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0"><i class="bi bi-speedometer2 me-2"></i>Tableau de bord</h1>
    <span class="text-muted">Bienvenue, <strong>${sessionScope.user.nomComplet}</strong></span>
</div>

<!-- Statistiques principales -->
<div class="row g-3 mb-4">
    <div class="col-md-3 col-sm-6">
        <div class="card stat-card h-100">
            <div class="card-body">
                <div class="text-muted small text-uppercase">Clients</div>
                <div class="stat-valeur">${stats.totalClients}</div>
                <div class="small text-muted">${stats.clientsActifs} actifs · +${stats.clientsNouveauMois} ce mois</div>
            </div>
        </div>
    </div>
    <div class="col-md-3 col-sm-6">
        <div class="card stat-card bleu h-100">
            <div class="card-body">
                <div class="text-muted small text-uppercase">Comptes</div>
                <div class="stat-valeur">${stats.totalComptes}</div>
                <div class="small text-muted">${stats.comptesActifs} actifs · ${stats.comptesBloques} bloqués</div>
            </div>
        </div>
    </div>
    <div class="col-md-3 col-sm-6">
        <div class="card stat-card or h-100">
            <div class="card-body">
                <div class="text-muted small text-uppercase">Solde total</div>
                <div class="stat-valeur">${f:fcfa(stats.soldeTotal)}</div>
                <div class="small text-muted">Moyenne : ${f:fcfa(stats.soldeMoyen)} par compte</div>
            </div>
        </div>
    </div>
    <div class="col-md-3 col-sm-6">
        <div class="card stat-card rouge h-100">
            <div class="card-body">
                <div class="text-muted small text-uppercase">Opérations du jour</div>
                <div class="stat-valeur">${stats.operationsDuJour}</div>
                <div class="small text-success"><i class="bi bi-arrow-down-circle"></i> ${f:fcfa(stats.depotsDuJour)}
                    ·
                    <span class="montant-moins">${f:fcfa(stats.retraitsDuJour)}</span></div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3">
    <!-- Bonus 3 : répartition par type -->
    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold">
                <i class="bi bi-pie-chart me-2"></i>Répartition des comptes
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>Comptes courants</span><strong>${stats.comptesCourant}</strong>
                    </div>
                    <div class="progress" role="progressbar">
                        <div class="progress-bar badge-courant"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesCourant * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <div class="mb-1">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>Comptes épargne</span><strong>${stats.comptesEpargne}</strong>
                    </div>
                    <div class="progress" role="progressbar">
                        <div class="progress-bar badge-epargne"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesEpargne * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <hr>
                <div class="d-grid gap-2 d-md-block">
                    <a class="btn btn-outline-success btn-sm"
                       href="${pageContext.request.contextPath}/clients/create">
                        <i class="bi bi-person-plus"></i> Nouveau client
                    </a>
                    <a class="btn btn-outline-primary btn-sm"
                       href="${pageContext.request.contextPath}/accounts/create">
                        <i class="bi bi-plus-circle"></i> Ouvrir un compte
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Dernières opérations -->
    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-clock-history me-2"></i>Dernières opérations</span>
                <a href="${pageContext.request.contextPath}/operations" class="btn btn-sm btn-outline-secondary">
                    Tout voir
                </a>
            </div>
            <table class="table table-microbank table-hover mb-0 align-middle">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Référence</th>
                    <th>Type</th>
                    <th>Compte</th>
                    <th class="text-end">Montant</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="op" items="${stats.dernieresOperations}">
                    <tr>
                        <td class="text-nowrap">${f:dateHeureFr(op.dateOperation)}</td>
                        <td>${op.reference}</td>
                        <td><span class="badge bg-secondary">${op.type}</span></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/accounts/details?id=${op.compte.id}">
                                    ${op.compte.numeroCompte}
                            </a>
                        </td>
                        <td class="text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                                ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)}
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty stats.dernieresOperations}">
                    <tr><td colspan="5" class="text-center text-muted py-4">Aucune opération enregistrée.</td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
