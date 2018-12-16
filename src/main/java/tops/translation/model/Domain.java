package tops.translation.model;

import java.util.ArrayList;
import java.util.List;


public class Domain {
	
    private int number;
    
    private List<DomainSegment> segments;

    public Domain(int number) {
        this.number = number;
        this.segments = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public int getNumber() {
        return this.number;
    }

    public String getID() {
        return Integer.toString(this.number);
    }

    public void addSegment(int start, int end) {
        this.segments.add(new DomainSegment(start, end));
    }

    public boolean contains(BackboneSegment backboneSegment) {
        if (this.isEmpty()) {
            return true;
        }

        for (int i = 0; i < this.segments.size(); i++) {
            DomainSegment domainSegment = this.segments.get(i);
            if (domainSegment.contains(backboneSegment)) {
                return true;
            }
        }
        return false;
    }

    public List<BackboneSegment> filter(List<BackboneSegment> backboneSegments) {
        List<BackboneSegment> segmentsInDomain = new ArrayList<>();
        for (DomainSegment domainSegment : this.segments) {
            segmentsInDomain.addAll(domainSegment.filter(backboneSegments));
        }
        return segmentsInDomain;
    }

    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(this.number);

        for (DomainSegment domainSegment : this.segments) {
            stringBuffer.append(' ').append((domainSegment).toString());
        }
        return stringBuffer.toString();
    }
}
