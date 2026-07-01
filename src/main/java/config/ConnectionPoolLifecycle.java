package kz.aceflow.config;

import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin lifecycle wrapper around the {@link ConnectionPool} singleton.
 * <p>
 * The pool itself is a manually-managed singleton (per the no-ORM / custom-pool
 * requirement), so Spring cannot construct or own it directly via {@code @Component}.
 * This bean exists purely so Spring's container lifecycle can call
 * {@link ConnectionPool#shutdown()} automatically when the web application
 * context is destroyed — i.e. when Tomcat undeploys or stops this app —
 * without needing a raw JVM shutdown hook.
 * <p>
 * A JVM shutdown hook ({@code Runtime.getRuntime().addShutdownHook(...)}) would
 * only fire on full JVM exit and would be skipped entirely on a hot redeploy
 * (e.g. dropping a new WAR while Tomcat keeps running), leaking the pool's
 * connections until the whole Tomcat process is killed. Registering this as a
 * Spring bean with a {@code destroy-method} ties cleanup to the actual
 * application context lifecycle, which fires correctly in both cases.
 */
public class ConnectionPoolLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolLifecycle.class);

    /**
     * Invoked by Spring as this bean's init-method when the context starts.
     * Eagerly triggers pool creation so connection problems surface at
     * startup rather than on the first user request.
     */
    public void init() {
        ConnectionPool.getInstance();
        log.info("ConnectionPoolLifecycle initialized — pool is ready");
    }

    /**
     * Invoked by Spring as this bean's destroy-method when the application
     * context is closed (context refresh failure, undeploy, or Tomcat stop).
     * Closes every pooled JDBC connection so the database link is cleanly
     * severed instead of leaking until the JVM exits.
     */
    public void destroy() {
        log.info("Application context closing — shutting down ConnectionPool");
        ConnectionPool.getInstance().shutdown();
    }
}
