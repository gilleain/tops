package tops.drawing.app;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

import tops.drawing.Cartoon;
import tops.drawing.CartoonLayout;
import tops.drawing.model.Topology;

public class LayoutTest extends JPanel implements ActionListener {
	
	private Cartoon cartoon;
	private JTextField textField;
	
	public LayoutTest(JTextField textField) {
		this.cartoon = null;
		this.textField = textField;
		this.setPreferredSize(new Dimension(300, 300));
	}
	
	public void setGraph(String topsString) {
		Topology topology = new Topology(topsString);
		CartoonLayout layout = new CartoonLayout();
		this.cartoon = layout.layout(topology);
//		Rectangle2D bb = this.cartoon.getBoundingBox();
//		int w = (int)bb.getWidth() + 50;
//		int h = (int)bb.getHeight() + 50;
//		this.cartoon.centerOn(w/2, h/2);
		this.cartoon.centerOn(150, 150);
		this.repaint();
	}
	
	public void paint(Graphics g) {
		if (this.cartoon != null) {
			this.cartoon.draw(g);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		this.setGraph(this.textField.getText());
	}

}
