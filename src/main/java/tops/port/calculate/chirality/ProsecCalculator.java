package tops.port.calculate.chirality;

import tops.port.model.Hand;
import tops.port.model.SSE;

public class ProsecCalculator implements ChiralityInterface {

    public Hand chiral3d(SSE sse, SSE other) {

        int a1s, a1f, a2s, a2f;
        // XXX TODO - what is this merge range stuff?
//        if (sse.getM > 0) {
//            a1s = this.MergeRanges[this.Merges - 1][0];
//            a1f = this.MergeRanges[this.Merges - 1][1];
//        } else {
            a1s = sse.sseData.SeqStartResidue;
            a1f = sse.sseData.SeqFinishResidue;
//        }
//
//        if (other.Merges > 0) {
//            a2s = other.MergeRanges[0][0];
//            a2f = other.MergeRanges[0][1];
//        } else {
            a2s = other.sseData.SeqStartResidue;
            a2f = other.sseData.SeqFinishResidue;
//        }

        return motifChirality(a1s, a1f, a2s, a2f);
    }

    //FIXME!! : link in the slidel code!
    public static Hand motifChirality(int a, int b, int c, int d) { 
        return Hand._unk_hand;
    }
}
