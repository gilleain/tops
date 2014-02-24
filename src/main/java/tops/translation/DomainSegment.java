package tops.translation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public List<BackboneSegment> filter(List<BackboneSegment> backboneSegments) {
        List<BackboneSegment> subList = new ArrayList<BackboneSegment>();

        Iterator<BackboneSegment> itr = backboneSegments.iterator();
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
