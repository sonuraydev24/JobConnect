package com.jobconnect.service;
import com.jobconnect.repository.UserRepository; import org.springframework.security.core.userdetails.*; import org.springframework.stereotype.Service;
@Service public class CustomUserDetailsService implements UserDetailsService {
 private final UserRepository users; public CustomUserDetailsService(UserRepository u){users=u;}
 public UserDetails loadUserByUsername(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("User not found"));return User.withUsername(u.getEmail()).password(u.getPassword()).authorities(u.getRole().name()).disabled(!u.isEnabled()).build();}
}
