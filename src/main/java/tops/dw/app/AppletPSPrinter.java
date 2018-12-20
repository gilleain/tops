package tops.dw.app;

import java.util.*;
import java.io.*;

public class AppletPSPrinter {

    private List<String> postscript = null;

    private String host = null;

    private int port = 80;

    private String cgiProg = null;

    private String errorString = "No error";

    private String baseUrl = null;

    public AppletPSPrinter(List<String> ps, String host, int port, String cgiPath, String cgiPrintProg) {
        this(ps, host, port, cgiPath, cgiPrintProg, null);
    }

    public AppletPSPrinter(List<String> ps, String host, int port, String cgiPath, String cgiPrintProg, String urlFilebase) {
        this.postscript = ps;
        this.host = host;
        this.port = port;
        this.cgiProg = cgiPath + cgiPrintProg;
        this.baseUrl = urlFilebase;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void doPrint() throws IOException {

        if (this.host == null) {
            this.errorString = "AppletPSPrinter: No Host";
            throw new IOException(this.errorString);
        }

        if (this.cgiProg == null) {
            this.errorString = "AppletPSPrinter: No CGI program";
            throw new IOException(this.errorString);
        }

        if (this.postscript == null) {
            this.errorString = "AppletPSPrinter: No PS";
            throw new IOException(this.errorString);
        }

        String query = this.formQuery(this.postscript);

        if (query != null) {

            CGIrequest cgir = new CGIrequest(this.host, this.port, "POST", this.cgiProg, query);
            InputStream cgiResponse = cgir.doRequest();

            if (cgiResponse == null) {
                this.errorString = "AppletPSPrinter: CGI error";
                throw new IOException(this.errorString);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(cgiResponse));
                String line;

                /* expect DONE in response */
                line = br.readLine();
                while ((line != null) && (!line.equals("DONE"))) {
                    line = br.readLine();
                }

                if (line == null) {
                    this.errorString = "AppletPSPrinter: Error unexpected CGI response";
                    throw new IOException(this.errorString);
                }
            }
            cgir.close();

        } else {
            this.errorString = "AppletPSPrinter: Error forming query";
            throw new IOException(this.errorString);
        }
    }

    private String formQuery(List<String> postscript2) {
        if (postscript2 == null)
            return null;

        StringBuilder query = new StringBuilder();

        query.append("URLbase:");
        query.append(this.baseUrl);
        query.append("\n");

        for (String element : postscript2) {
            query.append(element);
            query.append("\n");
        }
        return query.toString();
    }
}
