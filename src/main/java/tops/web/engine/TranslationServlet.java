package tops.web.engine;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import tops.dw.protein.Protein;
import tops.web.display.servlet.CartoonDataSource;
import tops.web.display.servlet.UploadCartoonDataSource;

public class TranslationServlet extends HttpServlet {

	private static final long serialVersionUID = -7794798388203061901L;

    private String pathToScratch;
    
    private String executablePath;

    @Override
    public void init() throws ServletException {
        String scratchDir = this.getInitParameter("scratch.dir");
        String exePath = this.getInitParameter("executable.path");
        
        this.pathToScratch = getServletContext().getRealPath(scratchDir);
        this.executablePath = getServletContext().getRealPath(exePath);
        
        this.log("scratch directory = " + scratchDir + " -> " + pathToScratch);
        this.log("exe path = " + exePath + " -> " + executablePath);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CartoonDataSource cartoonDataSource = 
        		new UploadCartoonDataSource(request, executablePath, pathToScratch);
        Protein protein = cartoonDataSource.getCartoon();
        HttpSession session = request.getSession();
        session.setAttribute("protein", protein);
        String url = "/summary";	// XXX TMP
        RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        dispatcher.forward(request, response);
    }

   

}
