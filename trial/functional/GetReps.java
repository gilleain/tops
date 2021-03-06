package functional;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

import tops.model.classification.CATHLevel;
import tops.model.classification.CATHTree;
import tops.model.classification.CathLevelCode;

/**
 * Get all the CATH representatives for a level (C, A, T, H ...) as pdb files
 * 
 * @author gilleain
 *
 */
public class GetReps {
	
	private void getLevel(CathLevelCode levelCode) throws IOException {
		String filename = "/home/gilleain/Documents/Research/cath-latest-release/cath-domain-list-S100.txt";
		CATHTree tree = CATHTree.fromFile(new File(filename));
		Iterator<CATHLevel> levelIterator = tree.getRoot().getSubLevelIterator(levelCode);
		while (levelIterator.hasNext()) {
			CATHLevel subLevel = levelIterator.next();
			System.out.println(subLevel);
		}
	}
	
	@Test
	public void testClass() throws IOException {
		getLevel(CathLevelCode.C);
	}
	
	@Test
	public void testArch() throws IOException {
		getLevel(CathLevelCode.A);
	}
	
	@Test
	public void testTopol() throws IOException {
		getLevel(CathLevelCode.T);
	}

}
