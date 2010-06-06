package tops.dw.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import tops.dw.protein.SecStrucElement;
import tops.dw.protein.TopsLinkedListException;

public class OrientInfo {

    private Vector dom_names = new Vector();

    private int EquivSSEs[][];

    private int NEquivs;
    
    private static int IDENTITY = 0;

    private static int ROT_X_180 = 1;

    private static int ROT_Y_180 = 2;

    private static int ROT_Z_180 = 3;

    public OrientInfo(File f) throws IOException, EquivFileFormatException {

        if (f == null) {
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(f));
        this.readSSEEquivFile(br);
    }
    
    public boolean hasMapping() {
    	return this.dom_names.isEmpty();
    }
    
    public Enumeration getNames() {
    	return this.dom_names.elements();
    }
    
    public int numberOfMappings() {
    	return this.NEquivs;
    }
    
    public int getMapping(int i, int j) {
    	return this.EquivSSEs[i][j];
    }
    
    public int[] getMapping(int i) {
    	return this.EquivSSEs[i];
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
	 * @param equiv_ref
	 * @param equiv
	 * @param tdc
	 * @throws TopsLinkedListException
	 */
	public void orient_consensus(SecStrucElement refroot,
	        SecStrucElement root, int equiv_ref[], int equiv[], TopsDrawCanvas tdc)
	        throws TopsLinkedListException {
	
	    SecStrucElement ref[] = new SecStrucElement[2];
	    SecStrucElement orient[] = new SecStrucElement[2];
	    int nequivs = equiv_ref.length;
	    int trans_counts[] = new int[4];
	    int i, j, relx, rely, relz;
	
	    for (i = 0; i < 4; i++)
	        trans_counts[i] = 0;
	
	    for (i = 0; i < nequivs; i++) {
	        ref[0] = refroot.GetSSEByNumber(equiv_ref[i]);
	        orient[0] = root.GetSSEByNumber(equiv[i]);
	
	        if (ref[0] == null || orient[0] == null) {
	            System.out.println("SSE number out of range in equivalences file");
	            throw new TopsLinkedListException();
	        }
	
	        for (j = i + 1; j < nequivs; j++) {
	            ref[1] = refroot.GetSSEByNumber(equiv_ref[j]);
	            orient[1] = root.GetSSEByNumber(equiv[j]);
	
	            if (ref[1] == null || orient[1] == null) {
	                System.out.println("SSE number out of range in equivalences file");
	                throw new TopsLinkedListException();
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
	                    trans_counts[OrientInfo.IDENTITY]++;
	                else
	                    trans_counts[OrientInfo.ROT_X_180]++;
	            } else if (relx == -1) {
	                if (relz == 1)
	                    trans_counts[OrientInfo.ROT_Z_180]++;
	                else
	                    trans_counts[OrientInfo.ROT_Y_180]++;
	            } else if (rely == 1) {
	                if (relz == 1)
	                    trans_counts[OrientInfo.IDENTITY]++;
	                else
	                    trans_counts[OrientInfo.ROT_Y_180]++;
	            } else if (rely == -1) {
	                if (relz == 1)
	                    trans_counts[OrientInfo.ROT_Z_180]++;
	                else
	                    trans_counts[OrientInfo.ROT_X_180]++;
	            }
	
	        }
	    }
	
	    // evaluate and do consensus transformation
	    int cons = OrientInfo.IDENTITY;
	    for (i = 0; i < 4; i++) {
	        if (trans_counts[i] > trans_counts[cons]) {
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
 

    private int relXOrient(SecStrucElement ref[], SecStrucElement orient[]) {
        int ref_pos1 = ref[0].GetPosition().x;
        int ref_pos2 = ref[1].GetPosition().x;
        int orient_pos1 = orient[0].GetPosition().x;
        int orient_pos2 = orient[1].GetPosition().x;

        if ((ref_pos2 > ref_pos1) && (orient_pos2 > orient_pos1))
            return 1;
        else if ((ref_pos2 < ref_pos1) && (orient_pos2 < orient_pos1))
            return 1;
        else if ((ref_pos2 > ref_pos1) && (orient_pos2 < orient_pos1))
            return -1;
        else if ((ref_pos2 < ref_pos1) && (orient_pos2 > orient_pos1))
            return -1;
        else
            return 0;
    }

    private int relYOrient(SecStrucElement ref[], SecStrucElement orient[]) {
        int ref_pos1 = ref[0].GetPosition().y;
        int ref_pos2 = ref[1].GetPosition().y;
        int orient_pos1 = orient[0].GetPosition().y;
        int orient_pos2 = orient[1].GetPosition().y;

        if ((ref_pos2 > ref_pos1) && (orient_pos2 > orient_pos1))
            return 1;
        else if ((ref_pos2 < ref_pos1) && (orient_pos2 < orient_pos1))
            return 1;
        else if ((ref_pos2 > ref_pos1) && (orient_pos2 < orient_pos1))
            return -1;
        else if ((ref_pos2 < ref_pos1) && (orient_pos2 > orient_pos1))
            return -1;
        else
            return 0;
    }

    // assumes that a consistent relative orientation can be evaluated
    // must be guaranteed whenever its called
    private int relZOrient(SecStrucElement ref[], SecStrucElement orient[]) {

        if (ref[0].Direction.equals(orient[0].Direction))
            return 1;
        else
            return -1;

    }

}
