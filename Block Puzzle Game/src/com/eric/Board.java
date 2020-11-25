package com.eric;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import com.eric.Block.Blocks;


public class Board extends JPanel implements ActionListener, Serializable {

	private static final long serialVersionUID = -6969666034717709972L;


	// Fields
	private final Color backgroundColor = Color.white;
	private final Color colors[] = {
			new Color(220, 220, 220),
			new Color(120, 130, 190),
			new Color(255, 200, 30),
			new Color(255, 140, 30),
			new Color(255, 70, 100),
			new Color(240, 40, 40),
			new Color(40, 210, 90),
			new Color(70, 160, 220),
			new Color(80, 230, 40),
			new Color(0, 210, 145)
	};
	private final Font GAME_OVER_FONT = new Font("Gadugi", Font.BOLD, 56);
	private final Font GAME_OVER_SUBFONT = new Font("Gadugi", Font.BOLD, 28);
	private final String GAME_OVER_STRING = "Game Over!";
	private final String GAME_OVER_SUBSTRING = "Press space to play again.";
	private final int ROWS = 10;
	private final int COLUMNS = 10;
	private final int BORDER = 30;
	private final int ROUNDED_RECT_CONSTANT = 10;
	private final int DELAY = 10;
	private int square;
	private float alpha;

	private Block[] nextThree;
	private Blocks[] board;
	private Block curPiece;
	private int curPieceX;
	private int curPieceY;
	private int curPieceIndex;

	private JLabel scoreLabel, highscoreLabel;
	private int score;
	private int highscore;

	private Random r = new Random();

	private Timer gameOverTimer;
	private boolean gameOver;


	// Constructor
	public Board(BlockPuzzle parent) {
		initBoard(parent);
	}


	// Initiate UI
	private void initBoard(BlockPuzzle parent) {
		setBackground(backgroundColor);
		setFocusable(true);

		scoreLabel = parent.getScoreLabel();
		highscoreLabel = parent.getHighscoreLabel();
		score = Integer.parseInt(parent.getScore());
		highscore = Integer.parseInt(parent.getHighscore());

		curPiece = new Block(Blocks.noShape);
		board = new Blocks[COLUMNS * ROWS];
		nextThree = new Block[3];
		
		gameOver = false;

		if (score > 0) {
			loadBoard();
			loadNextThree();
			checkGameOver();
		} else {
			resetBoard();
		}

		addKeyListener(new MyKeyAdapter());
		addMouseMotionListener(new MyMouseAdapter());
		addMouseListener(new MyMouseAdapter());
	}


	// Shape at point in board
	private Blocks shapeAt(int x, int y) {
		return board[y*ROWS + x];
	}


	// Do drawing
	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(
				RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_QUALITY);

		square = (getWidth() - BORDER*2) / COLUMNS;

		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				drawSquare(g2d, BORDER + j*square, BORDER + i*square, square, shapeAt(j, i));
			}
		}

		drawNextThree(g2d);
		drawCurPiece(g2d);

		if (gameOver) {
			drawGameOver(g2d);
		}
	}


	// Draw game over
	private void drawGameOver(Graphics2D g1d) {
		Graphics2D g2d = (Graphics2D) g1d.create();

		AlphaComposite alcom1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
		g2d.setComposite(alcom1);
		g2d.setPaint(Color.gray);
		g2d.fillRoundRect((int) (BORDER*1.5), BORDER + ROWS*7/20*square, getWidth() - BORDER*3, square*4, ROUNDED_RECT_CONSTANT, ROUNDED_RECT_CONSTANT);

		AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		g2d.setComposite(alcom);
		g2d.setPaint(Color.black);
		g2d.setFont(GAME_OVER_FONT);
		int stringWidth = g2d.getFontMetrics().stringWidth(GAME_OVER_STRING);
		g2d.drawString(GAME_OVER_STRING, (getWidth() - stringWidth) / 2, BORDER + ROWS*1/2f*square);

		g2d.setFont(GAME_OVER_SUBFONT);
		stringWidth = g2d.getFontMetrics().stringWidth(GAME_OVER_SUBSTRING);
		g2d.drawString(GAME_OVER_SUBSTRING, (getWidth() - stringWidth) / 2, BORDER + ROWS*3/5f*square);

		g2d.dispose();
	}


	// Draw square
	private void drawSquare(Graphics2D g2d, int x, int y, int square, Blocks shape) {   // Draw a round rectangle at (x, y) with size = square and color matching the shape
		RoundRectangle2D r = new RoundRectangle2D.Double(
				x, y, square*0.95, square*0.95, ROUNDED_RECT_CONSTANT, ROUNDED_RECT_CONSTANT);

		g2d.setColor(colors[shape.ordinal()]);
		g2d.fill(r);
	}


	// Draw next three pieces
	private void drawNextThree(Graphics2D g2d) {
		if (Arrays.stream(nextThree).allMatch(x -> x.getShape().equals(Blocks.noShape))) {   // If the nextThree array is empty, fill it with random shapes
			for (int i = 0; i < 3; i++) {
				int random = r.nextInt(9) + 1;
				nextThree[i] = new Block(Blocks.values()[random]);
			}
			curPiece = nextThree[0];
			curPieceIndex = 0;

			checkGameOver();
			save();
		}

		for (int i = 0; i < 3; i++) {   // Draw the next three shapes at the bottom of the screen
			Block piece = nextThree[i];
			if (curPiece.equals(piece) || piece.getShape().equals(Blocks.noShape)) continue;

			int numPoints = piece.getNumPoints();
			int squareScaled = (int) (square * 0.6);
			int xStart = (int) (BORDER + square*1.5 + square*3.5*i);
			int yStart = ROWS*square + BORDER*4;

			int[] centeringConstants = centeringConstants(piece, squareScaled);

			for (int j = 0; j < numPoints; j++) {
				drawSquare(g2d, xStart + centeringConstants[0] + piece.getX(j)*squareScaled, yStart + centeringConstants[1] + piece.getY(j)*squareScaled, squareScaled, piece.getShape());
			}
		}
	}


	// Calculate the centering constants for the next three pieces
	private int[] centeringConstants(Block piece, int squareSize) {
		int[] ret = new int[2];

		int minX = 0;
		int maxX = 0;
		int minY = 0;
		int maxY = 0;

		for (int j = 0, n = piece.getNumPoints(); j < n; j++) {
			minX = Math.min(minX, piece.getX(j));
			maxX = Math.max(maxX, piece.getX(j));
			minY = Math.min(minY, piece.getY(j));
			maxY = Math.max(maxY, piece.getY(j));
		}

		int width = -minX * squareSize + (maxX + 1) * squareSize;
		int halfWidth = width / 2;
		int centerX = minX*squareSize + halfWidth;
		ret[0] = -centerX;

		int height = (maxY - minY + 1) * squareSize;
		int halfHeight = height / 2;
		int centerY = (minY)*squareSize + halfHeight;
		ret[1] = -centerY;

		return ret;
	}


	// Draw current piece at mouse
	private void drawCurPiece(Graphics2D g2d) {
		if (gameOver) return;
		int[] centeringConstants = centeringConstants(curPiece, square);
		for (int i = 0, n = curPiece.getNumPoints(); i < n; i++) {
			drawSquare(g2d, curPieceX + centeringConstants[0] + curPiece.getX(i)*square, curPieceY + centeringConstants[1] + curPiece.getY(i)*square, square-4, curPiece.getShape());
		}
	}


	// Override paintComponent()
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}


	// Check if the placement is valid and change the board if it is
	private void placeCurPiece() {
		int[] centeringConstants = centeringConstants(curPiece, square);
		int numPoints = curPiece.getNumPoints();

		int[] squaresToChange = new int[numPoints];
		int validPoints = 0;

		for (int i = 0; i < numPoints; i++) {
			int x = curPieceX + centeringConstants[0] + curPiece.getX(i)*square;
			int y = curPieceY + centeringConstants[1] + curPiece.getY(i)*square;

			for (int j = 0, b = board.length; j < b; j++) {
				int boardX = BORDER + (j % COLUMNS)*square;
				int boardY = BORDER + (j/ROWS)*square;

				if (Math.abs(boardX - x) < square/2 && Math.abs(boardY - y) < square/2) {
					if (board[j] != Blocks.noShape) {
						break;
					}
					validPoints++;
					squaresToChange[i] = j;
				}
			}
		}

		if (validPoints == numPoints) {
			for (int i = 0; i < squaresToChange.length; i++) {
				board[squaresToChange[i]] = curPiece.getShape();
			}

			score += numPoints;
			scoreLabel.setText(String.valueOf(score));

			nextThree[curPieceIndex] = new Block(Blocks.noShape);

			save();
			
			shiftNextThree(1);

			checkGameOver();
			if (gameOver) return;
		}
	}


	// Remove full lines
	private void removeFullLines() {
		boolean full = false;

		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (shapeAt(j, i).equals(Blocks.noShape)) {
					full = false;
					break;
				}
				full = true;
			}
			if (full) {
				for (int j = 0; j < COLUMNS; j++) {
					board[i*ROWS + j] = Blocks.noShape;
				}
				score += COLUMNS;
				save();
			}
		}

		for (int i = 0; i < COLUMNS; i++) {
			for (int j = 0; j < ROWS; j++) {
				if (shapeAt(i,j).equals(Blocks.noShape)) {
					full = false;
					break;
				}
				full = true;
			}
			if (full) {
				for (int j = 0; j < ROWS; j++) {
					board[j*COLUMNS + i] = Blocks.noShape;
				}
				score += ROWS;
				save();
			}
		}
	}


	// Shift current piece
	private void shiftNextThree(int direction) {
		if (Arrays.stream(nextThree).allMatch(x -> x.getShape().equals(Blocks.noShape))) return;

		int nextThreeLength = nextThree.length;
		curPieceIndex = (curPieceIndex + nextThreeLength + direction) % nextThreeLength;

		if (nextThree[curPieceIndex].getShape().equals(Blocks.noShape)) {
			shiftNextThree(direction);
		}

		curPiece = nextThree[curPieceIndex];
		repaint();
	}


	// Save board, nextThree, score, highscore
	private void save() {
		saveScore();
		saveHighscore();
		saveBoard();
		saveNextThree();		
	}


	// Write score(){
	private void saveScore() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File output = new File(desktop, "score.txt");

			FileWriter myWriter = new FileWriter(output);
			myWriter.write(String.valueOf(score));
			myWriter.close();

			scoreLabel.setText(String.valueOf(score));
			repaint();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// Write highscore
	private void saveHighscore() {
		if (score > highscore) {
			try {
				File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
				File output = new File(desktop, "highscore.txt");

				FileWriter myWriter = new FileWriter(output);
				myWriter.write(String.valueOf(score));
				myWriter.close();

				highscoreLabel.setText(String.valueOf(score));
				highscore = score;
				repaint();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	// Save board
	private void saveBoard() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File output = new File(desktop, "board.txt");

			FileOutputStream fos = new FileOutputStream(output);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(board);
			oos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// Save nextThree
	private void saveNextThree() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File output = new File(desktop, "nextThree.txt");

			FileOutputStream fos = new FileOutputStream(output);
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			oos.writeObject(nextThree);
			oos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// Load board
	private void loadBoard() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File input = new File(desktop, "board.txt");

			FileInputStream fis = new FileInputStream(input);
			ObjectInputStream ois = new ObjectInputStream(fis);

			board = (Blocks[]) ois.readObject();
			ois.close();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	// Load next three
	private void loadNextThree() {
		try {
			File desktop = new File(System.getProperty("user.home"), "/Desktop/BlockPuzzle Data");
			File input = new File(desktop, "nextThree.txt");

			FileInputStream fis = new FileInputStream(input);
			ObjectInputStream ois = new ObjectInputStream(fis);

			nextThree = (Block[]) ois.readObject();
			ois.close();
			
			curPiece = nextThree[0];

			if (curPiece.getShape().equals(Blocks.noShape)) shiftNextThree(1);

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	// Check game over
	private void checkGameOver() {
		int invalidPieces = 0;
		int piecesChecked = 0;
		for (int i = 0; i < nextThree.length; i++) {

			if (nextThree[i].getShape() != Blocks.noShape) {
				piecesChecked++;
				Block piece = nextThree[i];
				int numPoints = piece.getNumPoints();

				for (int j = 0, b = board.length; j < b; j++) {
					int validPoints = 0;
					int boardX = (j % COLUMNS);
					int boardY = (j/ROWS);

					for (int k = 0; k < numPoints; k++) {
						int x = boardX + piece.getX(k);
						int y = boardY + piece.getY(k);

						if (x < 0 || x > 9 || y < 0 || y > 9) break;
						if (shapeAt(x, y).equals(Blocks.noShape)) validPoints++;
					}

					if (validPoints == numPoints) return;
				}
				invalidPieces++;
			}
		}
		if (invalidPieces == piecesChecked && piecesChecked != 0) {
			System.out.println("Pieces checked: " + piecesChecked);
			System.out.println("Invalid pieces: " + invalidPieces);
			gameOver();
		}
	}


	// Game over
	private void gameOver() {
		alpha = 0;
		gameOver = true;
		gameOverTimer = new Timer(DELAY, this);
		gameOverTimer.start();
	}


	// Reset board / fill board[] with noShapes
	private void resetBoard() {
		for (int i = 0; i < COLUMNS * ROWS; i++) {
			board[i] = Blocks.noShape;
		}

		for (int i = 0; i < 3; i++) {
			nextThree[i] = new Block(Blocks.noShape);
		}

		saveBoard();
	}


	// Reset next three
	private void resetNextThree() {
		for (int i = 0; i < nextThree.length; i++) {
			nextThree[i] = new Block(Blocks.noShape);
		}

		saveNextThree();
	}


	// Reset score
	private void resetScore() {
		score = 0;
		scoreLabel.setText(String.valueOf(score));
		repaint();

		saveScore();
	}


	// Override actionPerformed();
	@Override
	public void actionPerformed(ActionEvent e) {
		step();
		repaint();
	}


	// Step method for game over
	private void step() {
		alpha += 0.01f;

		if (alpha >= 1) {
			alpha = 1f;
			gameOverTimer.stop();
		}
	}


	// My key adapter
	public class MyKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();

			if (gameOver) {
				if (keyCode == 32) {
					gameOver = false;
					resetScore();
					resetBoard();
					resetNextThree();
				}
			}

			if (keyCode == 37) {
				shiftNextThree(-1);

			} else if (keyCode == 39) {
				shiftNextThree(1);
			}
		}
	}


	// My mouse adapter
	public class MyMouseAdapter extends MouseAdapter {
		@Override
		public void mouseMoved(MouseEvent e) {
			if (gameOver) return;
			int x = e.getX();
			int y = e.getY();

			for (int i = 0, n = curPiece.getNumPoints(); i < n; i++) {
				curPieceX = x;
				curPieceY = y;
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (gameOver) return;
			int boardBottom = BORDER/2 + ROWS*square;
			boardBottom = BORDER + (ROWS*square) + square/4;

			if (e.getY() < boardBottom) {
				placeCurPiece();
				removeFullLines();
				checkGameOver();
			}
		}
	}
}
