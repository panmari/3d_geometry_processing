package assignment2.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import assignment2.MortonCodes;

public class MortonCodesTests {

	long hash = 		0b1000101000100;
	
	//the hashes of its parent and neighbors
	long parent = 		0b1000101000;
	long nbr_plus_x = 	0b1000101100000;
	long nbr_plus_y =   0b1000101000110;
	long nbr_plus_z =   0b1000101000101;
	
	@Test
	public void testParent() {
		assertEquals(parent, MortonCodes.parentCode(hash));
		assertEquals(0b1000101100, MortonCodes.parentCode(nbr_plus_x));
		assertEquals(parent, MortonCodes.parentCode(nbr_plus_y));
		assertEquals(parent, MortonCodes.parentCode(nbr_plus_z));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 0));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 1));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 2));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 3));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 4));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 5));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 6));
		assertEquals(parent, MortonCodes.parentCode((parent << 3) + 7));
		assertFalse(parent == MortonCodes.parentCode((parent << 3) + 8)); 
	}
	
	@Test
	public void testCellonLevelXgrid() {
		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1000, 1));
		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1000000, 2));
		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1010, 1));
		assertTrue(MortonCodes.isCellOnLevelXGrid(0b1100, 1));
		
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000, 2));
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000000, 1));
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000000001, 1));
		assertFalse(MortonCodes.isCellOnLevelXGrid(0b1000111, 1));
	}
	
	private void assertEquals(long exp, long got) {
		if (exp != got)
			fail("expected: " + Long.toBinaryString(exp) + " but got: " + Long.toBinaryString(got));
		else org.junit.Assert.assertEquals(exp, got); //fall back to default assertion for green tests!
	}

}
