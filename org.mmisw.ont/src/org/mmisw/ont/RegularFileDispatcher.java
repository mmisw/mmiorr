package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Helper class to dispatch the request for a regular resource.
 * Note: since <code>&lt;url-pattern>/*&lt;/url-pattern></code> is used in web.xml, *everything* 
 * gets dispatched through this service, so, besides ontologies and terms, other possible resources
 * should be resolved by the service.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class RegularFileDispatcher {
	
	private final Log log = LogFactory.getLog(RegularFileDispatcher.class);
	

	/**
	 * Does the dispatch.
	 */
	void dispatch(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();

		// first, check if it is the "root" request, for example, in the mmisw.org deployment
		// with "ont" as the context, the root request is basically:
		//    http://mmisw.org/ont    or   http://mmisw.org/ont/
		// The "root" request is recognized when the requestedUri without any trailing
		// slashes is equal to the contextPath. For the 2nd request above, we have:
		//       request.getRequestURI()         = /ont/
		//       request.getContextPath()        = /ont
		if ( requestedUri.replaceAll("/*$", "").equals(contextPath) ) {
			// this is a "root" request. respond with information about this service:
			_showServiceInfo(request, response);
			return;
		}


		String path = request.getPathTranslated();
		File file = new File(path);
		if ( !file.canRead() || file.isDirectory() ) {
			if ( log.isDebugEnabled() ) {
				log.debug("Other resource: " +path+ ": not found or cannot be read");
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
			return;
		}

		String mime = servletContext.getMimeType(path);
		if ( mime != null ) {
			response.setContentType(mime);
		}

		FileInputStream is = new FileInputStream(file);
		ServletOutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		os.close();
	}
	
	/**
	 * Helper method to dispatch a "root" request.
	 */
	private void _showServiceInfo(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		String contextPath = request.getContextPath();
		PrintWriter out = null; 
		
		// start the response page:
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +UriResolver.FULL_TITLE+ "</title>");
		out.println("<link rel=stylesheet href=\"" +contextPath + "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<br/>");
		out.println(
				"<div align=\"center\">" +
				"<table>" +
				"<tr valign=\"center\">" +
				"<td align=\"center\">" +
				"<a href=\"http://marinemetadata.org/semanticframework\">" +
				"<img src=\"" +contextPath + "/img/" +"semantic_framework.jpg" + "\" border=\"0\"" +
						"alt=\"MMI Semantic Framework\"/>" +
				"</a>" +
				"<br/>" +
				"<br/>" +
				"<b>" +UriResolver.TITLE+ "</b>" +
				"</br>This service is part of the " +
				"<a href=\"http://marinemetadata.org/semanticframework\">" +
				"MMI Semantic Framework</a>" +
				"<br/>" +
				"<br/>" +
				"<font color=\"gray\" size=\"-2\">" +UriResolver.FULL_TITLE+ "</font>" +
				"</td>" +
				"</tr>" +
				"</table>" +
				"</div>"
		);
		
		out.println("</body>");
		out.println("</html>");
	}
}
