package kz.aceflow.dao;

import kz.aceflow.dao.impl.DocumentDaoImpl;
import kz.aceflow.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentDao Integration Tests")
class DocumentDaoTest extends AbstractDaoTest {

    private final DocumentDaoImpl dao = new DocumentDaoImpl();

    @Test
    @DisplayName("save and findByUserId: paginated list")
    void save_shouldPersistAndPaginate() {
        Document doc = new Document();
        doc.setUserId(1);
        doc.setTitle("Economics Notes");
        doc.setFileType("PDF");
        doc.setFileUrl("/tmp/test.pdf");
        doc.setFileSize(1024);
        doc.setLanguage("en");

        Document saved = dao.save(doc);
        assertTrue(saved.getDocId() > 0);

        List<Document> page = dao.findByUserId(1, 0, 10);
        assertTrue(page.stream().anyMatch(d -> "Economics Notes".equals(d.getTitle())));
        assertEquals(1, dao.countByUserId(1));
    }

    @Test
    @DisplayName("findById: returns saved document")
    void findById_shouldReturnDocument() {
        Document doc = new Document();
        doc.setUserId(1);
        doc.setTitle("History");
        doc.setFileType("TXT");
        doc.setFileUrl("/tmp/h.txt");
        doc.setFileSize(100);
        doc.setLanguage("en");
        dao.save(doc);

        Optional<Document> found = dao.findById(doc.getDocId());
        assertTrue(found.isPresent());
        assertEquals("History", found.get().getTitle());
    }

    @Test
    @DisplayName("deleteById: removes document")
    void deleteById_shouldRemove() {
        Document doc = new Document();
        doc.setUserId(1);
        doc.setTitle("Temp Doc");
        doc.setFileType("PDF");
        doc.setFileUrl("/tmp/t.pdf");
        doc.setFileSize(50);
        doc.setLanguage("en");
        dao.save(doc);
        int id = doc.getDocId();

        assertTrue(dao.deleteById(id));
        assertTrue(dao.findById(id).isEmpty());
    }
}
