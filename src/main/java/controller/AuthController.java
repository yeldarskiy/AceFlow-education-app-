package kz.aceflow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kz.aceflow.exception.AuthException;
import kz.aceflow.model.User;
import kz.aceflow.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles user authentication: login, registration, and logout.
 * Uses POST-Redirect-GET to prevent duplicate form submissions on F5.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final String SESSION_USER_KEY = "currentUser";
    private static final int SESSION_TIMEOUT_SECONDS = 1800;

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ── Login ──────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        if (session.getAttribute(SESSION_USER_KEY) != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginForm form,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.login(form.getEmail(), form.getPassword());
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_USER_KEY, user);
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
            log.info("User logged in: {}", user.getEmail());
            return "redirect:/dashboard";
        } catch (AuthException e) {
            redirectAttributes.addFlashAttribute("errorKey", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    // ── Register ───────────────────────────────────────────────────────

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute RegisterForm form,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerForm", form);
            return "auth/register";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("registerForm", form);
            model.addAttribute("passwordMismatch", true);
            return "auth/register";
        }
        try {
            userService.register(form.getName(), form.getEmail(), form.getPassword());
            redirectAttributes.addFlashAttribute("successKey", "auth.register.success");
            return "redirect:/auth/login";
        } catch (AuthException e) {
            model.addAttribute("registerForm", form);
            model.addAttribute("errorKey", e.getMessage());
            return "auth/register";
        }
    }

    // ── Logout ─────────────────────────────────────────────────────────

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = session.getAttribute(SESSION_USER_KEY) instanceof User u ? u.getEmail() : "unknown";
        session.invalidate();
        log.info("User logged out: {}", email);
        redirectAttributes.addFlashAttribute("successKey", "auth.logout.success");
        return "redirect:/auth/login";
    }

    // ── Inner form DTOs ─────────────────────────────────────────────────

    /** Login form data transfer object. */
    public static class LoginForm {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /** Registration form data transfer object. */
    public static class RegisterForm {
        private String name;
        private String email;
        private String password;
        private String confirmPassword;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}
