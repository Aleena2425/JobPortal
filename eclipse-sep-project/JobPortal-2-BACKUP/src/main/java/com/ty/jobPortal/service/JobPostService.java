package com.ty.jobPortal.service;

import com.ty.jobPortal.entity.JobPost;
import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JobPostService {

    @Autowired
    private JobPostRepository jobPostRepository;

    public JobPost postJob(JobPost jobPost, User hrUser) {
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Only HR users can post jobs.");
        }
        if (jobPost.getTitle() == null || jobPost.getTitle().isBlank()) {
            throw new IllegalArgumentException("Job title cannot be empty.");
        }
        
        jobPost.setPostedByHr(hrUser);
        jobPost.setStatus(JobPost.STATUS_ACTIVE); // Set initial status
        return jobPostRepository.save(jobPost);
    }

    // FIX 1: Filtered job listing for Job Seeker view
    public List<JobPost> findAllJobs(String keyword) {
        String activeStatus = JobPost.STATUS_ACTIVE;
        
        if (keyword == null || keyword.isBlank()) {
            // Return only ACTIVE jobs when no keyword is provided
            return jobPostRepository.findByStatusEquals(activeStatus);
        }
        
        // Search only within ACTIVE jobs
        return jobPostRepository.findByStatusEqualsAndTitleContainingIgnoreCaseOrStatusEqualsAndLocationContainingIgnoreCase(
            activeStatus, keyword, activeStatus, keyword);
    }
    
    public List<JobPost> findJobsByHrUser(User hrUser) {
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Cannot view specific posted jobs.");
        }
        // HR views ALL their jobs (active or closed)
        return jobPostRepository.findByPostedByHr(hrUser);
    }
    
    public Optional<JobPost> findJobById(Long id) {
        return jobPostRepository.findById(id);
    }
    
    // FIX 2: Method to close a job posting (safer than deletion)
    public JobPost closeJob(Long jobId, User hrUser) {
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Only HR users can close jobs.");
        }
        
        JobPost job = jobPostRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job post not found."));
            
        // Security check
        if (!job.getPostedByHr().getId().equals(hrUser.getId())) {
            throw new SecurityException("Access Denied: You can only manage jobs you posted.");
        }
        
        job.setStatus(JobPost.STATUS_CLOSED);
        return jobPostRepository.save(job);
    }
    
    public void deleteJob(Long jobId, User hrUser) {
        // ... (Deletion logic remains the same, but now CascadeType.ALL handles applications) ...
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Only HR users can delete jobs.");
        }
        
        JobPost job = jobPostRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job post not found."));
            
        if (!job.getPostedByHr().getId().equals(hrUser.getId())) {
            throw new SecurityException("Access Denied: You can only delete jobs you posted.");
        }
        
        jobPostRepository.delete(job);
    }
}