package tops.port.model;

import static java.lang.Character.isAlphabetic;
import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.vecmath.Point3d;

import tops.port.model.BridgePartner.Side;
import tops.port.model.Chain.SSEType;

public class DsspReader {
    
    private double hBondECutoff = -0.5;
    
	private HashMap<String, SSEType> codemap  = new HashMap<String, SSEType>() {{ 
		put("H", SSEType.RIGHT_ALPHA_HELIX); 
		put("G", SSEType.HELIX_310); 
		put("I", SSEType.PI_HELIX); 
		put("E", SSEType.EXTENDED); 
		put("T", SSEType.TURN); 
		put("B", SSEType.ISO_BRIDGE);
	}};

	// XXX TODO : convert this to values in the SSEType enum
	Map<SSEType, Character> ssswitch = 
	        new HashMap<SSEType, Character>() {{ 
	            put(SSEType.HELIX, 'H'); 
	            put(SSEType.EXTENDED, 'E'); 
	        }};

    public Protein readDsspFile(String filename) throws IOException {
        String[] parts = filename.split("/");
        String pdbid = parts[parts.length - 1].substring(0, 4);
        return readDsspFile(pdbid, new FileReader(new File(filename)));
    }

    public Protein readDsspFile(String pdbid, Reader reader) throws IOException {
    		
    		List<String> lines = getLines(reader);
    		// skip header
    		String[] headerbits = lines.get(6).split("\\s+");
//    		System.out.println(lines.get(6));
//    		System.out.println(Arrays.toString(headerbits));
    		// crude way to deal with leading spaces...
    		int x = 0;
    		for (; x < headerbits.length; x++) { if (!headerbits[x].equals("")) break; }
    		int numberResidues = Integer.parseInt(headerbits[x]);
    		int numberChains = Integer.parseInt(headerbits[x+1]);
    
    		// increase number of residues to include chain termination records in file 
    		numberResidues += numberChains - 1;
    
    		// skip over info up to residue by residue stuff
    		int startOfResidueData = 28;
    
    		// main loop reading residue by residue info 
    		if (lines.get(startOfResidueData).length() == 129){
    		    System.err.println("OLD FORMAT!\n");      // TODO : throw exception?
    		}
    
    		List<Record> records = new ArrayList<Record>();
    
    		Pattern hbond_pattern = Pattern.compile("(-?\\d+|-?\\d+\\.\\d+)(?:\\s+|,\\s?|$)");
    
//    		int index = 0;
    		for (String line : lines.subList(startOfResidueData, lines.size())) {
    			Map<String, String> bits = this.parseLine(line);
    			if (bits.values().size() == 0) continue;
    			Record record = new Record();
    
    			// residue name //
    			record.ResidueNames = bits.get("residue_type");
    
    			// PDB number - only for records which are not chain terminators //
    			if (!record.ResidueNames.equals("!") ) record.PDBIndices = Integer.parseInt(bits.get("pdb_number"));
    			else record.PDBIndices = -99999;
    
    			// Secondary structure //
    			record.SecStructure = bits.get("sse_type");
    
    			// chain id //
    			record.ChainId = bits.get("chain_name");
    
    			// bridge partners - again not ness separated by white space so use tempbuff //
    			String bridgePartString = bits.get("bridge_partners");
    			String[] bps = bridgePartString.split("\\s+");    // TODO 
//    			System.out.println(bridgePartString + Arrays.toString(bps));
    			record.LeftBridgePartner = parseResidueNumber(bps[0]);
    			record.RightBridgePartner = parseResidueNumber(bps[1]);
    
    			// Hydrogen bonds (just skip read errors as some dssp files have **** in place of integer here) //
    			// d1, d1e, a1, a1e, d2, d2e, a2, a2e
    			List<String> matches = new ArrayList<String>();
    			System.out.print("Matches for " + record.PDBIndices + " ");
    			int start = 0;
    			String hbondString = bits.get("hbonds");
    			for (int p = 0; p < 4; p++) {
    			    String match = hbondString.substring(start, start+ 11);
    			    System.out.print(String.format("[%s]", match));
    			    String[] pairs = match.split(",");
    			    matches.add(pairs[0].trim());
    			    matches.add(pairs[1].trim());
    			    start += 11;
    			}
    
    			record.DonatedHBond1 = Integer.parseInt(matches.get(0));
    			record.DonatedHBondEn1 = Float.parseFloat(matches.get(1));
    
    			record.AcceptedHBond1 = Integer.parseInt(matches.get(2));
    			record.AcceptedHBondEn1 = Float.parseFloat(matches.get(3));
    
    			record.DonatedHBond2 = Integer.parseInt(matches.get(4));
    			record.DonatedHBondEn2 = Float.parseFloat(matches.get(5));
    
    			record.AcceptedHBond2 = Integer.parseInt(matches.get(6));
    			record.AcceptedHBondEn2 = Float.parseFloat(matches.get(7));
    			
    			System.out.print(String.format("%s %2.2f %s %2.2f %s %2.2f %s %2.2f",
    			        record.DonatedHBond1, record.DonatedHBondEn1,
    			        record.AcceptedHBond1, record.AcceptedHBondEn1,
    			        record.DonatedHBond2, record.DonatedHBondEn2,
    			        record.AcceptedHBond2, record.AcceptedHBondEn2));
    			
    			System.out.println();
    
    			record.x = Double.parseDouble(bits.get("xca"));
                record.y = Double.parseDouble(bits.get("yca"));
                record.z = Double.parseDouble(bits.get("zca"));
                
                records.add(record);
    		}
    
    		//now convert this data into a Protein//
    
    		int nDsspRes = numberResidues;
    		int nChains = numberChains;
    		int nres = nDsspRes - nChains + 1;
    
//    		int[] indexMapping = new int[nDsspRes];
    		Map<String, List<Integer>> indexMapping = new HashMap<String, List<Integer>>();
    		for (int j = 0; j < nDsspRes && j < records.size(); j++) {
    		    Record record = records.get(j);
    		    List<Integer> mappings;
    		    String chainName = record.ChainId;
    			if (record.ResidueNames.equals("!") || !indexMapping.containsKey(chainName)){
    			    mappings = new ArrayList<Integer>();
    			    indexMapping.put(chainName, mappings);
    			} else {
    			    mappings = indexMapping.get(chainName);
//    				indexMapping[j] = j - 1 - chainCount;
    			}
    			mappings.add(record.PDBIndices);
    		}
    		
    		Protein protein = new Protein(pdbid);
    
    		// Copy sequence and make sses//
    		int currentResidue = 0;
    		int numberOfStructures = 0;
    		boolean newChain = true;
    		boolean firstChain = true;
    		boolean open = false;
    		Chain chain = null;
    		SSE p = null;
    		SSE q = null;
    		int LastResidue = -1;
    		SSEType secondaryStructure = SSEType.COIL;
    		for (int index = 0; index < nDsspRes; index++) {
    		    Record r = records.get(index);
    			if (r.ResidueNames.equals("!") && index < nDsspRes 
    			        && records.get(index + 1).PDBIndices < records.get(index - 1).PDBIndices) {
    				newChain = true;
    				currentResidue = 0;
    			} else {
    				if (newChain) {
    					if (!firstChain) {
    						// finish off the list //
    						if (open) {
    							p.sseData.SeqFinishResidue = currentResidue - 1;
    							p.sseData.PDBFinishResidue = LastResidue;
    							q = p;
    						}
    						SSE cTerm = new SSE('C');
    
    						q.To = q.Next = cTerm;
    						cTerm.sseData.SeqStartResidue = cTerm.sseData.SeqFinishResidue = currentResidue;
    						cTerm.sseData.PDBStartResidue = cTerm.sseData.PDBFinishResidue = LastResidue + 1;
    						cTerm.From = q;
    						numberOfStructures += 1;
    						cTerm.setSymbolNumber(numberOfStructures);
    						chain.addSSE(cTerm);
    					}
    
    					chain = new Chain(r.ChainId.charAt(0));
    					
    					SSE nTerminus = new SSE('N');
    					numberOfStructures = 0;
    					nTerminus.setSymbolNumber(numberOfStructures);
    					chain.addSSE(nTerminus);
    					protein.addChain(chain);
    
    					secondaryStructure = SSEType.COIL;
    					open = false;
    					p = null;
    					q = nTerminus;
    					newChain = false;
    					firstChain = false;
    				}
    
    				chain.addSequence(r.ResidueNames);
    				chain.addPDBIndex(r.PDBIndices);
    				chain.addSecondaryStructure(this.getDsspSSCode(r.SecStructure));
    				
    				List<Integer> mappings = indexMapping.get(r.ChainId);
    				if (r.LeftBridgePartner == 0) {
    				    chain.addLeftBridgePartner(new BridgePartner());
    				} else {
    				    int mappedIndex = mappings.get(r.LeftBridgePartner); 
    				    chain.addLeftBridgePartner(new BridgePartner(null, mappedIndex, BridgeType.UNK_BRIDGE_TYPE, Side.UNKNOWN));
    				}
    				if (r.RightBridgePartner == 0) {
    				    chain.addLeftBridgePartner(new BridgePartner());
    				} else {
    				    int mappedIndex = mappings.get(r.RightBridgePartner); 
    				    chain.addLeftBridgePartner(new BridgePartner(null, mappedIndex, BridgeType.UNK_BRIDGE_TYPE, Side.UNKNOWN));
    				}
    
    				this.doDonatedHBond(chain, index, r.DonatedHBond1, r.DonatedHBondEn1, nDsspRes, mappings);
    				this.doAcceptedHBond(chain, index, r.AcceptedHBond1, r.AcceptedHBondEn1, nDsspRes, mappings);
    				this.doDonatedHBond(chain, index, r.DonatedHBond2, r.DonatedHBondEn2, nDsspRes, mappings);
    				this.doAcceptedHBond(chain, index, r.AcceptedHBond2, r.AcceptedHBondEn2, nDsspRes, mappings);
    
    				chain.addCACoord(new Point3d(r.x, r.y, r.z));
    
    				// Get chain id and pdb index //
    				char chainID = chain.getName();
    				int Id = r.PDBIndices;
    				LastResidue = Id;
    			
    				// Eradicate structures that are not needed and limit type ie. H, E, C //
    				SSEType PreviousStructure = secondaryStructure;
    				secondaryStructure = chain.getSSEType(currentResidue);
    				
    				// Now form the appropriate structures //
    				if (secondaryStructure != PreviousStructure) {
    					if (open) {
    						// End Structure //
    						open = false;
    						p.sseData.SeqFinishResidue = currentResidue - 1;
    						p.sseData.PDBFinishResidue = LastResidue;
    						q = p;
    					}
    					if (secondaryStructure != SSEType.COIL) {
    						// New Structure //
    						open = true;
    						p = new SSE(this.getSSType(secondaryStructure));
    						chain.addSSE(p);
    						q.To = q.Next = p;
    						p.From = q;
    						numberOfStructures += 1;
    						p.setSymbolNumber(numberOfStructures);
    						p.sseData.SeqStartResidue = currentResidue;
    						p.sseData.PDBStartResidue = Id;
    						
    						p.Chain = chainID;
    					}
    				}
    				
    				currentResidue += 1;
    			}
    		}
    
    		// finish off the list //
    		if (open) {
    			p.sseData.SeqFinishResidue = currentResidue - 1;
    			p.sseData.PDBFinishResidue = LastResidue;
    			q = p;
    		}
    		SSE cTerm = new SSE('C');
    
    		q.To = q.Next = cTerm;
    		cTerm.sseData.SeqStartResidue = cTerm.sseData.SeqFinishResidue = currentResidue;
    		cTerm.sseData.PDBStartResidue = cTerm.sseData.PDBFinishResidue = LastResidue + 1;
    		cTerm.From = q;
    		numberOfStructures += 1;
    		cTerm.setSymbolNumber(numberOfStructures);
    		chain.addSSE(cTerm);
    
    		return protein;
    }
    
    private int parseResidueNumber(String residueNumberString) {
        int lastIndex = residueNumberString.length() - 1;
        if (isAlphabetic(residueNumberString.charAt(lastIndex))) {
            // XXX what to do with the letter?
            return parseInt(residueNumberString.substring(0, lastIndex));
        } else {
            return parseInt(residueNumberString);
        }
    }

    private SSEType getDsspSSCode(String ss) {
		if(this.codemap.containsKey(ss)) {
			return this.codemap.get(ss);
		} else { 
		    return SSEType.COIL;
		}
	}

    private char getSSType(SSEType ss) {
		if (ssswitch.containsKey(ss)) {
			return this.ssswitch.get(ss);
	    } else {
			return 'C';
	    }
	}

	

	private Map<String, String> parseLine(String line) {
	    
		Map<String, String> bits = new HashMap<String, String>();
		try {
			bits.put("dssp_number",line.substring(0, 5).trim());
			bits.put("pdb_number",line.substring(5, 10).trim());
			bits.put("chain_name",line.substring(11, 12));
			bits.put("residue_type",line.substring(13, 14));
			bits.put("sse_type",line.substring(16, 17));
			bits.put("structure",line.substring(16, 25).trim());
			bits.put("bridge_partners",line.substring(26, 33).trim());
			bits.put("hbonds",line.substring(39, 83));
			bits.put("tco",line.substring(83, 91));
			bits.put("kappa",line.substring(92, 97));
			bits.put("alpha",line.substring(97, 103));
			bits.put("phi",line.substring(104, 109));
			bits.put("psi",line.substring(109, 115));
			bits.put("xca",line.substring(115, 122).trim());
			bits.put("yca",line.substring(122, 129).trim());
			bits.put("zca",line.substring(129).trim());
		} catch (Exception e) {   // TODO
//			System.out.println();
//		    e.printStackTrace();
		}
		return bits;
	}
	
	private class Record {
        String ResidueNames;
        int PDBIndices;
        String SecStructure;
        String ChainId;
        int LeftBridgePartner;
        int RightBridgePartner;
        int DonatedHBond1;
        int AcceptedHBond1;
        int DonatedHBond2;
        int AcceptedHBond2;
        double DonatedHBondEn1;
        double AcceptedHBondEn1;
        double DonatedHBondEn2;
        double AcceptedHBondEn2;
        double x;
        double y;
        double z;
	    
	}
	
	private List<String> getLines(Reader input) throws IOException {
	    BufferedReader dsspfile = new BufferedReader(input);
	    List<String> lines = new ArrayList<String>();
        String line;
        do {
            line = dsspfile.readLine();
            lines.add(line);
        } while (line != null);
        dsspfile.close();
        return lines;
	}
	
	private void doDonatedHBond(Chain chain, int index, int offset, double energy, int nDsspRes, List<Integer> indexMapping) {
		int mappedPos = indexMapping.get(index);
		int partner = mappedPos + offset;
//		System.out.println("Donor index " + mappedPos + " offset " + offset + " = " + partner + String.format(" NRG %2.2f", energy));
		if (check(partner, nDsspRes, offset, energy)) {
			chain.addDonatedBond(mappedPos, partner, energy);
		}
	}

	private void doAcceptedHBond(Chain chain, int index, int offset, double energy, int nDsspRes, List<Integer> indexMapping) {
	    int mappedPos = indexMapping.get(index);
	    int partner = mappedPos + offset;
//	    System.out.println("Acceptor " + mappedPos + " offset " + offset + " = " + partner + String.format(" NRG %2.2f", energy));
		if (check(partner, nDsspRes, offset, energy)) {
			chain.addAcceptedBond(partner, mappedPos, energy);
		}
    }
	
	private boolean check(int index, int nDsspRes, int bond, double energy) {
	    return index > 0 && index <= nDsspRes 
	            // XXX - why is bond checked here as a float greater than 2.5?? isn't it an index??!?
//	            && Math.abs((float) bond) > 2.5 
	            && energy < hBondECutoff
	            ;
	}

}
