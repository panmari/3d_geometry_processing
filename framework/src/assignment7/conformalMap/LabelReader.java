package assignment7.conformalMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

import javax.vecmath.Point2f;

public class LabelReader {
	Scanner lblScanner;
	Scanner txcScanner;
	
	public LabelReader(String lblFilename, String txcFilename) throws FileNotFoundException {
		lblScanner = new Scanner(new File(lblFilename));
		if(txcFilename != null)
		{
			txcScanner = new Scanner(new File(txcFilename));
		}
	}
	
	public LinkedHashMap<String, Integer> readIndices()
	{
		int index;
		String labelname;
		
		LinkedHashMap<String, Integer> lbl = new LinkedHashMap<String, Integer>();
		
		while(lblScanner.hasNext())
		{
			if(!lblScanner.hasNextInt())
			{
				lblScanner.nextLine();
				continue;
			}
			index = lblScanner.nextInt();
			labelname = lblScanner.next();
			
			//System.out.println("" + index + "=>" + labelname);
			lbl.put(labelname, index);
		}
		
		return lbl;
	}
	
	public LinkedHashMap<Integer, Point2f> read()
	{
		HashMap<String, Integer> lbl = readIndices();
		
		LinkedHashMap<Integer, Point2f> out = new LinkedHashMap<Integer, Point2f>();
		String labelname;
		
		while(txcScanner.hasNext())
		{
			if(!txcScanner.hasNextFloat())
			{
				txcScanner.nextLine();
				continue;
			}
			Point2f p = new Point2f();
			p.x = txcScanner.nextFloat();
			p.y = txcScanner.nextFloat();
			labelname = txcScanner.next();
			assert(lbl.containsKey(labelname));
			out.put(lbl.get(labelname), p);
			//System.out.println(labelname + "=>" + p);
		}
		
		return out;
	}
}
