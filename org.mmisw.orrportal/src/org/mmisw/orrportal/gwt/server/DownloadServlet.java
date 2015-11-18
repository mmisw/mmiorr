package org.mmisw.orrportal.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet does a "download" operation, that is, it makes available a file 
 * stored in the {@link OrrConfig#voc2rdfDir} directory.
 * 
 * @author Carlos Rueda
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final Log log = LogFactory.getLog(DownloadServlet.class);
	
	
	private static File downloadsDir;

	
	public void init() throws ServletException {
		super.init();
		log.info("initializing download service ...");
		try {
			downloadsDir = OrrConfig.instance().voc2rdfDir;
			log.info("downloadsDir = " +downloadsDir);
		}
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		super.destroy();
		log.info("destroy called.\n\n");
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processDownloadFile(request, response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	private void processDownloadFile(HttpServletRequest request, HttpServletResponse response) 
	throws IOException {
		
		String internalPath = request.getParameter("ip");     // internal path
		String externalName = request.getParameter("xn");  // external name
		String contentType = request.getParameter("ct");  // content-type
		
		if ( internalPath == null ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		File file  = new File(downloadsDir, internalPath);
		log.info("processDownloadFile: " +file);

		if ( contentType != null ) {
			response.setContentType(contentType);
		}
		
		if ( externalName != null ) {
			response.setHeader("Content-Disposition", "attachment; filename=" +externalName);
		}
		
		InputStream is = new FileInputStream(file);
		ServletOutputStream os = response.getOutputStream();
		IOUtils.copyLarge(is, os);

		os.flush();
		os.close();
	}
}