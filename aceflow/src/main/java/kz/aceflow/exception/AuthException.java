package kz.aceflow.exception;

/**
 * Thrown on authentication failures (invalid credentials, duplicate email, etc.).
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
