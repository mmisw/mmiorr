package org.mmisw.orrportal.gwt.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet does the "pre-upload" operation, that is, it stores the uploaded file from
 * the client in the {@link OrrConfig#preUploadsDir} directory.
 * 
 * @author Carlos Rueda
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/** Maximum size of an uploaded file */
	// Was initially set to 2MB, too short; it's now unlimited.
	// See issue #153 "Large ontology file fails to load"
	private static final long MAX_FILE_SIZE = -1L;
	
	
	protected final Log log = LogFactory.getLog(UploadServlet.class);
	
	
	private static File preUploadsDir;

	
	public void init() throws ServletException {
		super.init();
		log.info("initializing " +getClass().getSimpleName()+ " service ...");
		try {
			OrrConfig.init();
			preUploadsDir = OrrConfig.instance().preUploadsDir;
			log.info("preUploadsDir = " +preUploadsDir);
		}
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		super.destroy();
		log.info("destroy called " +getClass().getSimpleName()+ "\n\n");
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if ( ! isMultipart ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "not a multipart request");
			return;
		}

		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// size threshold beyond which files are written directly to disk.
		factory.setSizeThreshold(10*1024);
//		factory.setRepository(yourTempDirectory);

		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		upload.setFileSizeMax(MAX_FILE_SIZE);
		
		
		// Parse the request
		List<?> /* FileItem */ items;
		try {
			items = upload.parseRequest(request);
		}
		catch (FileUploadException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
			return;
		}
		
		processItems(request, response, items);
	}
	
	/** 
	 * Processes the uploaded items.
	 *  In this class, the form fields are ignored. Just the file is processed, ie., 
	 *  a local copy is made.
	 * @throws IOException 
	 */
	protected void processItems(HttpServletRequest request,
			HttpServletResponse response, List<?> items) throws IOException {
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");

		try {
			File file = null;
			
			Iterator<?> iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
	
			    if (item.isFormField()) {
			        processFormField(item);
			    } 
			    else {
			        file = processUploadedFile(request, item);
			    }
			}
			
			if ( file != null ) {
				String filename = file.getAbsolutePath();

				out.println("<success><filename>" +filename+ "</filename></success>");
				if ( log.isDebugEnabled() ) {
					log.debug("temporary file: " +filename);
				}
			}
			else {
				log.error("SC_BAD_REQUEST: No file included");
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file included");
			}
		}
		catch (Exception ex) {
			out.println("<error>" +ex.getMessage()+ "</error>");
			log.error("Error while creating temporary file: " +ex.getMessage(), ex);
		}
		
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * Returns the temporary file in the local system.
	 * Returns null is the item's associated size is zero (which happens when the
	 * client does not specify a file).
	 */
	protected File processUploadedFile(HttpServletRequest request, FileItem item) 
	throws IOException {
		
		long size = item.getSize();
		if ( log.isDebugEnabled() ) {
			log.debug("processUploadedFile: item=" +item);
			log.debug("getContentType=" +item.getContentType());
			log.debug("getSize=" +size);
		}
		
		if ( size == 0 ) {
			return null;
		}
		
		String sessionId = request.getSession().getId();
		File file = File.createTempFile("ontmd_" +sessionId+"_", ".tmp", preUploadsDir );
		
		try {
			item.write(file);
		}
		catch (Exception e) {
			throw new IOException("Cannot write uploaded file to " +file);
		}
//		InputStream is = item.getInputStream();
//		OutputStream os = new FileOutputStream(file);
//		// don't use PrintWriter os = new PrintWriter(file) to avoid any potential conversions;
//		// the file should be handled as binary.
//		IOUtils.copy(is, os);
//
//		os.flush();
//		os.close();

		return file;
	}

	/** nothing done in this class */
	protected void processFormField(FileItem item) {
		// ignored
	}

}