package tops.translation;

import java.util.ArrayList;
import java.util.Iterator;

public class DomainSegment {

    private int start;

    private int end;

    public DomainSegment(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(BackboneSegment backboneSegment) {
        return backboneSegment.containedInPDBNumberRange(this.start, this.end);
    }

    public ArrayList filter(ArrayList backboneSegments) {
        ArrayList subList = new ArrayList();

        Iterator itr = backboneSegments.iterator();
        while (itr.hasNext()) {
            BackboneSegment backboneSegment = (BackboneSegment) itr.next();
            if (this.contains(backboneSegment)) {
                subList.add(backboneSegment);
                // System.err.println("Added " + backboneSegment + " to segment
                // " + this);
            }
        }
        return subList;
    }

    @Override
    public String toString() {
        return this.start + ":" + this.end;
    }

}
