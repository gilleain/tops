package tops.model.classification;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LevelIterator implements Iterator<Level> {

    private int relativeTargetDepth;

    private List<Level> subLevels;

    private int[] positions;

    private int nextPosition; // bad state!

    public LevelIterator(int parentDepth, int targetDepth, List<Level> subLevels) {
        this.relativeTargetDepth = targetDepth - parentDepth;
        this.subLevels = subLevels;
        this.positions = new int[this.relativeTargetDepth];
        this.positions[this.relativeTargetDepth - 1] = -1;
        this.nextPosition = 0;
    }

    public boolean hasNext() {
        this.nextPosition = this.nextPosition(1, this.subLevels);
        // System.out.println("next Position = " + this.nextPosition);
        if (this.nextPosition != -1) {
            return true;
        } else {
            return false;
        }
    }

    public int nextPosition(int currentDepth, List<Level> currentSubLevel) {
        if (currentDepth == this.relativeTargetDepth) {
            // System.out.println("At target depth : " + currentDepth);
            int nextPositionAtThisLevel = 1 + this.positions[this.positions.length - 1];
            if (nextPositionAtThisLevel >= currentSubLevel.size()) {
                // System.out.println("Reached end of level at depth : " +
                // currentDepth + " index = " + nextPositionAtThisLevel + " size
                // = " + currentSubLevel.size());
                this.positions[currentDepth - 1] = -1;
                return -1;
            } else {
                this.positions[this.positions.length - 1] = nextPositionAtThisLevel;
                return nextPositionAtThisLevel;
            }
        } else {
            // System.out.println("Reached depth : " + currentDepth);
            int indexAtThisLevel = this.positions[currentDepth - 1];
            Level subLevel = currentSubLevel.get(indexAtThisLevel);
            int nextPositionAtLowerLevel = this.nextPosition(currentDepth + 1, subLevel.getSubLevels());
            // not found => go on to the next
            if (nextPositionAtLowerLevel == -1) {
                // System.out.println("Shifting at depth : " + currentDepth);
                int nextIndexAtThisLevel = indexAtThisLevel + 1;
                if (nextIndexAtThisLevel < currentSubLevel.size()) {
                    this.positions[currentDepth - 1]++;
                    // try again
                    Level nextSubLevel = currentSubLevel.get(nextIndexAtThisLevel);
                    nextPositionAtLowerLevel = this.nextPosition(
                            currentDepth + 1, nextSubLevel.getSubLevels());
                } else {
                    this.positions[currentDepth - 1] = 0;
                    return -1;
                }
            }
            return nextPositionAtLowerLevel;
        }
    }

    public Level getLevelAtTargetDepth(int currentDepth, List<Level> currentSubLevel) {
        Level subLevel = currentSubLevel.get(this.positions[currentDepth - 1]);
        if (currentDepth == this.relativeTargetDepth) {
            return subLevel;
        } else {
            return this.getLevelAtTargetDepth(currentDepth + 1, subLevel.getSubLevels());
        }
    }

    public Level next() {
        if (this.nextPosition != -1) {
            return this.getLevelAtTargetDepth(1, this.subLevels);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
    }
}
