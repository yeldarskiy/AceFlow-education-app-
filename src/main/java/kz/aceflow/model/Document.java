package kz.aceflow.model;

import java.time.LocalDateTime;

/**
 * Represents an uploaded study document (PDF, DOCX, TXT).
 */
public class Document {

    private int docId;
    private int userId;
    private String title;
    private String fileType;
    private String fileUrl;
    private long fileSize;
    private String language;
    private LocalDateTime uploadedAt;

    // Derived counts (not stored in this table)
    private int testCount;
    private int cardCount;

    public Document() {}

    public int getDocId() { return docId; }
    public void setDocId(int docId) { this.docId = docId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public int getTestCount() { return testCount; }
    public void setTestCount(int testCount) { this.testCount = testCount; }

    public int getCardCount() { return cardCount; }
    public void setCardCount(int cardCount) { this.cardCount = cardCount; }

    /** Returns human-readable file size (e.g., "4.2 MB"). */
    public String getFileSizeFormatted() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}
