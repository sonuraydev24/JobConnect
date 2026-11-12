
package com.jobconnect.controller;

import com.jobconnect.model.AppUser;
import com.jobconnect.model.Role;
import com.jobconnect.model.JobApplication;

import com.jobconnect.repository.UserRepository;
import com.jobconnect.repository.ApplicationRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
public class ResumeController {

    private final UserRepository users;
    private final ApplicationRepository apps;

    private final Path uploadDirectory =
            Paths.get("uploads", "resumes")
                    .toAbsolutePath().normalize();

    public ResumeController(
            UserRepository users,
            ApplicationRepository apps) {

        this.users = users;
        this.apps = apps;
    }

    // Upload candidate resume
    @PostMapping("/seeker/resume/upload")
    public String uploadResume(
            Authentication auth,
            @RequestParam("resume") MultipartFile file,
            RedirectAttributes redirect) {

        try {
            AppUser user = users
                    .findByEmailIgnoreCase(auth.getName())
                    .orElseThrow();

            if (user.getRole() != Role.ROLE_JOB_SEEKER) {
                redirect.addFlashAttribute(
                        "error", "Only job seekers can upload resumes."
                );
                return "redirect:/seeker/profile";
            }

            if (file.isEmpty()
                    || file.getOriginalFilename() == null
                    || !file.getOriginalFilename()
                    .toLowerCase().endsWith(".pdf")
                    || !"application/pdf".equalsIgnoreCase(
                            file.getContentType())) {

                redirect.addFlashAttribute(
                        "error", "Please upload a valid PDF file."
                );
                return "redirect:/seeker/profile";
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                redirect.addFlashAttribute(
                        "error", "Resume size must be below 5 MB."
                );
                return "redirect:/seeker/profile";
            }

            Files.createDirectories(uploadDirectory);

            String storedName = UUID.randomUUID() + ".pdf";

            Path destination = uploadDirectory
                    .resolve(storedName).normalize();

            if (!destination.startsWith(uploadDirectory)) {
                throw new IOException("Invalid file path.");
            }

            file.transferTo(destination);

            user.setResumeName(
                    Paths.get(file.getOriginalFilename())
                            .getFileName().toString()
            );

            user.setResumePath(destination.toString());

            users.save(user);

            redirect.addFlashAttribute(
                    "success", "Resume uploaded successfully!"
            );

        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error", "Resume upload failed. Please try again."
            );
        }

        return "redirect:/seeker/profile";
    }

    // Recruiter downloads a candidate's resume
    @GetMapping("/recruiter/applicants/{candidateId}/resume")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long candidateId,
            Authentication auth) {

        AppUser recruiter = users
                .findByEmailIgnoreCase(auth.getName())
                .orElseThrow();

        if (recruiter.getRole() != Role.ROLE_RECRUITER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN
            );
        }

        boolean appliedToRecruiterJob =
                apps.findByJobRecruiterOrderByAppliedAtDesc(recruiter)
                        .stream()
                        .map(JobApplication::getApplicant)
                        .anyMatch(candidate ->
                                candidate.getId().equals(candidateId));

        if (!appliedToRecruiterJob) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this resume."
            );
        }

        AppUser candidate = users.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND
                ));

        if (candidate.getResumePath() == null
                || candidate.getResumePath().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Resume not uploaded."
            );
        }

        try {
            Path resumePath = Paths.get(candidate.getResumePath())
                    .toAbsolutePath().normalize();

            if (!resumePath.startsWith(uploadDirectory)
                    || !Files.isRegularFile(resumePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resume file not found."
                );
            }

            Resource resource = new FileSystemResource(resumePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"candidate-resume.pdf\""
                    )
                    .body(resource);

        } catch (InvalidPathException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );
        }
    }
}
