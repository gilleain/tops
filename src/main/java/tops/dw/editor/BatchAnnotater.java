package tops.dw.editor;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import tops.dw.protein.DomainDefinition;
import tops.dw.protein.Protein;
import tops.dw.protein.SecStrucElement;

public class BatchAnnotater {
	
	public static void annotateCartoon(File inFile, String outputDirectory, int[] residuesToAnnotate) {
		try {
			int w = 500;
	        int h = 500;
	        int b = 20;
			
			Protein p = new Protein(inFile);
			Vector<DomainDefinition> names = p.getDomainDefs();
			for (int i = 0; i < p.numberDomains(); i++) {
				SecStrucElement domain = p.getDomain(i);
				domain.fitToRectangle(0, 0, w, h, b);
				
				domain.highlightByResidueNumber(residuesToAnnotate);
				Image image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		        Graphics g = image.getGraphics();
		        g.fillRect(0, 0, w, h);
		        domain.paint(g);
	 
		        File file = new File(outputDirectory, names.get(i).toString() + ".png");
				ImageIO.write((RenderedImage) image, "PNG", file);
			}
			
						
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public static Map<String, List<Integer>> parse(File file) {
		Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(file));
			String line;
			while ((line = b.readLine()) != null) {
				String pdbid = line.substring(0, 4);
				String[] bits = line.split("\t");
				String resrange = bits[1];
				int d = resrange.indexOf("-");
				int start = Integer.parseInt(resrange.substring(0, d));
				int end = Integer.parseInt(resrange.substring(d+1));
				int mid = (start + end) / 2;
				
				List<Integer> list;
				if (map.containsKey(pdbid)) {
					list = map.get(pdbid);
					list.add(new Integer(mid));
				} else {
					list = new ArrayList<Integer>();
					list.add(new Integer(mid));
					map.put(pdbid, list);
				}
			}
			b.close();
		} catch (Exception e) {
			
		}
		
		return map;
	}

}
