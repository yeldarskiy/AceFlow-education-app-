package kz.aceflow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Set;

public class LocaleInterceptor implements HandlerInterceptor {

    private static final Set<String> SUPPORTED = Set.of("en", "ru", "kz");
    private final LocaleResolver localeResolver;

    public LocaleInterceptor(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String lang = request.getParameter("lang");
        if (lang != null && SUPPORTED.contains(lang.toLowerCase())) {
            localeResolver.setLocale(request, response, Locale.forLanguageTag(lang));
        }
        return true;
    }
}