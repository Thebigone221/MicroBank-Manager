<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="hero-page d-flex justify-content-between align-items-center flex-wrap gap-3">
    <div>
        <h1 class="hero-titre"><i class="bi bi-speedometer2 me-2"></i>Tableau de bord</h1>
        <div class="hero-sous-titre">
            <strong>${sessionScope.user.nomComplet}</strong> - voici l'activité de votre institution.
        </div>
    </div>
    <div class="d-flex gap-2 no-print position-relative" style="z-index:1">
        <a class="btn btn-warning btn-sm fw-semibold"
           href="${pageContext.request.contextPath}/operations/deposit?accountId=">
            <i class="bi bi-cash-coin me-1"></i>Dépôt
        </a>
        <a class="btn btn-outline-light btn-sm"
           href="${pageContext.request.contextPath}/clients/create">
            <i class="bi bi-person-plus me-1"></i>Nouveau client
        </a>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-xl-3 col-md-6">
        <div class="card stat-card h-100">
            <div class="card-body d-flex align-items-start gap-3">
                <span class="stat-icone icone-vert"><i class="bi bi-people-fill"></i></span>
                <div class="flex-grow-1">
                    <div class="stat-libelle">Clients</div>
                    <div class="stat-valeur">${stats.totalClients}</div>
                    <div class="stat-detail">${stats.clientsActifs} actifs
                        · +${stats.clientsNouveauMois} ce mois</div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl-3 col-md-6">
        <div class="card stat-card h-100">
            <div class="card-body d-flex align-items-start gap-3">
                <span class="stat-icone icone-bleu"><i class="bi bi-wallet2"></i></span>
                <div class="flex-grow-1">
                    <div class="stat-libelle">Comptes</div>
                    <div class="stat-valeur">${stats.totalComptes}</div>
                    <div class="stat-detail">${stats.comptesActifs} actifs
                        · ${stats.comptesBloques} bloqués</div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl-3 col-md-6">
        <div class="card stat-card h-100">
            <div class="card-body d-flex align-items-start gap-3">
                <span class="stat-icone icone-or"><i class="bi bi-safe2"></i></span>
                <div class="flex-grow-1">
                    <div class="stat-libelle">Encours total</div>
                    <div class="stat-valeur">${f:fcfa(stats.soldeTotal)}</div>
                    <div class="stat-detail">Moyenne : ${f:fcfa(stats.soldeMoyen)} / compte</div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xl-3 col-md-6">
        <div class="card stat-card h-100">
            <div class="card-body d-flex align-items-start gap-3">
                <span class="stat-icone icone-rouge"><i class="bi bi-arrow-left-right"></i></span>
                <div class="flex-grow-1 w-100">
                    <div class="stat-libelle">Opérations du jour</div>
                    <div class="stat-valeur">${stats.operationsDuJour}</div>
                    <div class="mini-stat stat-detail mt-1">
                        <span><i class="bi bi-arrow-down-circle text-success me-1"></i>dépôts</span>
                        <span class="mini-valeur montant-plus">${f:fcfa(stats.depotsDuJour)}</span>
                    </div>
                    <div class="mini-stat stat-detail">
                        <span><i class="bi bi-arrow-up-circle text-danger me-1"></i>retraits</span>
                        <span class="mini-valeur montant-moins">${f:fcfa(stats.retraitsDuJour)}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header fw-semibold">
                <i class="bi bi-pie-chart me-2 text-success"></i>Répartition des comptes
            </div>
            <div class="card-body py-4 px-4">
                <div class="mb-4">
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="fw-medium">Comptes courants</span>
                        <strong>${stats.comptesCourant}</strong>
                    </div>
                    <div class="progress" role="progressbar" style="height: 8px; border-radius: 6px;">
                        <div class="progress-bar badge-courant"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesCourant * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <div class="mb-4">
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="fw-medium">Comptes épargne</span>
                        <strong>${stats.comptesEpargne}</strong>
                    </div>
                    <div class="progress" role="progressbar" style="height: 8px; border-radius: 6px;">
                        <div class="progress-bar badge-epargne"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesEpargne * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <hr class="my-4">
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

    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-clock-history me-2 text-success"></i>Dernières opérations</span>
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
                        <td class="text-nowrap pe-0">${f:dateHeureFr(op.dateOperation)}</td>
                        <td><span class="ref-operation">${op.reference}</span></td>
                        <td>
                            <span class="badge ${op.type == 'DEPOT' ? 'badge-op-depot' : op.type == 'RETRAIT' ? 'badge-op-retrait' : 'badge-op-virement'}">
                                ${op.type}
                            </span>
                        </td>
                        <td>
                            <a href="${pageContext.request.contextPath}/accounts/details?id=${op.compte.id}"
                               class="fw-medium text-decoration-none">
                                ${op.compte.numeroCompte}
                            </a>
                        </td>
                        <td class="text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                                ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)}
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty stats.dernieresOperations}">
                    <tr><td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-inbox fs-4 d-block mb-2"></i>Aucune opération enregistrée.
                    </td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
