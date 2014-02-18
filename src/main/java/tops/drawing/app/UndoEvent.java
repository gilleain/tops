/*
* A.java
*
* Created on 08 December 2004, 20:41
*/

package tops.drawing.app;

import tops.drawing.Cartoon;

public class UndoEvent {
    private Cartoon cartoon;

    /** Creates a new instance of A */
    public UndoEvent(Cartoon cartoon) {
        this.cartoon = (Cartoon) cartoon.clone();
    }
    
    public Cartoon getCartoon() {
        return this.cartoon;
    }
    
    public String toString() {
        return "Undo Event : " + this.cartoon;
    }

}
