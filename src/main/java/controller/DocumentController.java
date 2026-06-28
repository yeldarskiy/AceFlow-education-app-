package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.dao.DocumentDao;
import kz.aceflow.model.Document;
import kz.aceflow.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/aceflow-uploads/";
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private final DocumentDao documentDao;

    @Autowired
    public DocumentController(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    @GetMapping
    public String listDocuments(@RequestParam(defaultValue = "0") int page,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        List<Document> documents = documentDao.findByUserId(user.getUserId(), page, 12);
        model.addAttribute("user", user);
        model.addAttribute("documents", documents);
        return "documents/index";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("title") String title,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file");
            return "redirect:/documents";
        }

        if (file.getSize() > MAX_SIZE) {
            redirectAttributes.addFlashAttribute("errorMessage", "File too large. Max 50 MB");
            return "redirect:/documents";
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toUpperCase()
                : "TXT";

        if (!ext.matches("PDF|DOCX|TXT")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only PDF, DOCX, TXT allowed");
            return "redirect:/documents";
        }

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path dest = Paths.get(UPLOAD_DIR + fileName);
            file.transferTo(dest.toFile());

            Document doc = new Document();
            doc.setUserId(user.getUserId());
            doc.setTitle(title.trim());
            doc.setFileType(ext);
            doc.setFileUrl(UPLOAD_DIR + fileName);
            doc.setFileSize(file.getSize());
            doc.setLanguage("en");
            documentDao.save(doc);

            redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully!");
            log.info("Document uploaded by user {}: {}", user.getUserId(), title);
        } catch (IOException e) {
            log.error("Upload failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed. Please try again.");
        }

        return "redirect:/documents";
    }
}