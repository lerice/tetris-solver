
/**
 * AI4211 Project
 * Object to hold data for a Tetris 'move' together
 *
 * @author 20925931 Eric (Jun) Tan
 * @version 26th May 2012
 */
public class Move {
	/**
	 * An instance variable to hold the identity of the piece, as specified 
	 * in the project specification (value of 1 - 7)
	 */
	public int iden;
	
	/**
	 * An instance variable to hold the anti-clockwise rotation of the piece
	 * by 90 * rotate degrees
	 */
	public int rotate;
	
	/**
	 * An instance variable to hold value of the position of the piece, pos  
	 * units away from the left wall (where any value larger than the width
	 * of the grid will be placed touching the right wall)
	 */
	public int pos;
	
	/**
	 * Basic constructor for a tetris move
	 * @param iden the identity of the piece
	 * @param rotate the value of rotation
	 * @param pos the value of the position to drop
	 */
	public Move(int iden, int rotate, int pos) {
		this.iden = iden;
		this.rotate = rotate;
		this.pos = pos;
	}
}
