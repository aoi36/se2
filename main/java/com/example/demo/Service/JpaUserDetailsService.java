package com.example.demo.Service;

import com.example.demo.User.MyUserDetail;

import com.example.demo.model.Administrator;
import com.example.demo.repository.AdminRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Administrator> admin = adminRepo.findByUsername(username);
        if (admin.isPresent()) {
            return new MyUserDetail(admin.orElse(null));
        } else {
            throw new UsernameNotFoundException("Admin not found: " + username);
        }
    }

    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Optional<Administrator> admin = adminRepo.findById(id);
        if (admin.isPresent()) {
            return new MyUserDetail(admin.get());
        } else {
            throw new UsernameNotFoundException("Admin not found: " + id);
        }
    }

    public Administrator findAdminById(Long id) {
        return adminRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found with id: " + id));
    }
}
