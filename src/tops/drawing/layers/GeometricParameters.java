package tops.drawing.layers;

public class GeometricParameters {

    public int leftMostCenter;
    public int centralAxis;
    public int separation;
    public int strandWidth;
    public int strandHeight;
    public int helixWidth;
    public int helixHeight;
    public int connectionLength;
    public int sheetSeparation;
    
    public GeometricParameters() {
        this.setToDefaults();
    }
    
    public GeometricParameters(String[] args) {
        try {
            this.leftMostCenter = Integer.parseInt(args[0]);
            this.centralAxis = Integer.parseInt(args[1]);
            this.separation = Integer.parseInt(args[2]);
            this.strandWidth = Integer.parseInt(args[3]);
            this.strandHeight = Integer.parseInt(args[4]);
            this.helixWidth = Integer.parseInt(args[5]);
            this.helixHeight = Integer.parseInt(args[6]);
            this.connectionLength = Integer.parseInt(args[7]);
            this.sheetSeparation = Integer.parseInt(args[8]);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            this.setToDefaults();
        }
    }

    public void setToDefaults() {
        this.leftMostCenter = 60;
        this.centralAxis = 200;
        this.separation = 90;
        this.strandWidth = 60;
        this.strandHeight = 200;
        this.helixWidth = 50;
        this.helixHeight = 180;
        this.connectionLength = 35;
        this.sheetSeparation = 45;
    }
}
