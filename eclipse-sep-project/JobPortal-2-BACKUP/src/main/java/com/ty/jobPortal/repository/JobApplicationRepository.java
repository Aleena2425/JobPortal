package com.ty.jobPortal.repository;

import com.ty.jobPortal.entity.JobApplication;
import com.ty.jobPortal.entity.JobPost;
import com.ty.jobPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByJobPostIn(List<JobPost> jobPosts);
    
    // FIX: New method signature required by JobApplicationService
    List<JobApplication> findByJobSeeker(User jobSeeker);
}