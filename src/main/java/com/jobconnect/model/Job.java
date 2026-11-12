
package com.jobconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;
    private String jobType;
    private String experience;
    private String salary;
    private String skills;

    @Column(length = 4000)
    private String description;

    private boolean active = true;

    private LocalDateTime postedAt = LocalDateTime.now();

    @ManyToOne(optional = false)
    private AppUser recruiter;

    protected Job() {
    }

    public Job(
            String t,
            String c,
            String l,
            String jt,
            String ex,
            String sal,
            String d,
            String sk,
            AppUser r) {

        title = t;
        company = c;
        location = l;
        jobType = jt;
        experience = ex;
        salary = sal;
        description = d;
        skills = sk;
        recruiter = r;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getJobType() {
        return jobType;
    }

    public String getExperience() {
        return experience;
    }

    public String getSalary() {
        return salary;
    }

    public String getDescription() {
        return description;
    }

    public String getSkills() {
        return skills;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean v) {
        active = v;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public AppUser getRecruiter() {
        return recruiter;
    }

    // Convert salary text into numeric value in rupees
    public Double getSalaryAmount() {

        if (salary == null || salary.isBlank()) {
            return null;
        }

        try {
            String value = salary
                    .replace(",", "")
                    .replace("₹", "")
                    .trim()
                    .toLowerCase();

            double multiplier = 1;

            if (value.contains("lpa")) {
                multiplier = 100000;
                value = value.replace("lpa", "").trim();

            } else if (value.endsWith("l")) {
                multiplier = 100000;
                value = value.substring(0, value.length() - 1).trim();

            } else if (value.endsWith("k")) {
                multiplier = 1000;
                value = value.substring(0, value.length() - 1).trim();

            } else if (value.contains("per month")) {
                value = value.replace("per month", "").trim();
            }

            return Double.parseDouble(value) * multiplier;

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
