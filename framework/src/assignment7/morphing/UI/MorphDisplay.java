package assignment7.morphing.UI;

import glWrapper.GLMorphHEStructure;
import glWrapper.GLUpdatableHEStructure;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import openGL.MyDisplay;
import assignment7.morphing.Morpher;



public class MorphDisplay extends MyDisplay {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5025768923535567025L;

	private GLUpdatableHEStructure glHe;

	public MorphDisplay(Morpher morpher) {
		super();
		this.eastpane.setVisible(false);
		MorphPanel morphPanel = new MorphPanel(morpher, this);
		JScrollPane scrollPane = new JScrollPane(morphPanel);
		this.getContentPane().add(scrollPane, BorderLayout.WEST);
		glHe = new GLMorphHEStructure(morpher.getCurrent());
		glHe.configurePreferredShader("shaders/trimesh_flat_diff.vert",
				"shaders/trimesh_flat_diff.frag", "shaders/trimesh_flat_diff.geom");
		this.addToDisplay(glHe);
	}

	public void updateMorph() {
		this.glHe.updatePosition();
		this.updateDisplay();
	}
}
