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

    private String topsStringName;
    private String numOfResultsToReturn;
    private String numOfResultsPerPage;
    private String subclassesType;
    private String submissionType;

    private JPanel topsNamePanel;
    private JPanel resultsNumPanel;
    private JPanel resultsPerPagePanel;
    private JPanel subclassesPanel;
    private JPanel submissionPanel;
    private JPanel submitButtonPanel;

    private JTextField topsName;
    private JComboBox<?> resultsNumList;
    private JComboBox<?> resultsPerPageList;
    private JComboBox<?> subclassesList;
    private JComboBox<?> submissionList;
    private JButton submit;
    private JButton cancel;

    /** Creates a new instance of SubmissionDialog */
    public SubmissionDialog(TopsEditor parentPanel)  {
        this.parentPanel = parentPanel;
        con = getContentPane();
        mainPanel = new JPanel();
        topsNamePanel = new JPanel();
        resultsNumPanel = new JPanel();
        resultsPerPagePanel = new JPanel();
        subclassesPanel = new JPanel();
        submissionPanel = new JPanel();
        submitButtonPanel = new JPanel();
        
        topsName = new JTextField("<default>");
        topsName.setSelectedTextColor(Color.gray);
        submit = new JButton("Submit");
        cancel = new JButton("Cancel");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Submission Data");
        this.setSize(400, 400);
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        topsNamePanel.setLayout(new FlowLayout(3));
        resultsNumPanel.setLayout(new FlowLayout(3));
        resultsPerPagePanel.setLayout(new FlowLayout(3));
        subclassesPanel.setLayout(new FlowLayout(3));
        submissionPanel.setLayout(new FlowLayout(3));
        
        resultsNumPanel.setLayout(new FlowLayout(3));
        resultsPerPagePanel.setLayout(new FlowLayout(3));
        subclassesPanel.setLayout(new FlowLayout(3));
        submissionPanel.setLayout(new FlowLayout(3));
        
        this.setModal(true);
        con.add(mainPanel);
        
        mainPanel.add(new JPanel());
        mainPanel.add(topsNamePanel);
        mainPanel.add(resultsNumPanel);
        mainPanel.add(resultsPerPagePanel);
        mainPanel.add(subclassesPanel);
        
        mainPanel.add(submissionPanel);
        mainPanel.add(submitButtonPanel);
        
        this.submitButtonPanel.add(submit);
        this.submitButtonPanel.add(cancel);
        topsName.setPreferredSize(new Dimension(150, 20));
        topsName.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        
        String[] data1 = {10 + "", 100 + "", 1000 + ""};
        String[] data2 = {10 + "", 100 + ""};
        String[] data3 = {"CATH treps", "CATH hreps", "CATH nreps", "CATH all",
                          "SCOP folds", "SCOP superfamilies", "SCOP families", "SCOP all"};
        String[] data4 = {"advanced-compare", "advanced-match", "insert-match"};
        
        resultsNumList = new JComboBox<>(data1);
        resultsPerPageList = new JComboBox<>(data2);
        subclassesList = new JComboBox<>(data3);
        submissionList = new JComboBox<>(data4);
        
        topsNamePanel.add(new JLabel("    TOPS Name:                                "));
        topsNamePanel.add(topsName);
        
        resultsNumPanel.add(new JLabel("      Num of results to Return:       "));
        resultsNumPanel.add(resultsNumList);
        
        resultsPerPagePanel.add(new JLabel("     Num of results per page:        "));
        resultsPerPagePanel.add(resultsPerPageList);
        
        subclassesPanel.add(new JLabel("       Subclasses:                           "));
        subclassesPanel.add(subclassesList);
        
        submissionPanel.add(new JLabel("       New Submission:                  "));
        submissionPanel.add(submissionList);
        submit.addActionListener(this);
        cancel.addActionListener(this);
        setVisible(false);
    }

    public String[] getSubmissionData() {
        String [] subData =
            { topsStringName, numOfResultsToReturn, numOfResultsPerPage,
              subclassesType, submissionType };

        return subData;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == submit) {
            topsStringName = topsName.getText();
            numOfResultsToReturn = (String)resultsNumList.getSelectedItem();
            numOfResultsPerPage = (String)resultsPerPageList.getSelectedItem();
            subclassesType = (String)subclassesList.getSelectedItem();
            submissionType = (String)submissionList.getSelectedItem();

            // this info is in the form in the
            //http://balabio.dcs.gla.ac.uk/tops/advanced.html page
            if (subclassesType.equals("CATH treps"))
                subclassesType = "cath,crep,arep,trep";
            else if (subclassesType.equals("CATH hreps"))
                subclassesType = "cath,crep,arep,trep,hrep";
            else if (subclassesType.equals("CATH nreps"))
                subclassesType = "cath,crep,arep,trep,hrep,nrep" ;
            else if (subclassesType.equals("cath all"))
                subclassesType = "cath,all";
            else if (subclassesType.equals("CATH treps"))
                subclassesType = "scop,class,fold";
            else if (subclassesType.equals("CATH hreps"))
                subclassesType = "scop,class,fold,superfamily";
            else if (subclassesType.equals("CATH nreps"))
                subclassesType = "scop,class,fold,superfamily,family" ;
            else
                subclassesType = "scop,all";


            //String url = getURL();
            setVisible(false);
//            parentPanel.showURL(url);
        } else
            setVisible(false);
    }

    public String getURL() {
        // get the sse_string info
        String stringInfo = parentPanel.getTopsString();
        StringBuilder vertices = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        StringTokenizer st = new StringTokenizer(stringInfo, " ");
        int i = 0;
        while (st.hasMoreTokens()) {
            if (i == 0)
                vertices.append(st.nextToken());
            else
                edges.append(st.nextToken());
            i++;
        }
        String[] subData = getSubmissionData();
        return ServletConnection.makeURL(vertices.toString(), edges.toString(), subData);
    }

}
