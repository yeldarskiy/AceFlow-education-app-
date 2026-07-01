package kz.aceflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorPageController {

    @GetMapping("/404")
    public String error404(Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("errorKey", "common.error.not.found");
        return "error/error";
    }

    @GetMapping("/403")
    public String error403(Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("errorKey", "common.error.forbidden");
        return "error/error";
    }

    @GetMapping("/500")
    public String error500(Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("errorKey", "common.error.generic");
        return "error/error";
    }
}
