package sn.microbank.service;

/**
 * Exception métier : son message est affiché directement à l'utilisateur
 * dans les JSP (bandeau d'erreur).
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }
}
