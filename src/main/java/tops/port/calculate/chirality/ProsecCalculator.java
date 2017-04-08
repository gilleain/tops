package tops.port.calculate.chirality;

import tops.port.model.Chain;
import tops.port.model.Hand;
import tops.port.model.SSE;

public class ProsecCalculator implements ChiralityInterface {

    public Hand chiral3d(Chain chain, SSE sse, SSE other) {

        int a1s, a1f, a2s, a2f;
        // XXX TODO - what is this merge range stuff?
//        if (sse.getM > 0) {
//            a1s = this.MergeRanges[this.Merges - 1][0];
//            a1f = this.MergeRanges[this.Merges - 1][1];
//        } else {
            a1s = sse.sseData.seqStartResidue;
            a1f = sse.sseData.seqFinishResidue;
//        }
//
//        if (other.Merges > 0) {
//            a2s = other.MergeRanges[0][0];
//            a2f = other.MergeRanges[0][1];
//        } else {
            a2s = other.sseData.seqStartResidue;
            a2f = other.sseData.seqFinishResidue;
//        }

        return motifChirality(a1s, a1f, a2s, a2f);
    }

    //FIXME!! : link in the slidel code!
    public static Hand motifChirality(int a, int b, int c, int d) { 
        return Hand.UNKNOWN;
    }
}
