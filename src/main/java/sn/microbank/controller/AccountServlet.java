package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.model.Client;
import sn.microbank.model.CompteStatut;
import sn.microbank.model.TypeCompte;
import sn.microbank.model.User;
import sn.microbank.service.AccountService;
import sn.microbank.service.ServiceException;

import java.io.IOException;
import java.util.Map;

/**
 * Gestion des comptes : liste + recherche, ouverture, détails,
 * changement de statut (blocage / clôture).
 */
@WebServlet(urlPatterns = {"/accounts", "/accounts/*"})
public class AccountServlet extends HttpServlet {

    private final AccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        switch (chemin) {
            case "/accounts" -> lister(request, response);
            case "/accounts/create" -> formulaireCreation(request, response);
            case "/accounts/details" -> details(request, response);
            case "/accounts/statut" -> changerStatut(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if ("/accounts/create".equals(chemin)) {
            creer(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // GET /accounts?search=&type=&statut=&agenceId=&page=&size=
    private void lister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = ServletUtil.page(request);
        int size = ServletUtil.size(request);

        var resultat = accountService.lister(
                request.getParameter("search"),
                typeParametre(request.getParameter("type")),
                statutParametre(request.getParameter("statut")),
                ServletUtil.id(request.getParameter("agenceId")),
                ServletUtil.id(request.getParameter("clientId")),
                page, size);

        request.setAttribute("resultat", resultat);
        request.setAttribute("recherche", paramOuVide(request, "search"));
        request.setAttribute("typeFiltre", paramOuVide(request, "type"));
        request.setAttribute("statutFiltre", paramOuVide(request, "statut"));
        request.setAttribute("agenceFiltre", paramOuVide(request, "agenceId"));
        request.setAttribute("clientIdFiltre", paramOuVide(request, "clientId"));
        // Pour le filtre par agence et la liste déroulante d'ouverture
        request.setAttribute("agences", accountService.toutesAgences());
        request.getRequestDispatcher("/WEB-INF/views/accounts/list.jsp").forward(request, response);
    }

    // GET /accounts/create?clientId=...  : formulaire d'ouverture
    private void formulaireCreation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String clientId = paramOuVide(request, "clientId");
        request.setAttribute("clientIdSelectionne", clientId);
        request.setAttribute("clients", new sn.microbank.service.ClientService()
                .lister(null, null, 0, Integer.MAX_VALUE).getItems());
        request.setAttribute("agences", accountService.toutesAgences());
        Map<String, String> valeurs = garderFormulaire(request);
        request.setAttribute("valeurs", valeurs);
        request.getRequestDispatcher("/WEB-INF/views/accounts/form.jsp").forward(request, response);
    }

    // POST /accounts/create
    private void creer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User agent = utilisateurConnecte(request);
        String clientId = paramOuVide(request, "clientId");
        String type = paramOuVide(request, "type");
        String depotInitial = paramOuVide(request, "depotInitial");
        String agenceId = paramOuVide(request, "agenceId");

        Map<String, String> erreurs = accountService.validerOuverture(clientId, type, depotInitial);
        if (!erreurs.isEmpty()) {
            garderFormulaireAvecErreurs(request, response, clientId, type, depotInitial, agenceId, erreurs);
            return;
        }

        try {
            var compte = accountService.ouvrirCompte(
                    ServletUtil.id(clientId),
                    TypeCompte.valueOf(type),
                    depotInitial.isBlank() ? null : new java.math.BigDecimal(depotInitial.trim()),
                    ServletUtil.id(agenceId),
                    agent);
            Flash.success(request.getSession(), "Compte " + compte.getNumeroCompte()
                    + " ouvert avec succès pour " + compte.getClient().getNomComplet() + ".");
            response.sendRedirect(request.getContextPath() + "/accounts/details?id=" + compte.getId());
        } catch (ServiceException e) {
            garderFormulaireAvecErreurGlobale(request, response, clientId, type, depotInitial, agenceId,
                    e.getMessage());
        }
    }

    // GET /accounts/details?id=...
    private void details(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long id = ServletUtil.id(request.getParameter("id"));
        var compte = id == null ? null : accountService.findById(id);
        if (compte == null) {
            Flash.error(request.getSession(), "Compte introuvable.");
            response.sendRedirect(request.getContextPath() + "/accounts");
            return;
        }

        // Dernières opérations du compte (5 plus récentes), via JPA.
        var historique = new sn.microbank.service.OperationService()
                .historique(id, null, null, null, null, null, null, null, 0, 5);

        request.setAttribute("compte", compte);
        request.setAttribute("dernieresOperations", historique.getItems());
        request.getRequestDispatcher("/WEB-INF/views/accounts/details.jsp").forward(request, response);
    }

    // GET /accounts/statut?id=...&statut=BLOQUE|ACTIF|CLOTURE
    private void changerStatut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        var session = request.getSession();
        Long id = ServletUtil.id(request.getParameter("id"));
        String statutStr = paramOuVide(request, "statut");
        try {
            if (id == null || statutStr.isBlank()) {
                throw new ServiceException("Requête invalide.");
            }
            CompteStatut nouveauStatut = CompteStatut.valueOf(statutStr);
            var compte = accountService.changerStatut(id, nouveauStatut);
            Flash.success(session, "Compte " + compte.getNumeroCompte()
                    + " : statut changé en " + traduireStatut(nouveauStatut) + ".");
        } catch (IllegalArgumentException | ServiceException e) {
            Flash.error(session, "Changement de statut impossible : "
                    + (e instanceof IllegalArgumentException ? "statut inconnu." : e.getMessage()));
        }
        response.sendRedirect(request.getContextPath() + "/accounts/details?id=" + id);
    }

    static String traduireStatut(CompteStatut statut) {
        return switch (statut) {
            case ACTIF -> "Actif";
            case BLOQUE -> "Bloqué";
            case CLOTURE -> "Clôturé";
        };
    }

    private TypeCompte typeParametre(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : TypeCompte.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private CompteStatut statutParametre(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : CompteStatut.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String paramOuVide(HttpServletRequest request, String nom) {
        String valeur = request.getParameter(nom);
        return valeur == null ? "" : valeur;
    }

    private User utilisateurConnecte(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

    private Map<String, String> garderFormulaire(HttpServletRequest request) {
        Map<String, String> valeurs = new java.util.HashMap<>();
        for (String champ : new String[]{"clientId", "type", "depotInitial", "agenceId"}) {
            valeurs.put(champ, paramOuVide(request, champ));
        }
        return valeurs;
    }

    private void garderFormulaireAvecErreurs(HttpServletRequest request, HttpServletResponse response,
                                             String clientId, String type, String depotInitial,
                                             String agenceId, Map<String, String> erreurs)
            throws ServletException, IOException {
        preparerFormulaire(request, clientId, type, depotInitial, agenceId, erreurs, null);
        request.getRequestDispatcher("/WEB-INF/views/accounts/form.jsp").forward(request, response);
    }

    private void garderFormulaireAvecErreurGlobale(HttpServletRequest request, HttpServletResponse response,
                                                   String clientId, String type, String depotInitial,
                                                   String agenceId, String message)
            throws ServletException, IOException {
        Map<String, String> erreurs = new java.util.HashMap<>();
        erreurs.put("_global", message);
        preparerFormulaire(request, clientId, type, depotInitial, agenceId, erreurs, null);
        request.getRequestDispatcher("/WEB-INF/views/accounts/form.jsp").forward(request, response);
    }

    private void preparerFormulaire(HttpServletRequest request, String clientId, String type,
                                    String depotInitial, String agenceId,
                                    Map<String, String> erreurs, String rien) {
        Map<String, String> valeurs = new java.util.HashMap<>();
        valeurs.put("clientId", clientId);
        valeurs.put("type", type);
        valeurs.put("depotInitial", depotInitial);
        valeurs.put("agenceId", agenceId);
        request.setAttribute("valeurs", valeurs);
        request.setAttribute("erreurs", erreurs);
        request.setAttribute("clientIdSelectionne", clientId);
        request.setAttribute("clients", new sn.microbank.service.ClientService()
                .lister(null, null, 0, Integer.MAX_VALUE).getItems());
        request.setAttribute("agences", accountService.toutesAgences());
    }
}
