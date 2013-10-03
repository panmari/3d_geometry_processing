package assignment2;

import java.io.IOException;


public class Assignment2 {
	
	public static void main(String[] args) throws IOException{
		
		mortonCodesDemo();
		
		//these demos will run once all methods in the MortonCodes class are
		//implemented.
		//hashTreeDemo(ObjReader.readAsPointCloud("./objs/dragon.obj", true));
		//hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest2.ply", true));
		
	}
	

	
	public static void mortonCodesDemo(){
		
		//example of a level 4 (cell) morton code
		long hash = 		0b1000101000100;
		
		//the hashes of its parent and neighbors
		long parent = 		0b1000101000;
		long nbr_plus_x = 	0b1000101100000;
		long nbr_plus_y =   0b1000101000110;
		long nbr_plus_z =   0b1000101000101;
		
		long nbr_minus_x = 	0b1000101000000;
		long nbr_minus_y =  -1; //invalid: the vertex lies on the boundary and an underflow should occur
		long nbr_minus_z =  0b1000100001101;
		
		
		//example of a a vertex morton code in a multigrid of
		//depth 4. It lies on the level 3 and 4 grids
		long vertexHash = 0b1000110100000;
				
		//you can test your MortonCode methods by checking these results, e.g. as a Junit test
		//Further test at least one case where -z underflow should occur
		//and a case where overflow occurs.
	
	}
}
