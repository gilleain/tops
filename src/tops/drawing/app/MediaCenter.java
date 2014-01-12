package tops.drawing.app;

import java.awt.Image;
import java.awt.Toolkit;

import java.net.URL;

import java.util.HashMap;

public class MediaCenter {

    private static MediaCenter reference;
    private static HashMap<String, Image> images;

    private MediaCenter() {
        MediaCenter.images = new HashMap<String, Image>();
        for (int i = 0; i < buttonImageNames.length; i++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.buttonImageNames[i] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.buttonImageNames[i], null);
                System.err.println("image not found : " + MediaCenter.buttonImageNames[i] + ".gif");
            } else {
                MediaCenter.images.put(MediaCenter.buttonImageNames[i], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }

        for (int j = 0; j < rolloverImageNames.length; j++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.rolloverImageNames[j] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.rolloverImageNames[j], null);
                System.err.println("image not found : " + MediaCenter.rolloverImageNames[j] + ".gif");
            } else {
                MediaCenter.images.put(MediaCenter.rolloverImageNames[j], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }

        for (int k = 0; k < cursorImageNames.length; k++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.cursorImageNames[k] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.cursorImageNames[k], null);
//                System.err.println("image not found : " + this.cursorImageNames[k] + ".gif"); XXX
            } else {
                MediaCenter.images.put(MediaCenter.cursorImageNames[k], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }
    }

    public static Image getImage(String imageName) {
        if (reference == null)
            reference = new MediaCenter();
        return (Image) MediaCenter.images.get(imageName);
    }

    public static String[] buttonImageNames = {
        "DefaultButton",
        "UndoButton",
        "ClearSelectedButton",
        "ClearButton",
        "ZoomInButton",
        "ZoomOutButton",
        "SubmitButton",

        "StrandUpButton",
        "StrandDownButton",
        "HelixUpButton",
        "HelixDownButton",
        "TemplateButton",

        "HBondButton",
        "RightArcButton",
        "LeftArcButton",
        "RangesButton",

        "FlipButton",
        "HorizontalButton",
        "VerticalButton",
        "FlipXButton",
        "FlipYButton",

        "ColorsButton",
        "ConstraintsButton",
        "NewButton",
        "OpenButton",
        "SaveButton",
        "SaveAsButton",
        "PrintButton",
        "ExportButton",
        "GoButton",
        "BackButton",
        "HomeButton",
        "ForwardButton",
        "RefreshButton",
        "greek_thumb",
        "rosma_thumb",
        "immun_thumb",
        "plait_thumb",
        "jelly_thumb",
        "timba_thumb",
        "keyba_thumb",
        "ubrol_thumb"
    };

    public static String[] rolloverImageNames = {
        "DefaultRollover",
        "UndoRollover",
        "ClearSelectedRollover",
        "ClearRollover",
        "ZoomInRollover",
        "ZoomOutRollover",
        "SubmitRollover",
        
        "StrandUpRollover",
        "StrandDownRollover",
        "HelixUpRollover",
        "HelixDownRollover",
        "TemplateRollover",
        
        "HBondRollover",
        "RightArcRollover",
        "LeftArcRollover",
        "RangesRollover",

        "FlipRollover",
        "HorizontalRollover",
        "VerticalRollover",
        "FlipXRollover",
        "FlipYRollover",
    };

    public static String[] cursorImageNames = {
        "Dummy",
        "Dummy",
        "Dummy",
        "Dummy",
        "Dummy",
        "Dummy",
        "Dummy",
        "StrandUpCursor",
        "StrandDownCursor",
        "HelixUpCursor",
        "HelixDownCursor",
        "HBondCursor",
        "RightArcCursor",
        "LeftArcCursor",
        "HorizontalAlignCursor",
        "VerticalAlignCursor"
    };

}
