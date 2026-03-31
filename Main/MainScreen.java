package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class MainScreen {
    private JFrame gameFrame; // when the game launches 
    private JPanel mainMenuPanel; // menu where game resides 
    private JPanel mainScreenPanel; 
    private int panelWidth = 1280; // MAX Width
    private int panelHeight = 720; // MAX Height

    // To run the Main Screen
    public MainScreen(){ 
        initialize();
    }

    // Where the MainScreen feature live
    public void initialize() {

        // CREATE MAIN WINDOW FRAME ( LESSON 1 )
        gameFrame = new JFrame();
        gameFrame.setTitle("Zihh Main Menu");
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setMinimumSize(new Dimension(panelWidth, panelHeight)); // Prevents the user from making the frame too small
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Opens window maximized

        // --- TASKBAR ICON ---
        ImageIcon originalLogoImage = new ImageIcon("resources/zihhlogo.png");
        Image scaledLogoImage = originalLogoImage.getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Scale logo
        gameFrame.setIconImage(scaledLogoImage);

        // CREATE MAIN MENU PANEL ( LESSON 1 )
        mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.Y_AXIS));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 5)); // Top, Left, Bottom, Right
        mainMenuPanel.setOpaque(false); // Important: allows background to show through
        mainMenuPanel.setPreferredSize(new Dimension(600, panelHeight)); // Sets preferred width and height

        // CREATE MAIN SCREEN PANEL ( LESSON 1 )
        mainScreenPanel = new JPanel(); 
        mainScreenPanel.setOpaque(false); // Important: allows background to show through

        // Create Buttons ( LESSON 2 )
        JButton gameScreenButton = createButton(
                "resources/playbutton.png",
                "asset/NewGameButtonHover.png",
        		" N E W  G A M E ",
        		KeyEvent.VK_R,
        		"New Game Button", 
        		"New Game Button Clicked!"
        		);

        // BUTTON ACTIONS ( LESSON 3 )
        gameScreenButton.addActionListener(e -> {
            System.out.println("New Game Button Clicked!");
            new GameScreen().setVisible(true); // Opens the new screen
            gameFrame.dispose(); // Closes the main menu
        });

        JButton settingScreenButton = createButton(
                "resources/settingsbutton.png",
                "asset/SettingsButtonHover.png",
        		" S E T T I N G S ",
        		KeyEvent.VK_R,
        		"Settings Button", 
        		"Settings Button Clicked!"
        		);

        JButton creditScreenButton = createButton(
                "resources/creditsbutton.png",
                "asset/CreditButtonHover.png",
        		" C R E D I T S ",
        		KeyEvent.VK_R,
        		"Credits Button", 
        		"Credits Button Clicked!"
        		);

        JButton tutorialScreenButton = createButton(
                "resources/tutorialbutton.png",
                "asset/TutorialButtonHover.png",
        		" T U T O R I A L ",
        		KeyEvent.VK_R,
        		"Tutorial Button", 
        		"Tutorial Button Clicked!"
        		);

        creditScreenButton.addActionListener(e -> {
            System.out.println("Credits Button Clicked!");
            new CreditScreen().setVisible(true); // Opens the new screen
            gameFrame.dispose(); // Closes the main menu
        });

        settingScreenButton.addActionListener(e -> {
            System.out.println("Settings Button Clicked!");
            new SettingScreen().setVisible(true); // Opens the new screen
            gameFrame.dispose(); // Closes the main menu
        });

        tutorialScreenButton.addActionListener(e -> {
            System.out.println("Tutorial Button Clicked!");
            new TutorialScreen().setVisible(true); // Opens the new screen
            gameFrame.dispose(); // Closes the main menu
        });

        // ADD BUTTONS TO MAIN MENU PANEL WITH SPACING 
        mainMenuPanel.add(Box.createVerticalGlue()); // Add space above buttons
        mainMenuPanel.add(gameScreenButton);
        mainMenuPanel.add(Box.createVerticalStrut(25)); // Add 25px space between buttons
        mainMenuPanel.add(settingScreenButton);
        mainMenuPanel.add(Box.createVerticalStrut(25)); // Add 25px space between buttons
        mainMenuPanel.add(creditScreenButton);
        mainMenuPanel.add(Box.createVerticalStrut(25)); // Add 25px space between buttons
        mainMenuPanel.add(tutorialScreenButton);
        mainMenuPanel.add(Box.createVerticalGlue()); // Add space below buttons

        // CREATE BACKGROUND PANEL WITH IMAGE ( LESSON 8 )
        JPanel backgroundPanelMainScreen = new JPanel() {
            private Image backgroundImage = new ImageIcon("resources/title_screen.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };

        backgroundPanelMainScreen.setLayout(new BorderLayout());

        // SET CONTENT PANEL TO BACKGROUND PANEL ( LESSON 8 )
        backgroundPanelMainScreen.add(mainMenuPanel, BorderLayout.WEST);
        backgroundPanelMainScreen.add(mainScreenPanel, BorderLayout.CENTER);

	    // Add background to the MainScreen
        gameFrame.setContentPane(backgroundPanelMainScreen); // -------> UPDATE w/ LESSON 8

        show(); 
    }// END INITIALIZE

    // ADDING BUTTON IMAGES ( LESSON 7 )
   private JButton createButton(String imagePath, String hoverPath, String buttonText, int mnemonicKey, String toolTipMessage, String clickMessage) {

        int buttonWidth = 350; 
        int buttonHeight = 120;

        ImageIcon buttonImage = new ImageIcon(imagePath);
        Image scaledImage = buttonImage.getImage().getScaledInstance(buttonWidth,buttonHeight, Image.SCALE_SMOOTH); 
        ImageIcon buttonIcon = new ImageIcon(scaledImage); 

        JButton button = new JButton(buttonIcon);
        button.setText(buttonText); 
        button.setBackground(Color.WHITE);
        button.setPreferredSize(new Dimension(buttonWidth + 5, buttonHeight + 5));

        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setText(null);       // no hidden text
        button.setFocusable(false);     // no focus dots

        button.setMnemonic(mnemonicKey);
        button.setToolTipText(toolTipMessage);
        button.addActionListener(e -> {
            System.out.println(clickMessage);
        });

	    // BUTTON STYLE
	    button.setFont(new Font("Times New Roman", Font.PLAIN, 24)); // Button Font
	    button.setMargin(new Insets(10, 10, 10, 10)); // Button spacing from other buttons

	    // BUTTON ACTIONS/BEHAVIOUR - Styling / Effects
        // Hover Effect (italic + cursor change)
	    button.addMouseListener(new java.awt.event.MouseAdapter() {
	        @Override
	        public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setIcon(buttonIcon);
	            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	            button.setFont(button.getFont().deriveFont(Font.ITALIC | Font.BOLD));
	        }
	        @Override
	        public void mouseExited(java.awt.event.MouseEvent evt) {
	            button.setForeground(Color.WHITE);
	            button.setFont(button.getFont().deriveFont(Font.BOLD));
	        }
	    });
	    return button;
	}// END CREATE BUTTON

    public void show() {
		this.gameFrame.setVisible(true);
	}// END SHOW  

    // ALLOW MAIN TO ACCESS SCREEN (SET VISIBLE) (LESSON 5)
	public void setVisible(boolean b) {
		gameFrame.setVisible(b);
	}
}