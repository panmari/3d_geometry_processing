package sparse;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import sparse.CSRMatrix.col_val;


/**
 * A sparse matrix container in CSR format, to allow Solver Library independent
 * Matrix definition and computations
 *
 * @author bertholet
 *
 */
public class CSRMatrix {
	
	// row[i] will be the compresse representation of the ith row of the matrix.
	// Each row is represented by a list of column/value pairs.
	public ArrayList<ArrayList<col_val>> rows;
	
	public int nRows; //rows
	public int nCols; //columns
	
	
	/**
	 * Construct an empty rows x cols matrix
	 * @param rows
	 * @param cols
	 */
	public CSRMatrix( int rows, int cols){
		this.nRows = rows;
		this.nCols = cols;
		this.rows = new ArrayList<>(rows);
		
		for(int i = 0; i < rows; i++){
			this.rows.add( new ArrayList<col_val>());
		}
	}
	
	/**
	 * number of stored entries
	 * @return
	 */
	public int numberofEntries() {
		int num = 0;
		for(ArrayList<col_val> row: rows){
			num+= row.size();
		}
		return num;
	}
	
	/**
	 * Gets entry at the demanded row/col
	 * @param row
	 * @param col
	 * @return
	 */
	public float getValueAt(int row, int col) {
		for(col_val e: rows.get(row))
			if (e.col == col)
				return e.val;
		return 0;
	}
	
	/**
	 * Get the last row of this matrix
	 * @return
	 */
	public ArrayList<col_val> lastRow(){
		return rows.get(rows.size()-1);
	}
	
	
	/**
	 * Append an empty row to this matrix.
	 * @return the newly created row
	 */
	public ArrayList<col_val> addRow() {
		ArrayList<col_val> newRow = new ArrayList<col_val>();
		this.rows.add(newRow);
		this.nRows++;
		return newRow;
	}
	
	
	/**
	 * append scale * row to this matrix
	 * @param row
	 * @param scale
	 */
	private void addRow(ArrayList<col_val> row, float scale) {
		this.rows.add(row);
		this.nRows++;
		
		ArrayList<col_val> r = lastRow();
		for(col_val v: r){
			v.val *= scale;
		}
	}
	

	/**
	 * append the matrix  scale * mat to the actual matrix
	 * @param mat
	 * @param scale
	 */
	public void append(CSRMatrix mat, float scale) {
		assert(mat.nCols == nCols);
		for(ArrayList<col_val> row: mat.rows){
			this.addRow(row, scale);
			
		}
	}
	
	/**
	 * Compute the transposed of this matrix
	 * @return
	 */
	public CSRMatrix transposed(){
		CSRMatrix mat_T = new CSRMatrix(nCols, nRows);
		
		int row = 0;
		for(ArrayList<col_val> r: rows){

			for(col_val c_v : r){
				mat_T.rows.get(c_v.col).add(new col_val(row, c_v.val));
			}
			row++;
		}
		
		for(ArrayList<col_val> r: mat_T.rows){
			Collections.sort(r);
		}
		
		return mat_T;
	}
	
	
	/**
	 * Compute A + B and store it in this matrix
	 * @param A
	 * @param B
	 */
	public void add(CSRMatrix A, CSRMatrix B){
		assert(B.nCols == A.nCols && B.nRows == A.nRows);
		
		this.nCols = A.nCols;
		this.rows.clear();
		int idx1, idx2;
		ArrayList<col_val> row1, row2, rowRes;
		for(int row = 0; row < A.nRows; row++){
			idx1 = 0;
			idx2 = 0;
			
			this.addRow();
			rowRes = this.lastRow();
			row1 = B.rows.get(row);
			row2 = A.rows.get(row);
			
			while(idx1 < row1.size() && idx2 < row2.size()){
				if(row1.get(idx1).col < row2.get(idx2).col){
					rowRes.add(new col_val(row1.get(idx1++)));
				}
				else if(row1.get(idx1).col > row2.get(idx2).col){
					rowRes.add(new col_val(row2.get(idx2++)));
				}
				else{
					rowRes.add(new col_val(row1.get(idx1).col, 
							row1.get(idx1++).val + row2.get(idx2++).val));
				}
			}
			while(idx1 < row1.size()){
				rowRes.add(new col_val(row1.get(idx1++)));
			}
			while(idx2 < row2.size()){
				rowRes.add(new col_val(row2.get(idx2++)));
			}
			
			
		}
	}
	
	
	/**
	 * Multiply this matrix with the vector other and write the result into result.
	 * @param other
	 * @param result
	 */
	public void mult(ArrayList<Float> other, ArrayList<Float> result){
		if(other.size() != nCols)
			throw new IllegalArgumentException("Dimensions don't match: " +
											other.size() + " vs " + nCols);
		result.clear();
		result.ensureCapacity(nRows);
		
		
		float res;
		for(ArrayList<col_val> row : rows){
			res = 0;
			for(col_val c : row){
				res += c.val * other.get(c.col);
			}
			result.add(res);
		}

	}
	
	public <T extends Tuple3f> void multTuple(ArrayList<T> other, ArrayList<T> result) {
		if (other.size() != nCols)
			throw new IllegalArgumentException("Matrix size does not match to size of vector: " + 
												other.size() + " vs " +nCols);
		result.clear();
		result.ensureCapacity(nRows);
		for(ArrayList<col_val> row : rows){
			Tuple3f res = new Point3f();
			for(col_val c : row){
				Point3f o = new Point3f(other.get(c.col));
				o.scale(c.val);
				res.add(o);
			}
			result.add((T)res);
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ArrayList<col_val> row: rows) { 
			sb.append(row.toString());
			sb.append("\n");
		}
		return sb.toString();
	}	

	/**
	 * scale the matrix by some factor
	 * @param scale
	 */
	public void scale(float scale) {
		for(ArrayList<col_val> row: rows){
			for(col_val el : row){
				el.val *=scale;
			}
		}
	}
	
	/**
	 * Contains an integer, denoting the column of the entry, and a float, denoting the
	 * value of the entry.
	 * @author Alf
	 *
	 */
	public static class col_val implements Comparable<col_val>{
		public int col;
		public float val;
		
		public col_val(int column, float value) {
			this.col = column;
			this.val  = value;
		}
		public col_val(col_val o) {
			this.col = o.col;
			this.val = o.val;
		}
		
		@Override
		public int compareTo(col_val o) {
			return 
				this.col < o.col ? -1 :
				this.col == o.col ? 0 :
					1;
		}
		
		public String toString(){
			return "("+ this.col + "," + this.val + ") ";
		}
	}

/* ********************************************************************************/
/////////////////////////////////////////////////////////////////////////////////////
// Do not pass. Ugly ,optimized, uncommented code ahead.	
// Do not ignore this warning.
/////////////////////////////////////////////////////////////////////////////////////
/* ********************************************************************************/
	/**
	 * Multiply two matrices and store the result in result.
	 * 
	 * The method is sped up by multiplying two lower resolution bit matrices
	 * and then pruning the computations accordingly.
	 * 
	 * The code is ugly. Very ugly. Don't look at it.
	 * @param other
	 * @param result
	 */
	public void mult(CSRMatrix other, CSRMatrix result){
		assert(this.nCols == other.nRows);
		
		CSRMatrix otherT = other.transposed();
		ArrayList<col_val> Arow, Bcol;
		
		
		result.rows.clear();
		
		float value;
		int idxA, idxB, sz1, sz2;
		boolean exists;
		
		BitSet[] B = new BitSet[otherT.nRows];
		BitSet A = new BitSet(numBits2);
		BitSet temp = new BitSet(numBits2);
		
		int idx = 0;
		for(ArrayList<col_val> row: otherT.rows){
			B[idx] = new BitSet(numBits2);
			for(col_val c_v: row){
				B[idx].set(c_v.col * numBits2 / otherT.nCols);
			}
			idx++;
		}
		
		
		for(int i = 0; i < this.nRows; i++){
			result.rows.add(new ArrayList<col_val>());
			
			Arow = this.rows.get(i);	
			A.clear();
			for(col_val c_v: Arow){
				A.set(c_v.col * numBits2 / otherT.nCols);
			}
			
			for(int j = 0; j < otherT.nRows; j++){
				Bcol = otherT.rows.get(j);
				
				if(Arow.get(Arow.size() -1).col < Bcol.get(0).col ||
					Bcol.get(Bcol.size() -1).col < Arow.get(0).col){
					continue;
				}
				
				temp.and(A);
				temp.or(A);
				temp.and(B[j]);
				if(temp.isEmpty()){
					continue;
				}
				
				sz1 = Arow.size(); sz2 = Bcol.size();
				
				exists = false;
				value = idxA = idxB =0;
				while(idxA < sz1 && idxB < sz2){
					if(Arow.get(idxA).col < Bcol.get(idxB).col){
						idxA++;
					}
					else if(Arow.get(idxA).col > Bcol.get(idxB).col){
						idxB++;
					}
					else{
						value += Arow.get(idxA).val * Bcol.get(idxB).val;
						exists = true;
						idxA++;idxB++;
					}
				}
				
				if(exists){
					result.lastRow().add(new col_val(j,value));
				}
				
			}
		}
		
		result.nRows = this.nRows;
		result.nCols = other.nCols;
		
	
	}

	//magic speedup number.
	static final int numBits2 = 6104;
	
	/**
	 * Multiply two matrices using multiple threads.
	 * The multiplication is also sped up using lower resolution
	 * bit matrices.
	 * 
	 * Do not look at the code please. Please.
	 * @param other
	 * @param result
	 */
	public void multParallel(CSRMatrix other, CSRMatrix result){
		assert(this.nCols == other.nRows);
		assert(this!= result && other != result);
		CSRMatrix otherT = other.transposed();
		
		result.rows.clear();
		for(int i = 0; i < this.nRows; i++){
			result.addRow();
		}

		
		int idx = 0;
		

		//This BitSet array stores for columns what rows are nonzero
		BitSet[] rowsWithCol = new BitSet[numBits2];
		for(int i = 0; i < numBits2; i++){
			rowsWithCol[i] = new BitSet(numBits2);
		}
		
		idx = 0;
		int step = (int) Math.ceil((1.f*otherT.nRows +1) / numBits2);
		//int max = numBits2*step;
		for(ArrayList<col_val> row: otherT.rows){
			
			assert(idx/step <= numBits2);
			for(col_val c_v: row){
				rowsWithCol[(int)(((long) c_v.col) * numBits2 / otherT.nCols)].set(idx / step);
			}
			
			
			idx++;
		}
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()-1));
		RowServer rowServer = new RowServer(this.nRows);
		
		for(int i = 0; i < 30; i++){
			executor.execute(new MultThread(rowServer, otherT, result, null/*B*/, rowsWithCol, step));
		}
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result.nRows = this.nRows;
		result.nCols = other.nCols;
	}
	
	/**
	 * To allow multithreaded matrix multiplications
	 * @author Alf
	 *
	 */
	private class MultThread implements Runnable{
		
		private CSRMatrix result;
		private CSRMatrix otherT;
		private RowServer rowServer;
		BitSet sparsityA, temp, intersectedRows;
		//BitSet[] sparsityB;
		private BitSet[] rows_col;
		private int row_col_step;

		public MultThread(RowServer rowServer, CSRMatrix otherT, CSRMatrix result, 
				BitSet[] sparsityPattern_other, BitSet[] rowsWithCol, int step){
			this.rowServer = rowServer;
			this.otherT = otherT;
			this.result = result;
			
			sparsityA = new BitSet(numBits2);
			temp = new BitSet(numBits2);
			//sparsityB = sparsityPattern_other;
			this.rows_col = rowsWithCol; 
			this.row_col_step = step;
			this.intersectedRows = new BitSet(numBits2);
		}
		
		@Override
		public void run(){
			ArrayList<col_val> Arow, Bcol;
			ArrayList<col_val> resultRow;
			boolean exists;
			int sz1, sz2, idxA, idxB;
			float value;
			
			
			//thread gets a new rowmultiplication task, while there are tasks left
			for(int row = rowServer.getRow(); row >=0; row = rowServer.getRow()){
				resultRow = result.rows.get(row);
				Arow = rows.get(row);
				
				sparsityA.clear();
				for(col_val c_v: Arow){
					sparsityA.set((int)(((long) c_v.col )* numBits2 / otherT.nCols));
				}

				
				intersectedRows.clear();
				for(int i = sparsityA.nextSetBit(0); i>=0; i = sparsityA.nextSetBit(i+1)){
					intersectedRows.or(rows_col[i]);
					
					assert(rows_col[i].cardinality() <= numBits2);
				}
				
				//for(int j = 0; j < otherT.nRows; j++){
				for(int i = intersectedRows.nextSetBit(0); i>=0; i = intersectedRows.nextSetBit(i+1)){
					assert(i < numBits2);
					
					for(int j = i*row_col_step; 
							j < (i+1)*row_col_step && j <  otherT.nRows; 
							j++){//*/
						
						Bcol = otherT.rows.get(j);
						if(Arow.get(Arow.size() -1).col < Bcol.get(0).col ||
								Bcol.get(Bcol.size() -1).col < Arow.get(0).col){
							continue;
						}
					
						sz1 = Arow.size(); sz2 = Bcol.size();
						
						exists = false;
						value = idxA = idxB =0;
						while(idxA < sz1 && idxB < sz2){
							if(Arow.get(idxA).col < Bcol.get(idxB).col){
								idxA++;
							}
							else if(Arow.get(idxA).col > Bcol.get(idxB).col){
								idxB++;
							}
							else{
								value += Arow.get(idxA).val * Bcol.get(idxB).val;
								exists = true;
								idxA++;idxB++;
							}
						}
						
						if(exists){
							resultRow.add(new col_val(j,value));
							
						}
					}//iterate over relevant subset of js.	\
				}//end iterate over filled blocks			/
			}//end getTask loop.
		}//end run()
	}//end class
	
	
	/**
	 * Allow cpu-paralelized matrix multiplication.
	 * @author Alf
	 *
	 */
	private class RowServer{
		int nextRow;
		int numRows;
		
		RowServer(int numRows){
			this.nextRow = 0;
			this.numRows = numRows;
		}
		
		public synchronized int getRow(){
			if(nextRow < numRows){
				int result = nextRow;
				nextRow++;
				if(nextRow % 10000 == 0){
					System.out.println("do Row " + nextRow + " out of " + numRows + "!");
				}
				return result;
			}
			return -1;
		}
	}

}


