import java.io.BufferedReader;
import java.io.StringReader;

public class Test {
 
  public void doStuff() throws Exception {
    String multiLineString = "hello\nworld";
    StringReader stringReader = new StringReader(multiLineString);
    BufferedReader bufferedReader = new BufferedReader(stringReader);
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      System.out.println(line);
    }
  }
 
  public static void main(String[] args) throws Exception {
    new Test().doStuff();
  }
 
}
