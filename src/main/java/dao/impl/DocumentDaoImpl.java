package kz.aceflow.dao.impl;

import kz.aceflow.dao.DocumentDao;
import kz.aceflow.exception.DaoException;
import kz.aceflow.model.Document;
import kz.aceflow.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DocumentDaoImpl implements DocumentDao {

    private static final Logger log = LoggerFactory.getLogger(DocumentDaoImpl.class);
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SQL_SAVE =
            "INSERT INTO documents (user_id, title, file_type, file_url, file_size, language) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT doc_id, user_id, title, file_type, file_url, file_size, language, uploaded_at " +
                    "FROM documents WHERE doc_id = ?";

    private static final String SQL_FIND_BY_USER =
            "SELECT doc_id, user_id, title, file_type, file_url, file_size, language, uploaded_at " +
                    "FROM documents WHERE user_id = ? ORDER BY uploaded_at DESC LIMIT ? OFFSET ?";

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM documents WHERE user_id = ?";

    private static final String SQL_DELETE = "DELETE FROM documents WHERE doc_id = ?";

    @Override
    public Document save(Document doc) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_SAVE, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, doc.getUserId());
            ps.setString(2, doc.getTitle());
            ps.setString(3, doc.getFileType());
            ps.setString(4, doc.getFileUrl());
            ps.setLong(5, doc.getFileSize());
            ps.setString(6, doc.getLanguage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) doc.setDocId(keys.getInt(1));
            }
            return doc;
        } catch (SQLException e) {
            log.error("Failed to save document", e);
            throw new DaoException("Failed to save document", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public Optional<Document> findById(int docId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, docId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find document: {}", docId, e);
            throw new DaoException("Failed to find document", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public List<Document> findByUserId(int userId, int page, int pageSize) {
        List<Document> docs = new ArrayList<>();
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_USER)) {
            ps.setInt(1, userId);
            ps.setInt(2, pageSize);
            ps.setInt(3, page * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) docs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to find documents for user: {}", userId, e);
            throw new DaoException("Failed to find documents", e);
        } finally {
            pool.releaseConnection(conn);
        }
        return docs;
    }

    @Override
    public int countByUserId(int userId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_COUNT)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            log.error("Failed to count documents", e);
            throw new DaoException("Failed to count documents", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    @Override
    public boolean deleteById(int docId) {
        Connection conn = pool.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, docId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.error("Failed to delete document: {}", docId, e);
            throw new DaoException("Failed to delete document", e);
        } finally {
            pool.releaseConnection(conn);
        }
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Document doc = new Document();
        doc.setDocId(rs.getInt("doc_id"));
        doc.setUserId(rs.getInt("user_id"));
        doc.setTitle(rs.getString("title"));
        doc.setFileType(rs.getString("file_type"));
        doc.setFileUrl(rs.getString("file_url"));
        doc.setFileSize(rs.getLong("file_size"));
        doc.setLanguage(rs.getString("language"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) doc.setUploadedAt(ts.toLocalDateTime());
        return doc;
    }
}