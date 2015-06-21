// This class is embodies the
// dialgog that appears when
// The user decides to SUBMIT their
// query.


package tops.drawing.app;


import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/**
 *
 * @author  Martin Bans
 */
public class SubmissionDialog extends JDialog implements ActionListener {
    private TopsEditor parentPanel;

    private JPanel mainPanel;
    private Container con;

    private String TOPS_String_name;
    private String num_of_results_to_return;
    private String num_of_results_per_page;
    private String subclasses_type;
    private String submission_type;

    private JPanel tops_name_panel;
    private JPanel results_num_panel;
    private JPanel results_per_page_panel;
    private JPanel subclasses_panel;
    private JPanel submission_panel;
    private JPanel submit_but_panel;

    private JTextField tops_name;
    private JComboBox<?> results_num_list;
    private JComboBox<?> results_per_page_list;
    private JComboBox<?> subclasses_list;
    private JComboBox<?> submission_list;
    private JButton submit, cancel;

    public boolean finished = false;
    public boolean hasSubmitted = false;

    /** Creates a new instance of SubmissionDialog */
    public SubmissionDialog(TopsEditor parentPanel)  {
        this.parentPanel = parentPanel;
        con = getContentPane();
        mainPanel = new JPanel();
        tops_name_panel = new JPanel();
        results_num_panel = new JPanel();
        results_per_page_panel = new JPanel();
        subclasses_panel = new JPanel();
        submission_panel = new JPanel();
        submit_but_panel = new JPanel();
        
        tops_name = new JTextField("<default>");
        tops_name.setSelectedTextColor(Color.gray);
        submit = new JButton("Submit");
        cancel = new JButton("Cancel");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Submission Data");
        this.setSize(400, 400);
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        tops_name_panel.setLayout(new FlowLayout(3));
        results_num_panel.setLayout(new FlowLayout(3));
        results_per_page_panel.setLayout(new FlowLayout(3));
        subclasses_panel.setLayout(new FlowLayout(3));
        submission_panel.setLayout(new FlowLayout(3));
        
        results_num_panel.setLayout(new FlowLayout(3));
        results_per_page_panel.setLayout(new FlowLayout(3));
        subclasses_panel.setLayout(new FlowLayout(3));
        submission_panel.setLayout(new FlowLayout(3));
        
        this.setModal(true);
        con.add(mainPanel);
        
        mainPanel.add(new JPanel());
        mainPanel.add(tops_name_panel);
        mainPanel.add(results_num_panel);
        mainPanel.add(results_per_page_panel);
        mainPanel.add(subclasses_panel);
        
        mainPanel.add(submission_panel);
        mainPanel.add(submit_but_panel);
        
        this.submit_but_panel.add(submit);
        this.submit_but_panel.add(cancel);
        tops_name.setPreferredSize(new Dimension(150, 20));
        tops_name.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        
        String[] data1 = {10 + "", 100 + "", 1000 + ""};
        String[] data2 = {10 + "", 100 + ""};
        String[] data3 = {"CATH treps", "CATH hreps", "CATH nreps", "CATH all",
                          "SCOP folds", "SCOP superfamilies", "SCOP families", "SCOP all"};
        String[] data4 = {"advanced-compare", "advanced-match", "insert-match"};
        
        results_num_list = new JComboBox<Object>(data1);
        results_per_page_list = new JComboBox<Object>(data2);
        subclasses_list = new JComboBox<Object>(data3);
        submission_list = new JComboBox<Object>(data4);
        
        tops_name_panel.add(new JLabel("    TOPS Name:                                "));
        tops_name_panel.add(tops_name);
        
        results_num_panel.add(new JLabel("      Num of results to Return:       "));
        results_num_panel.add(results_num_list);
        
        results_per_page_panel.add(new JLabel("     Num of results per page:        "));
        results_per_page_panel.add(results_per_page_list);
        
        subclasses_panel.add(new JLabel("       Subclasses:                           "));
        subclasses_panel.add(subclasses_list);
        
        submission_panel.add(new JLabel("       New Submission:                  "));
        submission_panel.add(submission_list);
        submit.addActionListener(this);
        cancel.addActionListener(this);
        setVisible(false);
    }

    public String[] getSubmissionData() {
        String [] subData =
            { TOPS_String_name, num_of_results_to_return, num_of_results_per_page,
              subclasses_type, submission_type };

        return subData;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            TOPS_String_name = tops_name.getText();
            num_of_results_to_return = (String)results_num_list.getSelectedItem();
            num_of_results_per_page = (String)results_per_page_list.getSelectedItem();
            subclasses_type = (String)subclasses_list.getSelectedItem();
            submission_type = (String)submission_list.getSelectedItem();

            // this info is in the form in the
            //http://balabio.dcs.gla.ac.uk/tops/advanced.html page
            if (subclasses_type.equals("CATH treps"))
                subclasses_type = "cath,crep,arep,trep";
            else if (subclasses_type.equals("CATH hreps"))
                subclasses_type = "cath,crep,arep,trep,hrep";
            else if (subclasses_type.equals("CATH nreps"))
                subclasses_type = "cath,crep,arep,trep,hrep,nrep" ;
            else if (subclasses_type.equals("cath all"))
                subclasses_type = "cath,all";
            else if (subclasses_type.equals("CATH treps"))
                subclasses_type = "scop,class,fold";
            else if (subclasses_type.equals("CATH hreps"))
                subclasses_type = "scop,class,fold,superfamily";
            else if (subclasses_type.equals("CATH nreps"))
                subclasses_type = "scop,class,fold,superfamily,family" ;
            else
                subclasses_type = "scop,all";


            //String url = getURL();
            setVisible(false);
//            parentPanel.showURL(url);
        } else
            setVisible(false);
    }

    public String getURL() {
        // get the sse_string info
        String string_info = parentPanel.getTopsString();
        String vertices = "";
        String edges = "";
        StringTokenizer st = new StringTokenizer(string_info, " ");
        int i = 0;
        while (st.hasMoreTokens()) {
            if (i == 0)
                vertices += st.nextToken();
            else
                edges += st.nextToken();
            i++;
        }
        String[] sub_Data = getSubmissionData();
        String url = ServletConnection.makeURL(vertices, edges, sub_Data);
        return url;
    }

}
