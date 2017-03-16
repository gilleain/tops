package tops.cli;

public interface PipedCommand<O1, I1, I2, O2> {
    
    public void addPipeIn(Pipe<I1, O1> pipe);
    
    public void addPipeOut(Pipe<I2, O2> pipe);

}
