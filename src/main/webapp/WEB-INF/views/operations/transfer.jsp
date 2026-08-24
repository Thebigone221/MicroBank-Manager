<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<div class="row justify-content-center">
    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0 text-primary"><i class="bi bi-arrow-left-right me-2"></i>Virement</h1>
            </div>
            <div class="card-body">

                <c:if test="${not empty erreurs['_global']}">
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>${erreurs['_global']}
                    </div>
                </c:if>

                <!-- POST /operations/transfer -->
                <form method="post"
                      action="${pageContext.request.contextPath}/operations/transfer">

                    <div class="mb-3">
                        <label for="compteSource" class="form-label">Compte source <span
                                class="text-danger">*</span></label>
                        <select name="compteSource" id="compteSource"
                                class="form-select ${not empty erreurs['compteSource'] ? 'is-invalid' : ''}" required>
                            <option value="">— Sélectionner le compte à débiter —</option>
                            <c:forEach var="compteActif" items="${comptesActifs}">
                                <option value="${compteActif.id}"
                                        ${sourceSelectionnee eq compteActif.id ? 'selected' : ''}>
                                        ${compteActif.numeroCompte} — ${compteActif.client.nomComplet}
                                    (solde : ${compteActif.solde} FCFA)
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">${erreurs['compteSource']}</div>
                    </div>

                    <div class="mb-3">
                        <label for="compteDestination" class="form-label">Compte destination <span
                                class="text-danger">*</span></label>
                        <select name="compteDestination" id="compteDestination"
                                class="form-select ${not empty erreurs['compteDestination'] ? 'is-invalid' : ''}"
                                required>
                            <option value="">— Sélectionner le compte à créditer —</option>
                            <c:forEach var="compteActif" items="${comptesActifs}">
                                <option value="${compteActif.id}"
                                        ${destinationSelectionnee eq compteActif.id ? 'selected' : ''}>
                                        ${compteActif.numeroCompte} — ${compteActif.client.nomComplet}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">${erreurs['compteDestination']}</div>
                        <div class="form-text">Les comptes source et destination doivent être différents.</div>
                    </div>

                    <div class="mb-3">
                        <label for="montant" class="form-label">Montant (FCFA) <span class="text-danger">*</span></label>
                        <input type="number" step="1" min="1" id="montant" name="montant" required
                               class="form-control form-control-lg ${not empty erreurs['montant'] ? 'is-invalid' : ''}"
                               value="${montantSaisi}" placeholder="Ex : 25000" autofocus>
                        <div class="invalid-feedback">${erreurs['montant']}</div>
                    </div>

                    <div class="mb-4">
                        <label for="description" class="form-label">Motif du virement</label>
                        <textarea id="description" name="description" class="form-control" rows="2"
                                  placeholder="Ex : Paiement de tontine"></textarea>
                    </div>

                    <div class="alert alert-light border small text-muted mb-4">
                        <i class="bi bi-shield-lock me-1"></i>
                        Le débit, le crédit et l'enregistrement s'exécutent dans une seule transaction :
                        en cas d'erreur, tout est annulé (ROLLBACK).
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/accounts" class="btn btn-outline-secondary">
                            <i class="bi bi-x-lg me-1"></i>Annuler
                        </a>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-arrow-left-right me-1"></i>Effectuer le virement
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
