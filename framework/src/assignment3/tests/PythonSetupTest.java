package assignment3.tests;

import java.io.IOException;
import java.util.ArrayList;

import sparse.SCIPY;

public class PythonSetupTest {

	public static void main(String[] args) throws IOException{
		
		ArrayList<Float> x = new ArrayList<>();
		SCIPY.runLeastSquaresPy("",x);
		
		if(x.get(0)>=0.9999 && x.get(0) <= 1.0001 && 
				x.get(1) <= 0.33334 && x.get(1) >= 0.3333 && 
				x.get(2) <= 0.2001 && x.get(2) >= 0.1999){
			System.out.println("Congratulations! Python works!");
		}
	}
}
