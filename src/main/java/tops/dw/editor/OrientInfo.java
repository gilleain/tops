package tops.dw.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tops.dw.protein.Cartoon;
import tops.dw.protein.SecStrucElement;
import tops.web.display.applet.TopsDrawCanvas;

public class OrientInfo {

    private List<String> domNames = new ArrayList<>();

    private int[][] equivSSEs;

    private int nEquivs;
    
    private static final int IDENTITY = 0;

    private static final int ROT_X_180 = 1;

    private static final int ROT_Y_180 = 2;

    private static final int ROT_Z_180 = 3;

    public OrientInfo(File f) throws IOException, EquivFileFormatException {

        if (f == null) {
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        this.readSSEEquivFile(br);
    }
    
    public boolean hasMapping() {
    	return this.domNames.isEmpty();
    }
    
    public List<String> getNames() {
    	return this.domNames;
    }
    
    public int numberOfMappings() {
    	return this.nEquivs;
    }
    
    public int getMapping(int i, int j) {
    	return this.equivSSEs[i][j];
    }
    
    public int[] getMapping(int i) {
    	return this.equivSSEs[i];
    }

    /**
	 * Provides a consensus transformation to get an equivalent 
	 * orientation for two domains.
	 * 
	 * Allowed transforms are 180 deg rotations about x,y or z.
	 * A transformation is implied by each pair of SSEs.
	 * Consensus is the transformation implied by the most pairs. 
	 * (it is possible that two pairs may not imply the
	 *  same transformation).
	 * @param refroot
	 * @param root
	 * @param equivalentRef
	 * @param equiv
	 * @param tdc
	 */
	public void orientConsensus(Cartoon refroot, Cartoon root, int[] equivalentRef, int[] equiv, TopsDrawCanvas tdc) {
	
	    SecStrucElement[] ref = new SecStrucElement[2];
	    SecStrucElement[] orient = new SecStrucElement[2];
	    int nequivs = equivalentRef.length;
	    int[] transCounts = new int[4];
	    int i;
	    int j;
	    int relx;
	    int rely;
	    int relz;
	
	    for (i = 0; i < 4; i++)
	        transCounts[i] = 0;
	
	    for (i = 0; i < nequivs; i++) {
	        ref[0] = refroot.getSSEByNumber(equivalentRef[i]);
	        orient[0] = root.getSSEByNumber(equiv[i]);
	
	        if (ref[0] == null || orient[0] == null) {
	            throw new IllegalArgumentException(
	                    "SSE number out of range in equivalences file");
	        }
	
	        for (j = i + 1; j < nequivs; j++) {
	            ref[1] = refroot.getSSEByNumber(equivalentRef[j]);
	            orient[1] = root.getSSEByNumber(equiv[j]);
	
	            if (ref[1] == null || orient[1] == null) {
	                throw new IllegalArgumentException(
	                        "SSE number out of range in equivalences file");
	            }
	
	            // if relative directions are inconsistent then 
	            // this pair is not considered in the consensus
	            String d1 = ref[0].getRelDirection(ref[1]);
	            String d2 = orient[0].getRelDirection(orient[1]);
	            if (!d1.equals(d2))
	                break;
	
	            relx = this.relXOrient(ref, orient);
	            rely = this.relYOrient(ref, orient);
	            relz = this.relZOrient(ref, orient);
	
	            if (relx == 1) {
	                if (relz == 1)
	                    transCounts[OrientInfo.IDENTITY]++;
	                else
	                    transCounts[OrientInfo.ROT_X_180]++;
	            } else if (relx == -1) {
	                if (relz == 1)
	                    transCounts[OrientInfo.ROT_Z_180]++;
	                else
	                    transCounts[OrientInfo.ROT_Y_180]++;
	            } else if (rely == 1) {
	                if (relz == 1)
	                    transCounts[OrientInfo.IDENTITY]++;
	                else
	                    transCounts[OrientInfo.ROT_Y_180]++;
	            } else if (rely == -1) {
	                if (relz == 1)
	                    transCounts[OrientInfo.ROT_Z_180]++;
	                else
	                    transCounts[OrientInfo.ROT_X_180]++;
	            }
	
	        }
	    }
	
	    // evaluate and do consensus transformation
	    int cons = OrientInfo.IDENTITY;
	    for (i = 0; i < 4; i++) {
	        if (transCounts[i] > transCounts[cons]) {
	            cons = i;
	        }
	    }
	
	    if (tdc != null) {
	        if (cons == OrientInfo.ROT_X_180) {
	            tdc.RotateX();
	        } else if (cons == OrientInfo.ROT_Y_180) {
	            tdc.RotateY();
	        } else if (cons == OrientInfo.ROT_Z_180) {
	        	tdc.RotateZ();
	        }
	    } else {
	        System.out.println("No draw canvas?? Bug??");
	    }
	
	}

	private void readSSEEquivFile(BufferedReader br) throws IOException, EquivFileFormatException {
    }
 

    private int relXOrient(SecStrucElement[] ref, SecStrucElement[] orient) {
        int refPos1 = ref[0].getPosition().x;
        int refPos2 = ref[1].getPosition().x;
        int orientPos1 = orient[0].getPosition().x;
        int orientPos2 = orient[1].getPosition().x;

        if ((refPos2 > refPos1) && (orientPos2 > orientPos1))
            return 1;
        else if ((refPos2 < refPos1) && (orientPos2 < orientPos1))
            return 1;
        else if ((refPos2 > refPos1) && (orientPos2 < orientPos1))
            return -1;
        else if ((refPos2 < refPos1) && (orientPos2 > orientPos1))
            return -1;
        else
            return 0;
    }

    private int relYOrient(SecStrucElement[] ref, SecStrucElement[] orient) {
        int refPos1 = ref[0].getPosition().y;
        int refPos2 = ref[1].getPosition().y;
        int orientPos1 = orient[0].getPosition().y;
        int orientPos2 = orient[1].getPosition().y;

        if ((refPos2 > refPos1) && (orientPos2 > orientPos1))
            return 1;
        else if ((refPos2 < refPos1) && (orientPos2 < orientPos1))
            return 1;
        else if ((refPos2 > refPos1) && (orientPos2 < orientPos1))
            return -1;
        else if ((refPos2 < refPos1) && (orientPos2 > orientPos1))
            return -1;
        else
            return 0;
    }

    // assumes that a consistent relative orientation can be evaluated
    // must be guaranteed whenever its called
    private int relZOrient(SecStrucElement[] ref, SecStrucElement[] orient) {

        if (ref[0].getDirection().equals(orient[0].getDirection()))
            return 1;
        else
            return -1;

    }

}
