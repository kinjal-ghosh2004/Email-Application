package com.example.demo.Model;

//import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyAppUserService implements UserDetailsService {

    @Autowired
    private MyAppUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        MyAppUser user = repository.findByEmail(identifier);
        
        if (user == null) {
            user = repository.findByUsername(identifier);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isVerified())
                .roles("USER")
                .build();
    }
}
