package maze;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MazePanel extends JPanel {

    /*
     * *********************************************************
     * Constants of class maze.MazePanel
     * *********************************************************
     */

    private static final int EMPTY = 0; // empty cell
    private static final int OBST = 1; // cell with obstacle
    private static final int START = 2; // the position of the robot
    private static final int TARGET = 3; // the position of the target
    private static final int FRONTIER = 4; // cells that form the frontier (OPEN SET)
    private static final int ROUTE = 5; // cells that form the robot-to-target path
    private static final int CLOSED = 6; // cells that form the CLOSED SET

    /*
     * *********************************************************
     * Variables of class maze.MazePanel
     * *********************************************************
     */

    int rows; // the number of rows of the grid
    int columns; // the number of columns of the grid
    int squareSize = 1; // the cell size in pixels

    Cell robotStart; // the initial position of the robot
    Cell targetPos; // the position of the target
    Cell currentCell;

    Deque<Cell> openCellsDFS = new ArrayDeque<>();
    Queue<Cell> openCellsBFS = new LinkedList<>();

    int[][] grid; // the grid

    // the Timer which governs the execution speed of the animation
    Timer timer;

    boolean found; // flag that the goal was found

    JButton btnDFS;
    JButton btnDfsAnimation;
    JButton btnDfsRecursion;
    JButton btnBFS;
    JButton btnBfsAnimation;

    /**
     * The creator of the panel
     */
    public MazePanel() {
        grid = loadBMPImage("img/maze.bmp");

        // ////////////////
        grid[0][1] = grid[599][600] = OBST;
        grid[1][1] = START;
        // ////////////////

        rows = grid.length;
        columns = grid[0].length;

        int width = rows * squareSize + 2;
        int height = columns * squareSize + 2;

        setLayout(null);
        setPreferredSize(new Dimension(width + 180, height));

        btnDFS = new JButton("DFS");
        btnDFS.addActionListener(e -> {
            if (timer != null)
                timer.stop();
            setTarget(599, 599);
            resetGrid();
            dfs();
        });
        add(btnDFS);
        btnDFS.setBounds(width + 5, 175, 170, 25);

        btnDfsAnimation = new JButton("DFS animation");
        btnDfsAnimation.addActionListener(e -> {
            if (timer != null)
                timer.stop();
            setTarget(599, 599);
            resetGrid();
            openCellsDFS.clear();
            openCellsDFS.push(new Cell(robotStart.getX(), robotStart.getY(), null));

            timer = new Timer(0, actionEvent -> {
                if (found) {
                    setBackground(Color.GREEN);
                    timer.stop();
                    buildRoute();
                }
                dfsOneStep();
                repaint();
            });
            timer.start();
        });
        add(btnDfsAnimation);
        btnDfsAnimation.setBounds(width + 5, 215, 170, 25);

        btnDfsRecursion = new JButton("DFS Recursion");
        btnDfsRecursion.addActionListener(e -> {
            grid[431][143] = OBST;
            setTarget(431, 143);
            resetGrid();
            dfsRecursion(robotStart);
            repaint();
        });
        add(btnDfsRecursion);
        btnDfsRecursion.setBounds(width + 5, 255, 170, 25);

        btnBFS = new JButton("BFS");
        btnBFS.addActionListener(e -> {
            if (timer != null)
                timer.stop();
            setTarget(599, 599);
            resetGrid();
            bfs();
        });
        add(btnBFS);
        btnBFS.setBounds(width + 5, 295, 170, 25);

        btnBfsAnimation = new JButton("BFS animation");
        btnBfsAnimation.addActionListener(e -> {
            if (timer != null)
                timer.stop();
            setTarget(599, 599);
            resetGrid();
            openCellsBFS.clear();
            openCellsBFS.add(new Cell(robotStart.getX(), robotStart.getY(), null));

            timer = new Timer(0, actionEvent -> {
                if (found) {
                    setBackground(Color.GREEN);
                    timer.stop();
                    buildRoute();
                }
                bfsOneStep();
                repaint();
            });
            timer.start();
        });
        add(btnBfsAnimation);
        btnBfsAnimation.setBounds(width + 5, 335, 170, 25);
    }

    /**
     * @param x
     * @param y
     */
    public void setTarget(int x, int y) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (grid[row][col] == TARGET) {
                    grid[row][col] = EMPTY;
                }
            }
        }
        grid[x][y] = TARGET;
    }

    /**
     * Gives initial values for the cells in the grid. Clears the data of any
     * search was performed (Frontier, Closed Set, Route) and leaves intact the
     * obstacles and the robot and target positions in order to be able to run
     * another algorithm with the same data.
     */
    private void resetGrid() {
        found = false;
        setBackground(Color.GRAY);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (grid[row][col] == FRONTIER || grid[row][col] >= CLOSED || grid[row][col] == ROUTE) {
                    grid[row][col] = EMPTY;
                }
                if (grid[row][col] == START) {
                    robotStart = new Cell(row, col, null);
                }
                if (grid[row][col] == TARGET) {
                    targetPos = new Cell(row, col, null);
                }
            }
        }
        repaint();
    }

    /**
     * paints the grid
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (grid[row][col] == EMPTY) {
                    g.setColor(Color.WHITE);
                } else if (grid[row][col] == START) {
                    g.setColor(Color.RED);
                } else if (grid[row][col] == TARGET) {
                    g.setColor(Color.GREEN);
                } else if (grid[row][col] == OBST) {
                    g.setColor(Color.BLACK);
                } else if (grid[row][col] == FRONTIER) {
                    g.setColor(Color.BLUE);
                } else if (grid[row][col] >= CLOSED) {
                    // g.setColor(Color.CYAN);
                    g.setColor(new Color(200, 250, 50 + 5 * grid[row][col]));
                } else if (grid[row][col] == ROUTE) {
                    g.setColor(Color.MAGENTA);
                }
                g.fillRect(1 + col * squareSize, 1 + row * squareSize, squareSize, squareSize);
            }
        }
    }

    /**
     * @param bmpFileName
     * @return
     */
    public int[][] loadBMPImage(String bmpFileName) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource(bmpFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int[][] array2D = new int[image.getWidth()][image.getHeight()];

        for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
            for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {
                int color = image.getRGB(yPixel, xPixel);
                if (color == Color.BLACK.getRGB()) {
                    array2D[xPixel][yPixel] = OBST;
                } else {
                    array2D[xPixel][yPixel] = EMPTY;
                }
            }
        }
        return array2D;
    }

    // *****************************
    // maze.Maze solving algorithms
    // *****************************

    /**
     * DFS Recursion
     *
     * @param robotStart
     * @return
     */
    public boolean dfsRecursion(Cell robotStart) {
        final int[] row = {0, 0, -1, 1};
        final int[] col = {-1, 1, 0, 0};

        int x = robotStart.x;
        int y = robotStart.y;

        if (grid[x][y] == TARGET)
            return true;

        if (grid[x][y] != START)
            grid[x][y] = CLOSED;

        for (int i = 0; i < 4; i++) {
            if (grid[x + row[i]][y + col[i]] == EMPTY || grid[x + row[i]][y + col[i]] == TARGET) {
                if (dfsRecursion(new Cell(x + row[i], y + col[i], null))) {
                    if (grid[x][y] != START)
                        grid[x][y] = ROUTE;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * DFS
     */
    public void dfs() {
        final int[] row = {0, 0, -1, 1};
        final int[] col = {-1, 1, 0, 0};

        Deque<Cell> openCells = new ArrayDeque<>();
        int routeMarker = 0;

        currentCell = null;
        openCells.push(new Cell(robotStart.x, robotStart.y, null));

        while (!openCells.isEmpty() && !found) {
            currentCell = openCells.pop();

            for (int i = 0; i < 4; i++) {
                if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == TARGET) {
                    routeMarker++;
                    found = true;
                    break;
                } else if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != CLOSED + routeMarker
                        && grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != OBST
                        && grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != START) {

                    openCells.push(new Cell(currentCell.getX() + row[i], currentCell.getY() + col[i], currentCell));
                    grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] = CLOSED + routeMarker;
                }
            }
        }

        buildRoute();
    }

    /**
     * DFS Animation
     */
    public void dfsOneStep() {
        final int[] row = {0, 0, -1, 1};
        final int[] col = {-1, 1, 0, 0};

        if (!openCellsDFS.isEmpty()) {
            currentCell = openCellsDFS.pop();
            if (grid[currentCell.getX()][currentCell.getY()] != START)
                grid[currentCell.getX()][currentCell.getY()] = CLOSED;
            for (int i = 0; i < 4; i++) {
                if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == TARGET) {
                    found = true;
                    return;
                } else if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == EMPTY) {
                    openCellsDFS.push(new Cell(currentCell.getX() + row[i], currentCell.getY() + col[i], currentCell));
                    grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] = FRONTIER;
                }
            }
        }
    }

    /**
     * BFS
     */
    public void bfs() {
        final int[] row = {0, 0, -1, 1};
        final int[] col = {-1, 1, 0, 0};

        Queue<Cell> openCells = new LinkedList<>();
        int routeMarker = 0;

        currentCell = null;
        openCells.add(new Cell(robotStart.x, robotStart.y, null));

        while (!openCells.isEmpty() && !found) {
            currentCell = openCells.poll();
            for (int i = 0; i < 4; i++) {
                if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == TARGET) {
                    routeMarker++;
                    found = true;
                    break;
                } else if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != CLOSED + routeMarker
                        && grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != OBST
                        && grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] != START) {

                    openCells.add(new Cell(currentCell.getX() + row[i], currentCell.getY() + col[i], currentCell));
                    grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] = CLOSED + routeMarker;
                }
            }
        }

        buildRoute();
    }

    /**
     * BFS Animation
     */
    public void bfsOneStep() {
        final int[] row = {0, 0, -1, 1};
        final int[] col = {-1, 1, 0, 0};

        if (!openCellsBFS.isEmpty()) {
            currentCell = openCellsBFS.poll();
            if (grid[currentCell.getX()][currentCell.getY()] != START)
                grid[currentCell.getX()][currentCell.getY()] = CLOSED;
            for (int i = 0; i < 4; i++) {
                if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == TARGET) {
                    found = true;
                    return;
                } else if (grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] == EMPTY) {
                    openCellsBFS.add(new Cell(currentCell.getX() + row[i], currentCell.getY() + col[i], currentCell));
                    grid[currentCell.getX() + row[i]][currentCell.getY() + col[i]] = FRONTIER;
                }
            }
        }
    }

    /**
     * Plots route from current cell
     */
    private void buildRoute() {
        if (currentCell != null) {
            while (currentCell.prev != null) {
                grid[currentCell.getX()][currentCell.getY()] = ROUTE;
                currentCell = currentCell.prev;
            }
        }
    }

    /**
     * Nested class Cell
     */
    private class Cell {
        private int x, y;
        private Cell prev;

        public Cell(int x, int y, Cell prev) {
            this.x = x;
            this.y = y;
            this.prev = prev;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

} // end class maze.MazePanel
