package sn.microbank.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.microbank.model.User;
import sn.microbank.service.AuthService;
import sn.microbank.service.ServiceException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int TENTATIVES_MAX = 5;
    private static final long DUREE_BLOCAGE_MS = 10 * 60 * 1000L;

    private final AuthService authService = new AuthService();
    private final Map<String, FenetreTentatives> tentatives = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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
        String cle = (login == null ? "inconnu" : login.trim().toLowerCase()) + "|" + ipClient(request);

        FenetreTentatives fenetre = tentatives.get(cle);
        if (fenetre != null && fenetre.estBloque()) {
            long secondes = fenetre.secondesRestantes();
            request.setAttribute("erreur", "Trop de tentatives. Réessayez dans "
                    + ((secondes / 60) + 1) + " minute(s).");
            request.setAttribute("loginSaisi", login);
            try {
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        try {
            User user = authService.login(login, motDePasse);
            tentatives.remove(cle);

            HttpSession ancienne = request.getSession(false);
            if (ancienne != null) {
                ancienne.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);

            Flash.success(session, "Bienvenue " + user.getNomComplet() + " !");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (ServiceException e) {
            enregistrerEchec(fenetre, cle);
            request.setAttribute("erreur", e.getMessage());
            request.setAttribute("loginSaisi", login);
            try {
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void enregistrerEchec(FenetreTentatives fenetre, String cle) {
        if (fenetre == null || fenetre.expired()) {
            fenetre = new FenetreTentatives();
            tentatives.put(cle, fenetre);
        }
        fenetre.echec();
    }

    private static String ipClient(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private static final class FenetreTentatives {
        private int compteur;
        private long premierEchec;
        private long blocageJusqua;

        synchronized boolean expired() {
            return System.currentTimeMillis() - premierEchec > DUREE_BLOCAGE_MS;
        }

        synchronized void echec() {
            long maintenant = System.currentTimeMillis();
            if (premierEchec == 0 || maintenant - premierEchec > DUREE_BLOCAGE_MS) {
                premierEchec = maintenant;
                compteur = 0;
            }
            compteur++;
            if (compteur >= TENTATIVES_MAX) {
                blocageJusqua = maintenant + DUREE_BLOCAGE_MS;
            }
        }

        synchronized boolean estBloque() {
            return System.currentTimeMillis() < blocageJusqua;
        }

        synchronized long secondesRestantes() {
            return Math.max(0, (blocageJusqua - System.currentTimeMillis()) / 1000);
        }
    }
}
