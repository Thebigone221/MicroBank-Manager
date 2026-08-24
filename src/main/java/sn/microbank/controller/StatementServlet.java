package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sn.microbank.service.StatementPdf;
import sn.microbank.service.StatementService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * Génération du relevé d'un compte :
 * - GET /statements/pdf  : téléchargement du relevé PDF
 * - GET /statements/csv  : export CSV de l'historique (filtres conservés)
 * - GET /statements/print : version imprimable HTML (Bonus 5)
 */
@WebServlet(urlPatterns = {"/statements/pdf", "/statements/csv", "/statements/print"})
public class StatementServlet extends HttpServlet {

    private final StatementService statementService = new StatementService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String chemin = request.getServletPath();
        Long accountId = ServletUtil.id(request.getParameter("accountId"));

        LocalDate du = sn.microbank.util.ValidationUtil.date(request.getParameter("du"));
        LocalDate au = sn.microbank.util.ValidationUtil.date(request.getParameter("au"));

        try {
            StatementService.Releve releve = statementService.construire(accountId, du, au);
            switch (chemin) {
                case "/statements/pdf" -> genererPdf(response, releve);
                case "/statements/csv" -> exporterCsv(response, releve);
                default -> {
                    request.setAttribute("releve", releve);
                    request.setAttribute("dateEdition", java.time.LocalDateTime.now());
                    request.setAttribute("duParam", request.getParameter("du"));
                    request.setAttribute("auParam", request.getParameter("au"));
                    request.getRequestDispatcher("/WEB-INF/views/statements/print.jsp")
                            .forward(request, response);
                }
            }
        } catch (sn.microbank.service.ServiceException e) {
            Flash.error(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/accounts");
        }
    }

    private void genererPdf(HttpServletResponse response, StatementService.Releve releve)
            throws IOException {
        response.reset();
        response.setContentType("application/pdf");
        String nomFichier = "releve-" + releve.getCompte().getNumeroCompte() + ".pdf";
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + nomFichier + "\"");
        new StatementPdf().ecrire(releve, response.getOutputStream());
    }

    private void exporterCsv(HttpServletResponse response, StatementService.Releve releve)
            throws IOException {
        String contenu = statementService.exporterCsv(releve);

        response.reset();
        response.setContentType("text/csv; charset=UTF-8");
        String nomFichier = "operations-" + releve.getCompte().getNumeroCompte() + ".csv";
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(nomFichier, StandardCharsets.UTF_8) + "\"");

        // BOM UTF-8 pour que Excel reconnaisse l'encodage.
        var sortie = response.getWriter();
        sortie.write('\uFEFF');
        sortie.write(contenu);
        sortie.flush();
    }
}
