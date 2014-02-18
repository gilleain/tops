package tops.engine.drg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import tops.engine.Result;
import tops.engine.TParser;
import tops.engine.TopsStringFormatException;

public class AllVAllWrapper {

    public AllVAllWrapper() {}
    

    public Result[] allVsAllWithResults(ArrayList<String[]> pairList, HashMap<String, String> instMap) throws TopsStringFormatException {
        //the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = pairList.toArray(new String[0][]);
        String[] couple = new String[2];
        TParser tp = new TParser();
        Result[] results = new Result[pairList.size()];
        Comparer comparer = new Comparer();

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple[0] = instMap.get(pair1);
            couple[1] = instMap.get(pair2);

            if (couple[0] != null && couple[1] != null) {
                //do the real work!
                try {
                    Pattern chi = comparer.findPattern(couple);
                    
                    tp.load(couple[0]);
                    String classification1 = tp.getClassification();

                    tp.load(couple[1]);
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
    
    public void allVsAll(List<String[]> pairList, HashMap<String, String> instMap) throws TopsStringFormatException {
        //the domain names we are comparing are (pairKey, pairValue).
        String pair1, pair2;
        String[][] pairs = (String[][]) pairList.toArray(new String[0][]);
        String[] couple = new String[2];
        TParser tp = new TParser();
        Comparer comparer = new Comparer();

        for (int i = 0; i < pairs.length; ++i) {
            pair1 = pairs[i][0];
            pair2 = pairs[i][1];
            couple[0] = (String) instMap.get(pair1);
            couple[1] = (String) instMap.get(pair2);

            if (couple[0] != null && couple[1] != null) {
                //do the real work!
                try {
                    Pattern chi = comparer.findPattern(couple);
                    
                    System.out.print(Utilities.doCompression(couple, chi));
                    System.out.print('\t');
                    //print out the pattern
                    System.out.print(pair1 + '\t' + pair2 + '\t');  //say which we are trying
                    System.out.print(chi);
                    System.out.print('\t');
                    tp.load(couple[0]);
                    System.out.print(tp.getClassification());
                    System.out.print('\t');
                    tp.load(couple[1]);
                    System.out.print(tp.getClassification());
                    System.out.print('\n');
                    
                    comparer.clear();
                } catch (Exception e) { 
                    System.err.println(pair1 + " , " + pair2 + ", " + e); 
                }
            } else { System.out.println("ONE OF THE NAMES WAS NULL!"); }
        }
    }


    public ArrayList<String> getNames(ArrayList<String> examples) {
        ArrayList<String> names = new ArrayList<String>();
        TParser tp = new TParser();
        Iterator<String> itr = examples.iterator();

        while (itr.hasNext()) {
            String nextLine = itr.next();
            tp.load(nextLine);
            String name = tp.getName();
            names.add(name);
        }

        return names;
    }

    // do an all-v-all on the examples
    public Result[] run(ArrayList<String> names, ArrayList<String> examples) {
        ArrayList<String[]> pairList = new ArrayList<String[]>();
        HashMap<String, String> instMap = new HashMap<String, String>(names.size());

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
    
    public void printResultsAsOCInput(ArrayList<String> names, Result[] results, PrintStream stream) {
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

    public static void main(String[] args) {
        String filename = args[0];

        // read the examples from the filename supplied into a list
        ArrayList<String> examples = new ArrayList<String>();
        String line;
        try {
            BufferedReader buff = new BufferedReader(new FileReader(filename));
            while ((line = buff.readLine()) != null) {
                examples.add(line);
            }
            buff.close();
        } catch (IOException ioe) { System.out.println(ioe); }

        // run the wrapper on the examples to get an array of Result objects
        AllVAllWrapper allVAllWrapper = new AllVAllWrapper();
        ArrayList<String> names = allVAllWrapper.getNames(examples);
        Result[] results = allVAllWrapper.run(names, examples);

        // format / print the results
        if (args.length > 1) {
            if (args[1].equals("--oc-output")) {
                allVAllWrapper.printResultsAsOCInput(names, results, System.out);
            }
        } else {
            for (int i = 0; i < results.length; i++) {
                System.out.println(results[i]);
            }
        }
    }
}
