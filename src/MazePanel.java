import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

public class MazePanel extends JPanel {

	private static final long serialVersionUID = -1195211287462453986L;

	/*
	 * *********************************************************
	 * Constants of class MazePanel
	 * *********************************************************
	 */

	private final static int EMPTY = 0, // empty cell
			OBST = 1, // cell with obstacle
			START = 2, // the position of the robot
			TARGET = 3, // the position of the target
			FRONTIER = 4, // cells that form the frontier (OPEN SET)
			ROUTE = 5, // cells that form the robot-to-target path
			CLOSED = 6; // cells that form the CLOSED SET

	/*
	 * *********************************************************
	 * Variables of class MazePanel
	 * *********************************************************
	 */

	int rows, // the number of rows of the grid
			columns, // the number of columns of the grid
			squareSize = 1; // the cell size in pixels

	Cell robotStart; // the initial position of the robot
	Cell targetPos; // the position of the target
	Cell currentCell;

	Stack<Cell> openCellsDFS = new Stack<>();
	Queue<Cell> openCellsBFS = new LinkedList<>();

	int[][] grid; // the grid

	// the Timer which governs the execution speed of the animation
	Timer timer;

	boolean found; // flag that the goal was found
	boolean doSearch; // flag that the search is in progress

	JButton btnDFS, btnDFSanimation, btnDFSRecursion, btnBFS, btnBFSanimation;

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
		btnDFS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer != null)
					timer.stop();
				setTarget(599, 599);
				resetGrid();
				dfs();
			}
		});
		add(btnDFS);
		btnDFS.setBounds(width + 5, 175, 170, 25);

		btnDFSanimation = new JButton("DFS animation");
		btnDFSanimation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer != null)
					timer.stop();
				setTarget(599, 599);
				resetGrid();
				openCellsDFS.push(new Cell(robotStart.getX(),
						robotStart.getY(), null));

				timer = new Timer(0, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (found) {
							setBackground(Color.GREEN);
							timer.stop();
							plotRoute(currentCell);
						}
						dfsOneStep();
						repaint();
					}
				});
				timer.start();
			}
		});
		add(btnDFSanimation);
		btnDFSanimation.setBounds(width + 5, 215, 170, 25);

		btnDFSRecursion = new JButton("DFS Recursion");
		btnDFSRecursion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				grid[431][143] = OBST;
				setTarget(431, 143);
				resetGrid();
				dfsRecursion(robotStart);
				repaint();
			}
		});
		add(btnDFSRecursion);
		btnDFSRecursion.setBounds(width + 5, 255, 170, 25);

		btnBFS = new JButton("BFS");
		btnBFS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer != null)
					timer.stop();
				setTarget(599, 599);
				resetGrid();
				bfs();
			}
		});
		add(btnBFS);
		btnBFS.setBounds(width + 5, 295, 170, 25);

		btnBFSanimation = new JButton("BFS animation");
		btnBFSanimation.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (timer != null)
					timer.stop();
				setTarget(599, 599);
				resetGrid();
				openCellsBFS.add(new Cell(robotStart.getX(), robotStart.getY(),
						null));

				timer = new Timer(0, new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (found) {
							setBackground(Color.GREEN);
							timer.stop();
							plotRoute(currentCell);
						}
						bfsOneStep();
						repaint();
					}
				});
				timer.start();
			}
		});
		add(btnBFSanimation);
		btnBFSanimation.setBounds(width + 5, 335, 170, 25);
	} // end constructor

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void setTarget(int x, int y) {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (grid[r][c] == TARGET) {
					grid[r][c] = EMPTY;
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

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (grid[r][c] == FRONTIER || grid[r][c] >= CLOSED
						|| grid[r][c] == ROUTE) {
					grid[r][c] = EMPTY;
				}
				if (grid[r][c] == START) {
					robotStart = new Cell(r, c, null);
				}
				if (grid[r][c] == TARGET) {
					targetPos = new Cell(r, c, null);
				}
			}
		}
		repaint();
	} // end resetGrid()

	/**
	 * paints the grid
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (grid[r][c] == EMPTY) {
					g.setColor(Color.WHITE);
				} else if (grid[r][c] == START) {
					g.setColor(Color.RED);
				} else if (grid[r][c] == TARGET) {
					g.setColor(Color.GREEN);
				} else if (grid[r][c] == OBST) {
					g.setColor(Color.BLACK);
				} else if (grid[r][c] == FRONTIER) {
					g.setColor(Color.BLUE);
				} else if (grid[r][c] >= CLOSED) {
					// g.setColor(Color.CYAN);
					g.setColor(new Color(200, 250, 50 + 5 * grid[r][c]));
				} else if (grid[r][c] == ROUTE) {
					g.setColor(Color.MAGENTA);
				}
				g.fillRect(1 + c * squareSize, 1 + r * squareSize, squareSize,
						squareSize);
			}
		}
	} // end paintComponent()

	/**
	 * 
	 * @param BMPFileName
	 * @return
	 */
	public int[][] loadBMPImage(String BMPFileName) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResource(BMPFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}

		int[][] array2D = new int[image.getHeight()][image.getWidth()];

		for (int xPixel = 0; xPixel < image.getHeight(); xPixel++) {
			for (int yPixel = 0; yPixel < image.getWidth(); yPixel++) {
				int color = image.getRGB(yPixel, xPixel);
				if (color == Color.BLACK.getRGB()) {
					array2D[xPixel][yPixel] = OBST; // OBST
				} else {
					array2D[xPixel][yPixel] = EMPTY; // EMPTY
				}
			}
		}
		return array2D;
	}

	/**
	 * 
	 * @param image
	 */
	public void saveBMPImage(BufferedImage image) {
		try {
			ImageIO.write(image, "bmp", new File("frame.bmp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// *************************
	// Maze solving algorithms
	// *************************

	/**
	 * DFS Recursion
	 * 
	 * @param robotStart
	 * @return
	 */
	public boolean dfsRecursion(Cell robotStart) {
		final int[] k = { 0, 0, -1, 1 };
		final int[] l = { -1, 1, 0, 0 };

		int x = robotStart.x;
		int y = robotStart.y;

		if (grid[x][y] == TARGET)
			return true;

		if (grid[x][y] != START)
			grid[x][y] = CLOSED;

		for (int i = 0; i < 4; i++) {
			if (grid[x + k[i]][y + l[i]] == EMPTY
					|| grid[x + k[i]][y + l[i]] == TARGET) {
				if (dfsRecursion(new Cell(x + k[i], y + l[i], null))) {
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
		final int[] r = { 0, 0, -1, 1 };
		final int[] c = { -1, 1, 0, 0 };

		int x = robotStart.x;
		int y = robotStart.y;

		Stack<Cell> openCells = new Stack<>();
		ArrayList<Cell> routes = new ArrayList<>();
		int routeMarker = 0;

		openCells.push(new Cell(x, y, null));
		while (!openCells.isEmpty()) {
			Cell v = openCells.pop();

			for (int i = 0; i < 4; i++) {
				if (grid[v.getX() + r[i]][v.getY() + c[i]] == TARGET) {
					found = true;
//					System.out.println("Target found");
					routeMarker++;
					routes.add(v);
				} else if (grid[v.getX() + r[i]][v.getY() + c[i]] != CLOSED + routeMarker
						&& grid[v.getX() + r[i]][v.getY() + c[i]] != OBST
						&& grid[v.getX() + r[i]][v.getY() + c[i]] != START) {

					openCells.push(new Cell(v.getX() + r[i], v.getY() + c[i], v));
					grid[v.getX() + r[i]][v.getY() + c[i]] = CLOSED + routeMarker;
				}
			}
		}

		for (Cell cell : routes) {
			plotRoute(cell);
		}
	}

	/**
	 * DFS Animation
	 */
	public void dfsOneStep() {
		final int[] k = { 0, 0, -1, 1 };
		final int[] l = { -1, 1, 0, 0 };

		if (!openCellsDFS.isEmpty()) {
			Cell v = openCellsDFS.pop();
			if (grid[v.getX()][v.getY()] != START)
				grid[v.getX()][v.getY()] = CLOSED;
			for (int i = 0; i < 4; i++) {
				if (grid[v.getX() + k[i]][v.getY() + l[i]] == TARGET) {
					found = true;
					return;
				}

				if (grid[v.getX() + k[i]][v.getY() + l[i]] == EMPTY) {
					openCellsDFS.push(new Cell(v.getX() + k[i],
							v.getY() + l[i], v));
					grid[v.getX() + k[i]][v.getY() + l[i]] = FRONTIER;
				}
			}
		}
	}

	/**
	 * BFS
	 */
	public void bfs() {
		final int[] k = { 0, 0, -1, 1 };
		final int[] l = { -1, 1, 0, 0 };

		int x = robotStart.x;
		int y = robotStart.y;

		Queue<Cell> openCells = new LinkedList<>();
		ArrayList<Cell> routes = new ArrayList<>();
		int routeMarker = 0;

		openCells.add(new Cell(x, y, null));
		while (!openCells.isEmpty() && !found) {
			Cell v = openCells.poll();
			for (int i = 0; i < 4; i++) {
				if (grid[v.getX() + k[i]][v.getY() + l[i]] == TARGET) {
				    found = true;
					routeMarker++;
					routes.add(v);
				}

				if (grid[v.getX() + k[i]][v.getY() + l[i]] != CLOSED + routeMarker
						&& grid[v.getX() + k[i]][v.getY() + l[i]] != OBST
						&& grid[v.getX() + k[i]][v.getY() + l[i]] != START) {

					openCells.add(new Cell(v.getX() + k[i], v.getY() + l[i], v));
					grid[v.getX() + k[i]][v.getY() + l[i]] = CLOSED + routeMarker;
				}
			}
		}

		for (Cell cell : routes) {
			plotRoute(cell);
		}
	}

	/**
	 * BFS Animation
	 */
	public void bfsOneStep() {
		final int[] k = { 0, 0, -1, 1 };
		final int[] l = { -1, 1, 0, 0 };

		if (!openCellsBFS.isEmpty()) {
			Cell v = openCellsBFS.poll();
			if (grid[v.getX()][v.getY()] != START)
				grid[v.getX()][v.getY()] = CLOSED;
			for (int i = 0; i < 4; i++) {
				if (grid[v.getX() + k[i]][v.getY() + l[i]] == TARGET) {
					found = true;
					return;
				}

				if (grid[v.getX() + k[i]][v.getY() + l[i]] == EMPTY) {
					openCellsBFS.add(new Cell(v.getX() + k[i], v.getY() + l[i],
							v));
					grid[v.getX() + k[i]][v.getY() + l[i]] = FRONTIER;
				}
			}
		}
	}

	/**
	 * Plots route from current cell
	 * 
	 * @param current
	 */
	private void plotRoute(Cell current) {
		System.out.println("plot route");
		while (current.prev != null) {
			grid[current.getX()][current.getY()] = ROUTE;
			current = current.prev;
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

} // end classs MazePanel
