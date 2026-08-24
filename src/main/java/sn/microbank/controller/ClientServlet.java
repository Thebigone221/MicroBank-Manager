package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import sn.microbank.dao.PagedResult;
import sn.microbank.model.Client;
import sn.microbank.model.Statut;
import sn.microbank.service.ClientService;
import sn.microbank.service.ServiceException;
import sn.microbank.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@WebServlet(urlPatterns = {"/clients", "/clients/*"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class ClientServlet extends HttpServlet {

    static final Path REPERTOIRE_UPLOADS = Paths.get(
            System.getProperty("user.home"), "microbank-uploads");

    private final ClientService clientService = new ClientService();

    private static String chemin(HttpServletRequest request) {
        return request.getServletPath()
                + (request.getPathInfo() == null ? "" : request.getPathInfo());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = chemin(request);
        switch (chemin) {
            case "/clients" -> lister(request, response);
            case "/clients/create", "/clients/edit" -> formulaire(request, response);
            case "/clients/details" -> details(request, response);
            case "/clients/delete" -> supprimer(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = chemin(request);
        switch (chemin) {
            case "/clients/create" -> creer(request, response);
            case "/clients/update" -> modifier(request, response);
            case "/clients/upload" -> uploader(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void lister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String recherche = request.getParameter("search");
        Statut statut = statutParametre(request.getParameter("statut"));
        int page = ServletUtil.page(request);
        int size = ServletUtil.size(request);

        PagedResult<Client> resultat = clientService.lister(recherche, statut, page, size);
        request.setAttribute("resultat", resultat);
        request.setAttribute("recherche", recherche == null ? "" : recherche);
        request.setAttribute("statutFiltre", request.getParameter("statut") == null ? "" : request.getParameter("statut"));
        request.getRequestDispatcher("/WEB-INF/views/clients/list.jsp").forward(request, response);
    }

    private Statut statutParametre(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : Statut.valueOf(valeur);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void formulaire(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id != null && !id.isBlank()) {
            Client client = clientService.findById(ServletUtil.id(id));
            if (client == null) {
                Flash.error(request.getSession(), "Client introuvable.");
                response.sendRedirect(request.getContextPath() + "/clients");
                return;
            }
            request.setAttribute("client", client);
        }

        request.setAttribute("valeurs", request.getAttribute("valeurs"));
        request.setAttribute("erreurs", request.getAttribute("erreurs"));
        request.getRequestDispatcher("/WEB-INF/views/clients/form.jsp").forward(request, response);
    }

    private void creer(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, String> valeurs = lireFormulaire(request);
        Map<String, String> erreurs = clientService.valider(
                valeurs.get("nom"), valeurs.get("prenom"), valeurs.get("dateNaissance"),
                valeurs.get("telephone"), valeurs.get("email"), valeurs.get("numeroPiece"));

        if (!erreurs.containsKey("numeroPiece")
                && clientService.existsNumeroPiece(valeurs.get("numeroPiece"), null)) {
            erreurs.put("numeroPiece", "Ce numéro de pièce existe déjà.");
        }

        if (!erreurs.isEmpty()) {
            renvoyerFormulaire(request, response, valeurs, erreurs, "/WEB-INF/views/clients/form.jsp");
            return;
        }
        try {
            Client client = clientService.creer(valeurs);
            Flash.success(request.getSession(), "Client " + client.getNomComplet() + " créé avec succès.");
            response.sendRedirect(request.getContextPath() + "/clients/details?id=" + client.getId());
        } catch (ServiceException e) {
            renvoyerFormulaireAvecErreur(request, response, valeurs, e.getMessage());
        }
    }

    private void modifier(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long id = ServletUtil.id(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/clients");
            return;
        }
        Map<String, String> valeurs = lireFormulaire(request);
        Map<String, String> erreurs = clientService.valider(
                valeurs.get("nom"), valeurs.get("prenom"), valeurs.get("dateNaissance"),
                valeurs.get("telephone"), valeurs.get("email"), valeurs.get("numeroPiece"));

        if (!erreurs.containsKey("numeroPiece")
                && clientService.existsNumeroPiece(valeurs.get("numeroPiece"), id)) {
            erreurs.put("numeroPiece", "Ce numéro de pièce appartient déjà à un autre client.");
        }

        if (!erreurs.isEmpty()) {
            valeurs.put("id", String.valueOf(id));
            renvoyerFormulaire(request, response, valeurs, erreurs, "/WEB-INF/views/clients/form.jsp");
            return;
        }
        try {
            Client client = clientService.modifier(id, valeurs);
            Flash.success(request.getSession(), "Client modifié avec succès.");
            response.sendRedirect(request.getContextPath() + "/clients/details?id=" + client.getId());
        } catch (ServiceException e) {
            valeurs.put("id", String.valueOf(id));
            renvoyerFormulaireAvecErreur(request, response, valeurs, e.getMessage());
        }
    }

    private void supprimer(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        Long id = ServletUtil.id(request.getParameter("id"));
        try {
            if (id == null) {
                throw new ServiceException("Identifiant de client invalide.");
            }
            Client client = clientService.findById(id);
            if (client == null) {
                throw new ServiceException("Client introuvable.");
            }
            clientService.supprimer(id);
            Flash.success(session, "Client " + client.getNomComplet() + " supprimé.");
        } catch (ServiceException e) {
            Flash.error(session, e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/clients");
    }

    private void details(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long id = ServletUtil.id(request.getParameter("id"));
        Client client = id == null ? null : clientService.findById(id);
        if (client == null) {
            Flash.error(request.getSession(), "Client introuvable.");
            response.sendRedirect(request.getContextPath() + "/clients");
            return;
        }
        request.setAttribute("client", client);
        request.getRequestDispatcher("/WEB-INF/views/clients/details.jsp").forward(request, response);
    }

    private void uploader(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession();
        Long id = ServletUtil.id(request.getParameter("id"));
        try {
            Part part = request.getPart("pieceIdentite");
            if (id == null || part == null || part.getSize() == 0) {
                throw new ServiceException("Veuillez sélectionner un fichier (PDF ou image, 5 Mo max).");
            }
            String typeMime = part.getContentType() == null ? "" : part.getContentType();
            boolean accepte = typeMime.startsWith("image/") || typeMime.equals("application/pdf");
            if (!accepte) {
                throw new ServiceException("Format non supporté : seuls les images et PDF sont acceptés.");
            }

            Client client = clientService.findById(id);
            if (client == null) {
                throw new ServiceException("Client introuvable.");
            }

            Files.createDirectories(REPERTOIRE_UPLOADS);
            String extension = typeMime.startsWith("image/") ? "png" : "pdf";
            String nomFichier = "client-" + id + "-piece." + extension;
            Path destination = REPERTOIRE_UPLOADS.resolve(nomFichier);
            part.write(destination.toString());

            client.setPieceIdentite(nomFichier);
            clientService.modifierSansValidation(client);

            Flash.success(session, "Pièce d'identité enregistrée.");
        } catch (ServiceException e) {
            Flash.error(session, e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/clients/details?id=" + id);
    }

    private Map<String, String> lireFormulaire(HttpServletRequest request) {
        Map<String, String> valeurs = ValidationUtil.nouvellesErreurs();
        valeurs.put("nom", parametre(request, "nom"));
        valeurs.put("prenom", parametre(request, "prenom"));
        valeurs.put("dateNaissance", parametre(request, "dateNaissance"));
        valeurs.put("telephone", parametre(request, "telephone"));
        valeurs.put("email", parametre(request, "email"));
        valeurs.put("adresse", parametre(request, "adresse"));
        valeurs.put("numeroPiece", parametre(request, "numeroPiece"));
        return valeurs;
    }

    private String parametre(HttpServletRequest request, String nom) {
        String valeur = request.getParameter(nom);
        return valeur == null ? "" : valeur;
    }

    private void renvoyerFormulaire(HttpServletRequest request, HttpServletResponse response,
                                    Map<String, String> valeurs, Map<String, String> erreurs,
                                    String vue) throws ServletException, IOException {
        request.setAttribute("valeurs", valeurs);
        request.setAttribute("erreurs", erreurs);
        request.getRequestDispatcher(vue).forward(request, response);
    }

    private void renvoyerFormulaireAvecErreur(HttpServletRequest request, HttpServletResponse response,
                                              Map<String, String> valeurs, String message)
            throws ServletException, IOException {
        Map<String, String> erreurs = ValidationUtil.nouvellesErreurs();
        erreurs.put("_global", message);
        renvoyerFormulaire(request, response, valeurs, erreurs, "/WEB-INF/views/clients/form.jsp");
    }
}
