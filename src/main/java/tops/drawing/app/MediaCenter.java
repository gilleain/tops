package tops.drawing.app;

import java.awt.Image;
import java.awt.Toolkit;

import java.net.URL;

import java.util.HashMap;

public class MediaCenter {

    private static MediaCenter reference;
    private static HashMap<String, Image> images;

    private MediaCenter() {
        MediaCenter.images = new HashMap<>();
        for (int i = 0; i < BUTTON_IMAGE_NAMES.length; i++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.BUTTON_IMAGE_NAMES[i] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.BUTTON_IMAGE_NAMES[i], null);
                System.err.println("image not found : " + MediaCenter.BUTTON_IMAGE_NAMES[i] + ".gif");
            } else {
                MediaCenter.images.put(MediaCenter.BUTTON_IMAGE_NAMES[i], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }

        for (int j = 0; j < ROLLOVER_IMAGE_NAMES.length; j++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.ROLLOVER_IMAGE_NAMES[j] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.ROLLOVER_IMAGE_NAMES[j], null);
                System.err.println("image not found : " + MediaCenter.ROLLOVER_IMAGE_NAMES[j] + ".gif");
            } else {
                MediaCenter.images.put(MediaCenter.ROLLOVER_IMAGE_NAMES[j], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }

        for (int k = 0; k < CURSOR_IMAGE_NAMES.length; k++) {
            URL imageURL = this.getClass().getResource("images/" + MediaCenter.CURSOR_IMAGE_NAMES[k] + ".gif");
            if (imageURL == null) {
                MediaCenter.images.put(MediaCenter.CURSOR_IMAGE_NAMES[k], null);
//                System.err.println("image not found : " + this.cursorImageNames[k] + ".gif"); XXX
            } else {
                MediaCenter.images.put(MediaCenter.CURSOR_IMAGE_NAMES[k], Toolkit.getDefaultToolkit().getImage(imageURL));
            }
        }
    }

    public static Image getImage(String imageName) {
        if (reference == null)
            reference = new MediaCenter();
        return MediaCenter.images.get(imageName);
    }

    public static String[] BUTTON_IMAGE_NAMES = {
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

    public static String[] ROLLOVER_IMAGE_NAMES = {
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

    public static String[] CURSOR_IMAGE_NAMES = {
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
