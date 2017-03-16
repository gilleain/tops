package tops.cli;

/**
 * Put a type to an output.
 * 
 * @author maclean
 *
 * @param <T>
 */
public interface OutputHandler<T> {

    public void put(T item);
}
