package tops.model.classification;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import tops.engine.TopsStringFormatException;
import tops.engine.drg.Comparer;
import tops.engine.drg.Utilities;
import tops.engine.inserts.Matcher;
import tops.engine.inserts.Pattern;

public class RepSet {

    private List<Rep> reps;

    private BitSet repBitSet;

    public RepSet() {
        this.reps = new ArrayList<Rep>();
        this.repBitSet = new BitSet();
    }

    public int size() {
        return this.reps.size();
    }

    public int cardinality() {
        return this.reps.size() - this.repBitSet.cardinality();
    }

    public void addRep(Rep rep) {
        this.reps.add(rep);
    }

    public Rep getRep(int i) {
        return (Rep) this.reps.get(i);
    }

    public int indexOfID(String pdbID) {
        for (int i = 0; i < this.reps.size(); i++) {
            Rep rep = this.getRep(i);
            if (rep.getPDBID().equals(pdbID)) {
                return i;
            }
        }
        return -1;
    }

    public RepSet removeAtIndex(int index) {
        this.repBitSet.set(index, true);
        RepSet singleton = new RepSet();
        singleton.addRep(this.getRep(index));
        return singleton;
    }

    public RepSet removeSlice(int start, int end) {
        RepSet subset = new RepSet();
        for (int i = start; i <= end; i++) {
            subset.addRep(this.getRep(i));
            this.repBitSet.set(i, true);
        }
        return subset;
    }

    public RepSet randomSubset(int size) {
        int i = 0;
        RepSet subset = new RepSet();
        size = Math.min(this.size(), size);
        while (i < size) {
            int index = this.randomIndex();
            if (this.repBitSet.get(index)) {
                continue;
            } else {
                subset.addRep(this.getRep(index));
                this.repBitSet.set(index, true);
                i++;
            }
        }
        return subset;
    }

    public int randomIndex() {
        return (int) (Math.random() * this.size());
    }

    public void replaceSubset(RepSet subSet) {
        for (int i = 0; i < subSet.size(); i++) {
            Rep rep = subSet.getRep(i);
            int index = this.indexOfID(rep.getPDBID());
            this.repBitSet.set(index, false);
        }
    }

    public void resetBitSet() {
        this.repBitSet.clear();
    }

    public String[] getInstances() {
        String[] instances = new String[this.cardinality()];
        int j = 0;
        for (int i = 0; i < this.size(); i++) {
            if (!this.repBitSet.get(i)) {
                instances[j] = ((Rep) this.reps.get(i)).getTopsString();
                j++;
            }
        }
        return instances;
    }

    public boolean matches(String s) {
        return this.matches(new Pattern(s));
    }

    public boolean matches(Pattern p) {
        Matcher m = new Matcher(this.getInstances());
        return m.runsSuccessfully(p);
    }

    public int numberMatching(String s) {
        return this.numberMatching(new Pattern(s));
    }

    public int numberMatching(Pattern p) {
        Matcher m = new Matcher(this.getInstances());
        return m.numberMatching(p);
    }

    public tops.engine.inserts.Pattern generatePatternWithInserts() {
        try {
            Comparer ex = new Comparer();
            // String pattern =
            // ex.findPatternStringWithInserts(this.getInstances());
            tops.engine.drg.Pattern simplePattern = ex.findPattern(this
                    .getInstances());
            float compression = 1 - Utilities.doCompression(this.getInstances(),
                    simplePattern);
            String insertPatternString = ex.matchAndGetInserts(simplePattern);
            tops.engine.inserts.Pattern insertPattern = new tops.engine.inserts.Pattern(
                    insertPatternString);
            insertPattern.setCompression(compression);
            return insertPattern;
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
            return new tops.engine.inserts.Pattern();
        }
    }

    public tops.engine.drg.Pattern generatePattern() {
        try {
            Comparer ex = new Comparer();
            tops.engine.drg.Pattern pattern = ex.findPattern(this
                    .getInstances());
            // System.out.println("DEBUG : pattern created = " + pattern);
            return pattern;
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe);
            return new tops.engine.drg.Pattern();
        }
    }

    public void shuffle() {
        Collections.shuffle(this.reps);
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < this.size(); i++) {
            if (!this.repBitSet.get(i)) {
                stringBuffer.append(this.getRep(i).toString());
            }
        }
        return stringBuffer.toString();
    }
}
