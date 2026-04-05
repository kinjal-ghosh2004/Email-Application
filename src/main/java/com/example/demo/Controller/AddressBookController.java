package com.example.demo.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.AddressBookContact;
import com.example.demo.Model.AddressBookContactRepository;
import com.example.demo.Model.MyAppUser;
import com.example.demo.Model.MyAppUserRepository;

@Controller
@RequestMapping("/address-book")
public class AddressBookController {

    @Autowired
    private MyAppUserRepository userRepository;

    @Autowired
    private AddressBookContactRepository contactRepository;

    @GetMapping
    public String addressBook(Model model, Principal principal) {
        if (principal != null) {
            MyAppUser user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
            model.addAttribute("contacts", contactRepository.findByUserId(user.getId()));
        }
        return "address-book";
    }

    @GetMapping("/add")
    public String addContactForm(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
            model.addAttribute("contact", new AddressBookContact());
        }
        return "contact-form";
    }

    @PostMapping("/save")
    public String saveContact(@ModelAttribute("contact") AddressBookContact contact, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            MyAppUser user = userRepository.findByEmail(principal.getName());
            contact.setUserId(user.getId());
            contactRepository.save(contact);
            redirectAttributes.addFlashAttribute("message", "Contact saved successfully.");
            return "redirect:/email"; // Redirect to email mode as per requirement
        }
        return "redirect:/login";
    }

    @GetMapping("/{id}")
    public String viewContactDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
            AddressBookContact contact = contactRepository.findById(id).orElse(null);
            if (contact != null) {
                model.addAttribute("contact", contact);
                return "contact-details";
            }
        }
        return "redirect:/address-book";
    }

    @GetMapping("/edit/{id}")
    public String editContactForm(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("user", userRepository.findByEmail(principal.getName()));
            AddressBookContact contact = contactRepository.findById(id).orElse(null);
            if (contact != null) {
                model.addAttribute("contact", contact);
                return "contact-form";
            }
        }
        return "redirect:/address-book";
    }

    @PostMapping("/update/{id}")
    public String updateContact(@PathVariable("id") Long id, @ModelAttribute("contact") AddressBookContact updatedContact, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            AddressBookContact existingContact = contactRepository.findById(id).orElse(null);
            if (existingContact != null) {
                existingContact.setName(updatedContact.getName());
                existingContact.setEmail(updatedContact.getEmail());
                existingContact.setContactInformation(updatedContact.getContactInformation());
                contactRepository.save(existingContact);
                redirectAttributes.addFlashAttribute("message", "Contact updated successfully.");
                return "redirect:/email"; // Redirect to email mode as per requirement
            }
        }
        return "redirect:/address-book";
    }

    @PostMapping("/delete/{id}")
    public String deleteContact(@PathVariable("id") Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal != null) {
            contactRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Contact deleted successfully.");
            return "redirect:/email"; // Redirect to email mode as per requirement
        }
        return "redirect:/login";
    }
}
