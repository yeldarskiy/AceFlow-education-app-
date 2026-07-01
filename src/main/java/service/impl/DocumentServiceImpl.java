package kz.aceflow.service.impl;

import kz.aceflow.dao.DocumentDao;
import kz.aceflow.model.Document;
import kz.aceflow.model.PageResult;
import kz.aceflow.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link DocumentService}.
 * Only this class talks to {@link DocumentDao} or touches the filesystem for uploads.
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/aceflow-uploads/";
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private final DocumentDao documentDao;

    @Autowired
    public DocumentServiceImpl(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    @Override
    public Document uploadDocument(int userId, MultipartFile file, String title) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("validation.file.required");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("validation.file.size");
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf(".") + 1).toUpperCase()
            : "TXT";

        if (!ext.matches("PDF|DOCX|TXT")) {
            throw new IllegalArgumentException("validation.file.type");
        }

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path dest = Paths.get(UPLOAD_DIR + fileName);
            file.transferTo(dest.toFile());

            Document doc = new Document();
            doc.setUserId(userId);
            doc.setTitle(title.trim());
            doc.setFileType(ext);
            doc.setFileUrl(UPLOAD_DIR + fileName);
            doc.setFileSize(file.getSize());
            doc.setLanguage("en");

            Document saved = documentDao.save(doc);
            log.info("Document uploaded by user {}: {}", userId, title);
            return saved;
        } catch (IOException e) {
            log.error("Upload failed for user {}", userId, e);
            throw new RuntimeException("Failed to store uploaded file", e);
        }
    }

    @Override
    public PageResult<Document> getDocumentsPage(int userId, int page, int pageSize) {
        int total = countByUserId(userId);
        List<Document> documents = getDocumentsByUserId(userId, page, pageSize);
        return PageResult.of(documents, page, pageSize, total);
    }

    @Override
    public List<Document> getDocumentsByUserId(int userId, int page, int pageSize) {
        return documentDao.findByUserId(userId, page, pageSize);
    }

    @Override
    public int countByUserId(int userId) {
        return documentDao.countByUserId(userId);
    }
}
