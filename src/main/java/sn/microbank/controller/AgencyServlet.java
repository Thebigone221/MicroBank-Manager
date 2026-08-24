package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.service.AgencyService;
import sn.microbank.service.ServiceException;

import java.io.IOException;
import java.util.Map;

/**
 * Gestion des agences (Bonus 4) : liste, création, modification, suppression.
 */
@WebServlet(urlPatterns = {"/agencies", "/agencies/*"})
public class AgencyServlet extends HttpServlet {

    private final AgencyService agencyService = new AgencyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        switch (chemin) {
            case "/agencies" -> lister(request, response);
            case "/agencies/create" -> formulaire(request, response, null);
            case "/agencies/edit" -> formulaire(request, response,
                    ServletUtil.id(request.getParameter("id")));
            case "/agencies/delete" -> supprimer(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath() + (request.getPathInfo() == null ? "" : request.getPathInfo());
        if ("/agencies/create".equals(chemin)) {
            creer(request, response);
        } else if ("/agencies/update".equals(chemin)) {
            modifier(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void lister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("agences", agencyService.toutes());
        request.getRequestDispatcher("/WEB-INF/views/agencies/list.jsp").forward(request, response);
    }

    private void formulaire(HttpServletRequest request, HttpServletResponse response, Long id)
            throws ServletException, IOException {
        if (id != null) {
            var agence = agencyService.findById(id);
            if (agence == null) {
                Flash.error(request.getSession(), "Agence introuvable.");
                response.sendRedirect(request.getContextPath() + "/agencies");
                return;
            }
            request.setAttribute("agenceEdit", agence);
        }
        garderValeurs(request);
        request.getRequestDispatcher("/WEB-INF/views/agencies/form.jsp").forward(request, response);
    }

    private void creer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, String> erreurs = agencyService.valider(
                request.getParameter("code"), request.getParameter("nom"),
                request.getParameter("ville"));
        if (!erreurs.isEmpty()) {
            request.setAttribute("erreurs", erreurs);
            garderValeurs(request);
            request.getRequestDispatcher("/WEB-INF/views/agencies/form.jsp").forward(request, response);
            return;
        }
        try {
            var agence = agencyService.creer(request.getParameter("code"),
                    request.getParameter("nom"), request.getParameter("ville"));
            Flash.success(request.getSession(), "Agence " + agence.getNom() + " créée.");
            response.sendRedirect(request.getContextPath() + "/agencies");
        } catch (ServiceException e) {
            Map<String, String> erreursGlobales = new java.util.HashMap<>();
            erreursGlobales.put("_global", e.getMessage());
            request.setAttribute("erreurs", erreursGlobales);
            garderValeurs(request);
            request.getRequestDispatcher("/WEB-INF/views/agencies/form.jsp").forward(request, response);
        }
    }

    private void modifier(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long id = ServletUtil.id(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/agencies");
            return;
        }
        Map<String, String> erreurs = agencyService.valider(
                request.getParameter("code"), request.getParameter("nom"),
                request.getParameter("ville"));
        if (erreurs.isEmpty()) {
            try {
                var agence = agencyService.modifier(id, request.getParameter("code"),
                        request.getParameter("nom"), request.getParameter("ville"));
                Flash.success(request.getSession(), "Agence " + agence.getNom() + " modifiée.");
                response.sendRedirect(request.getContextPath() + "/agencies");
                return;
            } catch (ServiceException e) {
                erreurs.put("_global", e.getMessage());
            }
        }
        request.setAttribute("erreurs", erreurs);
        request.setAttribute("agenceEdit", agencyService.findById(id));
        garderValeurs(request);
        request.getRequestDispatcher("/WEB-INF/views/agencies/form.jsp").forward(request, response);
    }

    // GET /agencies/delete?id=...
    private void supprimer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = ServletUtil.id(request.getParameter("id"));
        try {
            var agence = id == null ? null : agencyService.findById(id);
            if (agence == null) {
                throw new ServiceException("Agence introuvable.");
            }
            String nom = agence.getNom();
            agencyService.supprimer(id);
            Flash.success(request.getSession(), "Agence " + nom + " supprimée.");
        } catch (ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/agencies");
    }

    private void garderValeurs(HttpServletRequest request) {
        Map<String, String> valeurs = new java.util.HashMap<>();
        valeurs.put("code", paramOuVide(request, "code"));
        valeurs.put("nom", paramOuVide(request, "nom"));
        valeurs.put("ville", paramOuVide(request, "ville"));
        request.setAttribute("valeurs", valeurs);
    }

    private String paramOuVide(HttpServletRequest request, String nom) {
        String valeur = request.getParameter(nom);
        return valeur == null ? "" : valeur;
    }
}
