package myutils;

import com.jogamp.opengl.math.FloatUtil;

public class MyMath {
	public static float cot(float z) {
		return 1/(float)Math.tan(z);
	}
	
	public static float cot(float z, boolean clamp) {
		float cot = cot(z);
		if (clamp)
			return (float)Math.min(1e2, Math.max(-1e2, cot));
		else return cot;
	}
}
