package PlayerMovement;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerSprite {
    private double x, y;
    private int width, height;
    private double angle = 0;
    private BufferedImage spriteImage;

    public PlayerSprite(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width * 2;
        this.height = height * 2;

        // Load the zombie image using a relative file path
        try {
            File imgFile = new File("resources/Zihh.png");
            
            if (imgFile.exists()) {
                spriteImage = ImageIO.read(imgFile);
                System.out.println("Successfully loaded Zihh.png");
            } else {
                System.err.println("Could not find image at: " + imgFile.getAbsolutePath());
                System.err.println("Fallback: Drawing red rectangle instead.");
            }
        } catch (IOException e) {
            System.err.println("Error reading the image file.");
            e.printStackTrace();
        }
    }

    // --- GETTERS ---
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getAngle() { return angle; }

    // --- SETTERS (ADDED TO FIX YOUR ERRORS) ---
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setAngle(double angle) { this.angle = angle; }

    /**
     * Updates the angle of the player to face the mouse cursor.
     * Note: mouseX and mouseY must be adjusted by the Camera offset (camX/camY) 
     * in the GameScreen class.
     */
    public void updateAngle(int mouseX, int mouseY) {
        double centerX = x + (width / 2.0);
        double centerY = y + (height / 2.0);
        angle = Math.atan2(mouseY - centerY, mouseX - centerX);
    }

    /**
     * Basic move method. 
     * Note: In the merged GameScreen, boundary checks are handled in actionPerformed 
     * using worldWidth and worldHeight.
     */
    public void move(boolean up, boolean down, boolean left, boolean right, boolean sprinting, int worldWidth, int worldHeight) {
        double speed = sprinting ? 7.0 : 4.0; // Updated speeds to match Tommy's core
        double dx = 0;
        double dy = 0;

        if (up) dy -= speed;
        if (down) dy += speed;
        if (left) dx -= speed;
        if (right) dx += speed;

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx = (dx / length) * speed;
            dy = (dy / length) * speed;
        }

        x += dx;
        y += dy;

        // World Boundaries (Prevents walking off the large map)
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > worldWidth) x = worldWidth - width;
        if (y + height > worldHeight) y = worldHeight - height;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform old = g2d.getTransform();

        // 1. Position the graphics context
        g2d.translate(x + width / 2.0, y + height / 2.0);
        
        // 2. Rotate (Adjusted by 90 degrees/PI/2 so the sprite faces the mouse)
        g2d.rotate(angle + Math.PI / 2);

        if (spriteImage != null) {
            // 3. Draw the sprite centered
            g2d.drawImage(spriteImage, -width / 2, -height / 2, width, height, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(-width / 2, -height / 2, width, height);
        }

        // 4. Restore transform
        g2d.setTransform(old);
    }
}