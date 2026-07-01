package kz.aceflow.controller.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kz.aceflow.exception.AuthException;
import kz.aceflow.exception.DaoException;
import kz.aceflow.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;

/**
 * Catches exceptions that {@link kz.aceflow.controller.GlobalExceptionHandler}
 * cannot see — notably Thymeleaf template failures during view rendering —
 * and forwards them to {@code error/error.html} so the user never sees a
 * raw Tomcat stack trace page.
 */
public class ThymeleafExceptionResolver implements HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(ThymeleafExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        Throwable root = unwrap(ex);

        if (root instanceof ResourceNotFoundException) {
            return errorView(404, "common.error.not.found", root);
        }
        if (root instanceof AuthException) {
            return errorView(401, root.getMessage(), root);
        }
        if (root instanceof SecurityException) {
            return errorView(403, "common.error.forbidden", root);
        }
        if (root instanceof IllegalArgumentException) {
            String key = root.getMessage();
            if (key == null || !key.contains(".")) {
                key = "common.error.generic";
            }
            return errorView(400, key, root);
        }
        if (root instanceof DaoException
                || root instanceof TemplateProcessingException
                || root instanceof TemplateInputException) {
            log.error("Rendering or database error on {}", request.getRequestURI(), root);
            return errorView(500, "common.error.generic", root);
        }

        log.error("Unhandled error on {}", request.getRequestURI(), root);
        return errorView(500, "common.error.generic", root);
    }

    private ModelAndView errorView(int status, String errorKey, Throwable root) {
        if (status >= 500) {
            log.error("Unhandled error [{}]: {}", status, root.getMessage(), root);
        } else {
            log.warn("Handled error [{}]: {}", status, root.getMessage());
        }
        ModelAndView mav = new ModelAndView("error/error");
        mav.addObject("status", status);
        mav.addObject("errorKey", errorKey);
        mav.setStatus(HttpStatus.valueOf(status));
        return mav;
    }

    private static Throwable unwrap(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
