package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;

/**
 * Interface for the marchingcubes implementation of Assignment 3. 
 * Both the {@link HashOctreeCell}s and {@link HashVertex}s
 * should implement this interface to allow primary and dual marching cubes
 * in a unified way.
 * @author bertholet
 *
 */
public interface MarchableCube {
	

	/**
	 * Get the position of this marchable cube. I.e. the center or the vertex position.
	 * @return
	 */
	public Point3f getPosition();
	
	/**
	 * Return the neighbor element denoted by 0bxyz- if this is a cell, this should return the denoted vertex;
	 * If this is a vertex, this should return the denoted neighbor cell. For vertices it is possible that
	 * the same element is returned for multiple arguments 0bxyz.
	 * @param Obxyz
	 * @param tree
	 * @return
	 */
	public MarchableCube getCornerElement(int Obxyz, HashOctree tree);
	
	/**
	 * Return an index which is unique i.e. the leafIndex of the vertexes index.
	 * @return
	 */
	public int getIndex();
}
