package kz.aceflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Root Spring application context configuration.
 * Scans all components except controllers (handled by WebConfig).
 */
@Configuration
@ComponentScan(basePackages = {
    "kz.aceflow.service",
    "kz.aceflow.dao"
})
public class AppConfig {

    /**
     * Message source for i18n — supports EN, RU, KZ locales.
     * Files: messages_en.properties, messages_ru.properties, messages_kz.properties
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    /**
     * Bean Validation factory wired to the Spring message source
     * so validation messages respect the current locale.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource());
        return factory;
    }

    /**
     * Multipart resolver for file uploads (PDF, DOCX, TXT).
     * Uses Jakarta Servlet 6 StandardServletMultipartResolver.
     */
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
