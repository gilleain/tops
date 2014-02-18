package tops.engine.crossvalidate;

import java.util.HashMap;
import java.util.Iterator;

import tops.model.classification.Level;
import tops.model.classification.RepSet;

import tops.engine.inserts.Matcher;
import tops.engine.inserts.Pattern;

public class CrossValidator {

    public static void doSuperfolds(String filename) {
        Level root = null;
        try {
            root = Level.fromFile(filename, Level.ROOT, "Superfolds");
        } catch (java.io.IOException ioe) {
            System.err.println(ioe);
            return;
        }

        // first, make the patterns
        HashMap<Level, HashMap<Level, Pattern>> tLevelHGroupPatterns = CrossValidator.generatePatterns(root);

        // now, do the validation
         CrossValidator.doSuperfoldValidation(tLevelHGroupPatterns);
    }

    public static HashMap<Level, HashMap<Level, Pattern>> generatePatterns(Level root) {
        // make patterns for each h-group
        Iterator<Level> tLevelIterator = root.getSubLevelIterator(Level.T);
        HashMap<Level, HashMap<Level, Pattern>> tLevelHGroupPatterns = new HashMap<Level, HashMap<Level, Pattern>>();
        while (tLevelIterator.hasNext()) {
            Level tLevel = (Level) tLevelIterator.next();
            HashMap<Level, Pattern> hLevelPatterns = new HashMap<Level, Pattern>();
            Iterator<Level> hLevelIterator = tLevel.getSubLevelIterator(Level.H);
            while (hLevelIterator.hasNext()) {
                Level hLevel = (Level) hLevelIterator.next();
                RepSet reps = hLevel.getRepSet();
                Pattern pattern = reps.generatePatternWithInserts();
                System.out.println(pattern.getCompression() + "\t" + pattern
                        + "\t" + hLevel);
                hLevelPatterns.put(hLevel, pattern);
            }
            tLevelHGroupPatterns.put(tLevel, hLevelPatterns);
        }
        return tLevelHGroupPatterns;
    }

    public static void doSuperfoldValidation(HashMap<Level, HashMap<Level, Pattern>> tLevelHGroupPatterns) {
        // go through the SReps, matching each set of HPatterns to determine
        // TLevel
        Matcher m = new Matcher();
        Iterator<Level> tLevelMapIterator = tLevelHGroupPatterns.keySet().iterator();
        while (tLevelMapIterator.hasNext()) {
            Level tLevelKey = tLevelMapIterator.next();
            Iterator<Level> anotherHLevelIterator = tLevelKey.getSubLevelIterator(Level.H);
            while (anotherHLevelIterator.hasNext()) {
                Level bundleOfSReps = (Level) anotherHLevelIterator.next();
                RepSet sReps = bundleOfSReps.getRepSet();
                String[] instances = sReps.getInstances();
                for (int i = 0; i < instances.length; i++) {
                    boolean TRUE_POSITIVES = false;
                    int TRUE_NEGATIVES = 0;
                    int FALSE_POSITIVES = 0;
                    Pattern instance = new Pattern(instances[i]);
                    Iterator<Level> anotherTLevelIterator = tLevelHGroupPatterns
                            .keySet().iterator();
                    while (anotherTLevelIterator.hasNext()) {
                        boolean matchedAnotherTLevel = false;
                        Level anotherTLevelKey = anotherTLevelIterator
                                .next();
                        HashMap<Level, Pattern> hLevelPatternMap = tLevelHGroupPatterns
                                .get(anotherTLevelKey);
                        Iterator<Level> hLevelMapIterator = hLevelPatternMap.keySet()
                                .iterator();
                        while (hLevelMapIterator.hasNext()) {
                            Level hLevel = (Level) hLevelMapIterator.next();
                            // don't match the SRep against the HLevel from
                            // which it originated!
                            if (hLevel.equals(bundleOfSReps)) {
                                continue;
                            }
                            Pattern hPattern = (Pattern) hLevelPatternMap
                                    .get(hLevel);
                            try {
                                if (m.singleMatch(hPattern, instance)) {
                                    if (tLevelKey.equals(anotherTLevelKey)) {
                                        TRUE_POSITIVES = true; // !
                                    } else {
                                        matchedAnotherTLevel = true;
                                    }
                                    break; // don't need to continue!
                                    // System.out.println(hPattern + " matches "
                                    // + instance);
                                }
                            } catch (Exception e) {
                                System.err.println(hPattern + " " + instance);
                                System.err.println(e);
                                e.printStackTrace(System.err);
                            }
                        }
                        if (matchedAnotherTLevel) {
                            FALSE_POSITIVES++;
                        } else {
                            if (!tLevelKey.equals(anotherTLevelKey)) {
                                TRUE_NEGATIVES++;
                            }
                        }
                    }
                    String name = instance.getName();
                    int TP = (TRUE_POSITIVES) ? 1 : 0;
                    int FN = (TRUE_POSITIVES) ? 0 : 1;
                    System.out.println(name + "\t"
                            + bundleOfSReps.getFullCode() + "\t" + TP + "\t"
                            + TRUE_NEGATIVES + "\t" + FALSE_POSITIVES + "\t"
                            + FN);
                }
            }
        }
    }

    public static void leaveOneOut(Level tLevel) {
        for (int j = 0; j < tLevel.numberOfSubLevels(); j++) {
            Level hLevel = tLevel.getSubLevel(j);
            if (!hLevel.isSingleton()) {
                System.out.println("Cross validating : " + hLevel);
                RepSet reps = hLevel.getRepSet();
                for (int i = 0; i < reps.size(); i++) {
                    int TRUE_POSITIVES = 0;
                    int TRUE_NEGATIVES = 0;
                    int FALSE_POSITIVES = 0;
                    int FALSE_NEGATIVES = 0;
                    RepSet nextInSet = reps.removeAtIndex(i);
                    Pattern pattern = reps.generatePatternWithInserts();
                    int numberMatching = reps.numberMatching(pattern);
                    TRUE_POSITIVES = numberMatching;
                    FALSE_NEGATIVES = (reps.size() - numberMatching);
                    System.out.println(pattern + " matches " + numberMatching
                            + " out of " + reps.size() + " in this hLevel");
                    reps.replaceSubset(nextInSet);
                    for (int k = 0; k < tLevel.numberOfSubLevels(); k++) {
                        if (k == j) {
                            continue;
                        }
                        Level otherHLevel = tLevel.getSubLevel(k);
                        RepSet otherReps = otherHLevel.getRepSet();
                        int otherNumberMatching = otherReps
                                .numberMatching(pattern);
                        FALSE_POSITIVES += otherNumberMatching;
                        TRUE_NEGATIVES += (otherReps.size() - otherNumberMatching);
                        System.out.println(pattern + " matches "
                                + otherNumberMatching + " out of "
                                + otherReps.size() + " in " + otherHLevel);
                    }
                    int top = (TRUE_POSITIVES * TRUE_NEGATIVES)
                            - (FALSE_POSITIVES * FALSE_NEGATIVES);
                    int bottom = (TRUE_POSITIVES + FALSE_POSITIVES)
                            * (FALSE_POSITIVES + TRUE_NEGATIVES)
                            * (TRUE_NEGATIVES + FALSE_NEGATIVES)
                            * (FALSE_NEGATIVES + TRUE_POSITIVES);
                    double cc = top / Math.sqrt(bottom);
                    System.out.println("TP : " + TRUE_POSITIVES + " TN "
                            + TRUE_NEGATIVES + " FP " + FALSE_POSITIVES
                            + " FN " + FALSE_NEGATIVES + " cc " + cc + " for "
                            + hLevel);
                }
            }
        }
    }

    public static void nFold(int n, Level tLevel) {
        for (int j = 0; j < tLevel.numberOfSubLevels(); j++) {
            Level hLevel = tLevel.getSubLevel(j);
            System.out.println("Cross validating : " + hLevel);
            RepSet reps = hLevel.getRepSet();
            int size = reps.size();
            for (int i = 0; i < size - n; i += n) {
                RepSet slice = reps.removeSlice(i, i + n - 1);
                Pattern pattern = reps.generatePatternWithInserts();
                int numberMatching = reps.numberMatching(pattern);
                System.out.println(pattern + " matches " + numberMatching
                        + " out of " + reps.size());
                reps.replaceSubset(slice);
            }
        }
    }

    public static void main(String[] args) {
        String filename = args[0];
        CrossValidator.doSuperfolds(filename);
        
//         String tLevelName = args[1];
         
//         try { 
//             Level tLevel = Level.fromFile(filename, Level.T, tLevelName);
//             System.out.println(tLevel); 
//             System.out.println("leave one out : ");
//             CrossValidator.leaveOneOut(tLevel); 
//             CrossValidator.nFold(2, tLevel); 
//         } catch(java.io.IOException ioe) {
//             System.err.println(ioe); 
//         }

    }
}
