package tops.web.engine;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleCompareServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2545927077861998063L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get a string, either in pieces or whole
        String target = request.getParameter("target");
        if (target == null) {
            String targetName = request.getParameter("target-name");
            String targetBody = request.getParameter("target-body");
            String targetTail = request.getParameter("target-tail");
            if ((targetName == null) || (targetName.equals(""))) {
                targetName = "test";
            }
            target = targetName.trim() + " " + targetBody.trim() + " " + targetTail.trim();
        }

        String topnum = request.getParameter("topnum");
        String targetService = request.getParameter("targetService");
        String pagesize = request.getParameter("pagesize");
        String sub = request.getParameter("subclasses");

        // why is target called target?
        this.log("request for : " + targetService + " query = " + target); 

        // doesn't matter what this is, so long as it's set!
        request.setAttribute("newSubmission", "true"); 

        request.setAttribute("topnum", topnum);
        request.setAttribute("targetService", targetService);
        request.setAttribute("pagesize", pagesize);
        request.setAttribute("target", target);
        request.setAttribute("sub", sub);
        String next = "/pattern/compare";
        RequestDispatcher dispatcher = request.getRequestDispatcher(next);
        dispatcher.forward(request, response);
    }

}// EOC
