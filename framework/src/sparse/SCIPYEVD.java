package sparse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Call a script to do the eigenvalue decomposition.
 * This calls a python method which can only be used for symmetric matices
 * @author Alf
 *
 */
public class SCIPYEVD {

	/**
	 * 
	 * @param mat : the matrix to decompose
	 * @param prefix : prefix for the files to dump the matrix to
	 * @param numEVs  : number of eigenvector/value pairs to compute
	 * @throws IOException
	 */
	public static void doSVD(CSRMatrix mat, 
			String prefix, 
			int numEVs, 
			ArrayList<Float> eigenValues,
			ArrayList<ArrayList<Float>> eigenVectors) throws IOException{
		
		//dump stuff in files
		SCIPY.toPythonFiles(mat, new ArrayList<Float>(), prefix);
		
		//run the script
		runSVDScript(prefix, numEVs);
		
		//read the results and copy them to the provided arrays.
		loadResults(prefix, numEVs, eigenValues, eigenVectors);
	}

	/**
	 * To load results which where already computed. Load k eigenvectors and eigenvalues
	 * @param prefix
	 * @param eigs
	 * @param vecs
	 * @throws IOException
	 */
	public static void loadResults(
			String prefix,
			int k,
			ArrayList<Float> eigs,
			ArrayList<ArrayList<Float>> vecs) throws IOException {
		
		readVector("./python/temp/" + prefix + "out_vals", eigs);
		while(eigs.size() > k){
			eigs.remove(eigs.size()-1);
		}
		
		readVectors("./python/temp/" + prefix + "out_vecs", vecs, Math.min(eigs.size(), k));
		
	}

	private static void runSVDScript(String matrix_name, int k) throws IOException {
		/*execute the script*/
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("python ./python/doSparseEig.py " +
				"-i ./python/temp/" + matrix_name + "ifile " +
				"-j ./python/temp/" + matrix_name + "jfile " +
				"-v ./python/temp/" + matrix_name + "vfile " +
				"-o ./python/temp/" + matrix_name + "out " +
				"-k " + k);
		
	    
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		/*get std.err and std.out*/
		printConsoleOutput(pr);
	}
	
	/**
	 * Read " " delimited file, where each line corresponds to one vector
	 * @param file
	 * @param vecs
	 * @param k 
	 * @throws IOException
	 */
	private static void readVectors(String file,
			ArrayList<ArrayList<Float>> vecs, int k) throws IOException {
		
		vecs.clear();
		BufferedReader in = new BufferedReader(
				new FileReader(file));
		String line;
		
		int readVecs = 0;
	    while ((line = in.readLine()) != null && readVecs<k) {
	    	readVecs++;
	    	vecs.add(new ArrayList<Float>());
	    	ArrayList<Float> vec = vecs.get(vecs.size() -1);
	    	for(String num : line.split(" ")){
	    		vec.add(new Float(num));
	    	}
	    	
	    	normalize(vec);
	    }
	    in.close();
	}

	private static void normalize(ArrayList<Float> vec) {
		float norm = 0;
		for(float v : vec){
			norm += v*v;
		}
		norm = (float) Math.sqrt(norm);
		for(int i = 0; i < vec.size(); i++){
			vec.set(i, vec.get(i)/norm);
		}
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
