package tops.dw.app;

import java.util.*;
import java.io.*;

public class AppletPSPrinter {


    private List<String> Postscript = null;

    private String host = null;

    private int port = 80;

    private String cgi_prog = null;

    private String ErrorString = "No error";

    private String base_url = null;

    public AppletPSPrinter(Vector<String> ps, String Host, int Port, String CGI_path,
            String CGI_print_prog) {
        this(ps, Host, Port, CGI_path, CGI_print_prog, null);
    }

    public AppletPSPrinter(List<String> ps, String Host, int Port, String CGI_path,
            String CGI_print_prog, String URLfilebase) {
        this.Postscript = ps;
        this.host = Host;
        this.port = Port;
        this.cgi_prog = CGI_path + CGI_print_prog;
        this.base_url = URLfilebase;
    }

    public String getErrorString() {
        return this.ErrorString;
    }

    public void doPrint() throws IOException {

        if (this.host == null) {
            this.ErrorString = "AppletPSPrinter: No Host";
            throw new IOException(this.ErrorString);
        }

        if (this.cgi_prog == null) {
            this.ErrorString = "AppletPSPrinter: No CGI program";
            throw new IOException(this.ErrorString);
        }

        if (this.Postscript == null) {
            this.ErrorString = "AppletPSPrinter: No PS";
            throw new IOException(this.ErrorString);
        }

        String query = this.formQuery(this.Postscript);

        if (query != null) {

            CGIrequest cgir = new CGIrequest(this.host, this.port, "POST", this.cgi_prog,
                    query);
            InputStream cgi_response = cgir.doRequest();

            if (cgi_response == null) {
                this.ErrorString = "AppletPSPrinter: CGI error";
                throw new IOException(this.ErrorString);
            } else {

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        cgi_response));
                String line;

                /* expect DONE in response */
                line = br.readLine();
                while ((line != null) && (!line.equals("DONE"))) {
                    line = br.readLine();
                }

                if (line == null) {
                    this.ErrorString = "AppletPSPrinter: Error unexpected CGI response";
                    throw new IOException(this.ErrorString);
                }

            }

            cgir.Close();

        } else {
            this.ErrorString = "AppletPSPrinter: Error forming query";
            throw new IOException(this.ErrorString);
        }

    }

    private String formQuery(List<String> postscript2) {

        String query = null;

        if (postscript2 == null)
            return null;

        StringBuffer sb = new StringBuffer();

        sb.append("URLbase:");
        sb.append(this.base_url);
        sb.append("\n");

        for (String element : postscript2) {
            sb.append(element);
            sb.append("\n");
        }

        query = sb.toString();

        return query;

    }

}
