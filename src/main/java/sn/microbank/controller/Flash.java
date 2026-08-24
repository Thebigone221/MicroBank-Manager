package sn.microbank.controller;

import jakarta.servlet.http.HttpSession;

/**
 * Messages flash stockés en session : affichés une fois puis supprimés.
 */
public final class Flash {

    public static final String SUCCESS = "flashSuccess";
    public static final String ERROR = "flashError";

    private Flash() {
    }

    public static void success(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(SUCCESS, message);
        }
    }

    public static void error(HttpSession session, String message) {
        if (session != null) {
            session.setAttribute(ERROR, message);
        }
    }
}
