package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Handles document listing and uploads. Validation and storage logic
 * live in {@link DocumentService} — this controller never touches a DAO
 * or the filesystem directly.
 */
@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final MessageSource messageSource;

    @Autowired
    public DocumentController(DocumentService documentService, MessageSource messageSource) {
        this.documentService = documentService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String listDocuments(@RequestParam(defaultValue = "0") int page,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        int pageSize = 12;
        int total = documentService.countByUserId(user.getUserId());
        int totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));

        model.addAttribute("user", user);
        model.addAttribute("documents", documentService.getDocumentsByUserId(user.getUserId(), page, pageSize));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", total);
        return "documents/index";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("title") String title,
                                 HttpSession session,
                                 Locale locale,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        try {
            documentService.uploadDocument(user.getUserId(), file, title);
            redirectAttributes.addFlashAttribute("successMessage",
                messageSource.getMessage("documents.upload.success", null, locale));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                messageSource.getMessage(e.getMessage(), null, locale));
        }
        return "redirect:/documents";
    }
}
