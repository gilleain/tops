package tops.cli.drawing;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.cli.ParseException;

import tops.cli.Command;
import tops.drawing.app.LayoutTest;

public class LayoutCommand implements Command {

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handle(String[] args) throws ParseException {
//      String topsString = "test NEeEeEeEeC 1:2A2:3A3:4A5:6A6:7A7:8A";
//      String topsString = "test NEeEeEeEeC 1:8A2:7A3:6A3:8A4:5A4:7A";
//      String topsString = "test NEhEhEhEC 1:3P3:5P5:7P";
        
        JPanel panel = new JPanel(new BorderLayout());
        JButton button = new JButton("Go");
        JTextField textField = new JTextField();
        LayoutTest t = new LayoutTest(textField);
        button.addActionListener(t);
        panel.add(button, BorderLayout.NORTH);
        panel.add(textField, BorderLayout.SOUTH);
        panel.add(t, BorderLayout.CENTER);
        
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.setLocation(750, 300);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

}
