package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.microbank.model.User;
import sn.microbank.service.AuthService;

import java.io.IOException;

/**
 * Authentification : affiche le formulaire (GET) et connecte l'utilisateur (POST).
 * La connexion s'appuie sur HttpSession comme demandé dans le sujet :
 * session.setAttribute("user", user);
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Déjà connecté -> tableau de bord directement.
        if (request.getSession(false) != null && request.getSession(false).getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String login = request.getParameter("login");
        String motDePasse = request.getParameter("motDePasse");

        try {
            User user = authService.login(login, motDePasse);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            Flash.success(session, "Bienvenue " + user.getNomComplet() + " !");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (sn.microbank.service.ServiceException e) {
            request.setAttribute("erreur", e.getMessage());
            request.setAttribute("loginSaisi", login);
            try {
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } catch (ServletException | IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
