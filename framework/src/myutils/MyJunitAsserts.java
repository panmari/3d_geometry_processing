package myutils;

import static org.junit.Assert.assertTrue;

import javax.vecmath.Tuple3f;

public class MyJunitAsserts {

	/**
	 * Compares the two given tuples a and b (with an epsilon equal to 0.01)
	 * @param a
	 * @param b
	 */
	public static <T extends Tuple3f> void assertEquals(T a, T b) {
		assertTrue("expected " + a + ", but was" + b, a.epsilonEquals(b, 0.01f));
	}
}
