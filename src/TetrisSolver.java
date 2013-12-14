
import java.io.BufferedReader;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * AI4211 Project
 * AI player for Tetris
 *
 * @author 20925931 Eric (Jun) Tan
 * @version 28th May 2012
 */
public class TetrisSolver {	
	
	/*************************************************************************
	 *                   PROJECT ASSUMED STATIC VARIABLES                    *
	 *                   These can be altered at any point                   *
	 *************************************************************************/
	
	/**
	 * The width of the tetris grid. Default set to 11
	 */
	final static int GRID_WIDTH = 11;
	
	/**
	 * The cap on the amount of pieces to be delivered from the open end of the board
	 * Set to 1000 on default, can be altered at any time
	 */
	final static int N_PIECES = 1001;
	
	/**
	 * The size of the buffer
	 */
	final static int BUFFER_SIZE = 1;
	
	/*************************************************************************
	 *                         STATIC VARIABLES                              *
	 *************************************************************************/
	
	/**
	 * The height of the tetris grid. Technically semi-infinite but actually set
	 * dynamically to be of worse-case scenario, so the height will never be 
	 * exceeded
	 */
	private static int GRID_HEIGHT;
	
	/**
	 * My Tetris grid, storing the state of the current game
	 */
	private static boolean[][] grid;
	
	/**
	 * The buffer, holding up the BUFFER_SIZE pieces at one time
	 */
	private static int[] buffer = new int[BUFFER_SIZE];

	/**
	 * An integer array holding the all pieces to be placed on the board, data
	 * and order extracted from the input file.
	 */
	private static int[] pieces = new int[N_PIECES];
	
	/**
	 * The count of total number of pieces to be placed
	 */
	private static int count = 0;
	
	/**
	 * An array list holder for the list of processed pieces, to be output 
	 * into the output file at the conclusion of runtime
	 */
	private static ArrayList<String> solution = new ArrayList<String>();
	
	/*************************************************************************
	 *                       PRIVATE HELPER METHODS                          *
	 *************************************************************************/
	
	/**
	 * Reads the input file
	 * @param filename the name of the input file
	 * @throws IOException if the input file does not exist or cannot be opened
	 */
	private static void readInFile(String filename) throws IOException {
		Path path = Paths.get(filename);
		
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line = null;
			int counter = 0;
			
			while ((line = reader.readLine()) != null) {
				int len = line.length();
				
				for (int i = 0; i < len; i++) {
					char current = line.charAt(i);
					
					if (current >= '1' && current <= '7') {
						pieces[counter++] = current - '0';
					}
				}
			}
			count = counter;
		}
	}
	
	/**
	 * Sets the height and initializes the grid
	 */
	private static void setGrid() {
		GRID_HEIGHT = count * 4 / GRID_WIDTH;
		grid = new boolean[GRID_WIDTH][GRID_HEIGHT];
	}
	
	/**
	 * Writes the solution to the output file
	 * @param filename the name of the output file. Does not need to exist
	 * 	If it does exist, it rewrites it
	 * @throws IOException if the output file is unable for access
	 */
	private static void writeOutFile(String filename) throws IOException {
		Path path = Paths.get(filename);		
		Files.write(path, solution, StandardCharsets.UTF_8);
	}
	
	/**
	 * Copies the current game state and returns it
	 * @return the copied game state
	 */
	private static boolean[][] copyGrid() {
		boolean[][] temp = new boolean[GRID_WIDTH][GRID_HEIGHT];
		for (int j = GRID_HEIGHT - 1; j >= 0; j--) {
			for (int i = 0; i < GRID_WIDTH; i++) {
				temp[i][j] = grid[i][j];
			}
		}
		return temp;
	}
	
	/**
	 * Reverts the current game state to the given game state
	 * @param temp the original game state to return to
	 */
	private static void revertGrid(boolean[][] temp) {
		for (int j = GRID_HEIGHT - 1; j >= 0; j--) {
			for (int i = 0; i < GRID_WIDTH; i++) {
				grid[i][j] = temp[i][j];
			}
		}
	}
	
	/**
	 * Method to return the specific amount of rotations for a piece
	 * @param pieceIden the identity of the piece
	 * @return the total unique rotations
	 */
	private static int findNRotations(int pieceIden) {
		int rotation = 1;
		switch (pieceIden) {
			case 1:
				rotation = 2;
				break;
			case 2:
				rotation = 1;
				break;
			case 3:
				rotation = 4;
				break;
			case 4:
				rotation = 4;
				break;
			case 5:
				rotation = 4;
				break;
			case 6:
				rotation = 2;
				break;
			case 7:
				rotation = 2;
				break;
		}
		return rotation;
	}
	
	/**
	 * Loads the first BUFFER_SIZE pieces from pieces into the buffer
	 */
	private static void loadInitialBuffer() {
		for (int i = 0; i < BUFFER_SIZE; i++) {
			buffer[i] = pieces[i];
		}
	}	
	
	/**
	 * Plays the best judged move and adds it into the solution
	 * @param bestMove
	 */
	private static void playBestPiece(Move bestMove) {
		//play the best scored processed move from all possibilities
		placePiece(bestMove.iden, bestMove.rotate, bestMove.pos);
		//store the move in the 'solution'
		solution.add(bestMove.iden + " " + bestMove.rotate + " " + bestMove.pos);
		
		//printGrid();
	}
	
	/**
	 * Method the place a piece onto my tetris gamestate within tetris rules
	 * @param iden the identity of the piece
	 * @param rotate the rotation
	 * @param pos the position, from the left wall
	 */
	private static void placePiece(int iden, int rotate, int pos) {
		int height;
		switch (iden) {
			//the I tetromino
			case 1: 
				//if original orientation
				if (rotate % 2 == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH)
						pos = GRID_WIDTH - 1;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 4;
					while (height > 0 && !grid[pos][height-1])
						height--;
					//finally, place the I tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos][height+2] = true;
					grid[pos][height+3] = true;
				} else { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 3)
						pos = GRID_WIDTH - 4;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT-1;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1] || grid[pos+2][height-1] || grid[pos+3][height-1]))
						height--;
					//finally, place the I tetromino piece
					grid[pos][height] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height] = true;
					grid[pos+3][height] = true;
				}
				break;
				
			//the square tetromino
			case 2: 
				//check the position, fix if necessary
				if (pos >= GRID_WIDTH - 1)
					pos = GRID_WIDTH - 2;
				//find appropriate height to place, within tetris 'original algorithm' gravity rules
				height = GRID_HEIGHT - 2;
				while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1]))
					height--;
				//finally, place the square tetromino piece
				grid[pos][height] = true;
				grid[pos+1][height] = true;
				grid[pos][height+1] = true;
				grid[pos+1][height+1] = true;
				break;
				
			//the T tetromino
			case 3: 
				//if original orientation
				if (rotate == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height]))
						height--;
					//finally, place the T tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos][height+2] = true;
				} else if (rotate == 1) { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1] || grid[pos+2][height-1]))
						height--;
					//finally, place the T tetromino piece
					grid[pos][height] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height] = true;
				} else if (rotate == 2) { // or rotated 180 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height] || grid[pos+1][height-1]))
						height--;
					//finally, place the T tetromino piece
					grid[pos+1][height+2] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
				} else if (rotate == 3) { // or rotated 270 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height] || grid[pos+1][height-1] || grid[pos+2][height]))
						height--;
					//finally, place the T tetromino piece
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height+1] = true;
				}
				break;
	
			//the J tetromino
			case 4: 
				//if original orientation
				if (rotate == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height+1]))
						height--;
					//finally, place the J tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos][height+2] = true;
					grid[pos+1][height+2] = true;
				} else if (rotate == 1) { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1] || grid[pos+2][height-1]))
						height--;
					//finally, place the J tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height] = true;
				} else if (rotate == 2) { // or rotated 180 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1]))
						height--;
					//finally, place the J tetromino piece
					grid[pos][height] = true;
					grid[pos+1][height] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height+2] = true;
				} else if (rotate == 3) { // or rotated 270 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height] || grid[pos+1][height] || grid[pos+2][height-1]))
						height--;
					//finally, place the J tetromino piece
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+2][height+1] = true;
					grid[pos+2][height] = true;
				}
				break;
	
			//the L tetromino
			case 5: 
				//if original orientation
				if (rotate == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height+1] || grid[pos+1][height-1]))
						height--;
					//finally, place the L tetromino piece
					grid[pos][height+2] = true;
					grid[pos+1][height+2] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
				} else if (rotate == 1) { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height] || grid[pos+2][height]))
						height--;
					//finally, place the L tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+2][height+1] = true;
				} else if (rotate == 2) { // or rotated 180 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1]))
						height--;
					//finally, place the L tetromino piece
					grid[pos][height+2] = true;
					grid[pos][height+1] = true;
					grid[pos][height] = true;
					grid[pos+1][height] = true;
				} else if (rotate == 3) { // or rotated 270 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1] || grid[pos+2][height-1]))
						height--;
					//finally, place the L tetromino piece
					grid[pos][height] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height] = true;
					grid[pos+2][height+1] = true;
				}
				break;
	
			//the S tetromino
			case 6: 
				//if original orientation
				if (rotate % 2 == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height] || grid[pos+1][height-1]))
						height--;
					//finally, place the S tetromino piece
					grid[pos][height+2] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
				} else { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height-1] || grid[pos+2][height]))
						height--;
					//finally, place the S tetromino piece
					grid[pos][height] = true;
					grid[pos+1][height] = true;
					grid[pos+1][height+1] = true;
					grid[pos+2][height+1] = true;
				}
				break;
	
			//the Z tetromino
			case 7: 
				//if original orientation
				if (rotate % 2 == 0) {
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 1)
						pos = GRID_WIDTH - 2;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 3;
					while (height > 0 && !(grid[pos][height-1] || grid[pos+1][height]))
						height--;
					//finally, place the Z tetromino piece
					grid[pos][height] = true;
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height+2] = true;
				} else { // or rotated 90 degree anticlockwise
					//check the position, fix if necessary
					if (pos >= GRID_WIDTH - 2)
						pos = GRID_WIDTH - 3;
					//find appropriate height to place, within tetris 'original algorithm' gravity rules
					height = GRID_HEIGHT - 2;
					while (height > 0 && !(grid[pos][height] || grid[pos+1][height-1] || grid[pos+2][height-1]))
						height--;
					//finally, place the Z tetromino piece
					grid[pos][height+1] = true;
					grid[pos+1][height+1] = true;
					grid[pos+1][height] = true;
					grid[pos+2][height] = true;
				}
				break;
				
			default:
				//although this case will never occur, include for sake of coding practice
				break;
		}
		clearFullLines();
	}
	
	private static void clearFullLines() {
		//loop through each row
		for (int j = 0; j < GRID_HEIGHT; j++) {
			//check if the row is 'complete'
			boolean lineDone = grid[0][j];
			//loop through each column, only while the line is complete so far
			int i = 1;
			while (lineDone) {
				lineDone = lineDone && grid[i][j];
				i++;
				//if gone through the entire line, and is all complete, exit loop
				if (i == GRID_WIDTH)
					break;
			}
			//if the line checked is complete
			if (lineDone) {
				//remove it by copying all lines above it down 1 row
				for (int jj = j; jj < GRID_HEIGHT - 1; jj++) {
					for (int ii = 0; ii < GRID_WIDTH; ii++) {
						grid[ii][jj] = grid[ii][jj+1];
					}
				}
				//finally fill the top row with empty spaces
				for (int iii = 0; iii < GRID_WIDTH; iii++) {
					grid[iii][GRID_HEIGHT-1] = false;
				}
				//recheck the (now) current row (the previous row ontop)
				j--;
			}
		}
	}
	
	/**
	 * Method to print the current grid state onto the terminal window.
	 * Primarily used for debugging
	 *
	private static void printGrid() {
		for (int j = GRID_HEIGHT - 1; j >= 0; j--) {
			for (int i = 0; i < GRID_WIDTH; i++) {
				System.out.print(grid[i][j] ? '#' : '.');
			}
			System.out.print(j + "\n");
		}
	}*/
	
	/*************************************************************************
	 *                  ARTIFICIAL INTELLIGENCE METHODS                      *
	 *************************************************************************/
	
	/**
	 * The method that implements my AI and plays tetris. Uses a genetic algorithm
	 * similar to A*
	 */
	private static void playTetris() {
		//if start of playing, load the buffer initially (part of my algorithm)
		loadInitialBuffer();
		
		//now, iterate through the next n - BUFFER_SIZE pieces
		for (int counter = 0; counter < count - BUFFER_SIZE; counter++) {
			//make an array of all potential pieces to be placed this move
			int[] use = new int[BUFFER_SIZE + 1];
			use[0] = pieces[BUFFER_SIZE + counter];
			for (int i = 0; i < BUFFER_SIZE; i++) {
				use[i+1] = buffer[i];
			}
			
			//make a copy of the current grid state
			boolean[][] tempGrid = copyGrid();
			//hold the currently best scored 'move'
			Move bestMove = null;
			//hold the currently best move score, initially set so any first move will replace it
			int bestScore = -999999999;
			//hold whether or not a buffer piece was used (swapped out)
			boolean buffUsed = false;
			//hold the index of the buffer piece used
			int buffUsedIndex = -1;
			//hold the index of the 
			
			//iterate through all the potential pieces to place
			for (int useIndex = 0; useIndex < use.length; useIndex++) {
				//determine how many rotations need to be processed for the upcoming piece
				int rotation = findNRotations(use[useIndex]);
				
				//for each rotation of the currently selected potential piece
				for (int y = 0; y < rotation; y++) {
					//and for each position in the grid (width wise)
					for (int z = 0; z < GRID_WIDTH; z++) {
						//unify the piece identity, rotation and position
						Move currentMove = new Move(use[useIndex],y,z);
						//place the piece in the original grid
						placePiece(use[useIndex],y,z);
						
						//score the current updated grid
						int score = scoreGrid();
						
						//if the currently processed score is the best so far, store it
						if (score > bestScore) {
							bestScore = score;
							bestMove = currentMove;
							
							if (useIndex > 0) {
								buffUsed = true;
								buffUsedIndex = useIndex - 1;
							}
						}
						
						//revert the grid back to its original state
						revertGrid(tempGrid);
					}
				}
			}
			
			if (buffUsed) {
				buffer[buffUsedIndex] = use[0];
			}
			playBestPiece(bestMove);
		}
		clearFinalBuffer();
	}
	
	/**
	 * A method the judge a given tetris game state and score it, a high score
	 * relates to a desired game state and a low score relates to an undesired state
	 * @return the score of the current game state
	 */
	private static int scoreGrid() {
		//determine the height of the current tetris grid
		int height = 0;
		
		//save the total number of blockades in the grid (a blockade is a block covering a hole)
		int totalBlockades = 0;
		
		//save the total number of holes in the grid (a hole is a non-existent block that is surrounded by blocks)
		int numHoles = 0;

		//calculate the height
		boolean rowExists = true;
		while (rowExists && height < GRID_HEIGHT) {
			rowExists = false;
			for (int i = 0; i < GRID_WIDTH; i++) {
				//if a block exists
				if (grid[i][height]) {
					//increment the height
					height++;
					rowExists = true;
					//no need to continue checking the current row
					break;
				}
			}
		}

		//check for holes and blockades
		
		//check the first column
		for (int a = 0; a < GRID_HEIGHT - 1; a++) {
			if (grid[1][a] && grid[0][a+1] && !grid[0][a]) {
				numHoles++;
				int h = a + 1;
				while (h < GRID_HEIGHT && grid[0][h]) {
					h++;
					totalBlockades++;
				}
			}
		}
		//check all middle columns
		for (int z = 1; z < GRID_WIDTH - 1; z++) {
			for (int b = 0; b < GRID_HEIGHT - 1; b++) {
				if (grid[z-1][b] && grid[z][b+1] && grid[z+1][b] && !grid[z][b]) {
					numHoles++;
					int h = b + 1;
					while (h < GRID_HEIGHT && grid[0][h]) {
						h++;
						totalBlockades++;
					}
				}
			}
		}
		//check the last column
		for (int c = 0; c < GRID_HEIGHT - 1; c++) {
			if (grid[GRID_WIDTH - 2][c] && grid[GRID_WIDTH - 1][c+1] && grid[GRID_WIDTH - 1][c]) {
				numHoles++;
				int h = c + 1;
				while (h < GRID_HEIGHT && grid[0][h]) {
					h++;
					totalBlockades++;
				}
			}
		}
		
		//calculate the 'score' of the current grid. very subject to change
		//all coefficients are made up experimentally
		//BLOCKADES AND HOLES NOT WORKING AS INTENDED....(documented in doco)
		int score = (height * (-380)) +  (totalBlockades * (0)) + (numHoles * (0)); 
		
		return score;
	}
	
	/**
	 * Private helper method to clear the final pieces of the buffer onto the 
	 * tetris game state. Created to reduce the size of my playTetris method
	 */
	private static void clearFinalBuffer() {
		//now, all pieces in the pieces array have been processed, empty out the buffer
		for (int j = 0; j < BUFFER_SIZE; j++) {
			//make a copy of the current grid state
			boolean[][] tempGrid = copyGrid();
			
			//hold the currently best scored 'move'
			Move bestMove = null;
			//hold the currently best move score, initially set so any first move will replace it
			int bestScore = -999999999;
			
			for (int cBuffer = j; cBuffer < BUFFER_SIZE; cBuffer++) {
				//determine how many rotations need to be processed for the upcoming piece
				int rotation = findNRotations(buffer[cBuffer]);
				//for each rotation of the next piece in the unprocessed buffer array
				for (int y = 0; y < rotation; y++) {
					//and for each position in the grid (width wise)
					for (int z = 0; z < GRID_WIDTH; z++) {
						//hold the current move data together
						Move currentMove = new Move(buffer[cBuffer],y,z);
						//place the piece in the original grid
						placePiece(buffer[cBuffer],y,z);
						
						//score the current updated grid
						int score = scoreGrid();
						
						//if the currently processed score is the best so far, store it
						if (score > bestScore) {
							bestScore = score;
							bestMove = currentMove;
						}
						
						//revert the grid back to its original state
						revertGrid(tempGrid);
					}
				}				
			}
			playBestPiece(bestMove);
		}
	}
	
	/*************************************************************************
	 *                             MAIN METHOD                               *
	 *************************************************************************/
	
	public static void main(String args[]) {
		//Check the usage of TetrisSolver
		if (args.length != 2) {
			System.out.println("Usage: java TetrisSolver exampleinput.txt exampleoutput.txt");
			return;
		}
		
		try {
			//read the input file
			readInFile(args[0]);
			//set the grid
			setGrid();
			//play tetris!
			playTetris();
			//write the solution to the output file
			writeOutFile(args[1]);
		} catch (IOException e) {
			//if fail to open input file
			System.out.println("Failed to open file: " + args[0]);
		}
		
		return;
	}
}
