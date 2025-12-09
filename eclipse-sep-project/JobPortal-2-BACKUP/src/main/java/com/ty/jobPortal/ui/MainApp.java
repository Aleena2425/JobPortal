package com.ty.jobPortal.ui;

import com.ty.jobPortal.JobPortal2Application;
import com.ty.jobPortal.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class MainApp {

    private static ConfigurableApplicationContext context;
    private static LoginFrame loginFrameInstance; // FIX: Static reference for logout

    public static void main(String[] args) {
        // NOTE: If you still see the HeadlessException, add -Djava.awt.headless=false 
        // to your VM arguments in the Eclipse Run Configuration.
        context = SpringApplication.run(JobPortal2Application.class, args);

        SwingUtilities.invokeLater(() -> {
            try {
                UserService userService = context.getBean(UserService.class);
                
                loginFrameInstance = new LoginFrame(userService);
                loginFrameInstance.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Application failed to start GUI: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    public static void logout() { // FIX: Logout method
        if (loginFrameInstance != null) {
            SwingUtilities.invokeLater(() -> {
                loginFrameInstance.clearFields(); 
                loginFrameInstance.setVisible(true);
            });
        }
    }
    
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Spring application context has not been initialized.");
        }
        return context.getBean(beanClass);
    }
}