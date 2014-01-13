package tops.view.tops3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class testComponent {

    private static ArrayList<String> sheetReOrder(String hbonds) {
        ArrayList<String> newOrder = new ArrayList<String>();
        HashMap<String, ArrayList<String>> edgeMap = new HashMap<String, ArrayList<String>>();
        StringTokenizer tokenizer = new StringTokenizer(hbonds, "."); // split
                                                                        // on
                                                                        // dots
        // first, map the edge view to a vertex view
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();
            int colon = t.indexOf(':');
            String left = t.substring(0, colon);
            String right = t.substring(colon + 1, t.length() - 1);
            testComponent.mapVertex(left, right, edgeMap);
            testComponent.mapVertex(right, left, edgeMap);
        }
        // now, go through the vertices to find the endpoints (edgestrands)
        Set<String> keys = edgeMap.keySet();
        Iterator<String> i = keys.iterator();
        ArrayList<String> edgeStrands = new ArrayList<String>();
        while (i.hasNext()) {
            String vertex = (String) i.next();
            ArrayList<String> connections = edgeMap.get(vertex);
            if (connections.size() < 2)
                edgeStrands.add(vertex);
        }

        // gah! now find the smallest edgestrand
        int currentSmallest = keys.size(); // oddly, set the left end to be the
                                            // highest possible value!
        for (int e = 0; e < edgeStrands.size(); e++) {
            String v = (String) edgeStrands.get(e);
            int vnum = Integer.parseInt(v);
            if (vnum < currentSmallest)
                currentSmallest = vnum;
        }
        System.out.println("leftEnd : " + currentSmallest);
        // now follow the path of the sheet
        int k = 0;
        String currentVertex = String.valueOf(currentSmallest);
        String lastVertex = currentVertex;
        String nextVertex = new String();
        while (k < keys.size()) {
            ArrayList<String> conn = edgeMap.get(currentVertex);
            for (int c = 0; c < conn.size(); c++) {
                String v = (String) conn.get(c);
                if (!v.equals(lastVertex))
                    nextVertex = v;
            }
            newOrder.add(currentVertex);
            lastVertex = currentVertex;
            currentVertex = nextVertex;
            k++;
        }
        return newOrder;
    }

    private static void mapVertex(String one, String two, HashMap<String, ArrayList<String>> edgeMap) {
        // System.out.println("mapping : " + one + " and " + two);
        ArrayList<String> o = (ArrayList<String>) edgeMap.get(one);
        if (o == null)
            o = new ArrayList<String>();
        o.add(two);
        edgeMap.put(one, o);
        // System.out.println("edgemap now " + edgeMap);
    }

    public static void main(String[] args) {
        String vertices = args[0];
        String edges = args[1];

        ArrayList<String> newOrder = testComponent.sheetReOrder(edges);

        Structure s = new Structure("Sheet");
        for (int i = 0; i < vertices.length(); i++) {
            Element strand = new Element("Strand");
            strand.setOrder(i);
            // this is the position in the sheet
            int pos = Integer.parseInt((String) newOrder.get(i));
            s.addSubComponent(pos, strand);
        }
        System.out.println(s);
    }
}
