package tops.drawing.model;


public class Connection {
    
    private SSE first;
    private SSE second;
    
	public Connection(SSE first, SSE second) {
	    this.first = first;
	    this.second = second;
	}
	
	public SSE getFirst() {
	    return this.first;
	}
	
	public SSE getSecond() {
	    return this.second;
	}

}
