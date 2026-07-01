package kz.aceflow.service;

import kz.aceflow.model.Document;
import kz.aceflow.model.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Business logic for uploading and listing study documents.
 */
public interface DocumentService {

    Document uploadDocument(int userId, MultipartFile file, String title);

    PageResult<Document> getDocumentsPage(int userId, int page, int pageSize);

    List<Document> getDocumentsByUserId(int userId, int page, int pageSize);

    int countByUserId(int userId);
}
