package com.eric;

import java.io.Serializable;
import java.util.*;


public class Block implements Serializable {

	private static final long serialVersionUID  = -4838077122983682292L;


	// Fields
	public enum Blocks implements Serializable {noShape, one, two, three, four, five, smallCorner, bigCorner, smallBlock, bigBlock};

	private Blocks shape;
	private int[][][] coordsTable;
	private int[][] coords;
	private int numPoints;

	private Random r = new Random();


	// Getters and setters
	private void setCoords(Blocks shape) {
		this.shape = shape;
		numPoints = coordsTable[shape.ordinal()].length;

		coords = new int[numPoints][2];

		for (int i = 0; i < numPoints; i++) {
			for (int j = 0; j < 2; j++) {
				coords[i][j] = coordsTable[shape.ordinal()][i][j];
			}
		}

		rotateShape();
	}

	public Blocks getShape() {
		return shape;
	}

	public int getNumPoints() {
		return numPoints;
	}

	public void setX(int index, int x) {
		coords[index][0] = x;
	}

	public void setY(int index, int y) {
		coords[index][1] = y;
	}

	public int getX(int index) {
		return coords[index][0];
	}

	public int getY(int index) {
		return coords[index][1];
	}


	// Constructor
	public Block(Blocks shape) {
		initCollections();
		setCoords(shape);
	}


	// Initiate collections
	private void initCollections() {
		coordsTable = new int[][][] {
			{{0, 0}},   // noShape
			{{0, 0}},   // one
			{{-1, 0}, {0, 0}},   // two
			{{-1, 0}, {0, 0}, {1, 0}},   // three
			{{-2, 0}, {-1, 0}, {0, 0}, {1, 0}},   // four
			{{-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {2, 0}},   // five
			{{-1, 0}, {0, 0}, {0, -1}},   // smallCorner
			{{-2, 0}, {-1, 0}, {0, 0}, {0, -1}, {0, -2}},   // bigCorner
			{{-1, 1}, {0, 1}, {-1, 0}, {0, 0}},   // smallBlock
			{{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {-0, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}}   // bigBlock
		};
	}


	private void rotateShape() {
		if (shape.equals(Blocks.noShape) ||
				shape.equals(Blocks.one) ||
				shape.equals(Blocks.smallBlock) ||
				shape.equals(Blocks.bigBlock)) {
			return;
		}

		int rotations = r.nextInt(4);

		for (int r = 0; r < rotations; r++) {
			for (int i = 0; i < numPoints; i++) {
				int x = getX(i);
				int y = getY(i);

				setX(i, y);
				setY(i, -x);
			}
		}
	}
}
