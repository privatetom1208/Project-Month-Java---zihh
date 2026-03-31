 package Main;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.io.File;
import PlayerMovement.PlayerSprite;
import javax.sound.sampled.*;


public class TutorialScreen extends JPanel implements ActionListener {


    private JFrame gameFrame;
    private JPanel TutorialScreenPanel;
    private Timer timer;
    private PlayerSprite player;


    // --- MAP & CAMERA ---
    private BufferedImage worldMapImage;
    private BufferedImage collisionMap;
    private double MAP_SCALE = 7.0;
    private int worldWidth, worldHeight;
    private int camX = 0, camY = 0;


    // --- CORE VARIABLES ---
    public static float musicVolume = 0.5f;
    private JButton restartBtn, menuBtn;
    private int health = 6;
    private volatile Clip currentMusicClip;
    private boolean gameOver = false;
    private Image fullHeart, halfHeart, emptyHeart, gameOverImage;


    // Sounds & Movement
    private volatile Clip currentWalkingClip, currentRunningClip;
    private boolean isWalkingSoundPlaying = false;
    private boolean isRunningSoundPlaying = false;


    // Zombie Snake
    private List<Point> zombieTrail = new ArrayList<>();
    private List<String> zombieTrailTypes = new ArrayList<>();
    private int zombieSpacing = 50;
    private Map<String, Image> zombieImageCache = new HashMap<>();


    // Combat
    private boolean isAttacking = false;
    private long attackStartTime = 0;
    private final long attackDuration = 500;
    private final int attackRange = 120;
    private Image slashImage;
    private boolean rcReady = true, qReady = true, lcReady = true;


    private List<double[]> projectiles = new ArrayList<>();
    private double projectileSpeed = 8.0, projectileRange = 325.0;
    private Image projectileImage;
    private long lastShotTime = 0, lastAllyLaunchTime = 0;
    private long shotCooldown = 2000, allyLaunchCooldown = 2000;


    // Zum
    private Image zumImage, zumGrowImage, zumStayImage;
    private List<ZumProjectile> zumProjectiles = new ArrayList<>();
    private int currentMouseX, currentMouseY;
    private double zumScaleX = 10.0, zumScaleY = 8.0;


    // Allies
    private Image lcActive, lcDull, rcActive, rcDull, qActive, qDull;
    private List<AttackingAlly> activeAllies = new ArrayList<>();


    // Enemy
    private int boxHealth = 3;
    private int boxX, boxY, boxSize = 100;
    private boolean boxActive = false;
    private int damageCounter = 0, damageSpeed = 50;
    private String currentBoxType = "";
    private Image currentBoxImage = null, currentWeaponImage = null;
    private String currentWeaponType = "";
    private double boxAngle = 0;


    // Enemy Weapon System
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private long lastEnemyShotTime = 0;
    private int shotsInBurst = 0;
    private final int BURST_SIZE = 3;
    private final long BURST_DELAY = 300, BURST_COOLDOWN = 6000, SEMI_COOLDOWN = 3000;
    private final double WEAPON_PROJECTILE_SPEED = 7.0;


    // Can O' Zihh
    private Image canInventoryImage, canItemImage;
    private Image[] inventorySlots = new Image[4];
    private String[] inventoryTypes = new String[4];
    private List<Point> canItems = new ArrayList<>();
    private List<String> canItemTypes = new ArrayList<>();
    private long lastCanSpawnTime = 0;
    private final long canSpawnInterval = 5000; // 5 seconds


    // Trashcans
    private Image trashcanImage;
    private Image trashcanOpenImage;
    private List<Trashcan> trashcans = new ArrayList<>();
    private final double TRASHCAN_ANGLE_OFFSET = Math.toRadians(270);
    private long lastTrashcanSpawnTime = 0;
    private final long trashcanSpawnInterval = 2500; // 2.5 seconds
   
    private final Set<Integer> activeKeys = new HashSet<>();


    // Tutorial ability progression checklist
    private JTextArea tutorialChecklist;
    private boolean canMove = false;
    private boolean canSprint = false;
    private boolean canSlash = false;
    private boolean canCough = false;
    private boolean canAllySend = false;
    private boolean canPickup = false;
    private boolean canUseItem = false;
    private boolean canDodgeRoll = false;


    private double totalDistanceMoved = 0.0;
    private final double SPRINT_UNLOCK_DISTANCE = 200.0; // distance to unlock sprint
    // Helper Classes
    private class AttackingAlly {
        double x, y, speed, angle = 0;
        Image img; int health = 4;
       
        public AttackingAlly(double x, double y, Image img, String type, double speed) {
            this.x = x; this.y = y; this.img = img; this.speed = speed;
        }


        public void move(int targetX, int targetY) {
            double dx = targetX - x, dy = targetY - y;
            angle = Math.atan2(dy, dx);
            double dist = Math.sqrt(dx * dx + dy * dy);
           
            if (dist > speed) {
                double moveX = (dx / dist) * speed;
                double moveY = (dy / dist) * speed;
                double lookAhead = 20.0; // Vision distance to detect buildings early


                // 1. Check if the path ahead is clear
                if (isLocationPassable(x + moveX + 50 + (Math.cos(angle) * lookAhead),
                                     y + moveY + 50 + (Math.sin(angle) * lookAhead))) {
                    x += moveX;
                    y += moveY;
                } else {
                    // 2. Obstacle detected: Sweep angles to steer around
                    boolean foundPath = false;
                    for (int offset = 20; offset <= 140; offset += 20) {
                        double rad = Math.toRadians(offset);
                        double[][] anglesToTry = {
                            {Math.cos(angle - rad), Math.sin(angle - rad)},
                            {Math.cos(angle + rad), Math.sin(angle + rad)}
                        };


                        for (double[] dir : anglesToTry) {
                            double tx = dir[0] * speed;
                            double ty = dir[1] * speed;
                            if (isLocationPassable(x + tx + 50 + (dir[0] * lookAhead),
                                                 y + ty + 50 + (dir[1] * lookAhead))) {
                                x += tx;
                                y += ty;
                                foundPath = true;
                                break;
                            }
                        }
                        if (foundPath) break;
                    }
                }
            }
        }
    }


    private class ZumProjectile {
    double x, y, targetX, targetY, speed = 5.0, rotationAngle = 0, rotationSpeed = 0.1;
    enum State { MOVING, GROWING, STAYING }
    State state = State.MOVING;
    long growDuration = 1000; // 1 second
    long stayDuration = 5000; // 5 seconds
    long stateStartTime;
    Image currentImage = zumImage;


    public ZumProjectile(double x, double y, double tx, double ty) {
        this.x = x; this.y = y; this.targetX = tx; this.targetY = ty;
    }


    public void update() {
        long now = System.currentTimeMillis();
        if (state == State.MOVING) {
            double dx = targetX - x, dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 5) {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
            } else {
                state = State.GROWING;
                stateStartTime = now;
                currentImage = zumGrowImage;
            }
            rotationAngle += rotationSpeed;
        } else if (state == State.GROWING && now - stateStartTime >= growDuration) {
            state = State.STAYING;
            stateStartTime = now;
            currentImage = zumStayImage;
        } else if (state == State.STAYING && now - stateStartTime >= stayDuration) {
            // Will be removed in updateZumProjectiles
        }
    }


    // Update getBounds() in ZumProjectile to center the bounds at the projectile's position
    public Rectangle getBounds() {
        int w = (state == State.STAYING) ? (int)(50 * zumScaleX) : 50;
        int h = (state == State.STAYING) ? (int)(100 * zumScaleY) : 100;
        return new Rectangle((int)x - w/2, (int)y - h/2, w, h);
    }
}


    private class EnemyProjectile {
        double x, y, angle, distanceTraveled = 0;
        final double range = 325.0; // Same as projectileRange


        public EnemyProjectile(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }


        public void move() {
            x += Math.cos(angle) * WEAPON_PROJECTILE_SPEED;
            y += Math.sin(angle) * WEAPON_PROJECTILE_SPEED;
            distanceTraveled += WEAPON_PROJECTILE_SPEED;
        }


        public boolean isExpired() {
            return distanceTraveled > range;
        }


        public Rectangle getBounds() { return new Rectangle((int)x - 5, (int)y - 5, 10, 10); }
    }


    private class Trashcan {
        int x, y; Image image; double angle = 0; boolean locked = false;
        public Trashcan(int x, int y, Image image) { this.x = x; this.y = y; this.image = image; }
        public Rectangle getBounds() { return new Rectangle(x, y, 100, 100); }
    }


    public TutorialScreen() {
        loadAssets();
        initializeMap();
        player = new PlayerSprite(worldWidth / 2, worldHeight / 2, 50, 50);
        startMusic();
        makeTrail();
        initializeUI();
        setupGameOverButtons();
        timer = new Timer(10, this);
        timer.start();
        spawnBox();
    }


    private JPanel createSidePanel() {
        // 1. Layout: BorderLayout lets us put an image at Top (North) and text in Center
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(250, 0)); // Fixed width: 250 pixels
        sidePanel.setBackground(new Color(30, 30, 30));    // Dark gray background
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.WHITE)); // Left border line


        // 2. Top Image (Using your existing canItemImage)
        // We wrap it in a JLabel to display it
        JLabel imageLabel = new JLabel(new ImageIcon(canItemImage));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0)); // Add padding above/below
        sidePanel.add(imageLabel, BorderLayout.NORTH);


        // 3. Center Text (Instructions + Checklist)
        tutorialChecklist = new JTextArea();
        tutorialChecklist.setFont(new Font("Monospaced", Font.BOLD, 14));
        tutorialChecklist.setForeground(Color.WHITE);
        tutorialChecklist.setOpaque(false);
        tutorialChecklist.setEditable(false);
        tutorialChecklist.setLineWrap(true);
        tutorialChecklist.setWrapStyleWord(true);
        tutorialChecklist.setMargin(new Insets(10, 20, 10, 20));
        sidePanel.add(tutorialChecklist, BorderLayout.CENTER);
        refreshChecklist();
   
    return sidePanel;
}


    private void loadAssets() {
        try {
            fullHeart = new ImageIcon("resources/ZihhFullHeart.png").getImage();
            halfHeart = new ImageIcon("resources/ZihhHalfHeart.png").getImage();
            emptyHeart = new ImageIcon("resources/ZihhEmptyHeart.png").getImage();
            gameOverImage = new ImageIcon("resources/Gameovernobutton.png").getImage();
            projectileImage = new ImageIcon("resources/coughattack.gif").getImage();
            slashImage = new ImageIcon("resources/slashattack.gif").getImage();
            lcActive = new ImageIcon("resources/LCattack active.png").getImage();
            lcDull = new ImageIcon("resources/LCattack dull.png").getImage();
            rcActive = new ImageIcon("resources/RCattack active.png").getImage();
            rcDull = new ImageIcon("resources/RCattack dull.png").getImage();
            qActive = new ImageIcon("resources/Qattack active.png").getImage();
            qDull = new ImageIcon("resources/Qattack dull.png").getImage();
            canInventoryImage = new ImageIcon("resources/canozihh inventory.png").getImage();
            canItemImage = new ImageIcon("resources/Can o' zihh.png").getImage();
            trashcanImage = new ImageIcon("resources/trashcan.png").getImage();
            zumImage = new ImageIcon("resources/zum.png").getImage();
            zumGrowImage = new ImageIcon("resources/ZUMGrow.gif").getImage();
            zumStayImage = new ImageIcon("resources/ZUMStay.gif").getImage();
            // preload the open variant so we can swap without creating new icons during gameplay
            try { trashcanOpenImage = new ImageIcon("resources/trashcan_open.png").getImage(); } catch (Exception ex) { trashcanOpenImage = trashcanImage; }
        } catch (Exception e) { e.printStackTrace(); }
    }


    private void initializeMap() {
        try {
            worldMapImage = ImageIO.read(new File("resources/tutorialMap.png"));
            collisionMap = ImageIO.read(new File("resources/tutorialMapCol.png"));
            worldWidth = (int) (worldMapImage.getWidth() * MAP_SCALE);
            worldHeight = (int) (worldMapImage.getHeight() * MAP_SCALE);
        } catch (Exception e) {
            worldWidth = 4000; worldHeight = 4000;
        }
    }


    public void initializeUI() {
        gameFrame = new JFrame("Zihh Game - O Block");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // --- TASKBAR ICON ---
        ImageIcon originalLogoImage = new ImageIcon("resources/zihhlogo.png");
        Image scaledLogoImage = originalLogoImage.getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Scale logo
        gameFrame.setIconImage(scaledLogoImage);


        TutorialScreenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                camX = (int) player.getX() - getWidth() / 2;
                camY = (int) player.getY() - getHeight() / 2;
                camX = Math.max(0, Math.min(camX, worldWidth - getWidth()));
                camY = Math.max(0, Math.min(camY, worldHeight - getHeight()));
                g2d.translate(-camX, -camY);


                if (worldMapImage != null) g.drawImage(worldMapImage, 0, 0, worldWidth, worldHeight, this);


                // Draw World items (cans / zum pickups)
                for (int i = 0; i < canItems.size(); i++) {
                    Point can = canItems.get(i);
                    String type = (canItemTypes.size() > i) ? canItemTypes.get(i) : "can";
                    Image img = "zum".equals(type) ? zumImage : canItemImage;
                    g.drawImage(img, can.x, can.y, 50, 75, this);
                }
               
                // Draw Trashcans
                for (Trashcan t : trashcans) {
                    Graphics2D g2dt = (Graphics2D) g.create();
                    g2dt.translate(t.x + 50, t.y + 50);
                    g2dt.rotate(t.angle);
                    g2dt.drawImage(t.image, -50, -50, 100, 100, this);
                    g2dt.dispose();
                }


                // Draw Entities
                for (int i = 0; i < zombieTrail.size(); i++)
                    g.drawImage(getZombieImage(zombieTrailTypes.get(i)), zombieTrail.get(i).x, zombieTrail.get(i).y, 100, 100, this);
                for (AttackingAlly ally : activeAllies) {
                    Graphics2D a2d = (Graphics2D) g.create();
                    a2d.translate(ally.x + 50, ally.y + 50);
                    a2d.rotate(ally.angle + Math.PI / 2 + Math.PI);
                    a2d.drawImage(ally.img, -50, -50, 100, 100, this);
                    a2d.dispose();
                }


                player.draw(g);
                if (isAttacking) {
                    Graphics2D s2d = (Graphics2D) g.create();
                    s2d.translate(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2);
                    s2d.rotate(player.getAngle());
                    s2d.drawImage(slashImage, 5, -60, 120, 120, this);
                    s2d.dispose();
                }


                for (double[] proj : projectiles) {
                    Graphics2D p2d = (Graphics2D) g.create();
                    p2d.translate(proj[0], proj[1]);
                    p2d.rotate(proj[2] - Math.PI / 2);
                    p2d.drawImage(projectileImage, -60, -60, 120, 120, this);
                    p2d.dispose();
                }


                for (EnemyProjectile proj : enemyProjectiles) {
                    g.setColor(Color.BLUE); g.fillRect((int)proj.x - 5, (int)proj.y - 5, 10, 10);
                }


                // Add Zum drawing here
                for (ZumProjectile z : zumProjectiles) {
                    Graphics2D z2d = (Graphics2D) g.create();
                    int w = (z.state == ZumProjectile.State.MOVING) ? 50 : (int)(50 * zumScaleX);
                    int h = (z.state == ZumProjectile.State.MOVING) ? 100 : (int)(100 * zumScaleY);
                    z2d.translate(z.x, z.y);
                    if (z.state == ZumProjectile.State.MOVING) z2d.rotate(z.rotationAngle);
                    int drawX = -w/2;
                    int drawY = (z.state == ZumProjectile.State.MOVING) ? -h : -h/2;
                    z2d.drawImage(z.currentImage, drawX, drawY, w, h, this);
                    z2d.dispose();
                }


                if (boxActive && currentBoxImage != null) {
                    Graphics2D e2d = (Graphics2D) g.create();
                    e2d.translate(boxX + boxSize / 2, boxY + boxSize / 2);
                    e2d.rotate(boxAngle + Math.PI / 2 + Math.PI);
                    e2d.drawImage(currentBoxImage, -boxSize / 2, -boxSize / 2, boxSize, boxSize, this);
                    if (currentWeaponImage != null) e2d.drawImage(currentWeaponImage, (int)(boxSize * 0.4), -boxSize/2, boxSize, boxSize, this);
                    e2d.dispose();
                }


                g2d.translate(camX, camY); // HUD
                for (int i = 0; i < 3; i++) {
                    int x = 20 + (i * 60);
                    int rev = 2 - i;
                    Image hrt = (health >= rev * 2 + 2) ? fullHeart : (health == rev * 2 + 1 ? halfHeart : emptyHeart);
                    g.drawImage(hrt, x, 20, 50, 50, this);
                }


                // Inventory HUD
                if (canInventoryImage != null) {
                    int invX = getWidth() - 325;
                    g.drawImage(canInventoryImage, invX, 20, 325, 150, this);
                    int startX = invX + 35;
                    for (int i = 0; i < inventorySlots.length; i++) {
                        if (inventorySlots[i] != null) {
                            int x = startX + (i * 70);
                            g.drawImage(inventorySlots[i], x, 70, 50, 50, this);
                        }
                    }
                }
                drawAttackHUD(g);
                if (gameOver) {
                    g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };


        TutorialScreenPanel.setBackground(Color.DARK_GRAY);
        TutorialScreenPanel.setFocusable(true);
        addListeners();


        // --- NEW SPLIT LAYOUT CODE ---
        JPanel mainContainer = new JPanel(new BorderLayout());
   
        // 1. Add your Game Panel to the CENTER (It takes all remaining space)
        mainContainer.add(TutorialScreenPanel, BorderLayout.CENTER);
   
        // 2. Add the Text Panel to the EAST (Right Side)
        mainContainer.add(createSidePanel(), BorderLayout.EAST);
   
        // 3. Add the container to the frame
        gameFrame.add(mainContainer);
        // -----------------------------


        gameFrame.setVisible(true);
        TutorialScreenPanel.requestFocusInWindow();
    }


    private void drawAttackHUD(Graphics g) {
        int startX = 20, spacing = 120, iconSize = 100, y = TutorialScreenPanel.getHeight() - 130;
        for (int i = 0; i < 3; i++) {
            int x = startX + (i * spacing); Image img = null;
            if (i == 0) img = (System.currentTimeMillis() - lastShotTime < 200) ? lcActive : lcDull;
            else if (i == 1) img = isAttacking ? rcActive : rcDull;
            else if (i == 2) img = (System.currentTimeMillis() - lastAllyLaunchTime < 200) ? qActive : qDull;
            if (img != null) g.drawImage(img, x, y, iconSize, iconSize, null);
        }
    }


    private void addListeners() {
        TutorialScreenPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                activeKeys.add(e.getKeyCode());
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_E && !gameOver && canPickup) attemptPickup(); // Pickup Can o' Zihh (requires unlock)
                if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_4) { // Use inventory slot
                    int slot = code - KeyEvent.VK_1;
                    if (inventorySlots[slot] != null) {
                        if (!canUseItem) return; // prevent use until unlocked
                        String t = inventoryTypes[slot];
                        if ("zum".equals(t)) {
                            // throw ZUM from inventory (same as previous G behavior)
                            launchZum(currentMouseX, currentMouseY);
                            inventorySlots[slot] = null; inventoryTypes[slot] = null;
                        } else {
                            // default: consume can to heal
                            if (health < 6) {
                                health++; inventorySlots[slot] = null; inventoryTypes[slot] = null;
                                playSoundEffect("Audio/SFX/heartpop.wav");
                                playSoundEffect("Audio/SFX/tssss(fizzlesound).wav");
                                playSoundEffect("Audio/SFX/burp.wav");
                            }
                        }
                    }
                }
                if (code == KeyEvent.VK_Q && !zombieTrail.isEmpty() && !gameOver && qReady && canAllySend) {
                    launchAlly(); lastAllyLaunchTime = System.currentTimeMillis(); qReady = false;
                    // Unlock pickup after first ally send
                    if (!canPickup) { canPickup = true; refreshChecklist(); }
                    playSoundEffect(new Random().nextBoolean() ? "Audio/SFX/getem!.wav" : "Audio/SFX/runforest!.wav");
                }
                if (code == KeyEvent.VK_SPACE && !gameOver) {
                    // Dodge roll - unlock on first use
                    if (!canDodgeRoll) { canDodgeRoll = true; refreshChecklist(); }
                }
                // G key left in place but no longer required; inventory ZUMs trigger throw via number keys
            }
            @Override public void keyReleased(KeyEvent e) { activeKeys.remove(e.getKeyCode()); }
        });


        TutorialScreenPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                player.updateAngle(e.getX() + camX, e.getY() + camY);
                int panelWidth = TutorialScreenPanel.getWidth();
                int panelHeight = TutorialScreenPanel.getHeight();
                double camX_calc = Math.max(0, Math.min(player.getX() - panelWidth / 2.0, worldWidth - panelWidth));
                double camY_calc = Math.max(0, Math.min(player.getY() - panelHeight / 2.0, worldHeight - panelHeight));
                currentMouseX = (int)(e.getX() + camX_calc);
                currentMouseY = (int)(e.getY() + camY_calc);
            }
        });


        TutorialScreenPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) return;
                int mx = e.getX() + camX, my = e.getY() + camY;
                if (SwingUtilities.isLeftMouseButton(e) && lcReady && canCough) {
                    projectiles.add(new double[]{player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0, player.getAngle(), 0.0});
                    playSoundEffect("Audio/SFX/caugh.wav"); lastShotTime = System.currentTimeMillis(); lcReady = false;
                    // Unlock ally send after first cough
                    if (!canAllySend) { canAllySend = true; refreshChecklist(); }
                }
                if (SwingUtilities.isRightMouseButton(e) && rcReady && canSlash) {
                    // Environmental Interaction: Trashcan
                    for (Trashcan t : trashcans) {
                        if (t.getBounds().contains(mx, my) && !t.locked) {
                            double dx = player.getX() - t.x, dy = player.getY() - t.y;
                            t.angle = Math.atan2(dy, dx) + TRASHCAN_ANGLE_OFFSET;
                            if (trashcanOpenImage != null) t.image = trashcanOpenImage;
                            t.locked = true;
                            TutorialScreen.this.spawnCansFromTrashcan(t);
                            break;
                        }
                    }
                    if (!isAttacking) {
                        isAttacking = true; attackStartTime = System.currentTimeMillis();
                        performMeleeAttack(); rcReady = false;
                        // Unlock cough after first successful slash
                        if (!canCough) { canCough = true; refreshChecklist(); }
                    }
                }
            }
        });
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;
        long now = System.currentTimeMillis();
        updateEnemyWeapons();


        if (!lcReady && now - lastShotTime >= shotCooldown) lcReady = true;
        if (!qReady && now - lastAllyLaunchTime >= allyLaunchCooldown) qReady = true;
        if (isAttacking && now - attackStartTime >= attackDuration) {
            isAttacking = false;
            rcReady = true;
        }


        if (now - lastCanSpawnTime >= canSpawnInterval) {
            if (Math.random() < 0.125) spawnCan();
            lastCanSpawnTime = now;
        }
        if (now - lastTrashcanSpawnTime >= trashcanSpawnInterval) {
            if (Math.random() < 0.1) spawnTrashcan();
            lastTrashcanSpawnTime = now;
        }


        // --- PLAYER MOVEMENT WITH INCREASED SLIDE BUFFER ---
        boolean up = activeKeys.contains(KeyEvent.VK_W);
        boolean down = activeKeys.contains(KeyEvent.VK_S);
        boolean left = activeKeys.contains(KeyEvent.VK_A);
        boolean right = activeKeys.contains(KeyEvent.VK_D);
       
        handleMovementSounds((up || down || left || right), activeKeys.contains(KeyEvent.VK_SHIFT));


        double speed = (activeKeys.contains(KeyEvent.VK_SHIFT) && canSprint) ? 7.0 : 4.0;
        double dx = 0, dy = 0;
       
        if (up) dy -= speed;
        if (down) dy += speed;
        if (left) dx -= speed;
        if (right) dx += speed;
       
        if (dx != 0 && dy != 0) { dx /= 1.414; dy /= 1.414; }


        // track movement for unlock progression
        double prevX = player.getX();
        double prevY = player.getY();
        double nX = prevX + dx;
        double nY = prevY + dy;
        double pBuf = 50.0; // INCREASE THIS to make the "jump" to the next stair bigger


        // Check X movement with buffer
        double checkX = dx > 0 ? nX + player.getWidth()/2.0 + pBuf : nX + player.getWidth()/2.0 - pBuf;
        if (nX >= 0 && nX <= worldWidth - player.getWidth()) {
            if (isLocationPassable(checkX, player.getY() + player.getHeight() / 2.0)) {
                player.setX(nX);
            }
        }
       
        // Check Y movement with buffer
        double checkY = dy > 0 ? nY + player.getHeight()/2.0 + pBuf : nY + player.getHeight()/2.0 - pBuf;
        if (nY >= 0 && nY <= worldHeight - player.getHeight()) {
            if (isLocationPassable(player.getX() + player.getWidth() / 2.0, checkY)) {
                player.setY(nY);
            }
        }


        // Update distance moved and unlock sprint when threshold reached
        double moved = Math.hypot(player.getX() - prevX, player.getY() - prevY);
        if (moved > 0) {
            // mark that player has moved at least once
            if (!canMove) { canMove = true; refreshChecklist(); }
            totalDistanceMoved += moved;
            if (!canSprint && totalDistanceMoved >= SPRINT_UNLOCK_DISTANCE) {
                canSprint = true; refreshChecklist();
            }
            // If sprint is used while unlocked, unlock slash
            if (canSprint && !canSlash && activeKeys.contains(KeyEvent.VK_SHIFT) && (dx != 0 || dy != 0)) {
                canSlash = true; refreshChecklist();
            }
        }


        updateZombieTrail();
        updateCombatEntities();
        updateEnemyProjectiles();
        updateZumProjectiles();
        TutorialScreenPanel.repaint();
    }


    private void attemptPickup() {
        double px = player.getX() + player.getWidth()/2.0, py = player.getY() + player.getHeight()/2.0;
        Iterator<Point> it = canItems.iterator();
        int idx = 0;
        while (it.hasNext()) {
            Point can = it.next();
            String type = (canItemTypes.size() > idx) ? canItemTypes.get(idx) : "can";
            if (Math.sqrt(Math.pow(can.x+25-px, 2) + Math.pow(can.y+25-py, 2)) <= 100.0f) {
                for (int i = 0; i < 4; i++) {
                    if (inventorySlots[i] == null) {
                        inventorySlots[i] = "zum".equals(type) ? zumImage : canItemImage;
                        inventoryTypes[i] = type;
                        it.remove(); if (canItemTypes.size() > idx) canItemTypes.remove(idx);
                        playSoundEffect("Audio/SFX/Thank you!.wav");
                        // unlocking item use after first pickup
                        if (!canUseItem) { canUseItem = true; refreshChecklist(); }
                        return;
                    }
                }
            }
            idx++;
        }
    }


    private void updateEnemyWeapons() {
        if (!boxActive || currentWeaponImage == null) return;
        long now = System.currentTimeMillis();
        if (currentWeaponType.equals("bleachGun1") && now - lastEnemyShotTime >= SEMI_COOLDOWN) {
            fireEnemyProjectile(); lastEnemyShotTime = now;
        } else if (currentWeaponType.equals("bleachGun2")) {
            if (shotsInBurst < BURST_SIZE && now - lastEnemyShotTime >= (shotsInBurst == 0 ? BURST_COOLDOWN : BURST_DELAY)) {
                fireEnemyProjectile(); lastEnemyShotTime = now; shotsInBurst++;
                if (shotsInBurst >= BURST_SIZE) shotsInBurst = 0;
            }
        }
    }


    private void fireEnemyProjectile() {
        double ox = boxX + boxSize/2 + Math.cos(boxAngle)*boxSize*0.8;
        double oy = boxY + boxSize/2 + Math.sin(boxAngle)*boxSize*0.8;
        enemyProjectiles.add(new EnemyProjectile(ox, oy, boxAngle));
        playSoundEffect("Audio/SFX/EnemyShot.wav");
    }


    private void updateEnemyProjectiles() {
        Iterator<EnemyProjectile> it = enemyProjectiles.iterator();
        Rectangle pRect = new Rectangle((int)player.getX(), (int)player.getY(), (int)player.getWidth(), (int)player.getHeight());
       
        while (it.hasNext()) {
            EnemyProjectile p = it.next();
            p.move();


            // 1. Check Building Collision
            if (!isLocationPassable(p.x, p.y)) {
                it.remove();
                continue;
            }


            // 2. Check Player Collision
            if (pRect.intersects(p.getBounds())) {
                if (health > 2) health--; // Limit: don't lose more than 4 HP for tutorial
                it.remove();
                //if (health <= 0) triggerGameOver();
                continue;
            }


            // 3. Check Ally Collision (Turn back to enemy)
            boolean hitAlly = false;
            Iterator<AttackingAlly> allyIt = activeAllies.iterator();
            while (allyIt.hasNext()) {
                AttackingAlly ally = allyIt.next();
                if (new Rectangle((int)ally.x, (int)ally.y, 100, 100).intersects(p.getBounds())) {
                    // If hit, remove ally and respawn as box (enemy)
                    boxActive = false; // Force current box to reset or just spawn new
                    boxX = (int)ally.x;
                    boxY = (int)ally.y;
                    boxHealth = 3;
                    // Logic to restore original enemy image could be added here
                    boxActive = true;
                    allyIt.remove();
                    hitAlly = true;
                    break;
                }
            }


            if (hitAlly) {
                it.remove();
                continue;
            }


            // 4. Check Range/Bounds
            if (p.isExpired() || p.x < 0 || p.x > worldWidth || p.y < 0 || p.y > worldHeight) {
                it.remove();
            }
        }
    }


    private void updateZumProjectiles() {
        Iterator<ZumProjectile> it = zumProjectiles.iterator();
        while (it.hasNext()) {
            ZumProjectile p = it.next();
            p.update();
            if (p.state == ZumProjectile.State.STAYING && System.currentTimeMillis() - p.stateStartTime >= p.stayDuration) {
                it.remove();
            } else if (p.x < 0 || p.x > worldWidth || p.y < 0 || p.y > worldHeight) {
                it.remove();
            }
        }
    }


    private void updateCombatEntities() {
        activeAllies.removeIf(ally -> {
            if (!boxActive) return false;
            ally.move(boxX, boxY);
            if (new Rectangle((int)ally.x, (int)ally.y, 100, 100).intersects(new Rectangle(boxX, boxY, boxSize, boxSize))) {
                if (System.currentTimeMillis() % 300 < 20) {
                    ally.health--;
                    damageBox(1);
                }
            }
            return ally.health <= 0;
        });


        if (boxActive) {
            boolean isOnZum = false;
            for (ZumProjectile z : zumProjectiles) {
                if (z.state == ZumProjectile.State.STAYING && z.getBounds().contains(boxX, boxY)) {
                    isOnZum = true;
                    break;
                }
            }
           
            double effectiveSpeed = isOnZum ? 1.5 : 3.0;
            double dx = player.getX() - boxX;
            double dy = player.getY() - boxY;
            double dist = Math.sqrt(dx * dx + dy * dy);
           
           
           
            boxAngle = Math.atan2(dy, dx);
           
            if (new Rectangle((int)player.getX(), (int)player.getY(), (int)player.getWidth(), (int)player.getHeight())
                .intersects(new Rectangle(boxX, boxY, boxSize, boxSize))) {
                if (++damageCounter >= damageSpeed) {
                    if (health > 2) health--; // Limit: don't lose more than 4 HP
                    damageCounter = 0;
                    //if (health <= 0) triggerGameOver();
                }
            }
        } else {
            spawnBox();
        }


        projectiles.removeIf(p -> {
            p[0] += Math.cos(p[2]) * projectileSpeed;
            p[1] += Math.sin(p[2]) * projectileSpeed;
            p[3] += projectileSpeed;


            // Check Building Collision for player projectiles
            if (!isLocationPassable(p[0], p[1])) {
                return true;
            }


            if (boxActive && new Rectangle(boxX, boxY, boxSize, boxSize).contains(p[0], p[1])) {
                addZombieAt((int)p[0], (int)p[1]);
                damageBox(3);
                return true;
            }
            return p[3] > projectileRange;
        });
    }


    private void spawnBox() {
        int attempts = 0;
        do {
            boxX = (int)(Math.random()*(worldWidth-boxSize));
            boxY = (int)(Math.random()*(worldHeight-boxSize));
            attempts++;
        } while (!isLocationPassable(boxX + boxSize/2, boxY + boxSize/2) && attempts < 100);


        boxHealth = 3;
        String[] types = {"aaron.png", "basic1.png", "basic2.png", "jacob.png", "pablo.png"};
        currentBoxType = types[new Random().nextInt(types.length)];
        currentBoxImage = new ImageIcon("resources/" + currentBoxType).getImage();
        int w = new Random().nextInt(3);
        currentWeaponType = w==1 ? "bleachGun1" : (w==2 ? "bleachGun2" : "none");
        currentWeaponImage = currentWeaponType.equals("none") ? null : new ImageIcon("resources/" + (w==1?"bleachgun-1000.png":"bleachgun-3000.png")).getImage();
        boxActive = true;
        shotsInBurst = 0;
    }


    private void damageBox(int d) { boxHealth -= d; if (boxHealth <= 0) { boxActive = false; enemyProjectiles.clear(); } }


    private void performMeleeAttack() {
        if (!boxActive) return;
        double dist = Math.sqrt(Math.pow(boxX+boxSize/2.0-(player.getX()+player.getWidth()/2.0), 2) + Math.pow(boxY+boxSize/2.0-(player.getY()+player.getHeight()/2.0), 2));
        playSoundEffect("Audio/SFX/hwack! (meele sound).wav");
        if (dist < attackRange) { damageBox(3); playSoundEffect("Audio/SFX/STUUUUFFFEEDD.wav"); }
    }


    private void launchAlly() {
        if (zombieTrail.isEmpty()) return;
        int idx = new Random().nextInt(zombieTrail.size());
        Point p = zombieTrail.remove(idx); String t = zombieTrailTypes.remove(idx);
        activeAllies.add(new AttackingAlly(p.x, p.y, getZombieImage(t), t, 2.5));
        // unlocking pickup when ally is first sent
        if (!canPickup) { canPickup = true; refreshChecklist(); }
    }
    private void launchZum(int tx, int ty) {
        zumProjectiles.add(new ZumProjectile(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0, tx, ty));
}


    private void spawnCan() {
        int cx, cy;
        int attempts = 0;
        do {
            cx = (int)(Math.random()*(worldWidth-50));
            cy = (int)(Math.random()*(worldHeight-50));
            attempts++;
        } while (!isLocationPassable(cx + 25, cy + 37) && attempts < 50);
       
        canItems.add(new Point(cx, cy));
        canItemTypes.add("can");
    }


    private void spawnTrashcan() {
        int tx, ty;
        int attempts = 0;
        do {
            tx = (int)(Math.random()*(worldWidth-100));
            ty = (int)(Math.random()*(worldHeight-100));
            attempts++;
        } while (!isLocationPassable(tx + 50, ty + 50) && attempts < 50);
       
        trashcans.add(new Trashcan(tx, ty, trashcanImage));
    }


    // Spawn 0-2 cans near a trashcan when it is opened.
    // Probabilities: 40% -> 0, 40% -> 1, 20% -> 2
    private void spawnCansFromTrashcan(Trashcan t) {
        // Spawn cans and zums independently. Each uses probabilities: 40% -> 0, 40% -> 1, 20% -> 2.
        int countCan;
        int countZum;
        double r1 = Math.random();
        countCan = r1 < 0.4 ? 0 : (r1 < 0.8 ? 1 : 2);
        double r2 = Math.random();
        countZum = r2 < 0.4 ? 0 : (r2 < 0.8 ? 1 : 2);


        for (int i = 0; i < countCan; i++) {
            int offsetX = (int) (Math.random() * (t.getBounds().width + 50)) - 25;
            int offsetY = (int) (Math.random() * (t.getBounds().height + 50)) - 25;
            int cx = t.x + 50 + offsetX;
            int cy = t.y + 50 + offsetY;
            cx = Math.max(0, Math.min(cx, worldWidth - 50));
            cy = Math.max(0, Math.min(cy, worldHeight - 50));
            canItems.add(new Point(cx, cy));
            canItemTypes.add("can");
        }


        for (int i = 0; i < countZum; i++) {
            int offsetX = (int) (Math.random() * (t.getBounds().width + 50)) - 25;
            int offsetY = (int) (Math.random() * (t.getBounds().height + 50)) - 25;
            int zx = t.x + 50 + offsetX;
            int zy = t.y + 50 + offsetY;
            zx = Math.max(0, Math.min(zx, worldWidth - 50));
            zy = Math.max(0, Math.min(zy, worldHeight - 50));
            canItems.add(new Point(zx, zy));
            canItemTypes.add("zum");
        }
    }


    private void updateZombieTrail() {
        if (zombieTrail.isEmpty()) return;


        double hX = (player.getX() + player.getWidth()/2) + Math.cos(player.getAngle() + Math.PI) * 80;
        double hY = (player.getY() + player.getHeight()/2) + Math.sin(player.getAngle() + Math.PI) * 80;
       
        Point lead = zombieTrail.get(0);
        double ldx = (hX - 50) - lead.x;
        double ldy = (hY - 50) - lead.y;
       
        // Lead zombie sliding
        if (isLocationPassable(lead.x + ldx + 50, lead.y + 50)) lead.x += ldx;
        if (isLocationPassable(lead.x + 50, lead.y + ldy + 50)) lead.y += ldy;


        for (int i = 1; i < zombieTrail.size(); i++) {
            Point prev = zombieTrail.get(i - 1);
            Point curr = zombieTrail.get(i);
           
            double d = Math.sqrt(Math.pow(prev.x - curr.x, 2) + Math.pow(prev.y - curr.y, 2));
           
            if (d > zombieSpacing) {
                double moveX = ((prev.x - curr.x) / d) * (d - zombieSpacing);
                double moveY = ((prev.y - curr.y) / d) * (d - zombieSpacing);
               
                // --- ADDED TRAIL BUFFER ---
                double tBuf = 30.0; // Increase this to make the "jump" bigger


                // Check X with buffer look-ahead
                double checkX = moveX > 0 ? curr.x + moveX + 50 + tBuf : curr.x + moveX + 50 - tBuf;
                if (isLocationPassable(checkX, curr.y + 50)) {
                    curr.x += moveX;
                }
               
                // Check Y with buffer look-ahead
                double checkY = moveY > 0 ? curr.y + moveY + 50 + tBuf : curr.y + moveY + 50 - tBuf;
                if (isLocationPassable(curr.x + 50, checkY)) {
                    curr.y += moveY;
                }
            }
        }
    }


    private void handleMovementSounds(boolean m, boolean s) {
        if (m && !isWalkingSoundPlaying) playWalkingSound(); else if (!m && isWalkingSoundPlaying) stopWalkingSound();
        if (s && m && !isRunningSoundPlaying) playRunningSound(); else if ((!s || !m) && isRunningSoundPlaying) stopRunningSound();
    }




    private void playSoundEffect(String p) {
        new Thread(() -> { try { Clip c = AudioSystem.getClip(); c.open(AudioSystem.getAudioInputStream(new File(p))); c.start(); } catch (Exception e) {} }).start();
    }


    private void playWalkingSound() {
        isWalkingSoundPlaying = true;
        try { currentWalkingClip = AudioSystem.getClip(); currentWalkingClip.open(AudioSystem.getAudioInputStream(new File("Audio/SFX/walkingsound.wav"))); currentWalkingClip.loop(Clip.LOOP_CONTINUOUSLY); currentWalkingClip.start(); } catch (Exception e) {}
    }
    private void stopWalkingSound() { isWalkingSoundPlaying = false; if (currentWalkingClip != null) { currentWalkingClip.stop(); currentWalkingClip = null; } }
    private void playRunningSound() {
        isRunningSoundPlaying = true;
        try { currentRunningClip = AudioSystem.getClip(); currentRunningClip.open(AudioSystem.getAudioInputStream(new File("Audio/SFX/runningsound.wav"))); currentRunningClip.loop(Clip.LOOP_CONTINUOUSLY); currentRunningClip.start(); } catch (Exception e) {}
    }
    private void stopRunningSound() { isRunningSoundPlaying = false; if (currentRunningClip != null) { currentRunningClip.stop(); currentRunningClip = null; } }


    private void startMusic() {
        new Thread(() -> { try { currentMusicClip = AudioSystem.getClip(); currentMusicClip.open(AudioSystem.getAudioInputStream(new File("Audio/Music/Loonboon_KLICKAUD.wav"))); currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY); currentMusicClip.start(); } catch (Exception e) {} }).start();
    }


    private void makeTrail() { zombieTrail.clear(); zombieTrailTypes.clear(); }
    public void addZombieAt(int x, int y) { zombieTrail.add(new Point(x, y)); zombieTrailTypes.add(currentBoxType); }


    private boolean isLocationPassable(double worldX, double worldY) {
        if (collisionMap == null) return true;
       
        // Map world coordinates back to image pixels based on your MAP_SCALE
        int imgX = (int) (worldX / MAP_SCALE);
        int imgY = (int) (worldY / MAP_SCALE);


        // Boundary check to prevent Crash
        if (imgX < 0 || imgX >= collisionMap.getWidth() || imgY < 0 || imgY >= collisionMap.getHeight()) {
            return false;
        }


        // Get ARGB value: if Alpha is 0, the pixel is transparent (passable)
        int pixel = collisionMap.getRGB(imgX, imgY);
        int alpha = (pixel >> 24) & 0xff;
        return alpha == 0;
    }


    private Image getZombieImage(String t) {
        if (zombieImageCache.containsKey(t)) return zombieImageCache.get(t);
        String f = switch (t) {
            case "jacob.png" -> "zacob.png"; case "pablo.png" -> "zablo.png";
            case "aaron.png" -> "zaron.png"; case "basic1.png" -> "zombie1.png";
            case "basic2.png" -> "zombie2.png"; default -> "zacob.png";
        };
        Image img = new ImageIcon("resources/" + f).getImage(); zombieImageCache.put(t, img); return img;
    }


    private void setupGameOverButtons() {
        TutorialScreenPanel.setLayout(null);
        restartBtn = new JButton("RESTART"); menuBtn = new JButton("MAIN MENU");
        for (JButton b : new JButton[]{restartBtn, menuBtn}) {
            b.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); b.setVisible(false); TutorialScreenPanel.add(b);
        }
        restartBtn.setBounds(600, 750, 220, 70); menuBtn.setBounds(1100, 750, 220, 70);
        restartBtn.addActionListener(e -> { gameFrame.dispose(); new TutorialScreen(); });
        menuBtn.addActionListener(e -> { gameFrame.dispose(); new MainScreen(); });
    }
    static void waitTime(int milliseconds){
        try{Thread.sleep(milliseconds);}catch(Exception e){System.out.println("Zach's sleep schedule");}
    }    




    private void refreshChecklist() {
        StringBuilder sb = new StringBuilder();
        sb.append("TUTORIAL\n\n");
        sb.append("CONTROLS:\n");
        sb.append("----------------\n");
        sb.append("W, A, S, D  : Move\n");
        sb.append("SHIFT       : Sprint\n");
        sb.append("E           : Pickup Item\n");
        sb.append("ABILITIES:\n");
        sb.append("----------------\n");
        sb.append("L-CLICK     : Shoot\n");
        sb.append("R-CLICK     : Melee\n");
        sb.append("Q           : Ally Attack\n");
        sb.append("----------------\n");
        sb.append("TO DO LIST:\n");
        sb.append("[" + (canSprint ? "X" : " ") + "] Move\n");
        sb.append("[" + (canSlash ? "X" : " ") + "] Sprint\n");
        sb.append("[" + (canCough ? "X" : " ") + "] Slash\n");
        sb.append("[" + (canAllySend ? "X" : " ") + "] Cough\n");
        sb.append("[" + (canPickup ? "X" : " ") + "] Ally Send\n");
        sb.append("[" + (canUseItem ? "X" : " ") + "] Item Pickup\n");
        sb.append("[" + (canUseItem ? "X" : " ") + "] Use Item\n");
        sb.append("[" + (canDodgeRoll ? "X" : " ") + "] Dodge Roll\n");
            if (tutorialChecklist != null) tutorialChecklist.setText(sb.toString());
        }
    }

