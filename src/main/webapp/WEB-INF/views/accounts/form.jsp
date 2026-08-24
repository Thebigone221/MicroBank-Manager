<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0"><i class="bi bi-plus-circle me-2"></i>Ouverture d'un compte</h1>
            </div>
            <div class="card-body">

                <c:if test="${not empty erreurs['_global']}">
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>${erreurs['_global']}
                    </div>
                </c:if>

                <!-- POST /accounts/create -->
                <form method="post" action="${pageContext.request.contextPath}/accounts/create">

                    <div class="mb-3">
                        <label for="clientId" class="form-label">Client <span class="text-danger">*</span></label>
                        <select name="clientId" id="clientId"
                                class="form-select ${not empty erreurs['clientId'] ? 'is-invalid' : ''}" required>
                            <option value="">— Sélectionner un client —</option>
                            <c:forEach var="client" items="${clients}">
                                <option value="${client.id}"
                                        ${valeurs['clientId'] eq String.valueOf(client.id) or clientIdSelectionne eq String.valueOf(client.id) ? 'selected' : ''}>
                                        ${client.nomComplet} (${client.numeroPiece})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">${erreurs['clientId']}</div>
                    </div>

                    <label class="form-label d-block">Type de compte <span class="text-danger">*</span></label>
                    <div class="d-flex gap-4 mb-3 ps-1">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="type" id="typeCourant"
                                   value="COURANT" ${valeurs['type'] == 'COURANT' or empty valeurs['type'] ? 'checked' : ''}>
                            <label class="form-check-label" for="typeCourant">Compte courant</label>
                        </div>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="type" id="typeEpargne"
                                   value="EPARGNE" ${valeurs['type'] == 'EPARGNE' ? 'checked' : ''}>
                            <label class="form-check-label" for="typeEpargne">Compte épargne</label>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="depotInitial" class="form-label">Dépôt initial (FCFA)</label>
                            <input type="number" step="1" min="0" id="depotInitial" name="depotInitial"
                                   class="form-control ${not empty erreurs['depotInitial'] ? 'is-invalid' : ''}"
                                   value="${valeurs['depotInitial']}" placeholder="Ex : 100000">
                            <div class="invalid-feedback">${erreurs['depotInitial']}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="agenceId" class="form-label">Agence</label>
                            <select name="agenceId" id="agenceId" class="form-select">
                                <option value="">— Aucune —</option>
                                <c:forEach var="agence" items="${agences}">
                                    <option value="${agence.id}" ${valeurs['agenceId'] eq String.valueOf(agence.id) ? 'selected' : ''}>
                                            ${agence.code} — ${agence.nom}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <div class="alert alert-light border small text-muted">
                        Le numéro de compte est généré automatiquement et unique.
                        Un dépôt initial facultatif est enregistré comme première opération.
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/accounts" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-check-lg me-1"></i>Créer le compte
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
