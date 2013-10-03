package assignment2;


/**
 * Implement the Morton Code manipulations here. 
 *
 */
public class MortonCodes {
	
	/** the three masks for dilated integer operations */
	public static final long d100100 = 0b100100100100100100100100100100100100100100100100100100100100100L, 
			d010010 = 0b010010010010010010010010010010010010010010010010010010010010010L, 
			d001001 = 0b001001001001001001001001001001001001001001001001001001001001001L;
	
	
	/**
	 * return the parent morton code
	 * @param code
	 * @return
	 */
	public static long parentCode(long code){
		
		
		//implement this.
		return -1L;
	}
	
	/**
	 * return the (positive) neighbor code at the relative position encoded by
	 * 0bxyz using dilated addition
	 * @param code
	 * @param level
	 * @param Obxyz
	 * @return
	 */
	public static long nbrCode(long code, int level, int Obxyz){
		
		//implement this
		return -1L;
	}

	/**
	 * return the (negative) neighbor code at the relative position encoded by
	 * 0bxyz using dilated subtraction
	 * @param code
	 * @param level
	 * @param Obxyz
	 * @return
	 */	
	public static long nbrCodeMinus(long code, int level, int Obxyz){
		//implement this
		return -1L;
	}
	
	
	
	/**
	 * A test to check if an overflow/underflow has occurred. it is enough to test
	 * if the delimiter bit is untouched and is the highest bit set.
	 * @param code
	 * @param level
	 * @return
	 */
	public static boolean overflowTest(long code, int level){

		//implement this
		
		return true;
		
	}
	
	
	/**
	 * Check if the cell_code is a morton code associated to the grid level
	 * given in the argument. A cell code is associated to a specific level
	 * @param cell_code
	 * @param level
	 * @return
	 */
	public static boolean isCellOnLevelXGrid(long cell_code, int level){

		//implement this..
		return false;
	}
	
	
	/**
	 * A test to check if the vertex_code (a morton code padded with zeros to have the length
	 * 3*tree_depth + 1) is associated to a vertex which is part of the {@param level}-grid.
	 * 
	 * This is determined by the number of trailing zeros, and if a vertex lies on some level k
	 * it will lie on the levels k+1,k+2... tree_depth too.
	 */
	public static boolean isVertexOnLevelXGrid(long vertex_code, int level, int tree_depth){
		//implement this..
		return false;
	}
	
	/**
	 * A test to check if a vertex code is logically describing a boundary vertex.
	 */
	public static boolean isVertexOnBoundary(long vertex_code, int tree_depth){
		boolean is = (vertex_code & (0b111 << 3*(tree_depth-1)))!= 0 || //x==1, y==1 or z==1 in a unit cube
				(vertex_code & d100100) == 0 || //x==0
				(vertex_code & d010010) == 0 || //y==0
				(vertex_code & d001001) == (0b1 << 3*tree_depth) ; //z==0 (only the delimiter bit is set)
		
		return is;
	}
	
}
