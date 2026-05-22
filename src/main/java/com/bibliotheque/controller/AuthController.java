package com.bibliotheque.controller;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        if (session.getAttribute("user_id") != null) {
            String role = (String) session.getAttribute("user_role");
            return "admin".equals(role) ? "redirect:/admin/dashboard" : "redirect:/member/dashboard";
        }
        
        model.addAttribute("action", "login");
        return "auth";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email, @RequestParam String password, 
                          HttpSession session, RedirectAttributes redirectAttributes) {
        Utilisateur user = authService.login(email, password);
        if (user != null) {
            session.setAttribute("user_id", user.getId());
            session.setAttribute("user_role", user.getRole());
            session.setAttribute("user_name", user.getPrenom() + " " + user.getNom());
            return "admin".equals(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/member/dashboard";
        }
        
        redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect ou compte inactif.");
        return "redirect:/auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        if (session.getAttribute("user_id") != null) {
            String role = (String) session.getAttribute("user_role");
            return "admin".equals(role) ? "redirect:/admin/dashboard" : "redirect:/member/dashboard";
        }
        
        model.addAttribute("action", "register");
        return "auth";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute Utilisateur user, @RequestParam String password, 
                             @RequestParam String password_confirm, RedirectAttributes redirectAttributes) {
        if (!password.equals(password_confirm) || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas ou sont trop courts (6 min).");
            return "redirect:/auth/register";
        }

        try {
            authService.register(user, password);
            redirectAttributes.addFlashAttribute("success_msg", "Inscription réussie ! Connectez-vous.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/logout")
    public String doLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
