package tops.translation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Domain {

    private int number;

    private List<DomainSegment> segments;

    public Domain(int number) {
        this.number = number;
        this.segments = new ArrayList<DomainSegment>();
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
        for (int i = 0; i < this.segments.size(); i++) {
            DomainSegment domainSegment = (DomainSegment) this.segments.get(i);
            if (domainSegment.contains(backboneSegment)) {
                return true;
            }
        }
        return false;
    }

    public List<BackboneSegment> filter(List<BackboneSegment> backboneSegments) {
        List<BackboneSegment> segmentsInDomain = new ArrayList<BackboneSegment>();
        Iterator<DomainSegment> itr = this.segments.iterator();
        while (itr.hasNext()) {
            DomainSegment domainSegment = (DomainSegment) itr.next();
            segmentsInDomain.addAll(domainSegment.filter(backboneSegments));
        }
        return segmentsInDomain;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.number);

        Iterator<DomainSegment> itr = this.segments.iterator();
        while (itr.hasNext()) {
            stringBuffer.append(' ').append(
                    ((DomainSegment) itr.next()).toString());
        }
        return stringBuffer.toString();
    }
}
