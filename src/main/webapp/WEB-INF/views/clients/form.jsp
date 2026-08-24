<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/includes/header.jspf" %>

<c:set var="modeEdition" value="${not empty client}"/>
<c:url var="actionFormulaire" value="/clients/${modeEdition ? 'update' : 'create'}"/>

<div class="row justify-content-center">
    <div class="col-lg-7">
        <div class="card">
            <div class="card-header bg-white">
                <h1 class="h5 mb-0">
                    <i class="bi bi-person-${modeEdition ? 'pencil' : 'plus'} me-2"></i>
                        ${modeEdition ? "Modifier le client " : "Nouveau client "}
                    <c:if test="${modeEdition}">${client.nomComplet}</c:if>
                </h1>
            </div>
            <div class="card-body">

                <c:if test="${not empty erreurs['_global']}">
                    <div class="alert alert-danger">
                        <i class="bi bi-exclamation-triangle-fill me-1"></i>${erreurs['_global']}
                    </div>
                </c:if>

                <!-- POST /clients/create ou /clients/update -->
                <form method="post" action="${actionFormulaire}" novalidate>
                    <c:if test="${modeEdition}">
                        <input type="hidden" name="id" value="${client.id}">
                    </c:if>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="nom" class="form-label">Nom <span class="text-danger">*</span></label>
                            <input type="text" id="nom" name="nom"
                                   class="form-control ${not empty erreurs['nom'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['nom'] ? valeurs['nom'] : client.nom}" required
                                   placeholder="Ex : GAYE">
                            <div class="invalid-feedback">${erreurs['nom']}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="prenom" class="form-label">Prénom <span class="text-danger">*</span></label>
                            <input type="text" id="prenom" name="prenom"
                                   class="form-control ${not empty erreurs['prenom'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['prenom'] ? valeurs['prenom'] : client.prenom}" required
                                   placeholder="Ex : Abdoulaye">
                            <div class="invalid-feedback">${erreurs['prenom']}</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="dateNaissance" class="form-label">Date de naissance</label>
                            <input type="date" id="dateNaissance" name="dateNaissance"
                                   class="form-control ${not empty erreurs['dateNaissance'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['dateNaissance'] ? valeurs['dateNaissance'] : (client.dateNaissance != null ? f:dateFr(client.dateNaissance) : '')}">
                            <div class="invalid-feedback">${erreurs['dateNaissance']}</div>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="telephone" class="form-label">Téléphone <span class="text-danger">*</span></label>
                            <input type="tel" id="telephone" name="telephone"
                                   class="form-control ${not empty erreurs['telephone'] ? 'is-invalid' : ''}"
                                   value="${not empty valeurs['telephone'] ? valeurs['telephone'] : client.telephone}" required
                                   placeholder="77 123 45 67">
                            <div class="invalid-feedback">${erreurs['telephone']}</div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" id="email" name="email"
                               class="form-control ${not empty erreurs['email'] ? 'is-invalid' : ''}"
                               value="${not empty valeurs['email'] ? valeurs['email'] : client.email}"
                               placeholder="exemple@mail.com">
                        <div class="invalid-feedback">${erreurs['email']}</div>
                    </div>

                    <div class="mb-3">
                        <label for="adresse" class="form-label">Adresse</label>
                        <textarea id="adresse" name="adresse" class="form-control" rows="2"
                                  placeholder="Quartier, rue, ville...">${not empty valeurs['adresse'] ? valeurs['adresse'] : client.adresse}</textarea>
                    </div>

                    <div class="mb-4">
                        <label for="numeroPiece" class="form-label">Numéro de pièce d'identité <span
                                class="text-danger">*</span></label>
                        <input type="text" id="numeroPiece" name="numeroPiece"
                               class="form-control ${not empty erreurs['numeroPiece'] ? 'is-invalid' : ''}"
                               value="${not empty valeurs['numeroPiece'] ? valeurs['numeroPiece'] : client.numeroPiece}" required
                               placeholder="CNI ou passeport">
                        <div class="invalid-feedback">${erreurs['numeroPiece']}</div>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/clients" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                        <button type="submit" class="btn btn-success">
                            <i class="bi bi-check-lg me-1"></i>${modeEdition ? "Enregistrer les modifications" : "Créer le client"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<%@ include file="/includes/footer.jspf" %>
