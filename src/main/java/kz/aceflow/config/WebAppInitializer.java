package kz.aceflow.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Programmatic replacement for web.xml.
 * Registers the Spring DispatcherServlet and configures multipart uploads.
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    private static final long MAX_FILE_SIZE    = 50L * 1024 * 1024; // 50 MB
    private static final long MAX_REQUEST_SIZE = 55L * 1024 * 1024; // 55 MB

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{ AppConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{ WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setMultipartConfig(
            new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"),
                MAX_FILE_SIZE,
                MAX_REQUEST_SIZE,
                (int) (5L * 1024 * 1024) // 5 MB threshold before disk write
            )
        );
    }
}
