package org.mmisw.ont;

import java.io.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Helper class to dispatch the request for a regular resource.
 * Note: since <code>&lt;url-pattern>/*&lt;/url-pattern></code> is used in web.xml, *everything* 
 * gets dispatched through this service, so, besides ontologies and terms, other possible resources
 * should be resolved by the service.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
@ThreadSafe
class RegularFileDispatcher {
	
	private final Log log = LogFactory.getLog(RegularFileDispatcher.class);

	
	/** constructor (package-visible) */
	RegularFileDispatcher() {
	}

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
			// this is a "root" request.

			if (OntServlet._364) {
				String portalServiceUrl = OntConfig.Prop.PORTAL_SERVICE_URL.getValue();
				String html = OntServlet.getPortalMainHtml(portalServiceUrl, "");
				if ( log.isDebugEnabled() ) {
					log.debug("For 'root' request SERVING:\n\t|" + html.replaceAll("\n", "\n\t|"));
				}
				response.setContentType("text/html");
				StringReader is = new StringReader(html);
				ServletOutputStream sos = response.getOutputStream();
				IOUtils.copy(is, sos);
				sos.close();
				return;
			}

			// respond with information about this service:
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
		
		String portalServiceUrl = OntConfig.Prop.PORTAL_SERVICE_URL.getValue();
		
		// start the response page:
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +OntVersion.getFullTitle()+ "</title>");
		out.println("<link rel=\"stylesheet\" href=\"" +contextPath + "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<br/>");
		out.println(
				"<div align=\"center\">" + "\n" +
				"<table>" + "\n" +
				"<tr valign=\"center\">" + "\n" +
				"<td align=\"center\">" + "\n" +
				"<a href=\"http://marinemetadata.org/semanticframework\">" + 
				"<img src=\"" +contextPath + "/-/img/" +"semantic_framework.jpg" + "\" border=\"0\"" +
						"alt=\"MMI Semantic Framework\"/>" + "\n" +
				"</a>" + "\n" +
				"<br/>" + 
				"<br/>" + "\n" +
				"You have reached the main entry point of the URI resolution service:<br/>" +
				"<br/>" + 
				"<b>" +OntVersion.getTitle()+ "</b>" + "\n" +
				"<br/>" +
				"<font color=\"gray\" size=\"-2\">" +OntVersion.getVersionAndBuild()+ "</font>" +
				"<br/>" +
				"<br/>" +
				"<br/>" +
				"<br/>" +
				"</br>The <b>MMI Ontology Registry and Repository</b> portal is located at:<br/>\n" +
				"<br/>" + 
				"<a href=\"" +portalServiceUrl+ "\">" +portalServiceUrl+ "</a>" + "\n" +
				"<br/>" +
				"<br/>" +
				"<br/>" + 
				"<br/>" +
				"<br/>" +
				"</br>These services are part of the " +
				"<a href=\"http://marinemetadata.org/semanticframework\">" +
				"MMI Semantic Framework</a>." + "\n" +
				"</td>" +
				"</tr>" +
				"</table>" +
				"</div>"
		);
		
		out.println("</body>");
		out.println("</html>");
	}
}
