package tops.data.dssp;

import java.util.ArrayList;
import java.util.List;

public class DsspModel {
    
    public static class Line {
        public final String dsspNumber;
        public final String pdbNumber;
        public final String chainName;
        public final String aminoAcidName;
        public final String sseType;
        public final String structure;
        public final String bridgePartner1;
        public final String bridgePartner2;
        public final String sheetName;
        public final String acc;
        public final String nho1;
        public final String ohn1;
        public final String nho2;
        public final String ohn2;
        public final String tco;
        public final String kappa;
        public final String alpha;
        public final String phi;
        public final String psi;
        public final String xca;
        public final String yca;
        public final String zca;
        
        public Line(
         String dsspNumber,
         String pdbNumber,
         String chainName,
         String aminoAcidName,
         String sseType,
         String structure,
         String bridgePartner1,
         String bridgePartner2,
         String sheetName,
         String acc,
         String nho1,
         String ohn1,
         String nho2,
         String ohn2,
         String tco,
         String kappa,
         String alpha,
         String phi,
         String psi,
         String xca,
         String yca,
         String zca) {
            this.dsspNumber=  dsspNumber;
            this.pdbNumber = pdbNumber;
            this.chainName =chainName;
            this.aminoAcidName = aminoAcidName;
            this.sseType = sseType;
            this.structure = structure;
            this.bridgePartner1 = bridgePartner1;
            this.bridgePartner2 = bridgePartner2;
            this.sheetName = sheetName;
            this.acc = acc;
            this.nho1 = nho1;
            this.ohn1 = ohn1;
            this.nho2 = nho2;
            this.ohn2 = ohn2;
            this.tco = tco;
            this.kappa = kappa;
            this.alpha = alpha;
            this.phi = phi;
            this.psi = psi;
            this.xca = xca;
            this.yca = yca;
            this.zca = zca;
        }
        
        public Line(String line) {
            dsspNumber = line.substring(0, 5).trim();
            pdbNumber = line.substring(5, 10).trim();
            chainName = line.substring(11, 12);
            aminoAcidName = line.substring(13, 14);
            sseType = line.substring(16, 17);
            structure = line.substring(17, 25).trim();
            bridgePartner1 = line.substring(26, 30).trim();
            bridgePartner2 = line.substring(30, 33).trim();
            sheetName = line.substring(33, 34);
            acc = line.substring(35, 38).trim();
            nho1 = line.substring(40, 51).trim();
            ohn1 = line.substring(51, 62).trim();
            nho2 = line.substring(62, 73).trim();
            ohn2 = line.substring(73, 84).trim();
            tco = line.substring(83, 91).trim();
            kappa = line.substring(92, 97).trim();
            alpha = line.substring(97, 103).trim();
            phi = line.substring(103, 109).trim();
            psi = line.substring(109, 115).trim();
            xca = line.substring(115, 122).trim();
            yca = line.substring(122, 129).trim();
            zca = line.substring(129).trim();
        }
        
        public String toString() {
            String format = "%5s%5s%2s%2s%3s%8s%4s%4s%1s%4s%s%s%s%s%s%s%s%s%s%s%s%s";
            return String.format(format, 
                    dsspNumber, pdbNumber, chainName, aminoAcidName,
                    sseType, structure, bridgePartner1, bridgePartner2, sheetName,
                    acc, nho1, ohn1, nho2, ohn2, tco, kappa, alpha,
                    phi, psi, xca, yca, zca);
        }
    }
    
    private List<Line> lines;
    
    private int numberOfResidues;
    
    private int numberOfChains;
    
    public int getNumberOfResidues() {
        return numberOfResidues;
    }

    public void setNumberOfResidues(int numberOfResidues) {
        this.numberOfResidues = numberOfResidues;
    }

    public int getNumberOfChains() {
        return numberOfChains;
    }

    public void setNumberOfChains(int numberOfChains) {
        this.numberOfChains = numberOfChains;
    }

    public DsspModel() {
        this.lines = new ArrayList<>();
    }
    
    public List<Line> getLines() {
        return this.lines;
    }
    
    public Line addLine(String lineString) {
        Line line = new Line(lineString);
        lines.add(line);
        return line;
    }
    
    public void addLine(Line line) {
        this.lines.add(line);
    }
    
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Line line : lines) {
            buffer.append(line).append("\n");
        }
        return buffer.toString();
    }

}
