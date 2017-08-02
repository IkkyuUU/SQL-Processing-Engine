import java.applet.*;

import javax.swing.*;

import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * The simple interface to get the input.
 * @author Matix:Zhiqian Yu, Peiying Deng, Chao Xi
 *
 */


public class SimpleInterface extends Applet implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextArea textA;
	JTextField textF;
	JButton b1, b2;
	JRadioButton jr1;
	JRadioButton jr2;
	ButtonGroup bg=new ButtonGroup();
	
	public void init() {
		setSize(800, 500);
		textA = new JTextArea("", 20, 50);
		// textA.setBackground(Color.cyan);
		b1 = new JButton("Input Operands");
		textF = new JTextField("", 50);
		// textF.setBackground(Color.pink);
		b2 = new JButton("Input File URL");
		// textF.setEditable(false);
		jr1 = new JRadioButton("General Algorithm", true);
		jr2 = new JRadioButton("Optimized Algorithm");
		bg.add(jr1);
		bg.add(jr2);
		b1.addActionListener(this);
		b2.addActionListener(this);
		
		add(textA);
		add(b1);
		add(textF);
		add(b2);
		add(jr1);
		add(jr2);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == b1) {
			String s = textA.getText();
			if (s.isEmpty()) {
				textA.setText("Can't be empty!");
			} else {
				String filepath = "/Users/fisheryzhq/Documents/workspace/";
				String fileout = filepath + "input.text";
				File fout = new File(fileout);
				FileOutputStream output;
				try {
					output = new FileOutputStream(fout);
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(output));
					bw.write(s);
					bw.close();
					if(jr1.isSelected())
						GenerateAlgorithms.getFromInterface(fileout);
					if(jr2.isSelected())
						OptimizationAlgorithm.getFromInterface(fileout);
					System.exit(0);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (e.getSource() == b2) {
			String s = textF.getText();
			if (!(s.isEmpty())) {
				if(jr1.isSelected())
					GenerateAlgorithms.getFromInterface(s);
				if(jr2.isSelected())
					OptimizationAlgorithm.getFromInterface(s);
				System.exit(0);
			}
			else {
				textF.setText("Can't be empty!");
			}
			
		}
	}
}