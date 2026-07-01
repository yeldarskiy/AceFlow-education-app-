package kz.aceflow.dao;

import kz.aceflow.util.ConnectionPool;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Base class for DAO integration tests using an in-memory H2 database.
 */
public abstract class AbstractDaoTest {

    @BeforeAll
    static void initPool() {
        ConnectionPool.resetForTests();
        runSqlScript("db/schema-h2.sql");
    }

    private static void runSqlScript(String resourcePath) {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection conn = pool.getConnection();
        try (Statement stmt = conn.createStatement()) {
            String sql = loadResource(resourcePath);
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    stmt.execute(trimmed);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to run SQL script: " + resourcePath, e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private static String loadResource(String path) {
        try (InputStream in = AbstractDaoTest.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
