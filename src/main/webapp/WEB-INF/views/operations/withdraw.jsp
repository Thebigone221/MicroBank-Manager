<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://microbank.sn/functions" %>
<%@ include file="/includes/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-lg-5">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0 text-danger"><i class="bi bi-cash-stack me-2"></i>Retrait</h1>
            </div>
            <div class="card-body">

                <div class="alert alert-light border d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <span class="small text-muted d-block">Compte</span>
                        <strong>${compte.numeroCompte}</strong>
                    </div>
                    <div class="text-end">
                        <span class="small text-muted d-block">Titulaire</span>
                        <strong>${compte.client.nomComplet}</strong>
                    </div>
                    <div class="text-end">
                        <span class="small text-muted d-block">Solde disponible</span>
                        <strong class="text-success">${f:fcfa(compte.solde)}</strong>
                    </div>
                </div>

                <form method="post"
                      action="${pageContext.request.contextPath}/operations/withdraw?accountId=${compte.id}">
                    <input type="hidden" name="accountId" value="${compte.id}">

                    <div class="mb-3">
                        <label for="montant" class="form-label">Montant (FCFA) <span class="text-danger">*</span></label>
                        <input type="number" step="1" min="1" max="${compte.solde}" id="montant" name="montant"
                               required
                               class="form-control form-control-lg ${not empty erreurs['montant'] ? 'is-invalid' : ''}"
                               value="${montantSaisi}" placeholder="Ne peut pas dépasser le solde" autofocus>
                        <div class="invalid-feedback">${erreurs['montant']}</div>
                        <div class="form-text">Le retrait ne peut pas excéder le solde disponible.</div>
                    </div>

                    <div class="mb-4">
                        <label for="description" class="form-label">Description</label>
                        <textarea id="description" name="description" class="form-control" rows="2"
                                  placeholder="Ex : Retrait au guichet"></textarea>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/accounts/details?id=${compte.id}"
                           class="btn btn-outline-secondary"><i class="bi bi-x-lg me-1"></i>Annuler</a>
                        <button type="submit" class="btn btn-danger">
                            <i class="bi bi-check-lg me-1"></i>Valider le retrait
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
