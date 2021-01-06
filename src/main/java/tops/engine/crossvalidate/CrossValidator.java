package tops.engine.crossvalidate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tops.engine.inserts.Matcher;
import tops.engine.inserts.Pattern;
import tops.model.classification.CATHLevel;
import tops.model.classification.CathLevelCode;
import tops.model.classification.RepSet;

public class CrossValidator {

    public static void doSuperfolds(String filename) {
        CATHLevel root = null;
        try {
            root = CATHLevel.fromFile(filename, CathLevelCode.R, "Superfolds");
        } catch (IOException ioe) {
            System.err.println(ioe);
            return;
        }

        // first, make the patterns
        Map<CATHLevel, Map<CATHLevel, Pattern>> tLevelHGroupPatterns = CrossValidator.generatePatterns(root);

        // now, do the validation
        CrossValidator.doSuperfoldValidation(tLevelHGroupPatterns);
    }

    public static Map<CATHLevel, Map<CATHLevel, Pattern>> generatePatterns(CATHLevel root) {
        // make patterns for each h-group
        Iterator<CATHLevel> tLevelIterator = root.getSubLevelIterator(CathLevelCode.T.getLevel());
        Map<CATHLevel, Map<CATHLevel, Pattern>> tLevelHGroupPatterns = new HashMap<>();
        while (tLevelIterator.hasNext()) {
            CATHLevel tLevel = tLevelIterator.next();
            Map<CATHLevel, Pattern> hLevelPatterns = new HashMap<>();
            Iterator<CATHLevel> hLevelIterator = tLevel.getSubLevelIterator(CathLevelCode.H);
            while (hLevelIterator.hasNext()) {
                CATHLevel hLevel = hLevelIterator.next();
                RepSet reps = hLevel.getRepSet();
                Pattern pattern = reps.generatePatternWithInserts();
                System.out.println(pattern.getCompression() + "\t" + pattern + "\t" + hLevel);
                hLevelPatterns.put(hLevel, pattern);
            }
            tLevelHGroupPatterns.put(tLevel, hLevelPatterns);
        }
        return tLevelHGroupPatterns;
    }

    public static void doSuperfoldValidation(Map<CATHLevel, Map<CATHLevel, Pattern>> tLevelHGroupPatterns) {
        // go through the SReps, matching each set of HPatterns to determine
        // TLevel
        Matcher m = new Matcher();
        for (CATHLevel tLevelKey : tLevelHGroupPatterns.keySet()) {
            Iterator<CATHLevel> subLevelIterator = tLevelKey.getSubLevelIterator(CathLevelCode.H); 
            while (subLevelIterator.hasNext()) {
            	CATHLevel bundleOfSReps = subLevelIterator.next();
                RepSet sReps = bundleOfSReps.getRepSet();
                List<String> instances = sReps.getInstances();
                for (int i = 0; i < instances.size(); i++) {
                    boolean TRUE_POSITIVES = false;
                    int TRUE_NEGATIVES = 0;
                    int FALSE_POSITIVES = 0;
                    Pattern instance = new Pattern(instances.get(i));
                    for (CATHLevel anotherTLevelKey : tLevelHGroupPatterns.keySet()) {
                        boolean matchedAnotherTLevel = false;
                        Map<CATHLevel, Pattern> hLevelPatternMap = tLevelHGroupPatterns.get(anotherTLevelKey);
                        Iterator<CATHLevel> hLevelMapIterator = hLevelPatternMap.keySet().iterator();
                        while (hLevelMapIterator.hasNext()) {
                        	CATHLevel hLevel = hLevelMapIterator.next();
                            // don't match the SRep against the HLevel from
                            // which it originated!
                            if (hLevel.equals(bundleOfSReps)) {
                                continue;
                            }
                            Pattern hPattern = hLevelPatternMap.get(hLevel);
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
                    String fullCode = null;	// TODO - should be bundleOfSReps.getFullCode()
                    System.out.println(name + "\t"
                            + fullCode + "\t" + TP + "\t"
                            + TRUE_NEGATIVES + "\t" + FALSE_POSITIVES + "\t"
                            + FN);
                }
            }
        }
    }

    public static void leaveOneOut(CATHLevel tLevel) {
        for (int j = 0; j < tLevel.getChildren().size(); j++) {
        	CATHLevel hLevel = tLevel.getChildren().get(j);
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
                    for (int k = 0; k < tLevel.getChildren().size(); k++) {
                        if (k == j) {
                            continue;
                        }
                        CATHLevel otherHLevel = tLevel.getChildren().get(k);
                        RepSet otherReps = otherHLevel.getRepSet();
                        int otherNumberMatching = otherReps.numberMatching(pattern);
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

    public static void nFold(int n, CATHLevel tLevel) {
        for (int j = 0; j < tLevel.getChildren().size(); j++) {
        	CATHLevel hLevel = tLevel.getChildren().get(j);
            System.out.println("Cross validating : " + hLevel);
            RepSet reps = hLevel.getRepSet();
            int size = reps.size();
            for (int i = 0; i < size - n; i += n) {
                RepSet slice = reps.removeSlice(i, i + n - 1);
                Pattern pattern = reps.generatePatternWithInserts();
                int numberMatching = reps.numberMatching(pattern);
                System.out.println(
                        pattern + " matches " + numberMatching 
                        + " out of " + reps.size());
                reps.replaceSubset(slice);
            }
        }
    }
}
