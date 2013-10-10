package sparse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import sparse.CSRMatrix.col_val;

/**
 * An ugly basic interface to use the sparse least square solver
 * from scipy. It is assumed that numpy/scipy is installed and
 * is callable from the cmd prompt /bash. Install for example the
 * free Anaconda distribution: https://store.continuum.io/cshop/anaconda/
 * <p>
 * A sparse matrix is written into text files, then a python script
 * is called, that computes the least squares solution and writes it back to
 * some file; the result is then read back into the java program
 * </p>
 * @author Alf
 *
 */
public class SCIPY {
	
	
	/**
	 * Black box solver method. Name is used to generate
	 * @param l
	 * @param name
	 * @param x_out
	 * @throws IOException
	 */
	public static void solve(LinearSystem l, String name, ArrayList<Float> x_out) throws IOException{
		System.out.println("Dumping matrix to files...");
		toPythonFiles(l.mat, l.b, name);
		System.out.println("Running Python Script...");
		runLeastSquaresPy(name, x_out);
	}
	
	/**
	 * Dump the matrix and the vector stored in b into a bunch of
	 * text files, name is used to prefix the files.
	 * @param mat
	 * @param b
	 * @param name
	 * @throws IOException
	 */
	public static void toPythonFiles(CSRMatrix mat, ArrayList<Float> b, String name) throws IOException{
		File f_i = new File("./python/temp/" + name + "ifile");
		if(! f_i.getParentFile().exists()){
			f_i.getParentFile().mkdir();
		}
		BufferedWriter w_i = new BufferedWriter(new FileWriter(f_i));
		
		File f_j = new File("./python/temp/" + name + "jfile");
		BufferedWriter w_j = new BufferedWriter(new FileWriter(f_j));
		
		File f_v = new File("./python/temp/" + name + "vfile");
		BufferedWriter w_v = new BufferedWriter(new FileWriter(f_v));
		
		int i = 0;
		for(ArrayList<col_val> row: mat.rows){
			for(col_val c: row){
				w_i.write(i + "\n");
				w_j.write(c.col + "\n");
				w_v.write(c.val + "\n");
			}
			i++;
		}
		
		w_i.flush();
		w_i.close();
		w_j.flush();
		w_j.close();
		w_v.flush();
		w_v.close();
		
		
		File f_b = new File("./python/temp/" + name + "bfile");
		BufferedWriter w_b = new BufferedWriter(new FileWriter(f_b));
		for(float bval : b){
			w_b.write(bval + "\n");
		}
		w_b.flush();
		w_b.close();
	}
	
	public static void runLeastSquaresPy(String matrix_name, ArrayList<Float> x) throws IOException{
		
		
		/*execute the script*/
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("python ./python/doLeastSqr.py " +
				"-i ./python/temp/" + matrix_name + "ifile " +
				"-j ./python/temp/" + matrix_name + "jfile " +
				"-b ./python/temp/" + matrix_name + "bfile " +
				"-v ./python/temp/" + matrix_name + "vfile " +
				"-x ./python/temp/" + matrix_name + "xout ");
		
	    
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		/*get std.err and std.out*/
		printConsoleOutput(pr);
		
		readVector("./python/temp/" + matrix_name + "xout", x);
		
	}

	private static void readVector(String string, ArrayList<Float> x) throws IOException {
		x.clear();
		BufferedReader in = new BufferedReader(
				new FileReader(string));
		String line;
	    while ((line = in.readLine()) != null) {
	       x.add(new Float(line));
	    }
	    
	    in.close();
	}

	private static void printConsoleOutput(Process pr) throws IOException {
		String line;
		BufferedReader in = new BufferedReader(
				new InputStreamReader(pr.getErrorStream()));
	    while ((line = in.readLine()) != null) {
	        System.err.println(line);
	    }
	    in.close();
	    in = new BufferedReader(
				new InputStreamReader(pr.getInputStream()));
	    while ((line = in.readLine()) != null) {
	        System.out.println(line);
	    }
	    in.close();
	}
	

}
