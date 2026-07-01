package kz.aceflow.service;

import kz.aceflow.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Business logic for uploading and listing study documents.
 */
public interface DocumentService {

    /**
     * Validates and stores an uploaded file on disk, then persists its metadata.
     *
     * @param userId the owner of the document
     * @param file   the uploaded multipart file
     * @param title  the user-provided title
     * @return the saved {@link Document}
     * @throws IllegalArgumentException if the file is empty, too large, or has a disallowed extension;
     *                                   the message is an i18n key suitable for direct lookup
     */
    Document uploadDocument(int userId, MultipartFile file, String title);

    /**
     * Returns a page of documents belonging to a user.
     *
     * @param userId   the document owner
     * @param page     zero-based page index
     * @param pageSize number of documents per page
     * @return list of documents for the requested page
     */
    List<Document> getDocumentsByUserId(int userId, int page, int pageSize);
}
