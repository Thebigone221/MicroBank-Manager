package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.model.Client;
import sn.microbank.service.ClientService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Sert la copie de la pièce d'identité uploadée pour un client (Bonus 1).
 * GET /documents/client/{id}
 */
@WebServlet("/documents/*")
public class DocumentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String info = request.getPathInfo(); // /client/{id}
        if (info == null || !info.startsWith("/client/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Long id = ServletUtil.id(info.substring("/client/".length()));
        Client client = id == null ? null : new ClientService().findById(id);

        if (client == null || client.getPieceIdentite() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path fichier = ClientServlet.REPERTOIRE_UPLOADS.resolve(client.getPieceIdentite());
        if (!Files.exists(fichier)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(fichier.toString().endsWith(".pdf")
                ? "application/pdf" : "image/png");
        response.setHeader("Content-Disposition", "inline");
        Files.copy(fichier, response.getOutputStream());
    }
}
