package kz.aceflow.exception;

/**
 * Wraps checked SQLExceptions from the DAO layer into an unchecked exception.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
