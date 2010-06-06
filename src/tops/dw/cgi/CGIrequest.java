package tops.dw.cgi;

import java.net.*;
import java.io.*;

/**
 * a class to do a CGI request to a server program
 * 
 * @author David Westhead
 * @version 2.00 29 Sept. 1997
 */
public class CGIrequest {

    /* START instance variables */

    private String ErrorString = null;

    private String Host = null;

    private int Port = 80;

    private String Method = "GET";

    private String CGI_prog = null;

    private String Query = null;

    private Socket s = null;

    private PrintWriter pw = null;

    private InputStream response_stream = null;

    /* END instance variables */

    /**
     * The constructor takes
     * 
     * @param host:
     *            the host (eg. www.ebi.ac.uk)
     * @param port:
     *            the port number at which the server listens
     * @param method:
     *            "GET" (as yet "POST" is not implemented)
     * @param cgi_prog:
     *            the tops.dw.cgi program to use (+path info)
     * @param query:
     *            the query string
     */
    public CGIrequest(String host, int port, String method, String cgi_prog,
            String query) {

        this.Host = host;
        this.Port = port;
        this.Method = method;
        this.CGI_prog = cgi_prog;
        this.Query = query;

    }

    /**
     * method to actually do the request
     * 
     * @return : An input stream giving the servers response
     * 
     */
    public InputStream doRequest() {

        this.response_stream = null;

        try {
            String request = this.buildRequest();
            this.response_stream = this.queryHTTPServer(request);
        } catch (UnknownCGIMethodException ucme) {
            this.response_stream = null;
            this.ErrorString = "CGIRequest: unknown CGI method";
        } catch (SocketException se) {
            this.response_stream = null;
            this.ErrorString = "CGIRequest: a socket exception occurred";
        } catch (IOException ioe) {
            this.response_stream = null;
            this.ErrorString = "CGIRequest: an IO exception occurred";
        }
        // finally { return response_stream; }
        return this.response_stream;
    }

    /**
     * call when doRequest returns a null stream
     * 
     * @return : a string describing the error which occurred
     * 
     */
    public String getErrorString() {
        return this.ErrorString;
    }

    /**
     * MUST be called after doRequest but only when all data has been read from
     * the returned stream
     */
    public void Close() throws IOException {
        if (this.response_stream != null)
            this.response_stream.close();
        if (this.pw != null)
            this.pw.close();
        if (this.s != null)
            this.s.close();
    }

    private InputStream queryHTTPServer(String request) throws SocketException,
            IOException {

        InputStream response_stream = null;

        // try {

        this.s = new Socket(this.Host, this.Port);
        response_stream = this.s.getInputStream();

        PrintStream sout = new PrintStream(this.s.getOutputStream());
        sout.println(request);

        // PrintWriter stuff appears not to work in netscape 4.0.3 preview
        // release with JDK1.1 support
        // Use deprecated stuff above instead for now (this does work)
        // pw = new PrintWriter( s.getOutputStream() );
        // pw.println(request);

        // } finally { return response_stream; }
        return response_stream;
    }

    private String buildRequest() throws UnknownCGIMethodException {

        String request = null;

        if (this.Method.equals("GET")) {
            request = this.Method + " " + this.CGI_prog + "?" + this.Query + " HTTP/1.0\n"
                    + "Content-type: application/x-www-form-urlencoded\n"
                    + "Accept: text/plain\n\n";
        } else if (this.Method.equals("POST")) {
            request = this.Method + " " + this.CGI_prog + " HTTP/1.0\n"
                    + "Content-type: application/x-www-form-urlencoded\n"
                    + "Accept: text/plain\n" + "Content-length:"
                    + this.Query.length() + "\n\n" + this.Query + "\n";
        } else {
            throw new UnknownCGIMethodException();
        }

        return request;

    }

}
