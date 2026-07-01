package kz.aceflow.service;

import kz.aceflow.dao.DocumentDao;
import kz.aceflow.model.Document;
import kz.aceflow.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService Tests")
class DocumentServiceTest {

    @Mock private DocumentDao documentDao;
    @InjectMocks private DocumentServiceImpl documentService;

    @Test
    @DisplayName("getDocumentsByUserId: delegates to DAO with pagination")
    void getDocumentsByUserId_shouldDelegate() {
        when(documentDao.findByUserId(1, 0, 12)).thenReturn(List.of(new Document()));
        assertEquals(1, documentService.getDocumentsByUserId(1, 0, 12).size());
        verify(documentDao).findByUserId(1, 0, 12);
    }

    @Test
    @DisplayName("countByUserId: returns DAO count")
    void countByUserId_shouldReturnCount() {
        when(documentDao.countByUserId(1)).thenReturn(5);
        assertEquals(5, documentService.countByUserId(1));
    }

    @Test
    @DisplayName("uploadDocument: throws when file empty")
    void uploadDocument_shouldThrow_whenEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", new byte[0]);
        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadDocument(1, file, "Title"));
        verify(documentDao, never()).save(any());
    }

    @Test
    @DisplayName("uploadDocument: throws when file too large")
    void uploadDocument_shouldThrow_whenTooLarge() {
        byte[] big = new byte[51 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", big);
        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadDocument(1, file, "Title"));
    }

    @Test
    @DisplayName("uploadDocument: throws when invalid type")
    void uploadDocument_shouldThrow_whenInvalidType() {
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", "data".getBytes());
        assertThrows(IllegalArgumentException.class,
                () -> documentService.uploadDocument(1, file, "Title"));
    }

    @Test
    @DisplayName("uploadDocument: saves valid PDF")
    void uploadDocument_shouldSave_validPdf() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.pdf", "application/pdf", "pdf".getBytes());
        when(documentDao.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setDocId(1);
            return d;
        });

        Document result = documentService.uploadDocument(1, file, "My Notes");

        assertEquals("My Notes", result.getTitle());
        assertEquals("PDF", result.getFileType());
        verify(documentDao).save(any());
    }
}
