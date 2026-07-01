package kz.aceflow.controller;

import jakarta.servlet.http.HttpSession;
import kz.aceflow.model.User;
import kz.aceflow.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles document listing and uploads. Validation and storage logic
 * live in {@link DocumentService} — this controller never touches a DAO
 * or the filesystem directly.
 */
@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public String listDocuments(@RequestParam(defaultValue = "0") int page,
                                HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        var result = documentService.getDocumentsPage(user.getUserId(), page, 12);

        model.addAttribute("user", user);
        model.addAttribute("documents", result.items());
        model.addAttribute("currentPage", result.currentPage());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("totalItems", result.totalItems());
        return "documents/index";
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("title") String title,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentUser");
        try {
            documentService.uploadDocument(user.getUserId(), file, title);
            redirectAttributes.addFlashAttribute("successKey", "documents.upload.success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorKey", e.getMessage());
        }
        return "redirect:/documents";
    }
}
