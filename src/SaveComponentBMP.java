/**
 * http://stackoverflow.com/questions/4245179/java-paint-component-into-bitmap
 */

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SaveComponentBMP {

	public static void main(String args[]) throws Exception {
		JFrame frame = new JFrame("Test");
		frame.add(new JTable(new DefaultTableModel() {
			
			@Override
			public int getColumnCount() {
				return 10;
			}

			@Override
			public int getRowCount() {
				return 10;
			}

			@Override
			public Object getValueAt(int row, int column) {
				return row + " " + column;
			}
		}));

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(400, 300);
		frame.setVisible(true);

		// BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.translate(-100, -100);

		frame.paintComponents(g);

		g.dispose();

		ImageIO.write(image, "bmp", new File("frame.bmp"));
	}

}
