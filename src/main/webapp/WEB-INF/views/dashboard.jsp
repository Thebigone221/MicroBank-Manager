<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<header class="masthead d-flex justify-content-between align-items-end gap-3 flex-wrap">
    <div>
        <div class="sur-titre">MicroBank · Institution de microfinance</div>
        <h1 class="titre-page">Tableau de bord</h1>
        <div class="sous-titre">
            <strong>${fn:escapeXml(sessionScope.user.nomComplet)}</strong> - voici l'activité de votre institution.
        </div>
    </div>
    <div class="date-journal no-print">
        Écriture du <strong>${dateJour}</strong>
    </div>
</header>

<section class="registre-total" aria-label="Chiffres clés">
    <div>
        <div class="total-libelle">Clients</div>
        <div class="total-valeur">${stats.totalClients}</div>
        <div class="total-detail">${stats.clientsActifs} actifs, +${stats.clientsNouveauMois} ce mois</div>
    </div>
    <div>
        <div class="total-libelle">Comptes</div>
        <div class="total-valeur">${stats.totalComptes}</div>
        <div class="total-detail">${stats.comptesCourant} courants, ${stats.comptesEpargne} épargne</div>
    </div>
    <div>
        <div class="total-libelle">Encours total</div>
        <div class="total-valeur">${f:fcfa(stats.soldeTotal)}</div>
        <div class="total-detail">Moyenne ${f:fcfa(stats.soldeMoyen)} par compte</div>
    </div>
    <div>
        <div class="total-libelle">Opérations du jour</div>
        <div class="total-valeur">${stats.operationsDuJour}</div>
        <div class="total-ligne total-detail">
            <span>dépôts</span><span class="montant-plus">${f:nombre(stats.depotsDuJour)}</span>
        </div>
        <div class="total-ligne total-detail">
            <span>retraits</span><span class="montant-moins">${f:nombre(stats.retraitsDuJour)}</span>
        </div>
    </div>
</section>

<div class="row g-4">
    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header">Répartition des comptes</div>
            <div class="card-body py-4 px-4">
                <div class="mb-4">
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="fw-semibold">Comptes courants</span>
                        <span class="mono fw-semibold">${stats.comptesCourant}</span>
                    </div>
                    <div class="progress" role="progressbar">
                        <div class="progress-bar badge-courant"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesCourant * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <div class="mb-4">
                    <div class="d-flex justify-content-between small mb-2">
                        <span class="fw-semibold">Comptes épargne</span>
                        <span class="mono fw-semibold">${stats.comptesEpargne}</span>
                    </div>
                    <div class="progress" role="progressbar">
                        <div class="progress-bar badge-epargne"
                             style="width: ${stats.totalComptes == 0 ? 0 : stats.comptesEpargne * 100 / stats.totalComptes}%"></div>
                    </div>
                </div>
                <hr class="my-4 border-secondary-subtle opacity-25">
                <div class="d-grid gap-2 actions-journal">
                    <a class="btn btn-success btn-sm"
                       href="${pageContext.request.contextPath}/clients/create">
                        <i class="bi bi-person-plus me-1"></i>Nouveau client
                    </a>
                    <a class="btn btn-outline-success btn-sm"
                       href="${pageContext.request.contextPath}/accounts/create">
                        <i class="bi bi-plus-circle me-1"></i>Ouvrir un compte
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>Dernières écritures</span>
                <a href="${pageContext.request.contextPath}/operations"
                   class="btn btn-sm btn-outline-secondary">Tout voir</a>
            </div>
            <table class="table table-microbank align-middle">
                <thead>
                <tr>
                    <th>Date</th>
                    <th>Réf.</th>
                    <th>Type</th>
                    <th>Compte</th>
                    <th class="text-end">Montant</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="op" items="${stats.dernieresOperations}">
                    <tr>
                        <td class="mono text-nowrap pe-0">${f:dateHeureFr(op.dateOperation)}</td>
                        <td><span class="ref-operation">${op.reference}</span></td>
                        <td>
                            <span class="badge ${op.type == 'DEPOT' ? 'badge-op-depot' : op.type == 'RETRAIT' ? 'badge-op-retrait' : 'badge-op-virement'}">
                                ${op.type}
                            </span>
                        </td>
                        <td>
                            <a class="mono text-decoration-none fw-semibold"
                               href="${pageContext.request.contextPath}/accounts/details?id=${op.compte.id}">
                                ${op.compte.numeroCompte}
                            </a>
                        </td>
                        <td class="mono text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                            ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)}
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty stats.dernieresOperations}">
                    <tr>
                        <td colspan="5" class="text-center py-5" style="color: var(--encre-doux);">
                            Aucune opération enregistrée.
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
