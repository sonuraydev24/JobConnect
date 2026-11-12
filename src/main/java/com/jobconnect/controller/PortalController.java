
package com.jobconnect.controller;

import com.jobconnect.model.*;
import com.jobconnect.repository.*;
import com.jobconnect.service.RegistrationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
public class PortalController {

    private final UserRepository users;
    private final JobRepository jobs;
    private final ApplicationRepository apps;
    private final RegistrationService registration;

    public PortalController(
            UserRepository users,
            JobRepository jobs,
            ApplicationRepository apps,
            RegistrationService registration) {

        this.users = users;
        this.jobs = jobs;
        this.apps = apps;
        this.registration = registration;
    }

    private AppUser current(Authentication auth) {
        return users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow();
    }

    // HOME PAGE
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute(
                "jobs",
                jobs.findByActiveTrueOrderByPostedAtDesc()
        );
        return "index";
    }

    // JOB SEARCH FILTER
    @GetMapping("/jobs/search")
    public String searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            Model model) {

        keyword = clean(keyword);
        location = clean(location);
        jobType = clean(jobType);
        experience = clean(experience);

        var results = jobs.searchJobs(
                keyword, location, jobType, experience
        );

        final Double min = minSalary;
        final Double max = maxSalary;

        results = results.stream()
                .filter(job -> {
                    Double salary = job.getSalaryAmount();

                    if (min == null && max == null) {
                        return true;
                    }

                    if (salary == null) {
                        return false;
                    }

                    return (min == null || salary >= min)
                            && (max == null || salary <= max);
                })
                .toList();

        model.addAttribute("jobs", results);
        return "index";
    }

    private String clean(String value) {
        return value == null || value.isBlank()
                ? null : value.trim();
    }

    // JOB DETAILS
    @GetMapping("/jobs/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "job",
                jobs.findById(id).orElseThrow()
        );

        return "job-detail";
    }

    // LOGIN
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // REGISTER
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam String phone,
            @RequestParam String gender,
            @RequestParam(required = false) String qualification,
            Model model) {

        try {
            registration.register(
                    fullName, email, password,
                    role, phone, gender, qualification
            );

            return "redirect:/login?registered";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // DASHBOARD REDIRECTION
    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {

        return switch (current(auth).getRole()) {
            case ROLE_ADMIN -> "redirect:/admin/dashboard";
            case ROLE_RECRUITER -> "redirect:/recruiter/dashboard";
            case ROLE_JOB_SEEKER -> "redirect:/seeker/dashboard";
        };
    }

    // ADMIN DASHBOARD
    @GetMapping("/admin/dashboard")
    public String admin(Model model) {

        model.addAttribute("users", users.findAll());
        model.addAttribute("jobs", jobs.findAll());
        model.addAttribute("userCount", users.count());
        model.addAttribute("jobCount", jobs.count());
        model.addAttribute("appCount", apps.count());

        return "admin-dashboard";
    }

    // ENABLE / DISABLE USER
    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id) {

        AppUser user = users.findById(id).orElseThrow();

        if (user.getRole() != Role.ROLE_ADMIN) {
            user.setEnabled(!user.isEnabled());
            users.save(user);
        }

        return "redirect:/admin/dashboard";
    }

    // VERIFY RECRUITER
    @PostMapping("/admin/recruiters/{id}/verify")
    public String verifyRecruiter(@PathVariable Long id) {

        AppUser user = users.findById(id).orElseThrow();

        if (user.getRole() == Role.ROLE_RECRUITER) {
            user.setVerified(true);
            users.save(user);
        }

        return "redirect:/admin/dashboard";
    }

    // RECRUITER DASHBOARD
    @GetMapping("/recruiter/dashboard")
    public String recruiter(
            Authentication auth,
            Model model) {

        AppUser user = current(auth);

        model.addAttribute(
                "jobs",
                jobs.findByRecruiterOrderByPostedAtDesc(user)
        );

        model.addAttribute(
                "appCount",
                apps.findByJobRecruiterOrderByAppliedAtDesc(user).size()
        );

        return "recruiter-dashboard";
    }

    // NEW JOB FORM
    @GetMapping("/recruiter/jobs/new")
    public String newJob() {
        return "job-form";
    }

    // POST JOB
    @PostMapping("/recruiter/jobs")
    public String postJob(
            Authentication auth,
            @RequestParam String title,
            @RequestParam String company,
            @RequestParam String location,
            @RequestParam String jobType,
            @RequestParam String experience,
            @RequestParam String salary,
            @RequestParam String skills,
            @RequestParam String description) {

        Job job = new Job(
                title, company, location,
                jobType, experience, salary,
                description, skills, current(auth)
        );

        jobs.save(job);

        return "redirect:/recruiter/dashboard";
    }

    // CLOSE / REOPEN JOB
    @PostMapping("/recruiter/jobs/{id}/toggle")
    public String toggleJob(
            @PathVariable Long id,
            Authentication auth) {

        Job job = jobs.findById(id).orElseThrow();

        if (job.getRecruiter().getId()
                .equals(current(auth).getId())) {

            job.setActive(!job.isActive());
            jobs.save(job);
        }

        return "redirect:/recruiter/dashboard";
    }

    // RECRUITER APPLICANTS
    @GetMapping("/recruiter/applicants")
    public String applicants(
            Authentication auth,
            Model model) {

        model.addAttribute(
                "applications",
                apps.findByJobRecruiterOrderByAppliedAtDesc(
                        current(auth)
                )
        );

        return "applicants";
    }

    // UPDATE APPLICATION STATUS
    @PostMapping("/recruiter/applications/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam Status status,
            Authentication auth) {

        JobApplication application =
                apps.findById(id).orElseThrow();

        if (application.getJob().getRecruiter().getId()
                .equals(current(auth).getId())) {

            application.setStatus(status);
            apps.save(application);
        }

        return "redirect:/recruiter/applicants";
    }

    // JOB SEEKER DASHBOARD
    @GetMapping("/seeker/dashboard")
    public String seeker(
            Authentication auth,
            Model model) {

        AppUser user = current(auth);

        model.addAttribute("user", user);

        model.addAttribute(
                "jobs",
                jobs.findByActiveTrueOrderByPostedAtDesc()
        );

        model.addAttribute(
                "applications",
                apps.findByApplicantOrderByAppliedAtDesc(user)
        );

        return "seeker-dashboard";
    }

    // APPLY FOR JOB
    @PostMapping("/seeker/jobs/{id}/apply")
    public String apply(
            @PathVariable Long id,
            Authentication auth) {

        Job job = jobs.findById(id).orElseThrow();
        AppUser user = current(auth);

        if (job.isActive()
                && !apps.existsByJobAndApplicant(job, user)) {

            apps.save(new JobApplication(job, user));
        }

        return "redirect:/seeker/dashboard";
    }

    // OPEN PROFILE
    @GetMapping("/seeker/profile")
    public String seekerProfile(
            Authentication auth,
            Model model) {

        model.addAttribute("user", current(auth));
        return "seeker-profile";
    }

    // UPDATE PROFILE
    @PostMapping("/seeker/profile")
    public String updateSeekerProfile(
            Authentication auth,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String qualification,
            @RequestParam(required = false) String skills,
            Model model) {

        AppUser user = current(auth);

        if (fullName == null || fullName.isBlank()) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Name is required.");
            return "seeker-profile";
        }

        if (phone != null && !phone.isBlank()
                && !phone.matches("\\d{10}")) {

            model.addAttribute("user", user);
            model.addAttribute(
                    "error", "Enter valid 10-digit mobile number."
            );

            return "seeker-profile";
        }

        user.setFullName(fullName.trim());
        user.setPhone(phone);
        user.setGender(gender);
        user.setQualification(qualification);
        user.setSkills(skills);

        users.save(user);

        model.addAttribute("user", user);
        model.addAttribute(
                "success", "Profile updated successfully!"
        );

        return "seeker-profile";
    }
}
