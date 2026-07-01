package kz.aceflow.controller;

import kz.aceflow.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler — maps exceptions to user-friendly error pages.
 * Never exposes stack traces or internal details to the user.
 * <p>
 * Every handler here sets {@code errorKey} to a valid i18n message code before
 * returning the view; {@code error/error.html} resolves it with
 * {@code th:text="#{${errorKey}}"}. Keep that contract — Thymeleaf message
 * expressions ({@code #{...}}) do not support the Elvis operator
 * ({@code ?:}) inside them, so a fallback must never be attempted in the view.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorKey", "common.error.not.found");
        model.addAttribute("status", 404);
        return "error/error";
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    public String handleForbidden(SecurityException ex, Model model) {
        log.warn("Forbidden access: {}", ex.getMessage());
        model.addAttribute("errorKey", "common.error.forbidden");
        model.addAttribute("status", 403);
        return "error/error";
    }

    /**
     * Catches validation failures thrown by the service layer (e.g.
     * {@code DocumentService.uploadDocument}) that escape a controller
     * without being handled inline. The exception message is expected to
     * already be an i18n key (see {@code DocumentServiceImpl}).
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleValidation(IllegalArgumentException ex, Model model) {
        log.warn("Validation failed: {}", ex.getMessage());
        model.addAttribute("errorKey", "common.error.generic");
        model.addAttribute("status", 400);
        return "error/error";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("errorKey", "common.error.generic");
        model.addAttribute("status", 500);
        return "error/error";
    }
}
