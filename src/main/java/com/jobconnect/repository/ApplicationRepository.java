
package com.jobconnect.repository;

import com.jobconnect.model.AppUser;
import com.jobconnect.model.Job;
import com.jobconnect.model.JobApplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    // Check whether candidate already applied
    boolean existsByJobAndApplicant(
            Job job,
            AppUser applicant
    );

    // Get all applications of a candidate
    List<JobApplication> findByApplicantOrderByAppliedAtDesc(
            AppUser applicant
    );

    // Get all applications for recruiter's jobs
    List<JobApplication> findByJobRecruiterOrderByAppliedAtDesc(
            AppUser recruiter
    );

    // Count applications for a particular job
    long countByJob(Job job);

}