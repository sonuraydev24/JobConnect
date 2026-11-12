
package com.jobconnect.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "job_applications",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"job_id", "applicant_id"}
        )
    }
)
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(optional = false)
    @JoinColumn(name = "applicant_id")
    private AppUser applicant;

    private LocalDateTime appliedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.APPLIED;

    @Column(length = 2000)
    private String coverLetter;

    protected JobApplication() {
    }

    public JobApplication(Job job, AppUser applicant) {
        this.job = job;
        this.applicant = applicant;
        this.appliedAt = LocalDateTime.now();
        this.status = Status.APPLIED;
    }

    public Long getId() {
        return id;
    }

    public Job getJob() {
        return job;
    }

    public AppUser getApplicant() {
        return applicant;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}
