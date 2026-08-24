package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.dao.PagedResult;
import sn.microbank.model.Role;
import sn.microbank.model.Statut;
import sn.microbank.model.User;
import sn.microbank.service.ServiceException;
import sn.microbank.service.UserService;

import java.io.IOException;
import java.util.Map;

/**
 * Gestion des utilisateurs — réservée à l'ADMIN (contrôle dans AuthFilter) :
 * liste, création, modification, activation / désactivation.
 */
@WebServlet(urlPatterns = {"/users", "/users/*"})
public class UserServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        switch (chemin) {
            case "/users" -> lister(request, response);
            case "/users/create" -> formulaire(request, response, null);
            case "/users/edit" -> formulaire(request, response,
                    ServletUtil.id(request.getParameter("id")));
            case "/users/toggle" -> basculer(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if ("/users/create".equals(chemin)) {
            creer(request, response);
        } else if ("/users/update".equals(chemin)) {
            modifier(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void lister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recherche = request.getParameter("search");
        Statut statut = statutParametre(request.getParameter("statut"));
        int page = ServletUtil.page(request);
        int size = ServletUtil.size(request);

        PagedResult<User> resultat = userService.lister(recherche, statut, page, size);
        request.setAttribute("resultat", resultat);
        request.setAttribute("recherche", recherche == null ? "" : recherche);
        request.setAttribute("statutFiltre", request.getParameter("statut") == null ? "" : request.getParameter("statut"));
        request.getRequestDispatcher("/WEB-INF/views/users/list.jsp").forward(request, response);
    }

    private void formulaire(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        if (id != null) {
            User user = userService.findById(id);
            if (user == null) {
                Flash.error(request.getSession(), "Utilisateur introuvable.");
                response.sendRedirect(request.getContextPath() + "/users");
                return;
            }
            request.setAttribute("userEdit", user);
        }
        request.getRequestDispatcher("/WEB-INF/views/users/form.jsp").forward(request, response);
    }

    private void creer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, String> erreurs = userService.valider(
                request.getParameter("nom"), request.getParameter("prenom"),
                request.getParameter("login"), request.getParameter("motDePasse"),
                request.getParameter("role"));
        if (!erreurs.isEmpty()) {
            renvoyerFormulaireAvecErreurs(request, response, erreurs);
            return;
        }
        try {
            User cree = userService.creer(
                    request.getParameter("nom"), request.getParameter("prenom"),
                    request.getParameter("login"), request.getParameter("motDePasse"),
                    request.getParameter("role"));
            Flash.success(request.getSession(),
                    "Utilisateur " + cree.getNomComplet() + " créé.");
            response.sendRedirect(request.getContextPath() + "/users");
        } catch (ServiceException | IllegalArgumentException e) {
            Map<String, String> erreursGlobales = new java.util.HashMap<>();
            erreursGlobales.put("_global", e instanceof IllegalArgumentException
                    ? "Rôle invalide." : e.getMessage());
            renvoyerFormulaireAvecErreurs(request, response, erreursGlobales);
        }
    }

    private void modifier(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long id = ServletUtil.id(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/users");
            return;
        }
        try {
            User modifie = userService.modifier(id,
                    request.getParameter("nom"), request.getParameter("prenom"),
                    request.getParameter("login"), request.getParameter("motDePasse"),
                    request.getParameter("role"));
            Flash.success(request.getSession(),
                    "Utilisateur " + modifie.getNomComplet() + " modifié.");
            response.sendRedirect(request.getContextPath() + "/users");
        } catch (ServiceException | IllegalArgumentException e) {
            Map<String, String> erreurs = new java.util.HashMap<>();
            erreurs.put("_global", e instanceof IllegalArgumentException
                    ? "Rôle invalide." : e.getMessage());
            request.setAttribute("erreurs", erreurs);
            request.setAttribute("userEdit", userService.findById(id));
            garderValeurs(request);
            request.getRequestDispatcher("/WEB-INF/views/users/form.jsp").forward(request, response);
        }
    }

    // GET /users/toggle?id=...
    private void basculer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = ServletUtil.id(request.getParameter("id"));
        try {
            User user = userService.basculerStatut(id);
            Flash.success(request.getSession(), "Compte de " + user.getLogin()
                    + (user.getStatut() == Statut.ACTIF ? " activé." : " désactivé."));
        } catch (ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/users");
    }

    private void renvoyerFormulaireAvecErreurs(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Map<String, String> erreurs)
            throws ServletException, IOException {
        request.setAttribute("erreurs", erreurs);
        garderValeurs(request);
        request.getRequestDispatcher("/WEB-INF/views/users/form.jsp").forward(request, response);
    }

    /** Repositionne les valeurs saisies après une erreur de validation. */
    private void garderValeurs(HttpServletRequest request) {
        if (request.getAttribute("userEdit") == null
                && request.getParameter("id") != null) {
            User existant = userService.findById(ServletUtil.id(request.getParameter("id")));
            if (existant != null) {
                request.setAttribute("userEdit", existant);
            }
        }
        Map<String, String> valeurs = new java.util.HashMap<>();
        valeurs.put("nom", paramOuVide(request, "nom"));
        valeurs.put("prenom", paramOuVide(request, "prenom"));
        valeurs.put("login", paramOuVide(request, "login"));
        valeurs.put("role", paramOuVide(request, "role"));
        request.setAttribute("valeurs", valeurs);
    }

    private Statut statutParametre(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : Statut.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String paramOuVide(HttpServletRequest request, String nom) {
        String valeur = request.getParameter(nom);
        return valeur == null ? "" : valeur;
    }
}
