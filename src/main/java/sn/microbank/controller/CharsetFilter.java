package sn.microbank.controller;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.*;

import java.io.IOException;

/**
 * Force l'encodage UTF-8 de toutes les requêtes (accents dans les formulaires).
 */
@WebFilter(filterName = "CharsetFilter", urlPatterns = "/*")
public class CharsetFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
