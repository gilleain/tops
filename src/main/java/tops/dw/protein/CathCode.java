package tops.dw.protein;

public class CathCode {

    private static final int CODE_LENGTH = 6;

    private char[] code = new char[CathCode.CODE_LENGTH];

    public CathCode(String code) {
        int len = code.length();

        for (int i = 0; i < CathCode.CODE_LENGTH; i++) {
            this.code[i] = ' ';
        }
        int lim = (len < CathCode.CODE_LENGTH) ? len : 6;
        for (int i = 0; i < lim; i++) {
            this.code[i] = code.charAt(i);
        }
    }

    public char getChain() {
        return this.code[4];
    }

    public String getPcode() {
        return String.copyValueOf(this.code, 0, 4);
    }

    public int getDomain() {
        return Integer.parseInt(String.valueOf(this.code[5]));
    }

    @Override
    public String toString() {
        return String.copyValueOf(this.code);
    }

    public boolean equals(Object other) {
        if (other instanceof CathCode) {
            CathCode testcode = (CathCode) other;
            return this.toString().equals(testcode.toString());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
