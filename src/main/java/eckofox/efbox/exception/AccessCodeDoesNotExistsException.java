package eckofox.efbox.exception;

import java.rmi.NoSuchObjectException;

public class AccessCodeDoesNotExistsException extends Exception {

    public AccessCodeDoesNotExistsException(String s) {
        super(s);
    }
}
