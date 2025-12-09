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
import java.time.format.DateTimeFormatter;
import java.util.Optional; // Needed for finding job by ID

public class JobSeekerDashboard extends JFrame {

    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService; 

    private User currentUser; 
    
    // Browse Jobs Tab Components
    private JTabbedPane mainTabs;
    private JTable jobTable;
    private DefaultTableModel jobTableModel;
    private JTextArea jobDetailsArea; 
    private JButton applyButton;
    private JButton refreshButton; 
    private JTextField searchField; 
    private JButton searchButton; 
    
    // Application History Tab Components
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JButton refreshHistoryButton;
    
    private JButton logoutButton; 
    private JPanel headerPanel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public JobSeekerDashboard(JobPostService jobPostService, JobApplicationService jobApplicationService) {
        this.jobPostService = jobPostService; 
        this.jobApplicationService = jobApplicationService; 
        
        setTitle("Job Seeker Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        addListeners();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.setTitle("Job Seeker Dashboard - Logged in as: " + user.getUsername());
        updateHeaderPanel();
        loadJobs(null);
        loadApplicationHistory();
    }

    private void initComponents() {
        // Browse Jobs components
        String[] jobColumns = {"ID", "Title", "Location", "Salary Range"};
        jobTableModel = new DefaultTableModel(jobColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        jobTable = new JTable(jobTableModel);
        jobTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        jobDetailsArea = new JTextArea(5, 40);
        jobDetailsArea.setEditable(false);
        jobDetailsArea.setLineWrap(true);
        
        applyButton = new JButton("Apply for Selected Job");
        refreshButton = new JButton("Refresh Jobs"); 
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        
        // Application History components
        String[] historyColumns = {"Application ID", "Job Title", "Date Applied", "Status"};
        historyTableModel = new DefaultTableModel(historyColumns, 0);
        historyTable = new JTable(historyTableModel);
        historyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        refreshHistoryButton = new JButton("Refresh History");

        historyTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer()); 
        
        // General components
        logoutButton = new JButton("Logout"); 
        headerPanel = new JPanel(new BorderLayout());
        
        applyButton.setBackground(Color.decode("#00BCD4"));
        applyButton.setForeground(Color.WHITE);
        logoutButton.setBackground(Color.decode("#E57373"));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));

        // 1. Header Panel (NORTH)
        add(headerPanel, BorderLayout.NORTH);
        
        // 2. Tabbed Panel (CENTER)
        mainTabs = new JTabbedPane();
        mainTabs.addTab("Browse Jobs", createBrowseJobsPanel());
        mainTabs.addTab("My Applications", createHistoryPanel());

        add(mainTabs, BorderLayout.CENTER);
    }
    
    private JPanel createBrowseJobsPanel() {
        JPanel browsePanel = new JPanel(new BorderLayout(10, 10));
        browsePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top section (Search Bar, Refresh Button, and Table)
        JPanel tableSection = new JPanel(new BorderLayout());
        
        // Search Bar Panel
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBarPanel.add(new JLabel("Keyword (Title/Location):"));
        searchBarPanel.add(searchField);
        searchBarPanel.add(searchButton);
        searchBarPanel.add(refreshButton);
        
        tableSection.add(searchBarPanel, BorderLayout.NORTH);
        tableSection.add(new JScrollPane(jobTable), BorderLayout.CENTER);
        
        browsePanel.add(tableSection, BorderLayout.CENTER);

        // Bottom section (Details and Apply)
        JPanel detailsSection = new JPanel(new BorderLayout(5, 5));
        detailsSection.setBorder(BorderFactory.createTitledBorder("Selected Job Details"));
        
        detailsSection.add(new JScrollPane(jobDetailsArea), BorderLayout.CENTER);
        
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.add(applyButton);
        
        detailsSection.add(bottomBar, BorderLayout.SOUTH);
        
        browsePanel.add(detailsSection, BorderLayout.SOUTH);
        return browsePanel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top bar for refresh button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(refreshHistoryButton);
        historyPanel.add(topBar, BorderLayout.NORTH);

        // Table in the center
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        return historyPanel;
    }
    
    private void updateHeaderPanel() {
        headerPanel.removeAll(); 

        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(Color.decode("#ECEFF1"));
        
        JLabel welcomeLabel = new JLabel("Job Seeker Dashboard - Browse & Apply");
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
    
    private void loadJobs(String keyword) {
        jobTableModel.setRowCount(0);
        try {
            List<JobPost> jobs = jobPostService.findAllJobs(keyword);
            for (JobPost job : jobs) {
                jobTableModel.addRow(new Object[]{
                    job.getId(), 
                    job.getTitle(), 
                    job.getLocation(), 
                    job.getSalaryRange()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load jobs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadApplicationHistory() {
        historyTableModel.setRowCount(0);
        try {
            if (jobApplicationService == null) {
                JOptionPane.showMessageDialog(this, "Service Error: JobApplicationService is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (currentUser == null) return;

            List<JobApplication> applications = jobApplicationService.findApplicationsByJobSeeker(currentUser);
            
            for (JobApplication app : applications) {
                historyTableModel.addRow(new Object[]{
                    app.getId(), 
                    app.getJobPost().getTitle(), 
                    app.getApplicationDate(), 
                    app.getStatus() 
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load application history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addListeners() {
        applyButton.addActionListener(e -> showApplyDialog()); 
        
        logoutButton.addActionListener(e -> attemptLogout());
        refreshButton.addActionListener(e -> loadJobs(null));
        refreshHistoryButton.addActionListener(e -> loadApplicationHistory());
        
        searchButton.addActionListener(e -> loadJobs(searchField.getText())); 
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                attemptLogout();
            }
        });
        
        jobTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobTable.getSelectedRow() != -1) {
                displayJobDetails();
            }
        });
    }

    private void attemptLogout() {
        this.dispose(); 
        MainApp.logout(); 
    }

    private void displayJobDetails() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            jobDetailsArea.setText(""); 
            return;
        }

        Long jobId = (Long) jobTableModel.getValueAt(selectedRow, 0);
        
        // Use findJobById for a clean single lookup
        Optional<JobPost> jobOpt = jobPostService.findJobById(jobId);
        
        jobOpt.ifPresent(job -> {
            StringBuilder details = new StringBuilder();
            details.append("Title: ").append(job.getTitle()).append("\n");
            details.append("Location: ").append(job.getLocation()).append("\n");
            details.append("Salary Range: ").append(job.getSalaryRange()).append("\n");
            details.append("Status: ").append(job.getStatus()).append("\n");
            details.append("\n==================================\n");
            details.append("Description:\n").append(job.getDescription());
            
            jobDetailsArea.setText(details.toString()); 
            jobDetailsArea.setCaretPosition(0);
        });
    }

    private void showApplyDialog() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to apply.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Long jobId = (Long) jobTableModel.getValueAt(selectedRow, 0);
        
        // FIX: Check job status before proceeding
        Optional<JobPost> jobOpt = jobPostService.findJobById(jobId);
        if (jobOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Job not found or recently deleted.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JobPost job = jobOpt.get();
        
        if (JobPost.STATUS_CLOSED.equals(job.getStatus())) {
            JOptionPane.showMessageDialog(this, "This job posting is CLOSED and no longer accepting applications.", "Job Closed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // END FIX

        JTextField emailField = new JTextField(25);
        JTextField resumeUrlField = new JTextField(25);
        JTextArea coverLetterArea = new JTextArea(5, 25);
        coverLetterArea.setLineWrap(true);
        JScrollPane coverLetterScrollPane = new JScrollPane(coverLetterArea);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Contact Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Resume URL:"), gbc);
        gbc.gridx = 1; panel.add(resumeUrlField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.NORTHWEST; panel.add(new JLabel("Summary:"), gbc);
        gbc.gridx = 1; gbc.gridheight = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        panel.add(coverLetterScrollPane, gbc);

        emailField.setText(currentUser.getUsername() + "@jobseeker.com");

        int result = JOptionPane.showConfirmDialog(this, panel, "Application Details", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            attemptApplyForJob(emailField.getText(), resumeUrlField.getText(), coverLetterArea.getText());
        }
    }
    
    private void attemptApplyForJob(String email, String resumeUrl, String summary) {
        int selectedRow = jobTable.getSelectedRow();
        Long jobId = (Long) jobTableModel.getValueAt(selectedRow, 0);

        if (email.isBlank() || summary.isBlank()) {
            JOptionPane.showMessageDialog(this, "Email and Summary are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            jobApplicationService.applyForJob(jobId, currentUser, email, resumeUrl, summary);
            
            JOptionPane.showMessageDialog(this, "Application submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            loadApplicationHistory(); 
            
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Security Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Application Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}