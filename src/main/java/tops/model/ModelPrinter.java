package tops.model;

public class ModelPrinter {
    
    public static String toString(Protein protein) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Protein [Name = %s]", protein.getName()));
        return sb.toString();
    }

}
