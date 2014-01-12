package tops.engine;

import java.io.Serializable;

//accessory class to store result lines so that they can be sorted by compression more easily

public class Result implements Comparable<Result>, Serializable {

    private String id;

    private String data;

    private String classification;

    private float compression;

    public Result() {
    }

    public Result(float compression, String id, String data,
            String classification) {
        this.id = id;
        this.data = data;
        this.compression = compression;
        this.classification = classification;
    }

    public float getCompression() {
        return this.compression;
    }

    public String getID() {
        return this.id;
    }

    public String getData() {
        return this.data;
    }

    public String getClassification() {
        return this.classification;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public int compareTo(Result other) {
        float otherCompression = other.getCompression();
        return Float.compare(otherCompression, this.compression); // reverse
                                                                    // sort
    }

    @Override
    public String toString() {
        return new String(this.compression + "\t" + this.id + "\t" + this.data + "\t"
                + this.classification);
    }

}// EOC
