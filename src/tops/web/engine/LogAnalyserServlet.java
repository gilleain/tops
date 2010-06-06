package tops.web.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Calendar;
import java.util.Date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogAnalyserServlet extends HttpServlet {

    private String logDirectory;

    private String prefix = "access_log";

    private String suffix = "txt";

    private DateFormat inputDateFormat = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss +0000");

    private DateFormat outputTimeFormat = new SimpleDateFormat("HH:mm:ss");

    private DateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Pattern linePattern = Pattern
            .compile("\\[\\[(.*)\\]\\]\\s\\[(.*)\\]\\s\\[(.*)\\]\\s\\[(.*)\\]\\s\\[(.*)\\]");

    @Override
    public void init() throws ServletException {
        String home = this.getInitParameter("home.dir");
        this.logDirectory = home + "/accesslogs";
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Calendar calendar = Calendar.getInstance();
        String dateString = request.getParameter("date");
        String filename = this.makeFilename(dateString, calendar);
        File logFile = new File(this.logDirectory, filename);

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");

        if (logFile.exists()) {
            this.analyzeFile(calendar, logFile, out);
        } else {
            out.println("File " + logFile + " does not exist");
        }
        out.println("</body></html>");
    }

    public void analyzeFile(Calendar calendar, File logFile, PrintWriter out) {
        StringBuffer table = new StringBuffer();
        table.append("<table align=\"center\" border=\"1\">");
        table.append("<thead><th>TIME</th><th>IP</th></thead>");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    logFile));
            String line;
            String lastIP = null;
            String earliestTimeString = null;
            String lastTimeString = null;

            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = this.linePattern.matcher(line);
                if (matcher.matches()) {

                    String timeString = matcher.group(1);
                    String ip = matcher.group(2);

                    if (earliestTimeString == null) {
                        earliestTimeString = lastTimeString = timeString;
                        lastIP = ip;
                        continue;
                    }

                    if (!lastIP.equals(ip)) {
                        this.addRecordToTable(earliestTimeString,
                                lastTimeString, lastIP, table);
                        earliestTimeString = timeString;
                    }

                    lastIP = ip;
                    lastTimeString = timeString;
                }
            }
            this.addRecordToTable(earliestTimeString, lastTimeString, lastIP,
                    table);
        } catch (IOException ioe) {
            out.write(ioe.toString());
            return;
        }
        table.append("</table>");
        out.write(table.toString());
    }

    public void addRecordToTable(String earliestTimeString,
            String lastTimeString, String lastIP, StringBuffer table) {
        Date earliestTime = this.makeDate(earliestTimeString);
        Date latestTime = this.makeDate(lastTimeString);
        String timeSpan = this.outputTimeFormat.format(earliestTime) + " - "
                + this.outputTimeFormat.format(latestTime);

        table.append("<tr>");

        table.append("<td>");
        table.append(timeSpan);
        table.append("</td>");

        table.append("<td>");
        table.append(lastIP);
        table.append("</td>");

        table.append("</tr>");
    }

    public String makeFilename(String dateString, Calendar calendar) {
        if (dateString == null) {
            dateString = this.fileDateFormat.format(calendar.getTime());
        }
        return this.prefix + "." + dateString + "." + this.suffix;
    }

    public Date makeDate(String dateString) {
        try {
            return this.inputDateFormat.parse(dateString);
        } catch (ParseException pe) {
            System.out.println(pe);
            return new Date();
        }
    }

}// EOC

