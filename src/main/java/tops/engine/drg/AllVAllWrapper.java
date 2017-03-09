package tops.engine.drg;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tops.engine.Result;
import tops.engine.TParser;
import tops.engine.TopsStringFormatException;

public class AllVAllWrapper {

    public Result[] allVsAllWithResults(List<String[]> pairList, Map<String, String> instMap) throws TopsStringFormatException {
        //the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = pairList.toArray(new String[0][]);
        List<String> couple = new ArrayList<String>();
        TParser tp = new TParser();
        Result[] results = new Result[pairList.size()];
        Comparer comparer = new Comparer();

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple.add(instMap.get(pair1));
            couple.add(instMap.get(pair2));

            if (couple.get(0) != null && couple.get(1) != null) {
                //do the real work!
                try {
                    Pattern chi = comparer.findPattern(couple);
                    
                    tp.load(couple.get(0));
                    String classification1 = tp.getClassification();

                    tp.load(couple.get(1));
                    String classification2 = tp.getClassification();

                    String name = pair1 + '\t' + pair2 + '\t';

                    results[i] = new Result(Utilities.doCompression(couple, chi),
                    		name, chi.toString(), classification1 + "\t" + classification2);

                    comparer.clear();
                } catch (Exception e) { 
                    System.err.println(pair1 + " , " + pair2 + ", " + e); 
                }
            } else { System.err.println("ONE OF THE NAMES WAS NULL!"); }
        }
        return results;
    }
    
    public void allVsAll(List<String[]> pairList, Map<String, String> instMap) throws TopsStringFormatException {
        //the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = (String[][]) pairList.toArray(new String[0][]);
        List<String> couple = new ArrayList<String>();
        TParser tp = new TParser();
        Comparer comparer = new Comparer();

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple.add(instMap.get(pair1));
            couple.add(instMap.get(pair2));

            if (couple.get(0) != null && couple.get(1) != null) {
                //do the real work!
                try {
                    Pattern chi = comparer.findPattern(couple);
                    
                    System.out.print(Utilities.doCompression(couple, chi));
                    System.out.print('\t');
                    //print out the pattern
                    System.out.print(pair1 + '\t' + pair2 + '\t');  //say which we are trying
                    System.out.print(chi);
                    System.out.print('\t');
                    tp.load(couple.get(0));
                    System.out.print(tp.getClassification());
                    System.out.print('\t');
                    tp.load(couple.get(1));
                    System.out.print(tp.getClassification());
                    System.out.print('\n');
                    
                    comparer.clear();
                } catch (Exception e) { 
                    System.err.println(pair1 + " , " + pair2 + ", " + e); 
                }
            } else { System.out.println("ONE OF THE NAMES WAS NULL!"); }
        }
    }


    public List<String> getNames(List<String> examples) {
        List<String> names = new ArrayList<String>();
        TParser tp = new TParser();

        for (String nextLine : examples) {
            tp.load(nextLine);
            String name = tp.getName();
            names.add(name);
        }

        return names;
    }

    // do an all-v-all on the examples
    public Result[] run(List<String> names, List<String> examples) {
        List<String[]> pairList = new ArrayList<String[]>();
        Map<String, String> instMap = new HashMap<String, String>(names.size());

        // map the names to the examples
        for (int i = 0; i < examples.size(); i++) {
            String nextLine = examples.get(i);
            String nextName = names.get(i);
            instMap.put(nextName, nextLine);   
        }

        // construct a list of name-name pairs
        for (int i = 0; i < names.size(); i++) {
            for (int j = i + 1; j < names.size(); j++) {
                if (i != j) {
                    String[] miniContainer = {names.get(i), names.get(j)};
                    pairList.add(miniContainer);
                }
            }
        }

        // now that we have built the input data, do the actual comparisons
        
        Result[] results;

        try {
            results = this.allVsAllWithResults(pairList, instMap);
        } catch (TopsStringFormatException tsfe) {
            System.err.println(tsfe.toString());
            results = new Result[0];
        }

        return results;
    }
    
    public void printResultsAsOCInput(List<String> names, Result[] results, PrintStream stream) {
        // the number of examples
        stream.println(names.size());

        // the labels
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            stream.println(name);
        }

        // the upper matrix
        for (int j = 0; j < results.length; j++) {
            stream.println(results[j].getCompression());
        }
    }
}
