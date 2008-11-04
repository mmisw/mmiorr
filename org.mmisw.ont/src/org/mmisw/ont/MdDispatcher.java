package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Util;
import org.mmisw.ont.vocabulary.util.MdHelper;
import org.mmisw.ont.vocabulary.util.MdHelper.AttributeValue;

import com.hp.hpl.jena.rdf.model.Model;

import edu.drexel.util.rdf.JenaUtil;


/**
 * Dispatches the metadata output.
 * @author Carlos Rueda
 */
public class MdDispatcher {
	
	private final Log log = LogFactory.getLog(MdDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	
	
	MdDispatcher(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	/**
	 * Dispatch the display of metadata.
	 * @param request
	 * @param response
	 * @param mmiUri            Used is not null; otherwise, created internally.
	 * @param completePage      true to create complete page; false to just add the table.
	 * @throws IOException
	 * @throws ServletException
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, boolean completePage, String tableClass,
			String tableTitle
	) throws IOException, ServletException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("MdDispatcher.execute: starting response. tableClass=" +tableClass);
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		if ( mmiUri == null ) {
			try {
				mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
			}
			catch (URISyntaxException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");
				return;
			}
		}

		Ontology ontology = db.getOntologyWithExts(mmiUri, null);
		if ( ontology == null ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return;
		}

		File file = UriResolver._getFullPath(ontology, ontConfig, log);
		if ( ! file.canRead() ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return;
		}
		
		String uriFile = file.toURI().toString();
		if ( log.isDebugEnabled() ) {
			log.debug("MdDispatcher.loading model: " +uriFile);
		}
		Model model = JenaUtil.loadModel(uriFile, false);
		
		_dispatchMetadata(request, response, model, completePage, tableClass, tableTitle);
	}

	
	private void _dispatchMetadata(HttpServletRequest request, HttpServletResponse response, 
			Model model, boolean completePage, String tableClass, String tableTitle
	) throws IOException {

		boolean _md = Util.yes(request, "_md");

		MdHelper mdHelper = new MdHelper();
		
		// get attributes from the model:
		mdHelper.updateAttributesFromModel(model);
		
		// display the attributes:
		Collection<AttributeValue> attrs = mdHelper.getAttributes();
		
		// example: groups[DC.NS] == list of DC attributes:
		Map<String,List<AttributeValue>> groups = new LinkedHashMap<String,List<AttributeValue>>();
		for ( AttributeValue attr : attrs ) {
			String ns = attr.getNamespace();
			List<AttributeValue> list = groups.get(ns);
			if ( list == null ) {
				list = new ArrayList<AttributeValue>();
				groups.put(ns, list);
			}
			list.add(attr);
		}
		
		PrintWriter out = response.getWriter();
		
		if ( completePage ) {
			// start the response page:
			response.setContentType("text/html");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>metadata</title>");
			out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
			out.println("</head>");
			out.println("<body>");
		}
		
		// don't show the table if there are no values to show:
		boolean contentsGenerated = false;
		
		for ( String ns : groups.keySet() ) {
			List<AttributeValue> list = groups.get(ns);
			String prefix = MdHelper.getPreferredPrefix(ns);
			
			// don't show the group "header" if there are no associated values to show:
			boolean groupHeaderDone = false;
			
			for ( AttributeValue attr : list ) {
				String lbl = attr.getLabel();
				String val = attr.getValue();
				if ( val.trim().length() > 0 ) {
					
					if ( ! contentsGenerated ) {
						_printPre(out, completePage, _md, tableClass, tableTitle);
						contentsGenerated = true;
					}

					if ( ! groupHeaderDone ) {
						out.println("<tr><td colspan=\"2\" align=\"center\"><label> " +prefix+ " = " +ns+ " </label> </td> </tr>");
						groupHeaderDone = true;
					}
					
					out.printf("<tr><td><label>%s:%s</label></td> <td>%s</td> </tr> %n", prefix, lbl, val);
				}
			}
		}
		if ( contentsGenerated ) {
			_printPos(out, completePage, _md);
		}
		else {
			out.println("<!-- No Ont MD -->");
		}
		
		if ( completePage ) {
			out.println("</body>");
			out.println("</html>");
		}
	}
	
	private static void _printPre(PrintWriter out, 
			boolean completePage, boolean _md, String tableClass, String tableTitle) 
	{
		out.println("\n<!-- begin Ont MD -->");
		if ( completePage || ! _md ) {
			if ( tableClass != null ) {
				out.println("<table class=\"" +tableClass+ "\">");
			}
			else {
				out.println("<table class=\"inline\">");
			}
		}
		
		if ( tableTitle != null ) {
			out.printf("<tr><th colspan=\"2\">%s</th></tr>%n", tableTitle);
		}
		out.println("<tr><th>Attribute</th> <th>Value</th> </tr>");
	}
	
	private static void _printPos(PrintWriter out, boolean completePage, boolean _md) {
		if ( completePage || ! _md ) {
			out.println("</tbody>");
			out.println("</table>");
		}
		out.println("<!-- end Ont MD -->");
	}

}
