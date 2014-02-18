package tops.dw.protein;

public class CATHcode {

    private static final int CodeLen = 6;

    private char Code[] = new char[CATHcode.CodeLen];

    public CATHcode(String code) {
        int i, lim;
        int len = code.length();

        for (i = 0; i < CATHcode.CodeLen; i++)
            this.Code[i] = ' ';
        lim = (len < CATHcode.CodeLen) ? len : 6;
        for (i = 0; i < lim; i++)
            this.Code[i] = code.charAt(i);

        return;

    }

    public char getChain() {
        return this.Code[4];
    }

    public String getPcode() {

        String pcode = "";

        pcode = String.copyValueOf(this.Code, 0, 4);

        return pcode;

    }

    public int getDomain() {
        //String s = String.copyValueOf(this.Code, 4, 1); this says get the code[4] = chain id!
        return Integer.parseInt(String.valueOf(this.Code[5]));
    }

    @Override
    public String toString() {
        return String.copyValueOf(this.Code);
    }

    public boolean equals(CATHcode testcode) {

        String s1, s2;

        if (testcode == null)
            return false;

        s1 = this.toString();
        s2 = testcode.toString();

        if (s1.equals(s2))
            return true;
        else
            return false;

    }

}
