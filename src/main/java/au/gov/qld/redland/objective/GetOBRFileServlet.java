package au.gov.qld.redland.objective;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * Servlet implementation class GetOBRFileServlet
 */
public class GetOBRFileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetOBRFileServlet() {
	super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletReobjectIDsponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String objectiveID = request.getParameter("objectiveID");
	if (StringUtils.isEmpty(objectiveID)) {
	    objectiveID = "no Objective ID";
	}
	response.setContentType("text");
	response.setHeader("Content-Disposition", "attachment; filename=" + objectiveID + ".obr");
	response.setHeader("Cache-Control", "no cache");

	OutputStream out = response.getOutputStream();
	out.write(objectiveID.getBytes());

	out.flush();
	out.close();
    }

}
