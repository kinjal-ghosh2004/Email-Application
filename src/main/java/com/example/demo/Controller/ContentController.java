package com.example.demo.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Model.MyAppUserRepository;

@Controller
public class ContentController {

    @Autowired
    private MyAppUserRepository userRepository;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
        }
        return "home";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
        }
        return "profile";
    }

    
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

}