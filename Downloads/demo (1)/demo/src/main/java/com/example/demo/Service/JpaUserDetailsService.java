package com.example.demo.Service;

import com.example.demo.User.MyUserDetail;
import com.example.demo.User.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepo;
    @Override
    public UserDetails loadUserByUsername(String username)

            throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent()) {
            return new MyUserDetail(user.orElse(null));
        } else {
            throw new UsernameNotFoundException(
                    "User not found: " + username

            );
        }
    }

    public UserDetails loadUserById(Long id)

            throws UsernameNotFoundException {
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            return new MyUserDetail(user.orElse(null));
        } else {
            throw new UsernameNotFoundException(
                    "id not found: " + id

            );
        }
    }
    public User findUserById(Long id) {
        User user = userRepo.findUserById(id);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
    }
}