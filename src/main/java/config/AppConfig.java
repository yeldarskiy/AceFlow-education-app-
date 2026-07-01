package kz.aceflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
@ComponentScan(basePackages = {
        "kz.aceflow.service",
        "kz.aceflow.service.impl",
        "kz.aceflow.dao",
        "kz.aceflow.dao.impl"
})
public class AppConfig {

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource());
        return factory;
    }

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Ties the custom {@link kz.aceflow.util.ConnectionPool} singleton's
     * lifecycle to this Spring application context. Spring calls
     * {@code init()} on startup and {@code destroy()} when the context
     * shuts down (app undeploy/redeploy or Tomcat stop), which closes
     * every pooled JDBC connection so the database link is properly severed.
     *
     * @see ConnectionPoolLifecycle
     */
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public ConnectionPoolLifecycle connectionPoolLifecycle() {
        return new ConnectionPoolLifecycle();
    }
}
