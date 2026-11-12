
package com.jobconnect.repository;

import com.jobconnect.model.AppUser;
import com.jobconnect.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findByRole(Role role);

    List<AppUser> findByEnabled(boolean enabled);

}
