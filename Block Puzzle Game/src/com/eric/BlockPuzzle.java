package com.eric;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;


public class BlockPuzzle extends JFrame implements Serializable {

	private static final long serialVersionUID = 2081960022252650770L;


	// Fields
	private final int WIDTH = 450;
	private final int HEIGHT = 700;
	private final int BORDER = 35;

	private final Color BACKGROUND_COLOR = Color.white;
	private final Color SCORE_COLOR = new Color(80, 180, 230);
	private final Color HIGHSCORE_COLOR = new Color(120, 230, 80);
	private final Font SCORE_FONT = new Font("Gadugi", Font.PLAIN, 48);

	private JPanel scorePanel;
	private JLabel scoreLabel, trophyLabel, highscoreLabel;

	private String score, highscore;


	// Getters and setters
	public JLabel getScoreLabel() {
		return scoreLabel;
	}

	public JLabel getHighscoreLabel() {
		return highscoreLabel;
	}
	
	public String getScore() {
		return score;
	}

	public String getHighscore() {
		return highscore;
	}


	// Constructor
	public BlockPuzzle() {
		initUI();
		initScorePanel();
		add(new Board(this));
	}


	// Initiate UI
	private void initUI() {
		getContentPane().setBackground(BACKGROUND_COLOR);
		setTitle("Block Puzzle Game");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	// Initiate score panel
	private void initScorePanel() {
		scorePanel = new JPanel();
		scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.X_AXIS));
		scorePanel.setOpaque(false);

		Dimension labelDimension = new Dimension(((WIDTH - (BORDER*5)) / 2), BORDER*2);

		scoreLabel = new JLabel(loadScore(), SwingConstants.RIGHT);
		scoreLabel.setFont(SCORE_FONT);
		scoreLabel.setForeground(SCORE_COLOR);
		scoreLabel.setPreferredSize(labelDimension);

		highscoreLabel = new JLabel(loadHighscore(), SwingConstants.RIGHT);
		highscoreLabel.setFont(SCORE_FONT);
		highscoreLabel.setForeground(HIGHSCORE_COLOR);
		highscoreLabel.setPreferredSize(labelDimension);

		trophyLabel = new JLabel();
		trophyLabel.setIcon(new ImageIcon(
				readAndResizeTrophy()));

		scorePanel.add(Box.createHorizontalStrut(BORDER));
		scorePanel.add(scoreLabel);
		scorePanel.add(Box.createHorizontalGlue());
		scorePanel.add(trophyLabel);
		scorePanel.add(Box.createHorizontalGlue());
		scorePanel.add(highscoreLabel);
		scorePanel.add(Box.createHorizontalStrut(BORDER));

		this.add(scorePanel, BorderLayout.NORTH);
	}

	
	// Read score
	private String loadScore() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File input = new File(desktop, "score.txt");

			Scanner myReader = new Scanner(input);
			score = myReader.nextLine();
			myReader.close();

			return score;

		} catch (FileNotFoundException | NoSuchElementException e) {
			score = "0";
			return score;
		}
	}


	// Read highscore
	private String loadHighscore() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File input = new File(desktop, "highscore.txt");

			Scanner myReader = new Scanner(input);
			highscore = myReader.nextLine();
			myReader.close();

			return highscore;

		} catch (FileNotFoundException | NoSuchElementException e) {
			highscore = "0";
			return highscore;
		}
	}


	// Read and resize trophy
	private Image readAndResizeTrophy() {
		try {
			InputStream resourceStream = BlockPuzzle.class.getResourceAsStream("/resources/trophy.png");
			BufferedImage buffImg = ImageIO.read(resourceStream);
			return buffImg.getScaledInstance((int) (BORDER*1.5), (int) (BORDER*1.5), Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	// Main method
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				BlockPuzzle myBlockPuzzle = new BlockPuzzle();
				myBlockPuzzle.setVisible(true);
			}
		});
	}
}
