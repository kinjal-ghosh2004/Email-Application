package com.example.demo.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.EmailMessage;
import com.example.demo.Model.EmailMessageRepository;
import com.example.demo.Model.MyAppUser;
import com.example.demo.Model.MyAppUserRepository;

@Controller
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private MyAppUserRepository userRepository;

    @Autowired
    private EmailMessageRepository emailRepository;

    @GetMapping
    public String emailMode(Model model, Principal principal, @RequestParam(value = "search", required = false) String search) {
        if (principal != null) {
            MyAppUser user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
            
            List<EmailMessage> allInbox = emailRepository.findByReceiverEmailOrderBySentAtDesc(user.getEmail());
            List<EmailMessage> emails;
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                emails = allInbox.stream()
                        .filter(e -> (e.getSenderEmail() != null && e.getSenderEmail().toLowerCase().contains(searchLower)) || 
                                     (e.getSubject() != null && e.getSubject().toLowerCase().contains(searchLower)))
                        .toList();
            } else {
                emails = allInbox;
            }

            model.addAttribute("inboxEmails", emails);
            model.addAttribute("searchQuery", search);
        }
        return "email-list";
    }

    @GetMapping("/compose")
    public String composeEmail(@RequestParam(value = "to", required = false) String to, Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
            EmailMessage message = new EmailMessage();
            if (to != null) {
                message.setReceiverEmail(to);
            }
            model.addAttribute("email", message);
        }
        return "email-compose";
    }

    @PostMapping("/send")
    public String sendEmail(@ModelAttribute("email") EmailMessage email, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            MyAppUser user = userRepository.findByEmail(principal.getName());
            email.setSenderId(user.getId());
            email.setSenderEmail(user.getEmail());
            email.setSentAt(LocalDateTime.now());
            email.setRead(false);
            email.setDraft(false);

            emailRepository.save(email);
            redirectAttributes.addFlashAttribute("message", "Email sent successfully.");
            return "redirect:/email";
        }
        return "redirect:/login";
    }

    @GetMapping("/view/{id}")
    public String viewEmail(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal != null) {
            MyAppUser user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);

            EmailMessage currentEmail = emailRepository.findById(id).orElse(null);
            if (currentEmail != null && (currentEmail.getReceiverEmail().equals(user.getEmail()) || currentEmail.getSenderEmail().equals(user.getEmail()))) {
                
                // Mark as read if user is receiver
                if (currentEmail.getReceiverEmail().equals(user.getEmail()) && !currentEmail.isRead()) {
                    currentEmail.setRead(true);
                    emailRepository.save(currentEmail);
                }

                // Get conversation trail
                List<EmailMessage> trail = emailRepository.findConversation(user.getEmail(), 
                    currentEmail.getSenderEmail().equals(user.getEmail()) ? currentEmail.getReceiverEmail() : currentEmail.getSenderEmail());

                model.addAttribute("currentEmail", currentEmail);
                model.addAttribute("trail", trail);

                // Prepare reply object
                EmailMessage reply = new EmailMessage();
                reply.setReceiverEmail(currentEmail.getSenderEmail().equals(user.getEmail()) ? currentEmail.getReceiverEmail() : currentEmail.getSenderEmail());
                reply.setSubject("Re: " + currentEmail.getSubject());
                reply.setParentEmailId(currentEmail.getId());
                model.addAttribute("reply", reply);

                return "email-view";
            }
        }
        return "redirect:/email";
    }
}
