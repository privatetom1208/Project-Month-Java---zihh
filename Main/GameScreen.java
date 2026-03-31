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

public class GameScreen extends JPanel implements ActionListener {

    private JFrame gameFrame;
    private JPanel gameScreenPanel;
    private Timer timer;
    private PlayerSprite player;

    // --- MAP & CAMERA ---
    private BufferedImage worldMapImage;
    private BufferedImage collisionMap;
    private double MAP_SCALE = 4.0;
    private int worldWidth, worldHeight;
    private int camX = 0, camY = 0;

    // --- CORE VARIABLES ---
    public static float masterVolume = 1.0f;
    public static float musicVolume = 1.0f;
    private JButton restartBtn, menuBtn;
    private int health = 6;
    private volatile Clip currentMusicClip;
    private boolean gameOver = false;
    private Image fullHeart, halfHeart, emptyHeart, gameOverImage;

    // ===== SCORE =====
    private int score = 0;

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
    private long attackDuration = 500;
    private long lastMeleeTime = 0;
    private long meleeCooldown = 800; 
    private final int attackRange = 160;
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
    private Image drActive, drDull; // Add these
    private boolean drReady = true;  // Add this
    private List<AttackingAlly> activeAllies = new ArrayList<>();

    // Enemy
    private int boxSize = 100;
    private int damageSpeed = 50;
    private List<Enemy> activeEnemies = new ArrayList<>();
    private int enemiesPerSpawn = 2; // Variable to control how many enemies spawn at once
    int round = 1;

    // Enemy Weapon System
    private List<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private long lastEnemyShotTime = 0;
    private int shotsInBurst = 0;
    private final int BURST_SIZE = 3;
    private final long BURST_DELAY = 300, BURST_COOLDOWN = 6000, SEMI_COOLDOWN = 3000;
    private final double WEAPON_PROJECTILE_SPEED = 7.0;

//Killcount = 0;     "kill or infect " + goal - Killcount " enemies!"
// if (goal - killcount <= 0){ previousGoal = goal; new round(); goal = previousGoal * 2 } }
//enemiesperSpawn = goal;

    //Wave Spawn Control
    int killcount = 0;
    int goal = 2;
    int previousGoal = 1;
    private boolean waveComplete = false;
    private long waveCompleteStartTime = 0;
    private final long waveCompleteDuration = 3000; // 3 seconds


    //Cough
private List<PlayerCough> playerProjectiles = new ArrayList<>();

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
    
    // Enemy spawning interval
    private long lastEnemySpawnTime = 0;
    private long enemySpawnInterval = 10000; // 8 seconds, decreases per round
    private final long baseEnemySpawnInterval = 10000;
    
    private final Set<Integer> activeKeys = new HashSet<>();

    // Tracking melee kills and infections
    private int meleeKillCount = 0;
    private int infectionCount = 0;
    // Threshold for triggering power-up selection (increases by 5 each time)
    private int meleeThreshold = 5;

    // Power-up selection/pause UI
    private boolean showingPowerUpSelection = false;
    private Timer powerUpTimer = null; // runs while paused to animate selection
    private Image puAttackSpeedImg, puAttackStrengthImg, puSprintImg, puBleedImg;
    private Image puDodgerollImg;
    private Rectangle[] puRects = null;
    private int[] puCurrentY = null;
    private int puStartY = -250;
    private int puTargetY = 180;
    private long puAnimStart = 0;
    private long puAnimDuration = 600; // ms

    // Gameplay modifiers (applied by power-ups)
    // melee damage represented as percent of enemy max HP
    private double meleeDamage = 50.0; // 50%
    private double sprintSpeed = 7.0;
    private boolean bleedPowerSelected = false; // whether player selected bleed power-up
    private boolean dodgerollSelected = false; // whether player selected dodgeroll power-up
    private long invincibleUntil = 0; // timestamp while player is invincible

    // Dodge roll variables
    private boolean isDodgeRolling = false;
    private long dodgeRollStartTime = 0;
    private final long dodgeRollDuration = 800; // 800ms dodge roll duration
    private double dodgeRollRotationSpeed = 0.2; // radians per frame for spinning rotation
    private double dodgeRollAngle = 0; // current rotation angle during dodge roll
    private double dodgeRollDirection = 0; // direction angle to travel during dodge roll

    //Upgrade Variables
    private int currentHoverIdx = -1;
    private String[] puDescriptions = {
    "ATTACK SPEED: Reduces melee cooldown (Grants BLEED at max).", // Updated
    "ATTACK STRENGTH: Increases melee damage by 10 HP.",
    "MOBILITY: Increases sprint speed (Grants Dodge Roll at max).",
    "BLEED: Melee hits deal damage over time (3 seconds)."
};
    List<Image> availableImgs = new ArrayList<>();
    List<String> availableDescs = new ArrayList<>();


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

    private static void applyVolume(Clip clip, float volume) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gain.getMinimum();
            float max = gain.getMaximum();
            gain.setValue(min + (max - min) * volume);
        } catch (Exception ignored) {}
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
        final double range = 750.0; // Twice as far as projectileRange

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

  private class Enemy {
        int x, y;
        public int maxHealth = 100;
        int health = maxHealth;
        String boxType = "";
        Image boxImage = null, weaponImage = null;
        String weaponType = "";
        double angle = 0;
        int damageCounter = 0;
        boolean lastDamageByMelee = false;
        long flashUntil = 0;
        int bleedTicksLeft = 0;
        long bleedLastTickTime = 0;
        long lastShotTime = 0; 
        boolean isBoss = false;
        double scale = 1.0;

        public Enemy(int x, int y, boolean isBoss) {
            this.x = x;
            this.y = y;
            this.isBoss = isBoss;
            this.scale = isBoss ? 2.5 : 1.0;
            this.maxHealth = isBoss ? 1000 : 100;
            this.health = this.maxHealth;
            
            String[] types = {"aaron.png", "basic1.png", "basic2.png", "jacob.png", "pablo.png"};
            this.boxType = types[new Random().nextInt(types.length)];
            this.boxImage = new ImageIcon("resources/" + boxType).getImage();
            
            if (isBoss) {
                this.weaponType = "bleachGun2";
                this.weaponImage = new ImageIcon("resources/bleachgun-3000.png").getImage();
            } else {
                int w = new Random().nextInt(3);
                this.weaponType = w == 1 ? "bleachGun1" : (w == 2 ? "bleachGun2" : "none");
                this.weaponImage = weaponType.equals("none") ? null : new ImageIcon("resources/" + (w == 1 ? "bleachgun-1000.png" : "bleachgun-3000.png")).getImage();
            }
        }
        
        public void applyDamage(int d, boolean byMelee) {
            this.lastDamageByMelee = byMelee;
            this.health = Math.max(0, this.health - d);
            if (byMelee) this.flashUntil = System.currentTimeMillis() + 150;
        }

        public boolean isAlive() { return health > 0; }

        /**
         * The PURPLE SQUARE
         * Used for: Player attacking the enemy (Melee/Cough).
         * Scaled to 2x the visual size.
         */
        public Rectangle getBounds() {
            int visualSize = (int)(boxSize * scale);
            int hitboxSize = (int)(visualSize * 2); 
            
            int offset = (hitboxSize - visualSize) / 2;
            return new Rectangle(x - offset, y - offset, hitboxSize, hitboxSize);
        }

        /**
         * The YELLOW SQUARE
         * Used for: Enemy dealing damage to the player.
         * For bosses, this is smaller (60% of body) to allow "safe" melee.
         */
        public Rectangle getDamageBounds() {
            int visualSize = (int)(boxSize * scale);
            double dangerScale = isBoss ? 0.6 : 1.0; 
            int damageSize = (int)(visualSize * dangerScale);
            
            int offset = (damageSize - visualSize) / 2;
            return new Rectangle(x - offset, y - offset, damageSize, damageSize);
        }
    }

    private class Trashcan {
        int x, y;
        Image image;
        double angle = 0;
        boolean locked = false;
        long openedTime = 0;
        static final long DESPAWN_DELAY = 10000;

        public Trashcan(int x, int y, Image image) {
            this.x = x;
            this.y = y;
            this.image = image;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, 100, 100);
        }

        public boolean shouldDespawn() {
            return locked && System.currentTimeMillis() - openedTime >= DESPAWN_DELAY;
        }
    }

    public GameScreen() {
        loadAssets();
        initializeMap();
        player = new PlayerSprite(worldWidth / 2, worldHeight / 2, 50, 50);
        startMusic();
        makeTrail();
        initializeUI();
        setupGameOverButtons();
        timer = new Timer(10, this);
        timer.start();
        spawnEnemies();
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
            puAttackSpeedImg = new ImageIcon("resources/attackspeedup.png").getImage();
            puAttackStrengthImg = new ImageIcon("resources/attackstrength up.png").getImage();
            puSprintImg = new ImageIcon("resources/sprintspeedup.png").getImage();
            puBleedImg = new ImageIcon("resources/bleedattack.png").getImage();
            puDodgerollImg = new ImageIcon("resources/dodgeroll.png").getImage();
            drActive = new ImageIcon("resources/DodgeRollActive.png").getImage();
            drDull = new ImageIcon("resources/DodgeRollDull.png").getImage();
            // preload the open variant so we can swap without creating new icons during gameplay
            try { trashcanOpenImage = new ImageIcon("resources/trashcan_open.png").getImage(); } catch (Exception ex) { trashcanOpenImage = trashcanImage; }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void initializeMap() {
        try {
            worldMapImage = ImageIO.read(new File("resources/oblock.png"));
            collisionMap = ImageIO.read(new File("resources/oblockcol.png"));
            worldWidth = (int) (worldMapImage.getWidth() * MAP_SCALE);
            worldHeight = (int) (worldMapImage.getHeight() * MAP_SCALE);
        } catch (Exception e) { 
            worldWidth = 4000; worldHeight = 4000; 
        }
    }

    public void initializeUI() {
        gameFrame = new JFrame("Zihh Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // --- TASKBAR ICON ---
        ImageIcon originalLogoImage = new ImageIcon("resources/zihhlogo.png");
        Image scaledLogoImage = originalLogoImage.getImage()
                .getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Scale logo
        gameFrame.setIconImage(scaledLogoImage);
        
        gameScreenPanel = new JPanel() {
           @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;

    // --- 1. CAMERA CALCULATION ---
    camX = (int) player.getX() - getWidth() / 2;
    camY = (int) player.getY() - getHeight() / 2;
    camX = Math.max(0, Math.min(camX, worldWidth - getWidth()));
    camY = Math.max(0, Math.min(camY, worldHeight - getHeight()));

    // Apply Camera Translation
    g2d.translate(-camX, -camY);

    // --- 2. WORLD & MAP RENDERING ---
    if (worldMapImage != null) {
        g.drawImage(worldMapImage, 0, 0, worldWidth, worldHeight, this);
    }

    // Trashcans
    for (Trashcan t : trashcans) {
        Graphics2D g2dt = (Graphics2D) g.create();
        g2dt.translate(t.x + 50, t.y + 50);
        g2dt.rotate(t.angle);
        g2dt.drawImage(t.image, -50, -50, 100, 100, this);
        g2dt.dispose();
    }

    // World Items (Cans/Zums)
    for (int i = 0; i < canItems.size(); i++) {
        Point can = canItems.get(i);
        String type = (canItemTypes.size() > i) ? canItemTypes.get(i) : "can";
        Image img = "zum".equals(type) ? zumImage : canItemImage;
        g.drawImage(img, can.x, can.y, 50, 75, this);
    }

    // --- 3. ENTITIES (Zombies & Enemies) ---
    // Zombie Snake Trail
    for (int i = 0; i < zombieTrail.size(); i++) {
        g.drawImage(getZombieImage(zombieTrailTypes.get(i)), zombieTrail.get(i).x, zombieTrail.get(i).y, 100, 100, this);
    }

    // Attacking Allies
    for (AttackingAlly ally : activeAllies) {
        Graphics2D a2d = (Graphics2D) g.create();
        a2d.translate(ally.x + 50, ally.y + 50);
        a2d.rotate(ally.angle + Math.PI / 2 + Math.PI);
        a2d.drawImage(ally.img, -50, -50, 100, 100, this);
        a2d.dispose();
    }

    // Enemies
    for (Enemy enemy : activeEnemies) {
        Graphics2D e2d = (Graphics2D) g.create();
        int currentBoxSize = (int)(boxSize * enemy.scale);
        
        // Render Enemy Sprite
        e2d.translate(enemy.x + currentBoxSize / 2, enemy.y + currentBoxSize / 2);
        e2d.rotate(enemy.angle + Math.PI / 2 + Math.PI);
        e2d.drawImage(enemy.boxImage, -currentBoxSize / 2, -currentBoxSize / 2, currentBoxSize, currentBoxSize, this);
        
        // Render Enemy Weapon
        if (enemy.weaponImage != null) {
            e2d.drawImage(enemy.weaponImage, (int)(currentBoxSize * 0.4), -currentBoxSize / 2, currentBoxSize, currentBoxSize, this);
        }

        // Damage Flash Effect
        if (System.currentTimeMillis() < enemy.flashUntil) {
            e2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            e2d.setColor(Color.RED);
            e2d.fillRect(-currentBoxSize / 2, -currentBoxSize / 2, currentBoxSize, currentBoxSize);
        }
        e2d.dispose();

        // Bleed Particles
        if (enemy.bleedTicksLeft > 0) {
            g.setColor(Color.RED);
            int pCount = 6;
            for (int pi = 0; pi < pCount; pi++) {
                double angle = (2 * Math.PI / pCount) * pi + (System.currentTimeMillis() % 500) / 500.0 * Math.PI * 2;
                int ox = (int)(Math.cos(angle) * (currentBoxSize/2 + 6));
                int oy = (int)(Math.sin(angle) * (currentBoxSize/2 + 6));
                g.fillRect(enemy.x + currentBoxSize/2 + ox - 3, enemy.y + currentBoxSize/2 + oy - 3, 6, 6);
            }
        }
    }

    // --- 4. PLAYER & ATTACK EFFECTS ---
    long now = System.currentTimeMillis();
    if (now < invincibleUntil) {
        Graphics2D pg = (Graphics2D) g.create();
        pg.translate(player.getX() + player.getWidth() / 2.0, player.getY() + player.getHeight() / 2.0);
        pg.scale(1.18, 1.18);
        pg.translate(-(player.getX() + player.getWidth() / 2.0), -(player.getY() + player.getHeight() / 2.0));
        player.draw(pg);
        pg.dispose();
    } else {
        player.draw(g);
    }

    // Melee Slash
    if (isAttacking) {
        Graphics2D s2d = (Graphics2D) g.create();
        s2d.translate(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2);
        s2d.rotate(player.getAngle());
        s2d.drawImage(slashImage, 5, -60, 120, 120, this);
        s2d.dispose();
    }

    // Piercing Cough
    for (PlayerCough proj : playerProjectiles) {
        Graphics2D p2d = (Graphics2D) g.create();
        p2d.translate(proj.x, proj.y);
        p2d.rotate(proj.angle - Math.PI / 2);
        p2d.drawImage(projectileImage, -60, -60, 120, 120, this);
        p2d.dispose();
    }

    // Enemy Projectiles
    g.setColor(Color.BLUE);
    for (EnemyProjectile proj : enemyProjectiles) {
        g.fillRect((int)proj.x - 5, (int)proj.y - 5, 10, 10);
    }

    // Zum Projectiles
    for (ZumProjectile z : zumProjectiles) {
        Graphics2D z2d = (Graphics2D) g.create();
        int w = (z.state == ZumProjectile.State.MOVING) ? 50 : (int)(50 * zumScaleX);
        int h = (z.state == ZumProjectile.State.MOVING) ? 100 : (int)(100 * zumScaleY);
        z2d.translate(z.x, z.y);
        if (z.state == ZumProjectile.State.MOVING) z2d.rotate(z.rotationAngle);
        z2d.drawImage(z.currentImage, -w/2, (z.state == ZumProjectile.State.MOVING) ? -h : -h/2, w, h, this);
        z2d.dispose();
    }

    // --- 5. HUD RENDERING (STATIC) ---
    g2d.translate(camX, camY);

    // Goal UI
    String killText = "Enemies Infected: " + killcount + "/" + goal;
    g.setFont(new Font("Comic Sans MS", Font.BOLD, 22));
    g.setColor(Color.YELLOW);
    int sx = (getWidth() - g.getFontMetrics().stringWidth(killText)) / 2;
    g.drawString(killText, sx, 30);

    // Hearts
    for (int i = 0; i < 3; i++) {
        int x = 20 + (i * 60);
        int rev = 2 - i;
        Image hrt = (health >= rev * 2 + 2) ? fullHeart : (health == rev * 2 + 1 ? halfHeart : emptyHeart);
        g.drawImage(hrt, x, 20, 50, 50, this);
    }

    // Round Counter
    g.setColor(Color.YELLOW);
    g.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
    g.drawString("ROUND: " + round, getWidth() - 200, getHeight() - 40);

    // Inventory
    if (canInventoryImage != null) {
        int invX = getWidth() - 325;
        g.drawImage(canInventoryImage, invX, 20, 325, 150, this);
        for (int i = 0; i < inventorySlots.length; i++) {
            if (inventorySlots[i] != null) {
                g.drawImage(inventorySlots[i], invX + 35 + (i * 70), 70, 50, 50, this);
            }
        }
    }

    drawAttackHUD(g);

    if (waveComplete) drawWaveCompleteScreen(g);
    if (gameOver) {
        g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), this);

        // ===== DRAW SCORE ON GAME OVER =====
        g.setFont(new Font("Comic Sans MS", Font.BOLD, 40));
        g.setColor(Color.RED);
        g.drawString("FINAL SCORE: " + score,
                    gameScreenPanel.getWidth() / 2 - 180,
                    gameScreenPanel.getHeight() / 2 + 120);
    }
    // ===== SCORE HUD (BOTTOM OF SCREEN) =====
    g.setFont(new Font("Comic Sans MS", Font.BOLD, 30));
    g.setColor(Color.RED);

    String scoreText = "Score: " + score;

    // 20 px from left, 20 px from bottom
    g.drawString(scoreText, (getWidth() - g.getFontMetrics().stringWidth(scoreText)) / 2, getHeight() - 20);

    // Power-Up UI
    if (showingPowerUpSelection && puRects != null) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < availableImgs.size(); i++) {
            g.drawImage(availableImgs.get(i), puRects[i].x, puRects[i].y, 220, 220, this);
            g.setColor(Color.WHITE);
            g.drawRect(puRects[i].x, puRects[i].y, 220, 220);
        }
        if (currentHoverIdx != -1 && currentHoverIdx < availableDescs.size()) {
            g.setFont(new Font("Comic Sans MS", Font.ITALIC, 24));
            g.setColor(Color.YELLOW);
            String d = availableDescs.get(currentHoverIdx);
            g.drawString(d, (getWidth() - g.getFontMetrics().stringWidth(d)) / 2, puTargetY + 280);
        }
    }
}
        };

        gameScreenPanel.setBackground(Color.DARK_GRAY);
        gameScreenPanel.setFocusable(true);
        addListeners();
        gameFrame.add(gameScreenPanel);
        gameFrame.setVisible(true);
        gameScreenPanel.requestFocusInWindow();
    }

    private void drawAttackHUD(Graphics g) {
    int startX = 20, spacing = 120, iconSize = 100, y = gameScreenPanel.getHeight() - 130;
    
    for (int i = 0; i < 4; i++) { // Changed from 3 to 4
        int x = startX + (i * spacing); 
        Image img = null;
        
        if (i == 0) img = (System.currentTimeMillis() - lastShotTime < 200) ? lcActive : lcDull;
        else if (i == 1) img = isAttacking ? rcActive : rcDull;
        else if (i == 2) img = (System.currentTimeMillis() - lastAllyLaunchTime < 200) ? qActive : qDull;
        else if (i == 3 && dodgerollSelected) { // Draw Dodge Roll if unlocked
            img = isDodgeRolling ? drActive : drDull;
        }
        
        if (img != null) g.drawImage(img, x, y, iconSize, iconSize, null);
    }
}
    private class PlayerCough {
        double x, y, angle, distance;
        Set<Enemy> hitEnemies = new HashSet<>(); // Tracks who has already been infected

        public PlayerCough(double x, double y, double angle) {
            this.x = x; this.y = y; this.angle = angle; this.distance = 0;
        }
    }

    private void drawWaveCompleteScreen(Graphics g) {
        int panelWidth = gameScreenPanel.getWidth();
        int panelHeight = gameScreenPanel.getHeight();
        
        // Draw semi-transparent grey background
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(226, 101, 101, 180)); // Grey with 180 alpha for transparency
        g2d.fillRect(0, 0, panelWidth, panelHeight);
        
        // Calculate countdown (0-3 seconds)
        long elapsed = System.currentTimeMillis() - waveCompleteStartTime;
        int secondsRemaining = (int)((waveCompleteDuration - elapsed) / 1000) + 1;
        if (secondsRemaining < 0) secondsRemaining = 0;
        
        // Draw "Wave Complete!" text
        Font largeFont = new Font("Comic Sans MS", Font.BOLD, 72);
        g.setFont(largeFont);
        g.setColor(new Color(137, 0, 0));
        FontMetrics fm = g.getFontMetrics();
        String completeText = "Wave Complete!";
        int textWidth = fm.stringWidth(completeText);
        int x = (panelWidth - textWidth) / 2;
        int y = (panelHeight / 2) - 80;
        g.drawString(completeText, x, y);
        
        // Draw "Next wave starts in_" text with countdown
        Font smallFont = new Font("Comic Sans MS", Font.PLAIN, 36);
        g.setFont(smallFont);
        g.setColor(Color.RED);
        String countdownText = "Next wave starts in " + secondsRemaining;
        fm = g.getFontMetrics();
        textWidth = fm.stringWidth(countdownText);
        x = (panelWidth - textWidth) / 2;
        y = (panelHeight / 2) + 40;
        g.drawString(countdownText, x, y);
    }

    private void addListeners() {
    gameScreenPanel.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            activeKeys.add(e.getKeyCode());
            int code = e.getKeyCode();
            
            // Dodge roll key
            if (code == KeyEvent.VK_SPACE && dodgerollSelected && !isDodgeRolling && !gameOver) {
                isDodgeRolling = true;
                dodgeRollStartTime = System.currentTimeMillis();
                dodgeRollAngle = player.getAngle();
                double px = player.getX() + player.getWidth() / 2.0;
                double py = player.getY() + player.getHeight() / 2.0;
                dodgeRollDirection = Math.atan2(currentMouseY - py, currentMouseX - px);
                playSoundEffect("Audio/SFX/WHOOSH.wav"); 
            }
            
//                         // Inside addListeners -> keyPressed
//             if (code == KeyEvent.VK_I && !gameOver) {
//                 for (Enemy enemy : activeEnemies) {
//                     if (enemy.isBoss) {
//                         // Set health to 0 to trigger the cleanup logic in actionPerformed
//                         enemy.health = 0; 
//                         System.out.println("Dev Tool: Boss Terminated.");
//                     }
//                 }
//             }
//             if (code == KeyEvent.VK_O && !gameOver) {
//     for (Enemy enemy : activeEnemies) {
//         if (!enemy.isBoss) {
//             // Set health to 0 to trigger the cleanup logic in actionPerformed
//             enemy.health = 0; 
//             System.out.println("Dev Tool: Enemy Terminated.");
//             break; // Remove 'break' if you want to kill ALL non-boss enemies at once
//         }
//     }
// }

            if (code == KeyEvent.VK_E && !gameOver) attemptPickup(); 
            
            if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_4) {
                int slot = code - KeyEvent.VK_1;
                if (inventorySlots[slot] != null) {
                    String t = inventoryTypes[slot];
                    if ("zum".equals(t)) {
                        launchZum(currentMouseX, currentMouseY);
                        inventorySlots[slot] = null; inventoryTypes[slot] = null;
                    } else {
                        if (health < 6) {
                            health++; 
                            inventorySlots[slot] = null; inventoryTypes[slot] = null;
                            playSoundEffect("Audio/SFX/heartpop.wav");
                            playSoundEffect("Audio/SFX/tssss(fizzlesound).wav");
                            playSoundEffect("Audio/SFX/burp.wav");
                        }
                    }
                }
            }
            
            if (code == KeyEvent.VK_Q && !zombieTrail.isEmpty() && !gameOver && qReady) {
                launchAlly(); 
                lastAllyLaunchTime = System.currentTimeMillis(); 
                qReady = false;
                playSoundEffect(new Random().nextBoolean() ? "Audio/SFX/getem!.wav" : "Audio/SFX/runforest!.wav");
            }
        }
        @Override public void keyReleased(KeyEvent e) { activeKeys.remove(e.getKeyCode()); }
    });

    gameScreenPanel.addMouseMotionListener(new MouseMotionAdapter() {
        @Override 
        public void mouseMoved(MouseEvent e) { 
            if (!isDodgeRolling) {
                player.updateAngle(e.getX() + camX, e.getY() + camY);
            }
            int panelWidth = gameScreenPanel.getWidth();
            int panelHeight = gameScreenPanel.getHeight();
            double camX_calc = Math.max(0, Math.min(player.getX() - panelWidth / 2.0, worldWidth - panelWidth));
            double camY_calc = Math.max(0, Math.min(player.getY() - panelHeight / 2.0, worldHeight - panelHeight));
            currentMouseX = (int)(e.getX() + camX_calc);
            currentMouseY = (int)(e.getY() + camY_calc);
        }
    });

    gameScreenPanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            // 1. Power-Up Selection Logic
            if (showingPowerUpSelection) {
                Point p = e.getPoint();
                if (puRects != null) {
                    for (int i = 0; i < puRects.length; i++) {
                        if (puRects[i] != null && puRects[i].contains(p)) {
                            applyPowerUp(i);
                            return;
                        }
                    }
                }
                return;
            }

            if (gameOver) return;

            int mx = e.getX() + camX;
            int my = e.getY() + camY;

            // 2. LEFT CLICK: Piercing Cough Attack
            if (SwingUtilities.isLeftMouseButton(e) && lcReady) {
                double startX = player.getX() + player.getWidth() / 2.0;
                double startY = player.getY() + player.getHeight() / 2.0;
                
                // This adds the projectile to the NEW list used for piercing
                playerProjectiles.add(new PlayerCough(startX, startY, player.getAngle()));
                
                playSoundEffect("Audio/SFX/caugh.wav"); 
                lastShotTime = System.currentTimeMillis(); 
                lcReady = false;
            }

            // 3. RIGHT CLICK: Interaction and Melee
            if (SwingUtilities.isRightMouseButton(e)) {
                // Check Trashcans
                for (Trashcan t : trashcans) {
                    if (t.getBounds().contains(mx, my) && !t.locked) {
                        double dx = player.getX() - t.x;
                        double dy = player.getY() - t.y;
                        t.angle = Math.atan2(dy, dx) + TRASHCAN_ANGLE_OFFSET;
                        if (trashcanOpenImage != null) t.image = trashcanOpenImage;
                        t.locked = true;
                        t.openedTime = System.currentTimeMillis();
                        spawnCansFromTrashcan(t);
                        break;
                    }
                }

                // Trigger Melee Attack
                if (rcReady && !isAttacking) {
                    isAttacking = true; 
                    attackStartTime = System.currentTimeMillis();
                    lastMeleeTime = attackStartTime;
                    performMeleeAttack(); 
                    rcReady = false;
                }
            }
        }
    });
}

    @Override
public void actionPerformed(ActionEvent e) {
    if (gameOver) return;

    // --- 1. WAVE COMPLETION DETECTION ---
    if (killcount >= goal && !waveComplete) {
        waveComplete = true;
        waveCompleteStartTime = System.currentTimeMillis();
        
        playerProjectiles.clear();
        enemyProjectiles.clear();
        projectiles.clear(); 
        
        playSoundEffect("Audio/SFX/wave_clear.wav"); 
    }

    // --- 2. WAVE TRANSITION LOGIC ---
   // --- 2. WAVE TRANSITION LOGIC ---
if (waveComplete) {
    long elapsed = System.currentTimeMillis() - waveCompleteStartTime;
    if (elapsed >= waveCompleteDuration) {
        round++;
        killcount = 0;
        
        // If the NEW round is a boss round, set goal to 1
        if (round % 5 == 0) {
            previousGoal = goal; // Save the last regular enemy count
            goal = 1; 
        } else {
            // If we just finished a boss round, resume scaling from previousGoal
            // Otherwise, scale normally
            if ((round - 1) % 5 == 0) {
                goal = previousGoal * 2; 
            } else {
                goal = goal * 2;
            }
        }
        
        activeEnemies.clear(); 
        enemySpawnInterval = Math.max(5800, (long)(baseEnemySpawnInterval * Math.pow(0.85, round - 1)));
        waveComplete = false;
        
        spawnEnemies();
    }
    gameScreenPanel.repaint();
    return; 
}
    
    long now = System.currentTimeMillis();

    // --- 3. COOLDOWN MANAGEMENT ---
    if (!lcReady && now - lastShotTime >= shotCooldown) lcReady = true;
    if (!qReady && now - lastAllyLaunchTime >= allyLaunchCooldown) qReady = true;
    if (!rcReady && now - lastMeleeTime >= meleeCooldown) rcReady = true;
    
    if (isAttacking && now - attackStartTime >= attackDuration) {
        isAttacking = false;
    }

    // --- 4. PERIODIC SPAWNING ---
    if (now - lastEnemySpawnTime >= enemySpawnInterval && killcount < goal) {
        spawnEnemies();
        lastEnemySpawnTime = now;
    }

    if (now - lastCanSpawnTime >= canSpawnInterval) { 
        if (Math.random() < 0.125) spawnCan(); 
        lastCanSpawnTime = now; 
    }
    if (now - lastTrashcanSpawnTime >= trashcanSpawnInterval) { 
        if (Math.random() < 0.1) spawnTrashcan(); 
        lastTrashcanSpawnTime = now; 
    }

    // --- 5. PLAYER MOVEMENT & DODGE ROLL ---
    if (isDodgeRolling) {
        long dodgeElapsed = now - dodgeRollStartTime;
        if (dodgeElapsed < dodgeRollDuration) {
            dodgeRollAngle += dodgeRollRotationSpeed;
            player.setAngle(dodgeRollAngle);
            
            double dSpeed = sprintSpeed * 1.25;
            double dx = Math.cos(dodgeRollDirection) * dSpeed;
            double dy = Math.sin(dodgeRollDirection) * dSpeed;
            
            if (isLocationPassable(player.getX() + dx + player.getWidth()/2.0, player.getY() + player.getHeight()/2.0)) {
                player.setX(player.getX() + dx);
                player.setY(player.getY() + dy);
            }
        } else {
            isDodgeRolling = false;
            player.updateAngle(currentMouseX, currentMouseY);
        }
    } else {
        boolean up = activeKeys.contains(KeyEvent.VK_W);
        boolean down = activeKeys.contains(KeyEvent.VK_S);
        boolean left = activeKeys.contains(KeyEvent.VK_A);
        boolean right = activeKeys.contains(KeyEvent.VK_D);
        
        handleMovementSounds((up || down || left || right), activeKeys.contains(KeyEvent.VK_SHIFT));

        double speed = activeKeys.contains(KeyEvent.VK_SHIFT) ? sprintSpeed : 4.0;
        double dx = 0, dy = 0;
        if (up) dy -= speed; if (down) dy += speed;
        if (left) dx -= speed; if (right) dx += speed;
        if (dx != 0 && dy != 0) { dx /= 1.414; dy /= 1.414; }

        if (isLocationPassable(player.getX() + dx + player.getWidth()/2.0, player.getY() + dy + player.getHeight()/2.0)) {
            player.setX(player.getX() + dx);
            player.setY(player.getY() + dy);
        }
    }

    // --- 6. PLAYER PROJECTILE UPDATE ---
    playerProjectiles.removeIf(p -> {
        p.x += Math.cos(p.angle) * projectileSpeed; 
        p.y += Math.sin(p.angle) * projectileSpeed; 
        p.distance += projectileSpeed;
        for (Enemy enemy : activeEnemies) {
            // This uses the getBounds() which is the purple square!
            if (enemy.getBounds().contains(p.x, p.y) && !p.hitEnemies.contains(enemy)) {
                // Damage logic...
            }
        }
        if (!isLocationPassable(p.x, p.y)) return true;

        for (Enemy enemy : activeEnemies) {
            if (enemy.getBounds().contains(p.x, p.y) && !p.hitEnemies.contains(enemy)) {
                if (enemy.isBoss) {
                    enemy.applyDamage(enemy.maxHealth / 10, false);
                } else {
                    addZombieAt((int)p.x, (int)p.y, enemy.boxType);
                    enemy.applyDamage(enemy.maxHealth, false);
                }

                score += 20;

                p.hitEnemies.add(enemy); 
            }
        }
        return p.distance > projectileRange;
    });

    // --- 7. ENTITY UPDATES ---
    updateEnemyWeapons();
    updateEnemyProjectiles();
    updateCombatEntities(); 

    // --- 8. KILL CLEANUP & POWER-UP TRIGGER ---
    Iterator<Enemy> enemyIt = activeEnemies.iterator();
    while (enemyIt.hasNext()) {
        Enemy enemyItem = enemyIt.next();
        if (!enemyItem.isAlive()) {
            if (enemyItem.isBoss) {
                score += 100;
                killcount = goal; 
                playSoundEffect("Audio/SFX/boss_death.wav");
            } else {
                score += 20;
                killcount++;
            }

            if (enemyItem.lastDamageByMelee) {
                meleeKillCount++;
                playSoundEffect("Audio/SFX/STUUUUFFFEEDD.wav");
            }
            
            enemyIt.remove();

            if (meleeKillCount >= meleeThreshold && !showingPowerUpSelection) {
                showPowerUpSelection();
            }
        }
    }

    // --- 9. TRAIL & MISC UPDATES ---
    updateZombieTrail();
    updateTrashcans();
    updateZumProjectiles();

    gameScreenPanel.repaint();
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
                        playSoundEffect("Audio/SFX/Thank you!.wav"); return;
                    }
                }
            }
            idx++;
        }
    }

private void updateEnemyWeapons() {
    if (activeEnemies.isEmpty()) return;
    long now = System.currentTimeMillis();
    
    for (Enemy enemy : activeEnemies) {
        if (enemy.weaponImage == null) continue;
        
        long effectiveSemiCooldown = enemy.isBoss ? SEMI_COOLDOWN / 2 : SEMI_COOLDOWN;
        long effectiveBurstCooldown = enemy.isBoss ? BURST_COOLDOWN / 2 : BURST_COOLDOWN;
        int effectiveBurstSize = enemy.isBoss ? BURST_SIZE * 2 : BURST_SIZE;

        if (enemy.weaponType.equals("bleachGun1")) {
            // Use enemy.lastShotTime instead of the global lastEnemyShotTime
            if (now - enemy.lastShotTime >= effectiveSemiCooldown) {
                fireEnemyProjectile(enemy); 
                enemy.lastShotTime = now;
            }
        } else if (enemy.weaponType.equals("bleachGun2")) {
            boolean readyForNextBurstBall = (shotsInBurst > 0 && now - enemy.lastShotTime >= BURST_DELAY);
            boolean readyForNewBurstCycle = (shotsInBurst == 0 && now - enemy.lastShotTime >= effectiveBurstCooldown);

            if (shotsInBurst < effectiveBurstSize && (readyForNextBurstBall || readyForNewBurstCycle)) {
                fireEnemyProjectile(enemy);
                enemy.lastShotTime = now;
                shotsInBurst++;

                if (shotsInBurst >= effectiveBurstSize) {
                    shotsInBurst = 0;
                }
            }
        }
    }
}
    private void fireEnemyProjectile(Enemy enemy) {
        double ox = enemy.x + boxSize/2 + Math.cos(enemy.angle)*boxSize*0.8;
        double oy = enemy.y + boxSize/2 + Math.sin(enemy.angle)*boxSize*0.8;
        enemyProjectiles.add(new EnemyProjectile(ox, oy, enemy.angle));
        playSoundEffect("Audio/SFX/EnemyShot.wav");
    }

    private void updateEnemyProjectiles() {
    Iterator<EnemyProjectile> it = enemyProjectiles.iterator();
    
    // Create player bounds for collision check
    Rectangle pRect = new Rectangle((int)player.getX(), (int)player.getY(), (int)player.getWidth(), (int)player.getHeight());
    
    while (it.hasNext()) {
        EnemyProjectile p = it.next();
        p.move();

        // 1. Check World Map / Building Collision
        // We check the center of the projectile against the collision map
        if (!isLocationPassable(p.x, p.y)) {
            it.remove();
            continue;
        }

        // 2. Check Player Collision
        // If the player is currently dodge-rolling, they are invincible to projectiles
        if (pRect.intersects(p.getBounds())) {
            if (!isDodgeRolling && System.currentTimeMillis() > invincibleUntil) {
                health--; 
                // Optional: Play a "player hit" sound here
                playSoundEffect("Audio/SFX/player_hurt.wav");
                
                if (health <= 0) {
                    triggerGameOver();
                    return; // Exit method immediately if game is over
                }
            }
            it.remove(); 
            continue;
        }

        // 3. Check Ally Collision (The "Cure" Mechanic)
        // If an enemy projectile hits one of your zombie allies, they turn back into an enemy
        boolean hitAlly = false;
        Iterator<AttackingAlly> allyIt = activeAllies.iterator();
        while (allyIt.hasNext()) {
            AttackingAlly ally = allyIt.next();
            // Allies are 100x100
            if (new Rectangle((int)ally.x, (int)ally.y, 100, 100).intersects(p.getBounds())) {
                // Add a new enemy at the location where the ally was "cured"
                activeEnemies.add(new Enemy((int)ally.x, (int)ally.y, false));
                infectionCount++;
                allyIt.remove();
                hitAlly = true;
                break;
            }
        }

        if (hitAlly) {
            it.remove();
            continue;
        }

        // 4. Check Range and World Boundaries
        // Projectiles are removed if they exceed their max distance or leave the map
        if (p.isExpired() || p.x < 0 || p.x > worldWidth || p.y < 0 || p.y > worldHeight) {
            it.remove();
        }
    }
}

    private void updateTrashcans() {
        trashcans.removeIf(Trashcan::shouldDespawn);
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
        if (activeEnemies.isEmpty()) return false;
        
        // Allies target the first enemy in the list
        Enemy target = activeEnemies.get(0);
        ally.move((int)target.x, (int)target.y);
        
        // Allies are 100x100
        Rectangle allyRect = new Rectangle((int)ally.x, (int)ally.y, 100, 100);
        
        // Allies attack the Purple Square (getBounds) to ensure they can reach the boss
        if (allyRect.intersects(target.getBounds())) {
            if (System.currentTimeMillis() % 300 < 20) {
                ally.health--;
                target.applyDamage(1, false);
            }
        }
        return ally.health <= 0;
    });

    // Update each enemy behavior and player collision
    for (Enemy enemy : activeEnemies) {
        // 1. Slow down enemy if they are standing on a "Zum" puddle
        boolean isOnZum = false;
        for (ZumProjectile z : zumProjectiles) {
            if (z.state == ZumProjectile.State.STAYING && z.getBounds().intersects(enemy.getBounds())) {
                isOnZum = true;
                break;
            }
        }
        
        double effectiveSpeed = isOnZum ? 1.5 : 3.0;
        double dx = player.getX() - enemy.x;
        double dy = player.getY() - enemy.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        // 2. Enemy Movement with basic obstacle avoidance
        if (dist > effectiveSpeed) {
            double currentAngle = Math.atan2(dy, dx);
            double moveX = (dx / dist) * effectiveSpeed;
            double moveY = (dy / dist) * effectiveSpeed;
            double lookAhead = 25.0; 

            if (isLocationPassable(enemy.x + moveX + (boxSize / 2.0) + (Math.cos(currentAngle) * lookAhead), 
                                 enemy.y + moveY + (boxSize / 2.0) + (Math.sin(currentAngle) * lookAhead))) {
                enemy.x += moveX;
                enemy.y += moveY;
            } else {
                boolean moved = false;
                for (int angleOffset = 20; angleOffset <= 140; angleOffset += 20) {
                    double rad = Math.toRadians(angleOffset);
                    double[][] testDirs = {
                        {Math.cos(currentAngle + rad), Math.sin(currentAngle + rad)},
                        {Math.cos(currentAngle - rad), Math.sin(currentAngle - rad)}
                    };

                    for (double[] t : testDirs) {
                        double testX = t[0] * effectiveSpeed;
                        double testY = t[1] * effectiveSpeed;
                        if (isLocationPassable(enemy.x + testX + (boxSize / 2.0) + (t[0] * lookAhead), 
                                             enemy.y + testY + (boxSize / 2.0) + (t[1] * lookAhead))) {
                            enemy.x += testX;
                            enemy.y += testY;
                            moved = true;
                            break;
                        }
                    }
                    if (moved) break;
                }
            }
        }
        
        enemy.angle = Math.atan2(dy, dx);
        
        // 3. PLAYER DAMAGE LOGIC (The "Yellow Square" check)
        Rectangle pRect = new Rectangle((int)player.getX(), (int)player.getY(), (int)player.getWidth(), (int)player.getHeight());
        
        // Player only takes damage if hitting the smaller Yellow Hitbox
        if (pRect.intersects(enemy.getDamageBounds())) {
            if (++enemy.damageCounter >= damageSpeed) { 
                // Check for invincibility frames (dodge roll or post-hit)
                if (System.currentTimeMillis() >= invincibleUntil && !isDodgeRolling) {
                    health--; 
                    playSoundEffect("Audio/SFX/player_hurt.wav");
                    // Grant short invincibility after being hit
                    invincibleUntil = System.currentTimeMillis() + 1000; 
                }
                enemy.damageCounter = 0; 
                if (health <= 0) triggerGameOver(); 
            }
        }

        // 4. Bleed Damage processing
        if (enemy.bleedTicksLeft > 0) {
            long now = System.currentTimeMillis();
            if (now - enemy.bleedLastTickTime >= 200) {
                enemy.applyDamage(1, false);
                enemy.bleedTicksLeft--;
                enemy.bleedLastTickTime = now;
            }
        }
    }
}

    private void spawnEnemies() {
    // 1. BOSS SPAWNING LOGIC
    if (round % 5 == 0) {
        // Only spawn if no enemies exist (prevents double bosses)
        if (activeEnemies.isEmpty()) {
            int enemyX = (int)(Math.random() * (worldWidth - (boxSize * 2.5)));
            int enemyY = (int)(Math.random() * (worldHeight - (boxSize * 2.5)));
            activeEnemies.add(new Enemy(enemyX, enemyY, true));
            playSoundEffect("Audio/SFX/boss_spawn.wav");
        }
        return; 
    } 

    // 2. NORMAL SPAWNING LOGIC
    // Spawn regular enemies up to the goal limit
    for (int i = 0; i < enemiesPerSpawn; i++) {
        int attempts = 0;
        int enemyX, enemyY;
        
        do {
            enemyX = (int)(Math.random() * (worldWidth - boxSize));
            enemyY = (int)(Math.random() * (worldHeight - boxSize));
            attempts++;
        } while (!isLocationPassable(enemyX + boxSize/2, enemyY + boxSize/2) && attempts < 100);

        activeEnemies.add(new Enemy(enemyX, enemyY, false));
    }
    shotsInBurst = 0;
}

    private void performMeleeAttack() {
    if (activeEnemies.isEmpty()) return;
    playSoundEffect("Audio/SFX/hwack! (meele sound).wav");
    
    // Player center for distance/intersection
    double px = player.getX() + player.getWidth() / 2.0;
    double py = player.getY() + player.getHeight() / 2.0;

    for (Enemy enemy : activeEnemies) {
        Rectangle bounds = enemy.getBounds();
        double enemyCenterX = bounds.getCenterX();
        double enemyCenterY = bounds.getCenterY();

        double dist = Math.sqrt(Math.pow(enemyCenterX - px, 2) + Math.pow(enemyCenterY - py, 2));

        // Using the purple square (bounds) for collision
        if (dist < attackRange || bounds.intersects(new Rectangle((int)player.getX(), (int)player.getY(), (int)player.getWidth(), (int)player.getHeight()))) { 
            
            // APPLY FLAT DAMAGE
            int dmg = (int)Math.round(meleeDamage);
            enemy.applyDamage(dmg, true);

            score += 10;
            
            if (bleedPowerSelected && enemy.bleedTicksLeft == 0) {
                enemy.bleedTicksLeft = 15;
                enemy.bleedLastTickTime = System.currentTimeMillis();
            }
        }
    }
}

    private void launchAlly() {
        if (zombieTrail.isEmpty()) return;
        int idx = new Random().nextInt(zombieTrail.size());
        Point p = zombieTrail.remove(idx); String t = zombieTrailTypes.remove(idx);
        activeAllies.add(new AttackingAlly(p.x, p.y, getZombieImage(t), t, 2.5));
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

        if (countCan == 0 && countZum == 0) playSoundEffect("Audio/SFX/puh!(emptysound).wav");

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

    private void triggerGameOver() {
        if (gameOver) return; gameOver = true;
        if (currentMusicClip != null) currentMusicClip.stop();
        stopWalkingSound(); stopRunningSound();
        playSoundEffect("Audio/SFX/auuuuuugggggg.wav");
        timer.stop(); restartBtn.setVisible(true); menuBtn.setVisible(true);
        gameScreenPanel.repaint();
    }

    private void playSoundEffect(String p) {
        new Thread(() -> {
            try {
                Clip c = AudioSystem.getClip();
                c.open(AudioSystem.getAudioInputStream(new File(p)));
                applyVolume(c, masterVolume);
                c.start();
            } catch (Exception e) {}
        }).start();
    }

    private void playWalkingSound() {
        isWalkingSoundPlaying = true;
        try {
            currentWalkingClip = AudioSystem.getClip();
            currentWalkingClip.open(AudioSystem.getAudioInputStream(
                new File("Audio/SFX/walkingsound.wav")
            ));
            applyVolume(currentWalkingClip, masterVolume);
            currentWalkingClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentWalkingClip.start();
        } catch (Exception e) {}
    }
    private void stopWalkingSound() { isWalkingSoundPlaying = false; if (currentWalkingClip != null) { currentWalkingClip.stop(); currentWalkingClip = null; } }

    private void playRunningSound() {
        isRunningSoundPlaying = true;
        try {
            currentRunningClip = AudioSystem.getClip();
            currentRunningClip.open(AudioSystem.getAudioInputStream(
                new File("Audio/SFX/runningsound.wav")
            ));
            applyVolume(currentRunningClip, masterVolume);
            currentRunningClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentRunningClip.start();
        } catch (Exception e) {}
    }
    private void stopRunningSound() { isRunningSoundPlaying = false; if (currentRunningClip != null) { currentRunningClip.stop(); currentRunningClip = null; } }

    private void startMusic() {
        new Thread(() -> {
            try {
                currentMusicClip = AudioSystem.getClip();
                currentMusicClip.open(AudioSystem.getAudioInputStream(
                    new File("Audio/Music/Loonboon_KLICKAUD.wav")
                ));
                applyVolume(currentMusicClip, masterVolume * musicVolume);
                currentMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                currentMusicClip.start();
            } catch (Exception e) {}
        }).start();
    }

    private void makeTrail() { zombieTrail.clear(); zombieTrailTypes.clear(); }
    public void addZombieAt(int x, int y, String zombieType) { 
        zombieTrail.add(new Point(x, y)); 
        zombieTrailTypes.add(zombieType);
    }

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
        gameScreenPanel.setLayout(null);
        restartBtn = new JButton("RESTART"); menuBtn = new JButton("MAIN MENU");
        for (JButton b : new JButton[]{restartBtn, menuBtn}) {
            b.setFont(new Font("Comic Sans MS", Font.BOLD, 20)); b.setVisible(false); gameScreenPanel.add(b);
        }
        restartBtn.setBounds(600, 750, 220, 70); menuBtn.setBounds(1100, 750, 220, 70);
        restartBtn.addActionListener(e -> { gameFrame.dispose(); new GameScreen(); });
        menuBtn.addActionListener(e -> { gameFrame.dispose(); new MainScreen(); });
    }

  private void showPowerUpSelection() {
    showingPowerUpSelection = true;
    currentHoverIdx = -1;
    if (timer != null) timer.stop();

    // Reset the lists so they only contain the 3 current options
    availableImgs.clear();
    availableDescs.clear();

    // Now add the 3 options (This runs ONCE, preventing the freeze)
    if (!bleedPowerSelected) {
        availableImgs.add(meleeCooldown > 500 ? puAttackSpeedImg : puBleedImg);
        availableDescs.add(meleeCooldown > 500 ? puDescriptions[0] : puDescriptions[3]);
    }
    
    availableImgs.add(puAttackStrengthImg);
    availableDescs.add(puDescriptions[1]);

    if (!dodgerollSelected) {
        availableImgs.add(sprintSpeed < 8.5 ? puSprintImg : puDodgerollImg);
        availableDescs.add(sprintSpeed < 8.5 ? puDescriptions[2] : "DODGE ROLL: Press SPACE to dash.");
    }

    // Capture the current count of buttons to display
    final int count = availableImgs.size();
    puRects = new Rectangle[count];
    puCurrentY = new int[count];
    
    // Initialize the animation positions
    for (int i = 0; i < count; i++) {
        puCurrentY[i] = puStartY;
        puRects[i] = new Rectangle(0, 0, 0, 0); 
    }
    
    puAnimStart = System.currentTimeMillis();

    // Start the animation timer for the selection screen
    powerUpTimer = new Timer(10, ev -> {
        long now = System.currentTimeMillis();
        double t = Math.min(1.0, (double) (now - puAnimStart) / puAnimDuration);
        
        int iconW = 220, iconH = 220, spacing = 60;
        int totalW = iconW * count + spacing * (count - 1);
        int startX = (gameScreenPanel.getWidth() - totalW) / 2;

        for (int i = 0; i < count; i++) {
            // Smoothly move icons from puStartY to puTargetY
            puCurrentY[i] = (int) (puStartY + (puTargetY - puStartY) * t);
            puRects[i].setBounds(startX + i * (iconW + spacing), puCurrentY[i], iconW, iconH);
        }

        // Handle Hover Logic
        Point mouseP = gameScreenPanel.getMousePosition();
        currentHoverIdx = -1;
        if (mouseP != null) {
            for (int i = 0; i < count; i++) {
                if (puRects[i].contains(mouseP)) {
                    currentHoverIdx = i;
                    break;
                }
            }
        }
        
        // Refresh the panel to show animation frames
        gameScreenPanel.repaint();
    });
    
    powerUpTimer.start();
}

    private void applyPowerUp(int idx) {
    // We recreate the logic flow to see what was actually in that slot
    List<String> activeTypes = new ArrayList<>();
    
    if (!bleedPowerSelected) activeTypes.add(meleeCooldown > 500 ? "SPEED" : "BLEED");
    activeTypes.add("STRENGTH");
    if (!dodgerollSelected) activeTypes.add(sprintSpeed < 8.5 ? "MOBILITY" : "DODGE");

    // Safety check: make sure the click index is valid
    if (idx < 0 || idx >= activeTypes.size()) return;

    String selected = activeTypes.get(idx);

    switch (selected) {
        case "SPEED":    meleeCooldown = Math.max(500, meleeCooldown - 100); break;
        case "BLEED":    bleedPowerSelected = true; break;
        
        // UPDATE THIS CASE
        case "STRENGTH": meleeDamage += 10.0; break; 
        
        case "MOBILITY": sprintSpeed = Math.min(8.5, sprintSpeed + 0.5); break;
        case "DODGE":    dodgerollSelected = true; break;
    }

    // Resume the game
    showingPowerUpSelection = false;
    if (powerUpTimer != null) { powerUpTimer.stop(); powerUpTimer = null; }
    meleeKillCount = 0;
    meleeThreshold += 5;
    if (timer != null) timer.start();
}
    static void waitTime(int milliseconds){
        try{Thread.sleep(milliseconds);}catch(Exception e){System.out.println("Zach's sleep schedule");}
    }    
    public static void main(String[] args) { SwingUtilities.invokeLater(GameScreen::new); }
}