<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<c:set var="modeEdition" value="${not empty userEdit}"/>
<c:url var="actionFormulaire" value="/users/${modeEdition ? 'update' : 'create'}"/>

<div class="row justify-content-center">
    <div class="col-lg-6">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0">
                    <i class="bi bi-person-${modeEdition ? 'pencil' : 'plus'} me-2"></i>
                        ${modeEdition ? "Modifier l'utilisateur " : "Nouvel utilisateur "}
                    <c:if test="${modeEdition}">${userEdit.login}</c:if>
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
                        <input type="hidden" name="id" value="${userEdit.id}">
                    </c:if>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="nom" class="form-label">Nom <span class="text-danger">*</span></label>
                            <input type="text" id="nom" name="nom"
                                   class="form-control ${not empty erreurs['nom'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['nom'] ? fn:escapeXml(valeurs['nom']) : userEdit.nom}" required>
                            <div class="invalid-feedback">${erreurs['nom']}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="prenom" class="form-label">Prénom <span class="text-danger">*</span></label>
                            <input type="text" id="prenom" name="prenom"
                                   class="form-control ${not empty erreurs['prenom'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['prenom'] ? fn:escapeXml(valeurs['prenom']) : userEdit.prenom}" required>
                            <div class="invalid-feedback">${erreurs['prenom']}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="login" class="form-label">Login <span class="text-danger">*</span></label>
                            <input type="text" id="login" name="login"
                                   class="form-control ${not empty erreurs['login'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['login'] ? valeurs['login'] : userEdit.login}" required>
                            <div class="invalid-feedback">${erreurs['login']}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="role" class="form-label">Rôle <span class="text-danger">*</span></label>
                            <select name="role" id="role"
                                    class="form-select ${not empty erreurs['role'] ? 'is-invalid' : ''}" required>
                                <option value="">- Choisir -</option>
                                <option value="AGENT" ${(empty valeurs['role'] and userEdit.role == 'AGENT') or valeurs['role'] == 'AGENT' ? 'selected' : ''}>
                                    Agent
                                </option>
                                <option value="ADMIN" ${(empty valeurs['role'] and userEdit.role == 'ADMIN') or valeurs['role'] == 'ADMIN' ? 'selected' : ''}>
                                    Administrateur
                                </option>
                            </select>
                            <div class="invalid-feedback">${erreurs['role']}</div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="motDePasse" class="form-label">
                            Mot de passe
                            <c:choose>
                                <c:when test="${modeEdition}">
                                    <span class="text-muted small">(laisser vide pour conserver)</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-danger">*</span>
                                </c:otherwise>
                            </c:choose>
                        </label>
                        <input type="password" id="motDePasse" name="motDePasse"
                               class="form-control ${not empty erreurs['motDePasse'] ? 'is-invalid' : ''}"
                               ${modeEdition ? '' : 'required'} minlength="6" placeholder="6 caractères minimum">
                        <div class="invalid-feedback">${erreurs['motDePasse']}</div>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/users" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-check-lg me-1"></i>${modeEdition ? "Enregistrer" : "Créer l'utilisateur"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
