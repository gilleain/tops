package tops.cli;

public class Pipe<I, O> {
    
    private InputHandler<I> input;
    
    private OutputHandler<O> output;

    public Pipe(InputHandler<I> input, OutputHandler<O> output) {
        this.input = input;
        this.output = output;
    }

}
