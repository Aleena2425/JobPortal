package com.ty.jobPortal.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatusRenderer extends DefaultTableCellRenderer {

    // Define colors for different statuses
    private static final Color HIRED_COLOR = Color.decode("#A5D6A7"); // Light Green
    private static final Color REJECTED_COLOR = Color.decode("#FFCDD2"); // Light Red
    private static final Color INTERVIEW_COLOR = Color.decode("#FFF59D"); // Yellow
    private static final Color DEFAULT_COLOR = Color.WHITE;

    // Optional: Date Formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        // Use the default renderer implementation first
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // --- 1. Date Formatting (Column 2: Date Applied) ---
        // Assuming your history table has Date Applied in column index 2
        if (column == 2 && value instanceof LocalDateTime) {
            setText(DATE_FORMATTER.format((LocalDateTime) value));
        }
        
        // --- 2. Status Coloring (Column 3: Status) ---
        // Assuming your history table has Status in column index 3
        if (column == 3) {
            String status = value != null ? value.toString() : "";
            
            if (!isSelected) { // Do not override selection color
                switch (status) {
                    case "HIRED":
                        setBackground(HIRED_COLOR);
                        setForeground(Color.BLACK);
                        break;
                    case "REJECTED":
                        setBackground(REJECTED_COLOR);
                        setForeground(Color.RED.darker());
                        break;
                    case "INTERVIEW":
                        setBackground(INTERVIEW_COLOR);
                        setForeground(Color.BLACK);
                        break;
                    default: // PENDING, REVIEWED, etc.
                        setBackground(DEFAULT_COLOR);
                        setForeground(Color.BLACK);
                        break;
                }
            } else {
                // Keep the default selection background/foreground
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            setHorizontalAlignment(SwingConstants.CENTER); // Center status text
        } else if (!isSelected) {
             // Reset background for other columns if not selected
            setBackground(DEFAULT_COLOR);
            setForeground(Color.BLACK);
        }
        
        return this;
    }
}