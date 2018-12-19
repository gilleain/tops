package tops.cli;

public abstract class BaseCommand implements Command {
    
    protected void output(String message) {
        System.out.println(message);
    }
    
    protected void error(String message) {
        System.err.println(message);
    }
    
    protected void error(Exception exception) {
        System.err.println(exception.getMessage());
    }

}
