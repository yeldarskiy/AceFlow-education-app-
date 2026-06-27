package kz.aceflow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Set;

/**
 * Intercepts requests to switch the active locale when ?lang=XX is provided.
 * Supports: en, ru, kz.
 */
@Component
public class LocaleInterceptor implements HandlerInterceptor {

    private static final Set<String> SUPPORTED_LOCALES = Set.of("en", "ru", "kz");

    @Autowired
    private LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String lang = request.getParameter("lang");
        if (lang != null && SUPPORTED_LOCALES.contains(lang.toLowerCase())) {
            localeResolver.setLocale(request, response, Locale.forLanguageTag(lang));
        }
        return true;
    }
}
