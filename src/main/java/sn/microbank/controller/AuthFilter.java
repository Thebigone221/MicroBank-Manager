package sn.microbank.controller;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import sn.microbank.model.Role;
import sn.microbank.model.User;

import java.io.IOException;

@WebFilter(filterName = "AuthFilter", urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String chemin = request.getRequestURI().substring(request.getContextPath().length());

        if (chemin.equals("/login") || chemin.equals("/login.jsp")
                || chemin.startsWith("/assets/") || chemin.equals("/favicon.ico")
                || chemin.equals("/index.jsp")) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {

            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (chemin.startsWith("/users") || chemin.startsWith("/documents/users")) {

            if (user.getRole() != Role.ADMIN) {
                Flash.error(session, "Accès refusé : fonctionnalité réservée à l'administrateur.");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
        }

        chain.doFilter(req, resp);
    }
}
