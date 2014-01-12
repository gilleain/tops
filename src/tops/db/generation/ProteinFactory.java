package tops.db.generation;

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import tops.dw.protein.CATHcode;
import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;

public class ProteinFactory extends TopsFactory {

    public ProteinFactory() {
    }

    public Protein getProtein(String source, String chainName) {
        Protein p = new Protein();
        p.Name = chainName;

        // fill the SSE linked list with data
        HashMap<String, SecStrucElement> domainRoots = this.getDomains(source, chainName);
        for (String dom_id : domainRoots.keySet()) {
            System.err.println("adding domain : " + dom_id);
            SecStrucElement rootSSE = (SecStrucElement) domainRoots.get(dom_id);
            rootSSE = this.getRelations(chainName, rootSSE);
            rootSSE = this.getConnectionPoints(chainName, rootSSE);
            CATHcode code = new CATHcode(dom_id);
            DomainDefinition domainDefinition = new DomainDefinition(code);
            p.AddTopsLinkedList(rootSSE, domainDefinition);
        }

        return p;
    }

    private SecStrucElement getRelations(String chainName,
            SecStrucElement rootSSE) {
        return rootSSE;
    }

    private SecStrucElement getConnectionPoints(String chainName,
            SecStrucElement rootSSE) {
        return rootSSE;
    }

    private HashMap<String, SecStrucElement> getDomains(String source, String chainName) {
        String SSE_query = "SELECT SSE.*, SSE_DOM.* from Secondary_Structure_Element as SSE, SSE_DOM";
        SSE_query += " WHERE SSE_DOM.Source = '"
                + source
                + "' AND SSE.Chain_ID = SSE_DOM.Chain_ID AND SSE.SSE_No = SSE_DOM.SSE_No";
        SSE_query += " AND Type != 'U' AND SSE.Chain_ID='" + chainName
                + "' ORDER BY DOM_ID;";

        HashMap<String, SecStrucElement> domainMap = new HashMap<String, SecStrucElement>();

        ResultSet results = this.doQuery(SSE_query);
        SecStrucElement lastSSE = null;
        String lastDOM_ID = null;
        try {
            while (results.next()) {
                int SSE_No = results.getInt("SSE_No");
                String Type = results.getString("Type");

                int SeqStartResidue = results.getInt("SeqStartResidue");
                int SeqLength = results.getInt("SeqLength");
                int PDBStartResidue = results.getInt("PDBStartResidue");
                int PDBFinishResidue = results.getInt("PDBFinishResidue");

                float AxisStartX = results.getFloat("AxisStartX");
                float AxisStartY = results.getFloat("AxisStartY");
                float AxisStartZ = results.getFloat("AxisStartZ");
                float AxisEndX = results.getFloat("AxisEndX");
                float AxisEndY = results.getFloat("AxisEndY");
                float AxisEndZ = results.getFloat("AxisEndZ");
                float AxisLength = results.getFloat("AxisLength");

                String DOM_ID = results.getString("DOM_ID");
                String FixedType = results.getString("FixedType");
                String Direction = results.getString("Direction");

//                int NextFixedSSE = results.getInt("NextFixedSSE");
                int X = results.getInt("X");
                int Y = results.getInt("Y");
                int Red = results.getInt("Red");
                int Green = results.getInt("Green");
                int Blue = results.getInt("Blue");
                int Radius = results.getInt("Radius");

                SecStrucElement currentSSE = new SecStrucElement();
                if (DOM_ID.equals(lastDOM_ID)) {
                    currentSSE.SetFrom(lastSSE);
                    if (lastSSE != null)
                        lastSSE.SetTo(currentSSE);
                }
                lastDOM_ID = DOM_ID;
                lastSSE = currentSSE;

                currentSSE.Type = Type;
                currentSSE.SymbolNumber = SSE_No / 2;
                currentSSE.Direction = Direction;
                currentSSE.Label = null;
                currentSSE.Colour = new Color(Red, Green, Blue);

                // currentSSE.SetFixedIndex(??); //??
                // currentSSE.SetNextIndex(NextFixedSSE); //private??why?
                currentSSE.SetFixedType(FixedType);
                currentSSE.SetSeqStartResidue(SeqStartResidue);
                currentSSE.SetSeqFinishResidue(SeqStartResidue + SeqLength);
                currentSSE.PDBStartResidue = PDBStartResidue;
                currentSSE.PDBFinishResidue = PDBFinishResidue;

                currentSSE
                        .SetAxesStartPoint(AxisStartX, AxisStartY, AxisStartZ);
                currentSSE.SetAxesFinishPoint(AxisEndX, AxisEndY, AxisEndZ);
                currentSSE.SetAxisLength(AxisLength);

                currentSSE.PlaceElementX(X);
                currentSSE.PlaceElementY(Y);
                currentSSE.SetSymbolRadius(Radius);

                if (!domainMap.containsKey(DOM_ID)) { // put in the root SSE
                                                        // only if there isn't
                                                        // already a mapping
                    domainMap.put(DOM_ID, currentSSE);
                }

            }
        } catch (SQLException squeel) {
            System.out.println(squeel);
        }
        return domainMap;
    }

    public static void main(String[] args) {
        String source = args[0];
        String name = args[1];
        ProteinFactory proteinFactory = new ProteinFactory();
        Protein p = proteinFactory.getProtein(source, name);
        p.WriteTopsFile(System.out);
    }

}
