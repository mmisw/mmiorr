package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.drexel.util.rdf.JenaUtil;


/**
 * Dispatches the HTML output.
 * 
 * @author Carlos Rueda
 */
public class HtmlDispatcher {
	
	private final Log log = LogFactory.getLog(HtmlDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	
	// TODO This is hard-coded here:  http://mmisw.org/ont/
	private String servedRoot = "http://mmisw.org/ont/"; 
	
	HtmlDispatcher(OntConfig ontConfig, Db db) { // TODO Remove, MdDispatcher mdDispatcher) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	void init() {
	}

	/** 
	 * Dispatchs the HTML response.
	 * @param request
	 * @param response
	 * @param mmiUri
	 * @param ontology If not null, it will be used; otherwise it will try to
	 *           get the ontology by the given mmiUri
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	boolean dispatch(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, boolean unversionedRequest, Ontology ontology) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("HtmlDispatcher: starting 'HTML' response.");
		}
		
		boolean debug = Util.yes(request, "_htmldebug");
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		String foundUri = mmiUri.toString();
		
		if ( ontology == null ) {
			String[] foundUri_ = { null };
			ontology = db.getOntologyWithExts(mmiUri, foundUri_);
			if ( ontology == null ) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");
				return true;
			}
			
			foundUri = foundUri_[0];
		}

		File file = OntServlet.getFullPath(ontology, ontConfig, log);
		
		if ( ! file.canRead() ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}
		
		
		
		_startPage(request, response, fullRequestedUri);

		PrintWriter out = response.getWriter();
		
		
		// original model:
		OntModel model = null;
		
		// corresponding unversioned model in case is requested: 
		OntModel unversionedModel = null;
		
		String uriFile = file.toURI().toString();

		if ( unversionedRequest ) {
			
			model = JenaUtil.loadModel(uriFile, false);

			unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
			
			if ( unversionedModel != null ) {
				// but put both variables to the same unversioned model
				model = unversionedModel;
				if ( log.isDebugEnabled() ) {
					log.debug("dispatch: using obtained unversioned model");
				}
			}
			else {
				// error in conversion to unversioned version.
				// this is unexpected. 
				// Continue with original model, if necessary; see below.
				log.error("dispatch: unexpected: error in conversion to unversioned version.  But continuing with original model");
			}
		}
		else {
			model = JenaUtil.loadModel(uriFile, false);
		}
		

		if ( mmiUri.getTerm().length() > 0 ) {
			dispatchTerm(request, response, mmiUri, model, false);
		}
		else {
			_showAllTerms(mmiUri, foundUri, model, out, debug);
		}
		_endPage(request, response);
		
		return true;
	}
	
	
	private void _startPage(HttpServletRequest request, HttpServletResponse response, 
			String fullRequestedUri) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +fullRequestedUri+ "</title>");
		String contextPath = request.getContextPath();
		out.println("<link rel=stylesheet href=\"" +contextPath+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		
		
		out.println(
				"<div align=\"right\">" +
				"<table>" +
				"<tr valign=\"right\">" +
				"<td align=\"right\">" +
					"<a target=\"_blank\" href=\"http://marinemetadata.org/semanticframework\">" +
					"<img src=\"" +contextPath + "/img/" +"semantic_framework.jpg" + "\" border=\"0\"" +
							"alt=\"MMI Semantic Framework\"/>" +
					"</a>" +
				"</td>" +
				"<td>" +
					"<b>" +OntServlet.TITLE+ "</b>" +
					"<br/>" +
					"<font size=\"-1\">" +
					"This service is part of the " +
						"<a target=\"_blank\" href=\"http://marinemetadata.org/semanticframework\">" +
						"MMI Semantic Framework</a>" +
					"<br/>" +
					"<a target=\"_blank\" href=\"http://mmisw.org/or\">" +
						"MMI Ontology Registry and Repository</a>" +
					"</font>" +
				"</td>" +
				"</tr>" +
				"</table>" +
				"</div>" +
				"<hr/>"
		);
	}

	private void _endPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		out.println(
				"<hr/>" +
				"<div align=\"right\">" +
					"<font color=\"gray\" size=\"-2\">" +OntServlet.FULL_TITLE+ "</font>" +
				"</div>"
		);
	}

	
	/**
	 * Appends ".html" to the given URI if resolvable by this service.
	 * This means that, if:
	 * <ul>
	 *     <li> the uri is a valid MmiUri, and </li>
	 *     <li> the prefix until the root "belongs" to this service</li>
	 * </ul>
	 * then, ".html" is appended and returned. If the URI corresponds to a term, a slash is used as separator.
	 * Otherwise, the argument is returned unchanged.
	 * 
	 * @param uri A URI
	 * @return The URI with ".html" appended according to the above.
	 *         Otherwise, it returs the given URI unmodified.
	 */
	private String appendHtmlIfResolvableByThisService(String uri) {
		
		try {
			MmiUri mmiUri = new MmiUri(uri);
			String untilRoot = mmiUri.getUntilRoot();
			if ( untilRoot.equalsIgnoreCase(servedRoot) ) {
				uri = mmiUri.getTermUri() + ".html";
			}
		}
		catch (URISyntaxException e) {
			// ignore.
		}

		return uri;
	}

	/** 
	 * Generates a table with all the terms. 
	 * @param foundUri 
	 */
	private void _showAllTerms(MmiUri mmiUri, String foundUri, Model model, PrintWriter out, boolean debug) {
		
		out.printf("<div align=\"center\">%n"); 
		out.printf(" All subjects in ontology: " +foundUri+ "<br/>%n"); 
		out.println("<table class=\"inline\">");
		out.printf("<tr>%n");
		
		if ( debug ) {
			out.printf("<th>original element's URI</th>");
		}

		out.printf("<th>URI</th>");
		
		//out.printf("<th>Name</th>%n");
		out.printf("</tr>%n");

		ResIterator iter = model.listSubjects();
		while (iter.hasNext()) {
			Resource elem = iter.nextResource();
			String elemUri = elem.getURI();
			if ( elemUri == null ) {
				continue;
			}
			
			// generate anchor for the term using "id" in the row: 
			out.printf("<tr id=\"%s\">%n", elem.getLocalName());

			if ( debug ) {
				// original element's URI:
				out.printf("<td> <a href=\"%s\">%s</a> </td> %n", elemUri, elemUri);
			}

			out.printf("<td> <a href=\"%s\">%s</a> </td> %n", 
					appendHtmlIfResolvableByThisService(elemUri), elemUri);

			
			//out.printf("<td> %s </td> %n", elem.getLocalName());

			out.printf("</tr>%n");
		}
		out.println("</table>");
		out.printf("</div>%n");
	}
	
	
	/**
	 * Shows information about the requested term.
	 * @param request
	 * @param response
	 * @param mmiUri The URI of the requested term.
	 * @param model
	 * @param completePage 
	 * @throws IOException 
	 */
	void dispatchTerm(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, Model model, boolean completePage) throws IOException {
		
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		// get URI of term
		final String termUri = mmiUri.getTermUri("/");
		Resource termRes = null;
		
		// Fix to Issue 101: "Inexistent term is resolved"
		// Use an explicit "contains" check:
		if ( model.contains(ResourceFactory.createResource(termUri), (Property) null, (RDFNode) null) ) {
			termRes = model.getResource(termUri);
		}
		// Also, return 404 if not found:
		if ( termRes == null ) {
			log.debug("dispatchTerm: " +termUri+ ": Not Found");
			response.sendError(HttpServletResponse.SC_NOT_FOUND, termUri);
			return;
		}
		
		PrintWriter out = response.getWriter();
		
		if ( log.isDebugEnabled() ) {
			log.debug("dispatchTerm: termUri: " +termUri);
		}

		
//		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
//		String label = labelRes == null ? null : ""+labelRes.getObject();
		
		if ( completePage ) {
			String fullRequestedUri = request.getRequestURL().toString();
			_startPage(request, response, fullRequestedUri);
		}
		
		out.printf("<div align=\"center\">%n");
		out.println("<table class=\"inline\">");
		out.printf("<tr><th>%s</th></tr> %n", termUri);
		out.println("</table>");
		out.printf("</div>%n");
		

		if ( true ) { // get all statements about the term
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			if (iter.hasNext()) {
				out.println("<br/>");
				out.printf("<div align=\"center\">%n");
				out.println("<table class=\"inline\" >");
				out.printf("<tr><th colspan=\"2\">%s</th></tr> %n", "Statements");

				out.printf("<tr>%n");
				out.printf("<th>%s</th>", "Predicate");
				out.printf("<th>%s</th>", "Object");
				out.printf("</tr>%n");
				
				
				
				while (iter.hasNext()) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					out.printf("<tr>%n");
							
					Property prd = sta.getPredicate();
					String prdUri = prd.getURI();
					if ( prdUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", 
								appendHtmlIfResolvableByThisService(prdUri), prdUri);
					}
					else {
						out.printf("<td>%s</td>", prd.toString());
					}
					
					RDFNode obj = sta.getObject();
					String objUri = null;
					if ( obj instanceof Resource ) {
						Resource objRes = (Resource) obj;
						objUri = objRes.getURI();
					}
					if ( objUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", 
								appendHtmlIfResolvableByThisService(objUri), objUri);
					}
					else {
						assert obj instanceof Literal ;
						out.printf("<td>%s</td>", obj.toString());
					}
					
					out.printf("</tr>%n");
				}
				
				out.println("</table>");
				out.printf("</div>%n");
			}
		}
		
		if ( true ) { // test for subclasses
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				out.println("<br/>");
				out.printf("<div align=\"center\">%n");
				out.println("<table class=\"inline\">");
				out.printf("<tr>%n");
				out.printf("<th>Subclasses</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					out.printf("<tr>%n");
					
					Resource sjt = sta.getSubject();
					String sjtUri = sjt.getURI();

					if ( sjtUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", 
								appendHtmlIfResolvableByThisService(sjtUri), sjtUri);
					}
					else {
						out.printf("<td>%s</td>", sjt.toString());
					}

					out.printf("</tr>%n");
				}
				out.println("</table>");
				out.printf("</div>%n");
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			if ( iter.hasNext() ) {
				out.println("<br/>");
				out.printf("<div align=\"center\">%n");
				out.println("<table class=\"inline\">");
				out.printf("<tr>%n");
				out.printf("<th>Individuals</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					Resource idv = (Resource) iter.next();
					
					out.printf("<tr>%n");
					
					String idvUri = idv.getURI();
					
					if ( idvUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", 
								appendHtmlIfResolvableByThisService(idvUri), idvUri);
					}
					else {
						out.printf("<td>%s</td>", idv.toString());
					}
					
					out.printf("</tr>%n");
				}
				out.println("</table>");
				out.printf("</div>%n");
			}
		}
		
		if ( completePage ) {
			_endPage(request, response);
		}
		
	}		


}
