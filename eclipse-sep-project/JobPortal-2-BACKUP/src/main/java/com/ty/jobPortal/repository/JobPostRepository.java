package com.ty.jobPortal.repository;

import com.ty.jobPortal.entity.JobPost;
import com.ty.jobPortal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    
    List<JobPost> findByPostedByHr(User postedByHr); 
    
    // FIX 1: Method to find ACTIVE jobs by title or location keyword
    List<JobPost> findByStatusEqualsAndTitleContainingIgnoreCaseOrStatusEqualsAndLocationContainingIgnoreCase(
        String activeStatus1, String titleKeyword, String activeStatus2, String locationKeyword);

    // FIX 2: Method to find ALL ACTIVE jobs (for initial load)
    List<JobPost> findByStatusEquals(String status);
}