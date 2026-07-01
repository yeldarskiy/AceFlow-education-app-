package kz.aceflow.controller;

import kz.aceflow.exception.AuthException;
import kz.aceflow.exception.DaoException;
import kz.aceflow.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Global exception handler — maps exceptions thrown during controller/service
 * execution to user-friendly {@code error/error.html} pages.
 * Never exposes stack traces or internal details to the user.
 * <p>
 * Thymeleaf rendering failures are additionally handled by
 * {@link kz.aceflow.controller.support.ThymeleafExceptionResolver}.
 */
@ControllerAdvice(basePackages = "kz.aceflow.controller")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        return populate(model, 404, "common.error.not.found");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthException.class)
    public String handleAuth(AuthException ex, Model model) {
        log.warn("Authentication failed: {}", ex.getMessage());
        String key = ex.getMessage() != null ? ex.getMessage() : "auth.login.error";
        return populate(model, 401, key);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    public String handleForbidden(SecurityException ex, Model model) {
        log.warn("Forbidden access: {}", ex.getMessage());
        return populate(model, 403, "common.error.forbidden");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidation(IllegalArgumentException ex, Model model) {
        log.warn("Validation failed: {}", ex.getMessage());
        String key = ex.getMessage();
        if (key == null || !key.contains(".")) {
            key = "common.error.generic";
        }
        return populate(model, 400, key);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DaoException.class)
    public String handleDao(DaoException ex, Model model) {
        log.error("Database error", ex);
        return populate(model, 500, "common.error.generic");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({TemplateProcessingException.class, TemplateInputException.class})
    public String handleTemplate(Exception ex, Model model) {
        log.error("Template rendering error", ex);
        return populate(model, 500, "common.error.generic");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        return populate(model, 500, "common.error.generic");
    }

    private String populate(Model model, int status, String errorKey) {
        model.addAttribute("errorKey", errorKey);
        model.addAttribute("status", status);
        return "error/error";
    }
}
