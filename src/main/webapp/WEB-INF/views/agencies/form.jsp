<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<c:set var="modeEdition" value="${not empty agenceEdit}"/>
<c:url var="actionFormulaire" value="/agencies/${modeEdition ? 'update' : 'create'}"/>

<div class="row justify-content-center">
    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0">
                    <i class="bi bi-building me-2"></i>${modeEdition ? "Modifier l'agence" : "Nouvelle agence"}
                </h1>
            </div>
            <div class="card-body">

                <c:if test="${not empty erreurs['_global']}">
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>${erreurs['_global']}
                    </div>
                </c:if>

                <form method="post" action="${actionFormulaire}">
                    <c:if test="${modeEdition}">
                        <input type="hidden" name="id" value="${agenceEdit.id}">
                    </c:if>

                    <div class="mb-3">
                        <label for="code" class="form-label">Code agence <span class="text-danger">*</span></label>
                        <input type="text" id="code" name="code" maxlength="10"
                               class="form-control ${not empty erreurs['code'] ? 'is-invalid' : ''}"
                               value="${not empty valeurs['code'] ? valeurs['code'] : agenceEdit.code}" required
                               placeholder="Ex : DAK01">
                        <div class="invalid-feedback">${erreurs['code']}</div>
                    </div>

                    <div class="mb-3">
                        <label for="nom" class="form-label">Nom de l'agence <span class="text-danger">*</span></label>
                        <input type="text" id="nom" name="nom"
                               class="form-control ${not empty erreurs['nom'] ? 'is-invalid' : ''}"
                               value="${not empty valeurs['nom'] ? fn:escapeXml(valeurs['nom']) : agenceEdit.nom}" required
                               placeholder="Ex : Agence Plateau">
                        <div class="invalid-feedback">${erreurs['nom']}</div>
                    </div>

                    <div class="mb-4">
                        <label for="ville" class="form-label">Ville</label>
                        <input type="text" id="ville" name="ville" class="form-control"
                               value="${not empty valeurs['ville'] ? valeurs['ville'] : agenceEdit.ville}"
                               placeholder="Ex : Dakar">
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/agencies" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-check-lg me-1"></i>${modeEdition ? "Enregistrer" : "Créer l'agence"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
