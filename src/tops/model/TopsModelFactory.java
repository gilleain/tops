package tops.model;

public class TopsModelFactory {

    public static TopsModel generateModel(TopsGraph g) {
        int numberOfSheets = g.getNumberOfSheets();

        if (numberOfSheets == 1) {
            // return new SingleSheet(g);
            return null;
        } else {
            return null;
        }
    }
}
