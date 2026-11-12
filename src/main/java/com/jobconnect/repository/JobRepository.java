
package com.jobconnect.repository;

import com.jobconnect.model.AppUser;
import com.jobconnect.model.Job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByActiveTrueOrderByPostedAtDesc();

    List<Job> findByRecruiterOrderByPostedAtDesc(AppUser recruiter);

    @Query("""
        SELECT j FROM Job j
        WHERE j.active = true
        AND (
            :keyword IS NULL OR
            LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(j.skills) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :location IS NULL OR
            LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))
        )
        AND (
            :jobType IS NULL OR
            LOWER(j.jobType) = LOWER(:jobType)
        )
        AND (
            :experience IS NULL OR
            LOWER(j.experience) LIKE LOWER(CONCAT('%', :experience, '%'))
        )
        ORDER BY j.postedAt DESC
    """)
    List<Job> searchJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") String jobType,
            @Param("experience") String experience
    );
}
