package com.ty.jobPortal.ui;

import com.ty.jobPortal.entity.JobApplication;
import com.ty.jobPortal.entity.JobPost;
import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.service.JobApplicationService;
import com.ty.jobPortal.service.JobPostService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HRDashboard extends JFrame {

    private final JobPostService jobPostService; 
    private final JobApplicationService jobApplicationService; 
    private User currentUser;
    
    // UI components for Job Post tab
    private JTextField titleField, locationField, salaryRangeField;
    private JTextArea descriptionArea;
    private JButton postJobButton;
    private JButton deleteJobButton; 
    private JButton closeJobButton; 
    private JTable jobPostTable;     
    private DefaultTableModel jobPostTableModel;
    
    // UI components for Applicant View tab
    private JTabbedPane mainTabs;
    private JTable applicantsTable;
    private DefaultTableModel applicantsTableModel;
    private JButton refreshApplicantsButton;

    private JButton logoutButton; 
    private JPanel headerPanel;

    public HRDashboard(JobPostService jobPostService, JobApplicationService jobApplicationService) {
        this.jobPostService = jobPostService;
        this.jobApplicationService = jobApplicationService;
        
        setTitle("HR Dashboard"); 
        setSize(800, 650); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents(); 
        addListeners();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.setTitle("HR Dashboard - Posting as: " + user.getUsername());
        updateHeaderPanel();
        loadApplicants(); 
        loadPostedJobs(); 
    }

    private void initComponents() {
        // Job Post components
        titleField = new JTextField(25);
        locationField = new JTextField(25);
        salaryRangeField = new JTextField(25);
        descriptionArea = new JTextArea(8, 25);
        descriptionArea.setLineWrap(true);
        postJobButton = new JButton("Post Job");
        deleteJobButton = new JButton("Delete Selected Job"); 
        closeJobButton = new JButton("Close Job"); 
        
        deleteJobButton.setBackground(Color.decode("#FF9800"));
        closeJobButton.setBackground(Color.decode("#FF5722")); 
        
        // Table for posted jobs 
        String[] jobColumns = {"ID", "Title", "Location", "Salary Range", "Status"}; 
        jobPostTableModel = new DefaultTableModel(jobColumns, 0);
        jobPostTable = new JTable(jobPostTableModel);

        // Applicant View components
        String[] applicantColumns = {"Job Title", "Applicant", "Email", "Resume URL", "Status"};
        applicantsTableModel = new DefaultTableModel(applicantColumns, 0);
        applicantsTable = new JTable(applicantsTableModel);
        applicantsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        refreshApplicantsButton = new JButton("Refresh List");
        
        applicantsTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer()); 
        
        // General components
        logoutButton = new JButton("Logout");
        headerPanel = new JPanel(new BorderLayout());
        
        // Styling
        postJobButton.setBackground(Color.decode("#3F51B5")); postJobButton.setForeground(Color.WHITE);
        logoutButton.setBackground(Color.decode("#E57373"));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));
        
        add(headerPanel, BorderLayout.NORTH); 

        mainTabs = new JTabbedPane();
        mainTabs.addTab("Post & Manage Jobs", createJobPostPanel());
        mainTabs.addTab("View Applicants", createApplicantsPanel());
        
        add(mainTabs, BorderLayout.CENTER);
    }
    
    private JPanel createJobPostPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); 
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- A. Job Post Form Section (NORTH) ---
        JPanel formSection = new JPanel(new BorderLayout(0, 10)); 
        formSection.setBorder(BorderFactory.createTitledBorder("Post New Job"));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Form Fields
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; formPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; formPanel.add(locationField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; formPanel.add(new JLabel("Salary:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; formPanel.add(salaryRangeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(new JScrollPane(descriptionArea), gbc);
        
        JPanel postButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        postButtonPanel.add(postJobButton);
        
        formSection.add(formPanel, BorderLayout.CENTER);
        formSection.add(postButtonPanel, BorderLayout.SOUTH);
        
        // --- B. Job Management Table Section (CENTER) ---
        JPanel managePanel = new JPanel(new BorderLayout(5, 5));
        managePanel.setBorder(BorderFactory.createTitledBorder("Your Posted Jobs"));
        
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionBar.add(closeJobButton); 
        actionBar.add(deleteJobButton);
        
        managePanel.add(actionBar, BorderLayout.NORTH);
        managePanel.add(new JScrollPane(jobPostTable), BorderLayout.CENTER);

        mainPanel.add(formSection, BorderLayout.NORTH);
        mainPanel.add(managePanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JPanel createApplicantsPanel() {
        JPanel applicantPanel = new JPanel(new BorderLayout(10, 10));
        applicantPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton updateStatusButton = new JButton("Update Status"); 
        updateStatusButton.setBackground(Color.decode("#00BCD4"));
        updateStatusButton.setForeground(Color.WHITE);
        updateStatusButton.addActionListener(e -> attemptUpdateStatus());

        topBar.add(updateStatusButton); 
        topBar.add(refreshApplicantsButton);
        applicantPanel.add(topBar, BorderLayout.NORTH);

        applicantPanel.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);

        return applicantPanel;
    }

    private void updateHeaderPanel() {
        headerPanel.removeAll(); 
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(Color.decode("#ECEFF1"));
        
        JLabel welcomeLabel = new JLabel("HR Dashboard - Job Management");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel logoutContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutContainer.setBackground(headerPanel.getBackground());
        
        logoutContainer.add(new JLabel("User: " + currentUser.getUsername() + " | "));
        logoutContainer.add(logoutButton);
        headerPanel.add(logoutContainer, BorderLayout.EAST);
        
        headerPanel.revalidate();
        headerPanel.repaint();
    }
    
    private void loadPostedJobs() {
        jobPostTableModel.setRowCount(0);
        try {
            if (jobPostService == null) {
                JOptionPane.showMessageDialog(this, "Service Error: JobPostService is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<JobPost> jobs = jobPostService.findJobsByHrUser(currentUser); 
            
            for (JobPost job : jobs) {
                jobPostTableModel.addRow(new Object[]{
                    job.getId(), 
                    job.getTitle(), 
                    job.getLocation(), 
                    job.getSalaryRange(),
                    job.getStatus() 
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load posted jobs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadApplicants() {
        applicantsTableModel.setRowCount(0);
        try {
            if (jobApplicationService == null) {
                JOptionPane.showMessageDialog(this, "Service Error: JobApplicationService is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<JobApplication> applications = jobApplicationService.findApplicationsByHrUser(currentUser);
            
            for (JobApplication app : applications) {
                applicantsTableModel.addRow(new Object[]{
                    app.getJobPost().getTitle(),
                    app.getJobSeeker().getUsername(),
                    app.getContactEmail(),
                    app.getResumeUrl(),
                    app.getStatus() 
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load applicants: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptUpdateStatus() {
        int selectedRow = applicantsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an applicant to update the status.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<JobApplication> applications = jobApplicationService.findApplicationsByHrUser(currentUser);
            if (selectedRow >= applications.size()) return;
            
            JobApplication selectedApp = applications.get(selectedRow);
            Long applicationId = selectedApp.getId();

            String[] statuses = {"PENDING", "REVIEWED", "INTERVIEW", "REJECTED", "HIRED"};
            JComboBox<String> statusComboBox = new JComboBox<>(statuses);
            statusComboBox.setSelectedItem(selectedApp.getStatus()); 

            int result = JOptionPane.showConfirmDialog(this, 
                statusComboBox, 
                "Set Status for " + selectedApp.getJobSeeker().getUsername(),
                JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String newStatus = (String) statusComboBox.getSelectedItem();

                jobApplicationService.updateApplicationStatus(applicationId, newStatus, currentUser);
                JOptionPane.showMessageDialog(this, "Status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadApplicants(); 
            }
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, "Security Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        postJobButton.addActionListener(e -> attemptPostJob());
        deleteJobButton.addActionListener(e -> attemptDeleteJob()); 
        closeJobButton.addActionListener(e -> attemptCloseJob()); 
        logoutButton.addActionListener(e -> attemptLogout());
        refreshApplicantsButton.addActionListener(e -> loadApplicants()); 
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                attemptLogout();
            }
        });
    }

    private void attemptLogout() {
        this.dispose(); 
        MainApp.logout(); 
    }

    private void attemptPostJob() {
        String title = titleField.getText();
        String location = locationField.getText();
        String salaryRange = salaryRangeField.getText();
        String description = descriptionArea.getText();
        
        if (title.isBlank() || description.isBlank()) {
            JOptionPane.showMessageDialog(this, "Title and Description are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (jobPostService == null) {
            JOptionPane.showMessageDialog(this, "Service Error: JobPostService is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // FIX: Corrected constructor call (8 arguments)
            JobPost newJob = new JobPost(
                null, 
                title, 
                description, 
                location, 
                salaryRange, 
                null, // postedByHr
                null, // status
                null  // applications
            ); 
            
            jobPostService.postJob(newJob, currentUser); 
            
            JOptionPane.showMessageDialog(this, "Job Posted Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            titleField.setText("");
            locationField.setText("");
            salaryRangeField.setText("");
            descriptionArea.setText("");
            loadPostedJobs(); 
            
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); 
        }
    }
    
    private void attemptDeleteJob() {
        int selectedRow = jobPostTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long jobId = (Long) jobPostTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete Job ID " + jobId + "? This cannot be undone.", 
            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                jobPostService.deleteJob(jobId, currentUser);
                JOptionPane.showMessageDialog(this, "Job ID " + jobId + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPostedJobs(); 
            } catch (SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Security Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Deletion Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred during deletion.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void attemptCloseJob() {
        int selectedRow = jobPostTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to close.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String currentStatus = (String) jobPostTableModel.getValueAt(selectedRow, 4);
        if (JobPost.STATUS_CLOSED.equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Job is already closed.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long jobId = (Long) jobPostTableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to CLOSE Job ID " + jobId + "? Applicants will no longer see it.", 
            "Confirm Closure", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                jobPostService.closeJob(jobId, currentUser);
                JOptionPane.showMessageDialog(this, "Job ID " + jobId + " closed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPostedJobs(); 
            } catch (SecurityException ex) {
                JOptionPane.showMessageDialog(this, "Security Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Closure Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred during closure.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}