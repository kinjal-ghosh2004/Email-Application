package com.example.demo.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.MyAppUser;
import com.example.demo.Model.MyAppUserRepository;

@Controller
public class ProfileController {

    @Autowired
    private MyAppUserRepository userRepository;

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/uploads";

    @PostMapping("/profile/update")
    public String updateProfile(
            Principal principal,
            @RequestParam("address") String address,
            @RequestParam("dateOfBirth") String dateOfBirth,
            @RequestParam("preferences") String preferences,
            @RequestParam(value = "idProof", required = false) MultipartFile idProof,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        MyAppUser user = userRepository.findByEmail(principal.getName());

        boolean needsKyc = false;

        // Check if sensitive fields changed
        if (!address.equals(user.getAddress()) ||
                (dateOfBirth != null && !dateOfBirth.isEmpty()
                        && !LocalDate.parse(dateOfBirth).equals(user.getDateOfBirth()))) {
            needsKyc = true;
        }

        user.setAddress(address);
        if (dateOfBirth != null && !dateOfBirth.isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dateOfBirth));
        }
        user.setPreferences(preferences);

        if (idProof != null && !idProof.isEmpty()) {
            String contentType = idProof.getContentType();
            String originalFilename = idProof.getOriginalFilename();
            boolean isValidType = false;

            if (contentType != null) {
                if (contentType.equals("application/pdf") || contentType.startsWith("image/")) {
                    isValidType = true;
                }
            }

            if (!isValidType) {
                redirectAttributes.addFlashAttribute("error", "Invalid ID Proof file format. Only images and PDFs are allowed.");
                return "redirect:/profile";
            }
            try {
                Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String safeFileName = java.util.UUID.randomUUID().toString() + extension;
                Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, safeFileName);
                Files.write(fileNameAndPath, idProof.getBytes());
                user.setIdProofPath("/uploads/" + safeFileName);
                needsKyc = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (needsKyc) {
            user.setKycStatus("PENDING");
            redirectAttributes.addFlashAttribute("message",
                    "Profile updated. KYC approval is pending due to sensitive changes.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        }

        userRepository.save(user);

        return "redirect:/profile";
    }
}
