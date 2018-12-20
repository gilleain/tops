package tops.dw.app;

import java.net.*;
import java.io.*;

/**
 * a class to do a CGI request to a server program
 * 
 * @author David Westhead
 * @version 2.00 29 Sept. 1997
 */
public class CGIrequest {

    private String errorString = null;

    private String host = null;

    private int port = 80;

    private String method = "GET";

    private String cgiProg = null;

    private String query = null;

    private Socket s = null;

    private PrintWriter pw = null;

    private InputStream responseStream = null;


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
    public CGIrequest(String host, int port, String method, String cgiProg, String query) {
        this.host = host;
        this.port = port;
        this.method = method;
        this.cgiProg = cgiProg;
        this.query = query;

    }

    /**
     * method to actually do the request
     * 
     * @return : An input stream giving the servers response
     * 
     */
    public InputStream doRequest() {

        this.responseStream = null;

        try {
            String request = this.buildRequest();
            this.responseStream = this.queryHTTPServer(request);
        } catch (UnknownCGIMethodException ucme) {
            this.responseStream = null;
            this.errorString = "CGIRequest: unknown CGI method";
        } catch (SocketException se) {
            this.responseStream = null;
            this.errorString = "CGIRequest: a socket exception occurred";
        } catch (IOException ioe) {
            this.responseStream = null;
            this.errorString = "CGIRequest: an IO exception occurred";
        } finally {
        }
        return this.responseStream;

    }

    /**
     * call when doRequest returns a null stream
     * 
     * @return : a string describing the error which occurred
     * 
     */
    public String getErrorString() {
        return this.errorString;
    }

    /**
     * MUST be called after doRequest but only when all data has been read from
     * the returned stream
     */
    public void close() throws IOException {
        if (this.responseStream != null)
            this.responseStream.close();
        if (this.pw != null)
            this.pw.close();
        if (this.s != null)
            this.s.close();
    }

    private InputStream queryHTTPServer(String request) throws IOException {

        InputStream responseStream = null;

        try {

            this.s = new Socket(this.host, this.port);
            responseStream = this.s.getInputStream();

            PrintStream sout = new PrintStream(this.s.getOutputStream());
            sout.println(request);

            // PrintWriter stuff appears not to work in netscape 4.0.3 preview
            // release with JDK1.1 support
            // Use deprecated stuff above instead for now (this does work)
            // pw = new PrintWriter( s.getOutputStream() );
            // pw.println(request);

        } finally {
        }
        return responseStream;

    }

    private String buildRequest() throws UnknownCGIMethodException {

        String request = null;

        if (this.method.equals("GET")) {
            request = this.method + " " + this.cgiProg + "?" + this.query + " HTTP/1.0\n"
                    + "Content-type: application/x-www-form-urlencoded\n"
                    + "Accept: text/plain\n\n";
        } else if (this.method.equals("POST")) {
            request = this.method + " " + this.cgiProg + " HTTP/1.0\n"
                    + "Content-type: application/x-www-form-urlencoded\n"
                    + "Accept: text/plain\n" + "Content-length:"
                    + this.query.length() + "\n\n" + this.query + "\n";
        } else {
            throw new UnknownCGIMethodException();
        }

        return request;

    }

}
