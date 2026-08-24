package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.model.Account;
import sn.microbank.model.CompteStatut;
import sn.microbank.model.TypeOperation;
import sn.microbank.model.User;
import sn.microbank.service.AccountService;
import sn.microbank.service.OperationService;
import sn.microbank.service.ServiceException;
import sn.microbank.util.ValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@WebServlet(urlPatterns = {"/operations", "/operations/*"})
public class OperationServlet extends HttpServlet {

    private final OperationService operationService = new OperationService();
    private final AccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        switch (chemin) {
            case "/operations" -> historique(request, response);
            case "/operations/deposit" -> formulaireOperation(request, response, "deposit");
            case "/operations/withdraw" -> formulaireOperation(request, response, "withdraw");
            case "/operations/transfer" -> formulaireVirement(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        switch (chemin) {
            case "/operations/deposit" -> depot(request, response);
            case "/operations/withdraw" -> retrait(request, response);
            case "/operations/transfer" -> virement(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void historique(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long accountId = ServletUtil.id(request.getParameter("accountId"));
        TypeOperation type = typeParametre(request.getParameter("type"));
        LocalDate dateDu = ValidationUtil.date(request.getParameter("du"));
        LocalDate dateAu = ValidationUtil.date(request.getParameter("au"));
        BigDecimal montantMin = montantParametre(request.getParameter("min"));
        BigDecimal montantMax = montantParametre(request.getParameter("max"));
        int page = ServletUtil.page(request);
        int size = ServletUtil.size(request);

        LocalDateTime du = dateDu == null ? null : dateDu.atStartOfDay();
        LocalDateTime au = dateAu == null ? null : dateAu.plusDays(1).atStartOfDay().minusNanos(1);

        var resultat = operationService.historique(
                accountId, null, request.getParameter("numeroCompte"),
                type, du, au, montantMin, montantMax, page, size);

        if (accountId != null) {
            request.setAttribute("compte", accountService.findById(accountId));
        }
        request.setAttribute("resultat", resultat);

        request.setAttribute("filtreType", paramOuVide(request, "type"));
        request.setAttribute("filtreDu", paramOuVide(request, "du"));
        request.setAttribute("filtreAu", paramOuVide(request, "au"));
        request.setAttribute("filtreMin", paramOuVide(request, "min"));
        request.setAttribute("filtreMax", paramOuVide(request, "max"));
        request.setAttribute("filtreNumeroCompte", paramOuVide(request, "numeroCompte"));
        request.getRequestDispatcher("/WEB-INF/views/operations/list.jsp").forward(request, response);
    }

    private void depot(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long accountId = ServletUtil.id(request.getParameter("accountId"));
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        BigDecimal montant = ValidationUtil.montantPositif(erreurs, "montant",
                request.getParameter("montant"));

        if (!erreurs.isEmpty()) {
            renvoyerFormulaire(request, response, accountId, erreurs,
                    request.getParameter("montant"), "/WEB-INF/views/operations/deposit.jsp");
            return;
        }
        try {
            operationService.effectuerDepot(accountId, montant, utilisateurConnecte(request),
                    request.getParameter("description"));
            Flash.success(request.getSession(), String.format(
                    "Dépôt de %s FCFA effectué.", montant.toPlainString()));
            response.sendRedirect(request.getContextPath()
                    + "/accounts/details?id=" + accountId);
        } catch (ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
            renvoyerFormulaireSimple(request, response, "/WEB-INF/views/operations/deposit.jsp");
        }
    }

    private void retrait(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long accountId = ServletUtil.id(request.getParameter("accountId"));
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        BigDecimal montant = ValidationUtil.montantPositif(erreurs, "montant",
                request.getParameter("montant"));

        if (!erreurs.isEmpty()) {
            renvoyerFormulaire(request, response, accountId, erreurs,
                    request.getParameter("montant"), "/WEB-INF/views/operations/withdraw.jsp");
            return;
        }
        try {
            operationService.effectuerRetrait(accountId, montant, utilisateurConnecte(request),
                    request.getParameter("description"));
            Flash.success(request.getSession(), String.format(
                    "Retrait de %s FCFA effectué.", montant.toPlainString()));
            response.sendRedirect(request.getContextPath()
                    + "/accounts/details?id=" + accountId);
        } catch (ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
            renvoyerFormulaireSimple(request, response, "/WEB-INF/views/operations/withdraw.jsp");
        }
    }

    private void virement(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long sourceId = ServletUtil.id(request.getParameter("compteSource"));
        Long destinationId = ServletUtil.id(request.getParameter("compteDestination"));

        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        BigDecimal montant = ValidationUtil.montantPositif(erreurs, "montant",
                request.getParameter("montant"));
        if (sourceId == null) {
            erreurs.put("compteSource", "Veuillez sélectionner le compte source.");
        }
        if (destinationId == null) {
            erreurs.put("compteDestination", "Veuillez sélectionner le compte destination.");
        }
        if (sourceId != null && sourceId.equals(destinationId)) {
            erreurs.put("compteDestination",
                    "Les comptes source et destination doivent être différents.");
        }

        if (!erreurs.isEmpty()) {
            preparerFormulaireVirement(request, sourceId, destinationId,
                    request.getParameter("montant"), erreurs);
            request.getRequestDispatcher("/WEB-INF/views/operations/transfer.jsp")
                    .forward(request, response);
            return;
        }

        try {
            operationService.effectuerVirement(sourceId, destinationId, montant,
                    utilisateurConnecte(request), request.getParameter("description"));
            Flash.success(request.getSession(), String.format(
                    "Virement de %s FCFA effectué.", montant.toPlainString()));
            response.sendRedirect(request.getContextPath() + "/accounts/details?id=" + sourceId);
        } catch (ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
            preparerFormulaireVirement(request, sourceId, destinationId,
                    request.getParameter("montant"), ValidationUtil.nouvellesErreurs());
            request.getRequestDispatcher("/WEB-INF/views/operations/transfer.jsp")
                    .forward(request, response);
        }
    }

    private void formulaireOperation(HttpServletRequest request, HttpServletResponse response,
                                     String type) throws ServletException, IOException {
        Long accountId = ServletUtil.id(request.getParameter("accountId"));
        Account compte = accountId == null ? null : accountService.findById(accountId);
        if (compte == null) {
            Flash.error(request.getSession(), "Compte introuvable : sélectionnez un compte valide.");
            response.sendRedirect(request.getContextPath() + "/accounts");
            return;
        }
        request.setAttribute("compte", compte);
        request.getRequestDispatcher("/WEB-INF/views/operations/" + type + ".jsp")
                .forward(request, response);
    }

    private void formulaireVirement(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        preparerFormulaireVirement(request,
                ServletUtil.id(request.getParameter("compteSource")),
                ServletUtil.id(request.getParameter("compteDestination")),
                "", new java.util.HashMap<>());
        request.getRequestDispatcher("/WEB-INF/views/operations/transfer.jsp")
                .forward(request, response);
    }

    private void preparerFormulaireVirement(HttpServletRequest request, Long sourceId,
                                            Long destinationId, String montant,
                                            Map<String, String> erreurs) {
        var comptesActifs = accountService.lister(null, null, CompteStatut.ACTIF, null, null,
                0, Integer.MAX_VALUE).getItems();
        request.setAttribute("comptesActifs", comptesActifs);
        request.setAttribute("sourceSelectionnee", sourceId);
        request.setAttribute("destinationSelectionnee", destinationId);
        request.setAttribute("montantSaisi", montant);
        request.setAttribute("erreurs", erreurs);
    }

    private void renvoyerFormulaire(HttpServletRequest request, HttpServletResponse response,
                                    Long accountId, Map<String, String> erreurs,
                                    String montant, String vue)
            throws ServletException, IOException {
        Account compte = accountId == null ? null : accountService.findById(accountId);
        if (compte == null) {
            Flash.error(request.getSession(), "Compte introuvable.");
            response.sendRedirect(request.getContextPath() + "/accounts");
            return;
        }
        request.setAttribute("compte", compte);
        request.setAttribute("erreurs", erreurs);
        request.setAttribute("montantSaisi", montant);
        request.getRequestDispatcher(vue).forward(request, response);
    }

    private void renvoyerFormulaireSimple(HttpServletRequest request, HttpServletResponse response,
                                          String vue) throws ServletException, IOException {
        Long accountId = ServletUtil.id(request.getParameter("accountId"));
        Account compte = accountId == null ? null : accountService.findById(accountId);
        request.setAttribute("compte", compte);
        request.getRequestDispatcher(vue).forward(request, response);
    }

    private TypeOperation typeParametre(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : TypeOperation.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BigDecimal montantParametre(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(valeur.trim());
        } catch (NumberFormatException e) {
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
}
