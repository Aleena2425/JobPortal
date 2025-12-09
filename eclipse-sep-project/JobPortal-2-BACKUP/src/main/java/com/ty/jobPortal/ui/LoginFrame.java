package com.ty.jobPortal.ui;

import com.ty.jobPortal.entity.User;
import com.ty.jobPortal.service.JobApplicationService;
import com.ty.jobPortal.service.JobPostService;
import com.ty.jobPortal.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final UserService userService; 

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame(UserService userService) {
        this.userService = userService;
        
        setTitle("Job Portal Login");
        setSize(450, 350); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        initComponents();
        layoutComponents();
        addListeners();
    }
    
    private void initComponents() {
        usernameField = new JTextField(20); 
        passwordField = new JPasswordField(20);
        
        String[] roles = {User.ROLE_JOB_SEEKER, User.ROLE_HR};
        roleComboBox = new JComboBox<>(roles);
        
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        
        loginButton.setBackground(Color.decode("#3F51B5"));
        loginButton.setForeground(Color.WHITE);
        registerButton.setBackground(Color.decode("#9E9E9E"));
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel headerLabel = new JLabel("Job Portal Access", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(headerLabel, gbc);
        
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; mainPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; mainPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; mainPanel.add(roleComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void addListeners() {
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegistration());
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword()); 
        
        try {
            User loggedInUser = userService.login(username, password);
            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + loggedInUser.getRole());
            
            openDashboard(loggedInUser);
            this.dispose(); 
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void attemptRegistration() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        // FIX: UI Validation Check
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be blank.", 
                                          "Registration Failed", JOptionPane.WARNING_MESSAGE);
            return; 
        }

        try {
            User newUser = new User(null, username, password, role); 
            // The service layer will also validate this data
            userService.registerUser(newUser);
            JOptionPane.showMessageDialog(this, "Registration Successful! Please log in.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openDashboard(User user) {
        JobPostService jobPostService = MainApp.getBean(JobPostService.class);
        JobApplicationService jobApplicationService = MainApp.getBean(JobApplicationService.class);
        
        if (user.getRole().equals(User.ROLE_HR)) {
            HRDashboard hrDashboard = new HRDashboard(jobPostService, jobApplicationService); 
            hrDashboard.setCurrentUser(user);
            hrDashboard.setVisible(true);
        } else if (user.getRole().equals(User.ROLE_JOB_SEEKER)) {
            JobSeekerDashboard seekerDashboard = new JobSeekerDashboard(jobPostService, jobApplicationService);
            seekerDashboard.setCurrentUser(user);
            seekerDashboard.setVisible(true);
        }
    }
    
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);
    }
}