package tops.translation.model;

import java.util.ArrayList;
import java.util.List;


public class DomainSegment {
    private int start;
    private int end;

    public DomainSegment(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(BackboneSegment backboneSegment) {
        return backboneSegment.overlapsPDBNumberRange(this.start, this.end);
    }

    public List<BackboneSegment> filter(List<BackboneSegment> backboneSegments) {
        List<BackboneSegment> subList = new ArrayList<>();

        for (BackboneSegment backboneSegment : backboneSegments) {
            if (this.contains(backboneSegment)) {
                subList.add(backboneSegment);
            }
        }
        return subList;
    }

    public String toString() {
        return this.start + ":" + this.end;
    }

}
