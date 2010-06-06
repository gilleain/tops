package tops.beans;

public class Authors {

    public Authors() {
    }

    private String PDB_ID;

    private String Author;

    public String getPDB_ID() {
        return this.PDB_ID;
    }

    public String getAuthor() {
        return this.Author;
    }

    public void setPDB_ID(String PDB_ID) {
        this.PDB_ID = PDB_ID;
    }

    public void setAuthor(String Author) {
        this.Author = Author;
    }
}
