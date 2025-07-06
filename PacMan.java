import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U';
        int velocityX = 0, velocityY = 0;
        char nextDirection = 'U';

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
            this.direction = direction;
            updateVelocity();
        }

        void updateVelocity() {
            int speed = tileSize / 4;
            switch (direction) {
                case 'U': velocityX = 0;       velocityY = -speed; break;
                case 'D': velocityX = 0;       velocityY =  speed; break;
                case 'L': velocityX = -speed;  velocityY = 0;     break;
                case 'R': velocityX =  speed;  velocityY = 0;     break;
            }
        }

        void reset() {
            x = startX;
            y = startY;
            velocityX = velocityY = 0;
            direction = nextDirection = 'U';
        }
    }

    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    private final String[] tileMap = {
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

    private HashSet<Block> walls;
    private HashSet<Block> foods;
    private HashSet<Block> ghosts;
    private Block pacman;

    private final Timer gameLoop;
    private final char[] directions = {'U','D','L','R'};
    private final Random random = new Random();
    private int score = 0, lives = 3;
    private boolean gameOver = false, gameWon = false;

    public PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage        = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage   = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage   = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage    = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        pacmanUpImage    = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage  = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage  = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            ghost.updateDirection(directions[random.nextInt(directions.length)]);
        }

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tile = tileMap[r].charAt(c);
                int x = c * tileSize, y = r * tileSize;

                switch (tile) {
                    case 'X': walls.add(new Block(wallImage, x, y, tileSize, tileSize)); break;
                    case 'b': ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize)); break;
                    case 'o': ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize)); break;
                    case 'p': ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize)); break;
                    case 'r': ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize)); break;
                    case 'P': pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize); break;
                    case ' ': foods.add(new Block(null, x + 14, y + 14, 4, 4)); break;
                }
            }
        }
    }

    private char oppositeDirection(char dir) {
        switch (dir) {
            case 'U': return 'D';
            case 'D': return 'U';
            case 'L': return 'R';
            case 'R': return 'L';
            default:  return dir;
        }
    }

    private List<Character> getAvailableDirections(Block ghost) {
        List<Character> valid = new ArrayList<>();
        for (char dir : directions) {
            ghost.updateDirection(dir);
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean collided = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) { collided = true; break; }
            }

            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;

            if (!collided) valid.add(dir);
        }
        return valid;
    }

    private void updateGhostDirection(Block ghost) {
        List<Character> options = getAvailableDirections(ghost);
        if (!options.isEmpty()) {
            ghost.updateDirection(options.get(random.nextInt(options.size())));
        }
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        for (Block ghost : ghosts) g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        for (Block wall : walls)  g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        g.setColor(Color.WHITE);
        for (Block food : foods) g.fillRect(food.x, food.y, food.width, food.height);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + score, tileSize/2, tileSize/2);
        } else {
            g.drawString("Lives: " + lives + "  Score: " + score, tileSize/2, tileSize/2);
        }

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("Selamat! Anda menang!", boardWidth/2 - 150, boardHeight/2 + 30);
        }
    }

    private void move() {
        // Handle pacman nextDirection
        if (pacman.nextDirection != pacman.direction) {
            int oldX = pacman.x, oldY = pacman.y;
            char oldDir = pacman.direction;
            pacman.updateDirection(pacman.nextDirection);
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            boolean collided = false;
            for (Block wall : walls) if (collision(pacman, wall)) { collided = true; break; }

            if (collided) {
                pacman.x = oldX;
                pacman.y = oldY;
                pacman.updateDirection(oldDir);
            } else {
                pacman.direction = pacman.nextDirection;
            }
        }

        // Move pacman
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        // Check wall collision
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
        // Check bounds -> bounce
        boolean out = pacman.x < 0 || pacman.x + pacman.width > boardWidth
                || pacman.y < 0 || pacman.y + pacman.height > boardHeight;
        if (out) {
            pacman.x -= pacman.velocityX;
            pacman.y -= pacman.velocityY;
            pacman.direction = oppositeDirection(pacman.direction);
            pacman.updateVelocity();
            pacman.nextDirection = pacman.direction;
        }

        // Move ghosts
        for (Block ghost : ghosts) {
            if (ghost.x % tileSize == 0 && ghost.y % tileSize == 0)
                updateGhostDirection(ghost);
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            boolean col = false;
            for (Block w : walls) if (collision(ghost, w)) { col = true; break; }
            if (col || ghost.x < 0 || ghost.x + ghost.width > boardWidth
                    || ghost.y < 0 || ghost.y + ghost.height > boardHeight) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                updateGhostDirection(ghost);
            }

            if (collision(ghost, pacman)) {
                lives--;
                if (lives == 0) {
                    gameOver = true;
                    JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, "Nyawa tersisa: " + lives);
                    resetPositions();
                }
            }
        }

        // Eat food
        Block eaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                eaten = food;
                score += 10;
            }
        }
        if (eaten != null) foods.remove(eaten);

        // Check win
        if (foods.isEmpty() && !gameOver) {
            gameWon = true;
            JOptionPane.showMessageDialog(this, "Selamat! Anda menang! Score: " + score);
            gameLoop.stop();
        }
    }

    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x
                && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    private void resetPositions() {
        pacman.reset();
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection(directions[random.nextInt(directions.length)]);
        }
    }

    @Override public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver || gameWon) gameLoop.stop();
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override public void keyPressed(KeyEvent e) {
        if (gameOver || gameWon) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = gameWon = false;
            gameLoop.start();
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                pacman.nextDirection = 'U'; pacman.image = pacmanUpImage; break;
            case KeyEvent.VK_DOWN:
                pacman.nextDirection = 'D'; pacman.image = pacmanDownImage; break;
            case KeyEvent.VK_LEFT:
                pacman.nextDirection = 'L'; pacman.image = pacmanLeftImage; break;
            case KeyEvent.VK_RIGHT:
                pacman.nextDirection = 'R'; pacman.image = pacmanRightImage; break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("PacMan");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        PacMan panel = new PacMan();
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
