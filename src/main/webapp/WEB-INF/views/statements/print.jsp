<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Relevé imprimable - ${releve.compte.numeroCompte}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="bg-light">

<div class="container py-4 print-area">
    <div class="card shadow-sm">
        <div class="card-body p-4">

            <div class="d-flex justify-content-between align-items-start border-bottom pb-3 mb-4">
                <div>
                    <h1 class="h2 mb-0 text-success fw-bold">MICROBANK</h1>
                    <span class="text-muted">Institution de microfinance</span>
                </div>
                    <div class="text-end">
                        <h2 class="h4 mb-1">RELEVÉ DE COMPTE</h2>
                        <div class="small text-muted">Édité le ${f:dateHeureFr(dateEdition)}</div>
                    </div>
            </div>

            <div class="row mb-4">
                <div class="col-md-6">
                    <p class="mb-1"><strong>Client :</strong> ${fn:escapeXml(releve.compte.client.nomComplet)}</p>
                    <p class="mb-1"><strong>Compte :</strong> ${releve.compte.numeroCompte}</p>
                    <p class="mb-1"><strong>Type :</strong>
                        ${releve.compte.type == 'COURANT' ? "Compte courant" : "Compte épargne"}</p>
                </div>
                <div class="col-md-6">
                    <p class="mb-1"><strong>Période :</strong> ${releve.periodeAffichee}</p>
                    <p class="mb-1"><strong>Statut :</strong> ${releve.compte.statut}</p>
                    <p class="mb-0"><strong>Solde actuel :</strong>
                        <span class="fw-bold">${f:fcfa(releve.compte.solde)}</span></p>
                </div>
            </div>

            <table class="table table-sm table-bordered align-middle">
                <thead class="table-success">
                <tr>
                    <th>Date</th>
                    <th>Référence</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th class="text-end">Montant (FCFA)</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="op" items="${releve.operations}">
                    <tr>
                        <td class="text-nowrap">${f:dateHeureFr(op.dateOperation)}</td>
                        <td>${op.reference}</td>
                        <td>${op.type}</td>
                        <td>${fn:escapeXml(op.description)}</td>
                        <td class="text-end ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? 'montant-moins' : 'montant-plus'}">
                                ${op.type == 'RETRAIT' || (op.type == 'VIREMENT' and empty op.compteDestination) ? '-' : '+'}${f:nombre(op.montant)}
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty releve.operations}">
                    <tr><td colspan="5" class="text-center text-muted py-3">Aucune opération sur la période.</td></tr>
                </c:if>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="4" class="text-end"><strong>Total des dépôts</strong></td>
                    <td class="text-end montant-plus"><strong>${f:nombre(releve.totalDepots)}</strong></td>
                </tr>
                <tr>
                    <td colspan="4" class="text-end"><strong>Total des retraits</strong></td>
                    <td class="text-end montant-moins"><strong>- ${f:nombre(releve.totalRetraits)}</strong></td>
                </tr>
                </tfoot>
            </table>

            <div class="text-end mt-3">
                <span class="fs-5 fw-bold text-success">
                    Solde final : ${f:fcfa(releve.compte.solde)}
                </span>
            </div>

            <hr class="mt-4">
            <p class="small text-muted mb-0 text-center">
                Document généré automatiquement par MicroBank Manager.
            </p>
        </div>
    </div>

    <div class="text-center mt-3 no-print">
        <button onclick="window.print()" class="btn btn-primary">
            <i class="bi bi-printer me-1"></i>Imprimer
        </button>
        <a href="${pageContext.request.contextPath}/accounts/details?id=${releve.compte.id}"
           class="btn btn-outline-secondary ms-2">Retour au compte</a>
    </div>
</div>

</body>
</html>
