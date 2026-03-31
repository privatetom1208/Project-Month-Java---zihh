package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CreditScreen extends JFrame {
    private int panelWidth = 1280; // MAX Width
    private int panelHeight = 720;  // MAX Height

    // added panels
    private JPanel mainMenuPanel;
    private JPanel mainScreenPanel;
    
    // To run the Credit Screen
    public CreditScreen() {
        setTitle("Zihh Credits");
        setSize(panelWidth, panelHeight);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Opens window maximized
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // --- TASKBAR ICON ---
        ImageIcon originalLogoImage = new ImageIcon("resources/zihhlogo.png");
        Image scaledLogoImage = originalLogoImage.getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Scale logo
        this.setIconImage(scaledLogoImage);

        // initialize panels
        mainMenuPanel = new JPanel();
        mainMenuPanel.setOpaque(false);
        mainMenuPanel.setPreferredSize(new Dimension(300, panelHeight));

        mainScreenPanel = new JPanel();
        mainScreenPanel.setOpaque(false);

        JPanel backgroundPanelCreditScreen = new JPanel() {
            private Image backgroundImage = new ImageIcon("resources/Credits_Screen.png").getImage();
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        
        backgroundPanelCreditScreen.setLayout(new BorderLayout());
        backgroundPanelCreditScreen.add(mainMenuPanel, BorderLayout.WEST);
        backgroundPanelCreditScreen.add(mainScreenPanel, BorderLayout.CENTER);

        // Add background to the frame
        setContentPane(backgroundPanelCreditScreen);

        setVisible(true);

        // Make the frame focusable and add escape key listener
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose(); // Close the Credit Screen
                    new MainScreen(); // Open the Main Screen
                }
            }
        });

        JButton backButton = new JButton("Back to Main Menu");

        int backButtonWidth = 200;
        int backButtonHeight = 50;

        backButton.setText("Back to Main Menu");
        backButton.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        backButton.setFocusPainted(false);
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(Color.BLACK);
        backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        backButton.setPreferredSize(new Dimension(backButtonWidth, backButtonHeight));

        mainMenuPanel.add(Box.createVerticalStrut(100)); // Spacer
        mainMenuPanel.add(backButton);
        backButton.addActionListener(e -> {
            dispose(); // Close the Credit Screen
            new MainScreen(); // Open the Main Screen
        });
    }

    // Quick test launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CreditScreen::new);
    }
}