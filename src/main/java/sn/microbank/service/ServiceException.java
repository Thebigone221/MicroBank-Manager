package sn.microbank.service;

public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }
}
