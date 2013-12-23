package sparse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sparse.CSRMatrix.col_val;
import sparse.solver.Solver;


public class MATLAB extends Solver {
	
	private String name;

	public MATLAB(String matName){
		this.name = matName;
	}

	public static void toMatlabFile( CSRMatrix mat, String matrixName, String folder) throws IOException{
		File f = new File(folder,"mtlb" + matrixName + ".m");
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		
		ArrayList<Integer> nzs = new ArrayList<>();
		ArrayList<Integer> is = new ArrayList<>(), js = new ArrayList<>();
		ArrayList<Float> as = new ArrayList<>();
		
		for(int i = 0; i < mat.nRows; i++){
			nzs.clear();
			for(col_val val : mat.rows.get(i)){
				//one based matlab
				is.add(i+1);
				js.add(val.col +1);
				
				as.add(val.val);
			}
		}
		
		
		w.write("is = [");
		for(int i: is){
			w.write("" +i  + " ");
		}
		w.write("];\n\n");
		
		w.write("js = [");
		for(int j: js){
			w.write("" +j  + " ");
		}
		w.write("];\n\n");
		
		w.write("as = [");
		for(float a: as){
			w.write("" +a  + " ");
		}
		w.write("];\n\n");
		
		
		w.write(matrixName + " = sparse(is,js,as,"+ mat.nRows + "," + mat.nCols + ");" +
				" \n clear is; clear js; clear as; ");
		w.flush();
		w.close();
	}
	
	/**
	 * This is pretty bad and hacky, using this for debugging purposes until i have a reliable solver.
	 * @param matrixName
	 * @param folder
	 * @param x
	 * @param b
	 * @throws IOException
	 */
	public static void solveMatlab(CSRMatrix mat, String matrixName, String folder, ArrayList<Float> x ,ArrayList<Float> b) throws IOException{
		File f = new File(folder,"mtlb" + matrixName + ".m");
		BufferedWriter w = new BufferedWriter(new FileWriter(f));

		ArrayList<Integer> nzs = new ArrayList<>();
		ArrayList<Integer> is = new ArrayList<>(), js = new ArrayList<>();
		ArrayList<Float> as = new ArrayList<>();
		
		for(int i = 0; i < mat.nRows; i++){
			nzs.clear();
			for(col_val val : mat.rows.get(i)){
				//one based matlab
				is.add(i+1);
				js.add(val.col +1);
				
				as.add(val.val);
			}
		}
		
		
		w.write("is = [");
		for(int i: is){
			w.write("" +i  + " ");
		}
		w.write("];\n\n");
		
		w.write("js = [");
		for(int j: js){
			w.write("" +j  + " ");
		}
		w.write("];\n\n");
		
		w.write("as = [");
		for(float a: as){
			w.write("" +a  + " ");
		}
		w.write("];\n\n");
		
		
		w.write(matrixName + " = sparse(is,js,as,"+ mat.nRows + "," + mat.nCols + "); " +
				"\n clear is; clear js; clear as; ");

		/*w.write("x = [");
		for(float val: x){
			w.write("" + val + " ");
		}
		w.write("]';\n\n");*/
		
		w.write("b = [");
		for(float val: b){
			w.write("" + val  + " ");
		}
		w.write("]';\n\n");
		
		w.write("x = " + matrixName + " \\ b;\n\n");
		
		w.write("dlmwrite('mtlb" + matrixName + ".out', x, '\\n');\n\n");
		w.write("exit;");

		w.flush();
		w.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("matlab -nojvm -nodisplay -nosplash -logfile matlab/out.log -r run('" + f.getPath() + "') -wait");
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		
		readVector("./matlab/mtlb" + matrixName + ".out", x);
		
		
		BufferedReader r = new BufferedReader(new FileReader("./matlab/out.log"));
		String line;
		while((line = r.readLine())!= null){
			System.out.println(line);
		}
		r.close();
		
		System.out.println("there, there...");
	}
	
	
	/*public static void toMatlabFile(ImplicitMatrix mat, String matrixName, String folder) throws IOException{
		File f = new File(folder,"mtlb" + matrixName + ".m");
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		ArrayList<Integer> nzs = new ArrayList<>();
		ArrayList<Integer> is = new ArrayList<>(), js = new ArrayList<>();
		ArrayList<Float> as = new ArrayList<>();
		for(int i = 0; i < mat.getRows(); i++){
			nzs.clear();
			mat.nonzeroColumns(i, nzs);
			for(int j : nzs){
				//one based matlab
				is.add(i+1);
				js.add(j+1);
				
				as.add(mat.val(i,j));
			}
		}
		
		
		w.write("is = [");
		for(int i: is){
			w.write("" +i  + " ");
		}
		w.write("];\n\n");
		
		w.write("js = [");
		for(int j: js){
			w.write("" +j  + " ");
		}
		w.write("];\n\n");
		
		w.write("as = [");
		for(float a: as){
			w.write("" +a  + " ");
		}
		w.write("];\n\n");
		
		
		w.write(matrixName + " = sparse(is,js,as," + mat.getRows() + "," + mat.getColumns() + "); \n clear is; clear js; clear as; ");
		w.flush();
		w.close();
	}*/
	
	public static void executeMFile(String file) throws IOException{
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("matlab -nojvm -nodisplay -nosplash -logfile matlab/out.log -r run('" + file + "') -wait");
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
		
		BufferedReader r = new BufferedReader(new FileReader("./matlab/out.log"));
		String line;
		while((line = r.readLine())!= null){
			System.out.println(line);
		}
		r.close();
	}
	
	/**
	 * This is pretty bad and hacky, using this for debugging purposes until i have a reliable solver.
	 * @param matrixName
	 * @param folder
	 * @param x
	 * @param b
	 * @throws IOException
	 */
	/*public static void solveMatlab(ImplicitMatrix mat, 
			String matrixName, 
			String folder, 
			ArrayList<Float> x ,
			ArrayList<Float> b) throws IOException{
		File f = new File(folder,"mtlb" + matrixName + ".m");
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		ArrayList<Integer> nzs = new ArrayList<>();
		ArrayList<Integer> is = new ArrayList<>(), js = new ArrayList<>();
		ArrayList<Float> as = new ArrayList<>();
		for(int i = 0; i < mat.getRows(); i++){
			nzs.clear();
			mat.nonzeroColumns(i, nzs);
			for(int j : nzs){
				//one based matlab
				is.add(i+1);
				js.add(j+1);
				
				as.add(mat.val(i,j));
			}
		}
		
		
		w.write("is = [");
		for(int i: is){
			w.write("" +i  + " ");
		}
		w.write("];\n\n");
		
		w.write("js = [");
		for(int j: js){
			w.write("" +j  + " ");
		}
		w.write("];\n\n");
		
		w.write("as = [");
		for(float a: as){
			w.write("" +a  + " ");
		}
		w.write("];\n\n");
		
		
		w.write(matrixName + " = sparse(is,js,as,"+ mat.getRows()+ "," + mat.getColumns() + ");" +
				" \n clear is; clear js; clear as; ");


		
		w.write("b = [");
		for(float val: b){
			w.write("" + val  + " ");
		}
		w.write("]';\n\n");
		
		w.write("x = " + matrixName + " \\ b;\n\n");
		
		w.write("dlmwrite('mtlb" + matrixName + ".out', x, '\\n');\n\n");
		w.write("exit;");

		w.flush();
		w.close();
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("matlab -nojvm -nodisplay -nosplash -logfile matlab/out.log -r run('" + f.getPath() + "') -wait");
		try {
			pr.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		BufferedReader r = new BufferedReader(new FileReader("./matlab/out.log"));
		String line;
		while((line = r.readLine())!= null){
			System.out.println(line);
		}
		r.close();
		
		
		readVector("./matlab/mtlb" + matrixName + ".out", x);
		
		
		System.out.println("there, there...");
	}*/

	public static void readVector(String file, ArrayList<Float> x)
			throws FileNotFoundException, IOException {
		

		
		//read solution;
		BufferedReader r = new BufferedReader(new FileReader(file));
		String line;
		x.clear();
		while((line = r.readLine())!= null){
			x.add(Float.parseFloat(line));
		}
		r.close();
	}

	@Override
	public void solve(CSRMatrix mat, ArrayList<Float> b, ArrayList<Float> x) {
		try {
			solveMatlab(mat, name, "./matlab/", x, b);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
