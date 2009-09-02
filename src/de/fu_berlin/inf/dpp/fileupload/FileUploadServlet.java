package de.fu_berlin.inf.dpp.fileupload;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class FileUploadServlet
 */
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger
			.getLogger(FileUploadServlet.class.getName());

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FileUploadServlet() {
		super();
		// nothing to do here
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log.warn("Recieved a GET request from " + request.getRemoteAddr()
				+ ", but we don't process GET requests");

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.append("<html><body>\n");
		out.append("That's all you gonna GET: " + new Date().toString());
		out.append("\n</body></html>");
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		log.info(String.format("Recieved POST request from %s", request
				.getRemoteAddr()));

		// set up the response
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		// handle the file upload
		if (ServletFileUpload.isMultipartContent(request)) {
			FileUploadHandler handler = new FileUploadHandler(request);

			if (handler.processRequest())
				out.append("Uploading the file was successfull");
			else
				out.append("Uploading the file finished with errors");
		} else {
			String message = "The request didn't contain any multipart content";
			log.warn(message);
			out.append(message);
		}

		out.flush();
	}
}
