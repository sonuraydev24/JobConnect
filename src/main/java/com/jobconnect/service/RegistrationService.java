
package com.jobconnect.service;

import com.jobconnect.model.AppUser;
import com.jobconnect.model.Role;
import com.jobconnect.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public RegistrationService(
            UserRepository users,
            PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // Existing controller compatibility
    public void register(
            String name,
            String email,
            String raw,
            String type) {

        register(name, email, raw, type, "", "", "");
    }

    // New registration method
    public void register(
            String name,
            String email,
            String raw,
            String type,
            String phone,
            String gender,
            String qualification) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (raw == null || raw.length() < 8 || raw.length() > 72) {
            throw new IllegalArgumentException(
                "Password must be 8 to 72 characters."
            );
        }

        String cleanEmail = email.trim().toLowerCase();

        if (users.existsByEmailIgnoreCase(cleanEmail)) {
            throw new IllegalArgumentException(
                "Email already registered."
            );
        }

        if (phone != null && !phone.isBlank()
                && !phone.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                "Enter a valid 10-digit mobile number."
            );
        }

        Role role = switch (type) {
            case "RECRUITER" -> Role.ROLE_RECRUITER;
            case "JOB_SEEKER" -> Role.ROLE_JOB_SEEKER;
            default -> throw new IllegalArgumentException(
                "Select a valid account type."
            );
        };

        AppUser user = new AppUser(
            name.trim(),
            cleanEmail,
            encoder.encode(raw),
            role
        );

        user.setPhone(phone);
        user.setGender(gender);
        user.setQualification(qualification);

        users.save(user);
    }
}
