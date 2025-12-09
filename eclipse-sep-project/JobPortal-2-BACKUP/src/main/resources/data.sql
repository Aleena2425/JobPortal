-- Initial Data for Testing Job Portal
-- NOTE: Passwords use the simplified hash from UserService ("HASHED_" + password)

-- 1. HR User
INSERT INTO app_user (id, username, password, role) 
VALUES (100, 'hr_admin', 'HASHED_hrpass', 'HR');

-- 2. Job Seeker User
INSERT INTO app_user (id, username, password, role) 
VALUES (101, 'john_doe', 'HASHED_jobpass', 'JOB_SEEKER');

-- 3. Initial Job Post (Posted by HR User 100)
INSERT INTO job_post (id, title, description, location, salary_range, posted_by_hr_id) 
VALUES (500, 'Senior Java Developer', 'Design and implement core Spring Boot services.', 'Remote', '120k-150k', 100);

-- 4. Another Job Post
INSERT INTO job_post (id, title, description, location, salary_range, posted_by_hr_id) 
VALUES (501, 'Swing UI Specialist', 'Develop clean and responsive desktop UIs.', 'New York, NY', '90k-110k', 100);