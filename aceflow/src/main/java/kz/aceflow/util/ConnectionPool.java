package kz.aceflow.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread-safe custom JDBC connection pool implemented as a Singleton.
 * <p>
 * Design patterns used:
 * <ul>
 *   <li><b>Singleton</b> — only one pool instance exists per JVM</li>
 *   <li><b>Object Pool</b> — reuses database connections to avoid
 *       the overhead of creating a new connection for each request</li>
 * </ul>
 */
public final class ConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    private static volatile ConnectionPool instance;

    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;
    private static final long ACQUIRE_TIMEOUT_SECONDS = 30;

    /** Private constructor — loads config and fills the pool. */
    private ConnectionPool() {
        Properties props = loadProperties();
        this.url      = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
        this.poolSize = Integer.parseInt(props.getProperty("pool.size", "10"));

        String driver = props.getProperty("db.driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC driver not found: " + driver, e);
        }

        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            pool.offer(createConnection());
        }
        log.info("ConnectionPool initialized: {} connections to {}", poolSize, url);
    }

    /**
     * Returns the singleton instance of the connection pool.
     * Uses double-checked locking for thread safety.
     *
     * @return the singleton {@link ConnectionPool}
     */
    public static ConnectionPool getInstance() {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }

    /**
     * Acquires a connection from the pool, blocking up to 30 seconds.
     *
     * @return a valid {@link Connection}
     * @throws RuntimeException if no connection is available within the timeout
     */
    public Connection getConnection() {
        try {
            Connection conn = pool.poll(ACQUIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (conn == null) {
                throw new RuntimeException("Connection pool exhausted — no connection acquired within timeout");
            }
            if (!conn.isValid(5)) {
                log.warn("Stale connection detected, replacing...");
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for a connection", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate connection", e);
        }
    }

    /**
     * Returns a connection back to the pool.
     * If the connection is closed or invalid, a new one is created to replace it.
     *
     * @param connection the connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connection == null) return;
        try {
            if (connection.isClosed()) {
                pool.offer(createConnection());
            } else {
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
                pool.offer(connection);
            }
        } catch (SQLException e) {
            log.error("Error releasing connection, creating replacement", e);
            pool.offer(createConnection());
        }
    }

    /**
     * Shuts down the pool and closes all connections.
     * Should be called on application shutdown.
     */
    public void shutdown() {
        log.info("Shutting down ConnectionPool...");
        pool.forEach(conn -> {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Error closing connection during shutdown", e);
            }
        });
        pool.clear();
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database connection: " + e.getMessage(), e);
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
        return props;
    }
}
