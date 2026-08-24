<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="h4 mb-0">
        <i class="bi bi-person-vcard me-2"></i>${client.nomComplet}
        <span class="badge ${client.statut == 'ACTIF' ? 'bg-success' : 'bg-danger'} ms-2">${client.statut}</span>
    </h1>
    <div>
        <a href="${pageContext.request.contextPath}/clients/edit?id=${client.id}" class="btn btn-outline-secondary">
            <i class="bi bi-pencil me-1"></i>Modifier
        </a>
        <a href="${pageContext.request.contextPath}/clients/delete?id=${client.id}"
           class="btn btn-outline-danger"
           onclick="return confirm('Supprimer définitivement ce client ?');">
            <i class="bi bi-trash me-1"></i>Supprimer
        </a>
    </div>
</div>

<div class="row g-3">
    <!-- Fiche client -->
    <div class="col-lg-5">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold"><i class="bi bi-info-circle me-2"></i>Fiche client</div>
            <ul class="list-group list-group-flush">
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Identifiant</span><strong>C${client.id}</strong>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Nom / Prénom</span><strong>${client.nom} ${client.prenom}</strong>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Date de naissance</span>${f:dateFr(client.dateNaissance)}
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Téléphone</span><strong>${client.telephone}</strong>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Email</span>${client.email}
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Adresse</span>${client.adresse}
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">N° de pièce</span><strong>${client.numeroPiece}</strong>
                </li>
                <li class="list-group-item d-flex justify-content-between">
                    <span class="text-muted">Créé le</span>${f:dateHeureFr(client.dateCreation)}
                </li>
            </ul>
        </div>

        <!-- Bonus 1 : copie de la pièce d'identité -->
        <div class="card mt-3">
            <div class="card-header bg-white fw-semibold">
                <i class="bi bi-file-earmark-image me-2"></i>Pièce d'identité
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty client.pieceIdentite}">
                        <a class="btn btn-sm btn-outline-primary"
                           href="${pageContext.request.contextPath}/documents/client/${client.id}" target="_blank">
                            <i class="bi bi-box-arrow-up-right me-1"></i>Consulter la copie
                        </a>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted small mb-2">Aucune copie enregistrée (PDF ou image, 5 Mo max).</p>
                        <form method="post"
                              action="${pageContext.request.contextPath}/clients/upload?id=${client.id}"
                              enctype="multipart/form-data" class="d-flex gap-2">
                            <input type="file" name="pieceIdentite" accept="image/*,application/pdf"
                                   class="form-control form-control-sm" required>
                            <button type="submit" class="btn btn-sm btn-primary text-nowrap">Envoyer</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Comptes du client -->
    <div class="col-lg-7">
        <div class="card h-100">
            <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
                <span><i class="bi bi-wallet2 me-2"></i>Comptes (${client.accounts.size()})</span>
                <a class="btn btn-sm btn-primary"
                   href="${pageContext.request.contextPath}/accounts/create?clientId=${client.id}">
                    <i class="bi bi-plus-circle me-1"></i>Ouvrir un compte
                </a>
            </div>
            <table class="table table-microbank table-hover align-middle mb-0">
                <thead>
                <tr>
                    <th>Numéro</th>
                    <th>Type</th>
                    <th>Solde</th>
                    <th>Ouverture</th>
                    <th>Statut</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="compte" items="${client.accounts}">
                    <tr>
                        <td class="fw-semibold">${compte.numeroCompte}</td>
                        <td><span class="badge ${compte.type == 'COURANT' ? 'badge-courant' : 'badge-epargne'}">${compte.type}</span></td>
                        <td class="fw-semibold">${f:fcfa(compte.solde)}</td>
                        <td>${f:dateFr(compte.dateOuverture)}</td>
                        <td><span class="badge bg-secondary">${compte.statut}</span></td>
                        <td class="text-end">
                            <a class="btn btn-sm btn-outline-primary"
                               href="${pageContext.request.contextPath}/accounts/details?id=${compte.id}">
                                Détails
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty client.accounts}">
                    <tr><td colspan="6" class="text-center text-muted py-4">
                        Ce client n'a pas encore de compte.
                    </td></tr>
                </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
