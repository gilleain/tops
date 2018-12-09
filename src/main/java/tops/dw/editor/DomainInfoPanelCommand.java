package tops.dw.editor;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import tops.dw.protein.Cartoon;
import tops.web.display.applet.TopsDrawCanvas;

/**
 * a class which handles editor commands issued by the DomainInfoPanel
 */
class DomainInfoPanelCommand implements ItemListener {

    static final int SET_VISIBLE_COMMAND = 0;

    static final int CHANGE_EDIT_MODE_COMMAND = 1;

    private TopsEditor topsEditor;

    private Cartoon diagramSS;

    private int commandID;

    public DomainInfoPanelCommand(TopsEditor te, Cartoon s, int id) {
        this.topsEditor = te;
        this.diagramSS = s;
        this.commandID = id;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        TopsDisplayScroll tds = this.topsEditor.getDisplayScroll();

        switch (this.commandID) {
            case CHANGE_EDIT_MODE_COMMAND:
                if (tds != null) {
                    int em = 0;
                    String cs = e.getItem().toString();
                    if (cs.equals("Display information"))
                        em = TopsDrawCanvas.INFO_MODE;
                    else if (cs.equals("Colour symbols"))
                        em = TopsDrawCanvas.COLOUR_SYMBOLS_MODE;
                    else if (cs.equals("Move symbols"))
                        em = TopsDrawCanvas.MOVE_SYMBOLS_MODE;
                    else if (cs.equals("Move fixed structures"))
                        em = TopsDrawCanvas.MOVE_FIXEDS_MODE;
                    else if (cs.equals("Redraw connections"))
                        em = TopsDrawCanvas.REDRAW_CONNECTIONS_MODE;
                    else if (cs.equals("Delete symbols"))
                        em = TopsDrawCanvas.DELETE_SYMBOLS_MODE;
                    else if (cs.equals("Rotate 180 about x"))
                        em = TopsDrawCanvas.ROTATE_X_MODE;
                    else if (cs.equals("Rotate 180 about y"))
                        em = TopsDrawCanvas.ROTATE_Y_MODE;
                    else if (cs.equals("Rotate 180 about z"))
                        em = TopsDrawCanvas.ROTATE_Z_MODE;
                    else if (cs.equals("Reflect in xy plane"))
                        em = TopsDrawCanvas.REFLECT_XY_MODE;
                    else if (cs.equals("Add label"))
                        em = TopsDrawCanvas.ADD_USER_LABEL;
                    else if (cs.equals("Delete label"))
                        em = TopsDrawCanvas.DELETE_USER_LABEL;
                    else if (cs.equals("Move label"))
                        em = TopsDrawCanvas.MOVE_USER_LABEL;
                    else if (cs.equals("Add arrow"))
                        em = TopsDrawCanvas.ADD_USER_ARROW;
                    else if (cs.equals("Delete arrow"))
                        em = TopsDrawCanvas.DELETE_USER_ARROW;
                    else if (cs.equals("Align X direction"))
                        em = TopsDrawCanvas.ALIGN_X_MODE;
                    else if (cs.equals("Align Y direction"))
                        em = TopsDrawCanvas.ALIGN_Y_MODE;

                    tds.setEditMode(this.diagramSS, em);
                }
                break;
        }

    }
}
