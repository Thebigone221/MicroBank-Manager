package sn.microbank.controller;

import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.*;

import java.io.IOException;

@WebFilter(filterName = "CharsetFilter", urlPatterns = "/*")
public class CharsetFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (response instanceof jakarta.servlet.http.HttpServletResponse rep) {
            rep.setHeader("X-Content-Type-Options", "nosniff");
            rep.setHeader("X-Frame-Options", "DENY");
            rep.setHeader("Referrer-Policy", "no-referrer");
            rep.setHeader("Content-Security-Policy",
                    "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'");
        }
        chain.doFilter(request, response);
    }
}
