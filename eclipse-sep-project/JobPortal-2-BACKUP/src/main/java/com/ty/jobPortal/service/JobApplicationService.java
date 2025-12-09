package com.ty.jobPortal.service;

import com.ty.jobPortal.entity.JobApplication;
import com.ty.jobPortal.entity.JobPost;
import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.repository.JobApplicationRepository;
import com.ty.jobPortal.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    public JobApplication applyForJob(Long jobId, User jobSeeker, String contactEmail, String resumeUrl, String coverLetterSummary) {
        if (!jobSeeker.getRole().equals(User.ROLE_JOB_SEEKER)) {
            throw new SecurityException("Access Denied: Only Job Seekers can apply for jobs.");
        }

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job post with ID " + jobId + " not found."));

        JobApplication application = new JobApplication();
        application.setJobPost(jobPost);
        application.setJobSeeker(jobSeeker);
        application.setApplicationDate(LocalDateTime.now());
        
        application.setContactEmail(contactEmail);
        application.setResumeUrl(resumeUrl);
        application.setCoverLetterSummary(coverLetterSummary);
        
        application.setStatus(JobApplication.STATUS_PENDING);

        return jobApplicationRepository.save(application);
    }
    
    public List<JobApplication> findApplicationsByHrUser(User hrUser) {
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Only HR users can view applications.");
        }
        
        List<JobPost> hrJobs = jobPostRepository.findByPostedByHr(hrUser);
        
        return jobApplicationRepository.findByJobPostIn(hrJobs); 
    }

    /**
     * Retrieves all applications submitted by a specific Job Seeker.
     * @param jobSeeker The logged-in Job Seeker.
     * @return A list of submitted JobApplication entities.
     */
    // FIX: New Method for Job Seeker to view their applications
    public List<JobApplication> findApplicationsByJobSeeker(User jobSeeker) {
        if (!jobSeeker.getRole().equals(User.ROLE_JOB_SEEKER)) {
            throw new SecurityException("Access Denied: Only Job Seekers can view their applications.");
        }
        // Assuming JobApplicationRepository has a findByJobSeeker method (will be added in step 3)
        return jobApplicationRepository.findByJobSeeker(jobSeeker); 
    }

    public JobApplication updateApplicationStatus(Long applicationId, String newStatus, User hrUser) {
        if (!hrUser.getRole().equals(User.ROLE_HR)) {
            throw new SecurityException("Access Denied: Only HR users can update application status.");
        }

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
                
        if (!application.getJobPost().getPostedByHr().getId().equals(hrUser.getId())) {
             throw new SecurityException("Access Denied: You cannot manage applications for jobs you did not post.");
        }

        application.setStatus(newStatus);
        return jobApplicationRepository.save(application);
    }
}