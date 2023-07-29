package maze;

import javax.swing.JFrame;

public class Maze {

	public static void main(String[] args) {
		JFrame mazeFrame;
		mazeFrame = new JFrame("Maze");
		mazeFrame.setContentPane(new MazePanel());
		mazeFrame.pack();

		mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mazeFrame.setVisible(true);
	}

}
