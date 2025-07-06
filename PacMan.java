// [SEMUA IMPORT TETAP]
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;
        char nextDirection = 'U';  // arah yang diinginkan pengguna

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXrXX X XXXX",
            "O       bpo       O",
            "XXXX X XXXXX X XXXX",
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X",
            "X                 X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    // ✅ Tambahan variabel
    boolean gameWon = false;
    String notificationMessage = "";
    long notificationStartTime = 0;
    final int NOTIFICATION_DURATION = 3000;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghost.updateDirection(getValidDirection(ghost));
                }
                else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghost.updateDirection(getValidDirection(ghost));
                }
                else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghost.updateDirection(getValidDirection(ghost));
                }
                else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghost.updateDirection(getValidDirection(ghost));
                }
                else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    private char getValidDirection(Block ghost) {
        for (char dir : directions) {
            ghost.updateDirection(dir);
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    collided = true;
                    break;
                }
            }

            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;

            if (!collided) return dir;
        }
        return 'U'; // fallback default
    }


    void showNotification(String message) {
        notificationMessage = message;
        notificationStartTime = System.currentTimeMillis();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }

        // ✅ Tambahan animasi notifikasi
        if (!notificationMessage.isEmpty()) {
            long elapsed = System.currentTimeMillis() - notificationStartTime;
            if (elapsed < NOTIFICATION_DURATION) {
                float alpha = 1.0f - (float) elapsed / NOTIFICATION_DURATION;
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString(notificationMessage, boardWidth / 2 - 150, boardHeight / 2);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            } else {
                notificationMessage = "";
            }
        }

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Selamat Anda berhasil memenangkan permainan!", boardWidth / 2 - 280, boardHeight / 2 + 30);
        }
    }

    private void updateGhostDirection(Block ghost) {
        for (int i = 0; i < 4; i++) { // coba maksimal 4 arah
            char dir = directions[random.nextInt(directions.length)];
            ghost.updateDirection(dir);
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    collided = true;
                    break;
                }
            }

            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;

            if (!collided) {
                ghost.updateDirection(dir); // arah aman
                return;
            }
        }
    }

    public void move() {
        if (pacman.nextDirection != pacman.direction) {
            // Simpan posisi dan arah lama
            int oldX = pacman.x;
            int oldY = pacman.y;
            char oldDirection = pacman.direction;

            // Coba ubah ke arah yang baru
            pacman.updateDirection(pacman.nextDirection);
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            boolean collided = false;
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    collided = true;
                    break;
                }
            }

            if (collided) {
                // Gagal belok, revert ke arah lama
                pacman.x = oldX;
                pacman.y = oldY;
                pacman.updateDirection(oldDirection);
            } else {
                // Berhasil belok, simpan arah baru
                pacman.direction = pacman.nextDirection;
            }
        }

        int nextX = pacman.x + pacman.velocityX;
        int nextY = pacman.y + pacman.velocityY;

        if (nextX >= 0 && nextX + pacman.width <= boardWidth &&
                nextY >= 0 && nextY + pacman.height <= boardHeight) {
            pacman.x = nextX;
            pacman.y = nextY;
        }

        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    showNotification("Game Over!");
                    return;
                }
                showNotification("Nyawa Anda tersisa " + lives);
                resetPositions();
            }

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            // Coba gerak
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean ghostCollide = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghostCollide = true;
                    break;
                }
            }

            if (ghostCollide || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth ||
                    ghost.y <= 0 || ghost.y + ghost.height >= boardHeight) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                updateGhostDirection(ghost);
            }
        }

        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // ✅ Tambahan notifikasi menang
        if (foods.isEmpty() && !gameOver) {
            gameWon = true;
            showNotification("Selamat Anda berhasil memenangkan permainan!");
            gameLoop.stop();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char validDirection = getValidDirection(ghost);
            ghost.updateDirection(validDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver || gameWon) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver || gameWon) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameWon = false;
            gameLoop.start();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.nextDirection = 'U';
            pacman.image = pacmanUpImage;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.nextDirection = 'D';
            pacman.image = pacmanDownImage;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.nextDirection = 'L';
            pacman.image = pacmanLeftImage;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.nextDirection = 'R';
            pacman.image = pacmanRightImage;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}