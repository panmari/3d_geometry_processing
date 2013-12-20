package assignment7.morphing.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import meshes.reader.ObjWriter;
import assignment7.morphing.Morpher;
import assignment7.morphing.Weighting;


public class MorphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8859747355572494788L;

	public static final int MIN = 0, MAX = 150, INIT = 0;
	public static final float SCALE = 100f;

	public MorphPanel(Morpher morpher, MorphDisplay display) {
		super();

		this.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// this.setLayout(new GridLayout(0, 1));

		JTextField filenameField = new JTextField("morphed.obj");

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new SaveButtonListener(morpher,
				filenameField));

		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));
		adjustLayout(savePanel);
		filenameField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
				saveButton.getPreferredSize().height));
		savePanel.add(filenameField);
		savePanel.add(saveButton);
		this.add(savePanel);

		JCheckBox checkbox = new JCheckBox("Highlight diff with colors");
		checkbox.setSelected(true);
		checkbox.addActionListener(new CheckboxListener(morpher, display));
		adjustLayout(checkbox);
		this.add(checkbox);

		List<String> figureNames = morpher.getFigureNames();
		int index = 0;
		for (String name : figureNames) {
			JPanel personPanel = new JPanel();
			personPanel.setLayout(new BoxLayout(personPanel, BoxLayout.Y_AXIS));
			personPanel.setBorder(new CompoundBorder(BorderFactory
					.createEmptyBorder(2, 0, 2, 0), BorderFactory
					.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY)));
			this.add(personPanel);

			JLabel label = new JLabel(name);
			label.setForeground(Color.GRAY);
			adjustLayout(label);
			personPanel.add(label);

			JSlider slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, INIT);
			slider.setMajorTickSpacing((int) SCALE);
			// slider.setMinorTickSpacing((int) (SCALE / 2));
			slider.setPaintTicks(true);
			slider.addChangeListener(new SliderChangeListener(index, morpher,
					display));
			adjustLayout(slider);
			personPanel.add(slider);

			JLabel functionLabel = new JLabel("f(d,x,y,z)=");

			JTextField textField = new JTextField(Weighting.VAR_DIFF);
			textField.setMinimumSize(new Dimension(200, 0));

			Font monospace = new Font(Font.MONOSPACED, Font.PLAIN, 11);
			functionLabel.setFont(monospace);
			textField.setFont(monospace);

			JButton button = new JButton("Go");
			button.setEnabled(false);

			button.addActionListener(new ButtonClickListener(index, morpher,
					display, textField));
			textField.addKeyListener(new TextFieldChangeListener(button));

			JPanel functionPanel = new JPanel();
			functionPanel.setLayout(new BoxLayout(functionPanel,
					BoxLayout.X_AXIS));
			functionPanel.add(functionLabel);
			functionPanel.add(textField);
			functionPanel.add(button);
			textField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
					functionPanel.getPreferredSize().height));
			// setLayout(textField);
			// setLayout(button);
			adjustLayout(functionPanel);
			personPanel.add(functionPanel);

			index++;
		}

		display.repaint();
	}

	private void adjustLayout(JComponent component) {
		component.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(
				2, 4, 2, 4, this.getBackground()), component.getBorder()));
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		// component.setBackground(new Color(220, 220, 220));
		// component.setForeground(new Color(200, 200, 200));
	}

	private static class SliderChangeListener implements ChangeListener {

		protected int index;
		protected Morpher morpher;
		protected MorphDisplay display;

		public SliderChangeListener(int index, Morpher morpher,
				MorphDisplay display) {
			this.index = index;
			this.morpher = morpher;
			this.display = display;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider source = (JSlider) e.getSource();
			float weight = source.getValue() / SCALE;
			morpher.setWeighting(index, weight);
			display.updateMorph();
		}

	}

	private static class TextFieldChangeListener implements KeyListener {

		private JButton button;

		public TextFieldChangeListener(JButton button) {
			this.button = button;
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			button.setEnabled(true);
		}

	}

	private static class ButtonClickListener implements ActionListener {

		private int index;
		private Morpher morpher;
		private MorphDisplay display;
		private JTextField textField;

		public ButtonClickListener(int index, Morpher morpher,
				MorphDisplay display, JTextField textField) {
			this.index = index;
			this.morpher = morpher;
			this.display = display;
			this.textField = textField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			button.setEnabled(false);

			String function = textField.getText();
			morpher.setWeightingFunction(index, function);
			display.updateMorph();
		}

	}

	private static class SaveButtonListener implements ActionListener {

		private Morpher morpher;
		private JTextField filenameField;

		public SaveButtonListener(Morpher morpher, JTextField filenameField) {
			this.morpher = morpher;
			this.filenameField = filenameField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String file = filenameField.getText();
			try {
				new ObjWriter(file).write(morpher.getCurrent());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	private static class CheckboxListener implements ActionListener {

		private Morpher morpher;
		private MorphDisplay display;

		public CheckboxListener(Morpher morpher, MorphDisplay display) {
			this.morpher = morpher;
			this.display = display;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox checkbox = (JCheckBox) e.getSource();
			morpher.setDiffEnabled(checkbox.isSelected());
			display.updateMorph();
		}
	}
}
