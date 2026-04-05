package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Model.MyAppUser;
import com.example.demo.Model.MyAppUserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.utils.JwtTokenUtil;

@RestController
public class RegistrationController {
    
    @Autowired
    private MyAppUserRepository myAppUserRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    
    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<String> createUser(@Valid @RequestBody com.example.demo.Model.UserRegistrationDto dto, org.springframework.validation.BindingResult bindingResult){
        
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append("\n"));
            return new ResponseEntity<>(errors.toString().trim(), HttpStatus.BAD_REQUEST);
        }
        
        MyAppUser existingAppUser = myAppUserRepository.findByEmail(dto.getEmail());
        
        if(existingAppUser != null){
            if(existingAppUser.isVerified()){
                return new ResponseEntity<>("User Already exist and Verified.",HttpStatus.BAD_REQUEST);
            }else{
                String verificationToken = JwtTokenUtil.generateToken(existingAppUser.getEmail());
                existingAppUser.setVerficationToken(verificationToken);
                myAppUserRepository.save(existingAppUser);
                //Send Email Code
                emailService.sendVerificationEmail(existingAppUser.getEmail(), verificationToken);
                return new ResponseEntity<>("Verification Email resent. Check your inbox",HttpStatus.OK);
            }
        }
        
        MyAppUser user = new MyAppUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        String vericationToken =JwtTokenUtil.generateToken(user.getEmail());
        user.setVerficationToken(vericationToken);
        myAppUserRepository.save(user);
        //Send Email Code
        emailService.sendVerificationEmail(user.getEmail(), vericationToken);
        
        return new ResponseEntity<>("Registration successfull! Please Verify your Email", HttpStatus.OK);
    }
    
}