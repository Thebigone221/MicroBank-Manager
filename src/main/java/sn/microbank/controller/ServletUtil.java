package sn.microbank.controller;

import jakarta.servlet.http.HttpServletRequest;

public final class ServletUtil {

    public static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private ServletUtil() {
    }

    public static int page(HttpServletRequest request) {
        return Math.max(0, entier(request.getParameter("page"), 0));
    }

    public static int size(HttpServletRequest request) {
        int size = entier(request.getParameter("size"), DEFAULT_PAGE_SIZE);
        if (size < 1) {
            size = 1;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public static Long id(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? null : Long.valueOf(valeur.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int entier(String valeur, int defaut) {
        try {
            return valeur == null || valeur.isBlank() ? defaut : Integer.parseInt(valeur.trim());
        } catch (NumberFormatException e) {
            return defaut;
        }
    }

    public static String queryString(HttpServletRequest request, String... exclusions) {
        StringBuilder sb = new StringBuilder();
        request.getParameterMap().forEach((nom, valeurs) -> {
            for (String exclusion : exclusions) {
                if (exclusion.equals(nom)) {
                    return;
                }
            }
            for (String valeur : valeurs) {
                if (valeur != null && !valeur.isBlank()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(nom).append('=').append(java.net.URLEncoder.encode(valeur,
                            java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        });
        return sb.toString();
    }
}
