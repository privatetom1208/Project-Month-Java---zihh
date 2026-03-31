package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingScreen extends JFrame {
    private int panelWidth = 1280; // MAX Width
    private int panelHeight = 720;  // MAX Height

    public SettingScreen() {
        setTitle("Zihh Settings");
        setSize(panelWidth, panelHeight);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Opens window maximized
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(32, 126, 51));

        // --- TASKBAR ICON ---
        ImageIcon originalLogoImage = new ImageIcon("resources/zihhlogo.png");
        Image scaledLogoImage = originalLogoImage.getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Scale logo
        this.setIconImage(scaledLogoImage);

        // Ensure content pane uses BorderLayout
        getContentPane().setLayout(new BorderLayout());

        // Create a left-side panel to hold the label and slider
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false); // let frame background show
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 30, 50, 30)); // padding

        // Back Button at the top-left
        JButton backButton = new JButton("Back to Main Menu");

        Dimension buttonSize = new Dimension(175, 60);

        backButton.setText("Back to Main Menu");
        backButton.setFont(new Font("Comic Sans MS", Font.BOLD, 16));
        backButton.setForeground(Color.BLACK);
        backButton.setFocusPainted(false);
        backButton.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        backButton.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 4));
        backButton.setPreferredSize(buttonSize);
        backButton.setMinimumSize(buttonSize);
        backButton.setMaximumSize(buttonSize);
        backButton.setContentAreaFilled(false);
        backButton.setOpaque(false);
        backButton.setFocusPainted(false);

        leftPanel.add(Box.createVerticalStrut(50)); // Spacer
        leftPanel.add(backButton);
        backButton.addActionListener(e -> {
            dispose(); // Close the Credit Screen
            new MainScreen(); // Open the Main Screen
        });

        // Add the back button to the layout
        leftPanel.add(backButton);

        // Title in top-left above volume
        JLabel settingsTitle = new JLabel("Settings");
        Font titleFont = new Font("Comic Sans MS", Font.BOLD, 70);
        settingsTitle.setFont(titleFont);
        settingsTitle.setForeground(Color.BLACK);
        settingsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsTitle.setOpaque(false);

        // Label above the slider
        JLabel volumeLabel = new JLabel("Master Volume");
        Font comic = new Font("Comic Sans MS", Font.BOLD, 30);
        volumeLabel.setFont(comic);
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        volumeLabel.setOpaque(false);

        // Volume Slider (slightly bigger)
        JSlider volumeSlider = new JSlider(0, 100, 50); // min, max, initial
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setOpaque(false);
        volumeSlider.setPreferredSize(new Dimension(600, 75));
        volumeSlider.setMaximumSize(new Dimension(600, 75));
        volumeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        volumeSlider.setValue((int)(GameScreen.masterVolume * 100));

        volumeSlider.addChangeListener(e -> {
            GameScreen.masterVolume = volumeSlider.getValue() / 100f;
        });


        leftPanel.add(settingsTitle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(volumeLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(volumeSlider);

        // Music Volume label and slider (same settings as master volume)
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel musicLabel = new JLabel("Music Volume");
        musicLabel.setFont(comic);
        musicLabel.setForeground(Color.WHITE);
        musicLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        musicLabel.setOpaque(false);

        JSlider musicSlider = new JSlider(0, 100, 50);
        musicSlider.setMajorTickSpacing(10);
        musicSlider.setPaintTicks(true);
        musicSlider.setPaintLabels(true);
        musicSlider.setOpaque(false);
        musicSlider.setPreferredSize(new Dimension(600, 75));
        musicSlider.setMaximumSize(new Dimension(600, 75));
        musicSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        musicSlider.setValue((int)(GameScreen.musicVolume * 100));

        musicSlider.addChangeListener(e -> {
            GameScreen.musicVolume = musicSlider.getValue() / 100f;
        });

        leftPanel.add(musicLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(musicSlider);

        // Load IMAGE from resources folder; adjust path if needed when running
        ImageIcon controlsIcon = new ImageIcon("resources/Controllsnobg.png");
        JLabel controlsImage = new JLabel(controlsIcon);
        controlsImage.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(controlsImage);

        getContentPane().add(leftPanel, BorderLayout.WEST);

        //esc key to go back to main screen
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");

        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new MainScreen();
            }
        });
        // Show window after adding components
        setVisible(true);
    }
}