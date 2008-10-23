package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.drexel.util.rdf.JenaUtil;


/**
 * Dispatches the metadata output.
 * @author Carlos Rueda
 */
public class HtmlDispatcher {
	
	private final Log log = LogFactory.getLog(HtmlDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	private MdDispatcher mdDispatcher;
	
	
	HtmlDispatcher(OntConfig ontConfig, Db db, MdDispatcher mdDispatcher) {
		this.ontConfig = ontConfig;
		this.db = db;
		this.mdDispatcher = mdDispatcher;
	}


	/** 
	 * Dispatchs the HTML response.
	 * @param request
	 * @param response
	 * @param mmiUri
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	boolean dispatch(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("HtmlDispatcher: starting 'HTML' response.");
		}
		
		boolean debug = Util.yes(request, "_debug");
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		String ontologyUri = mmiUri.getOntologyUri();
		Ontology ontology = db.getOntology(ontologyUri);
		if ( ontology == null ) {
    		// if topic has extension different from ".owl", try with ".owl":
    		if ( ! ".owl".equalsIgnoreCase(mmiUri.getTopicExtension()) ) {
    			String withExt = mmiUri.getOntologyUriWithTopicExtension(".owl");
    			ontology = db.getOntology(withExt);
    			if ( ontology != null ) {
    				ontologyUri = withExt;
    			}
    		}
		}
		
		if ( ontology == null ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}

		String full_path = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY) 
						+ "/" +ontology.file_path + "/" + ontology.filename;
		
		File file = new File(full_path);
		if ( ! file.canRead() ) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, 
					request.getRequestURI()+ ": not found");
			return true;
		}
		
		
		
		_startPage(request, response, fullRequestedUri);

		PrintWriter out = response.getWriter();
		
		String uriFile = file.toURI().toString();
		Model model = JenaUtil.loadModel(uriFile, false);

		if ( mmiUri.getTerm().length() > 0 ) {
			dispatchTerm(request, response, mmiUri, model, false);
		}
		else {
			// start with the metadata:
			mdDispatcher.execute(request, response, mmiUri, false, "inline", "Metadata");
			
			_showAllTerms(mmiUri, model, out, debug);
		}
		
		return true;
	}
	
	
	private void _startPage(HttpServletRequest request, HttpServletResponse response, 
			String fullRequestedUri) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +fullRequestedUri+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
	}


	/** Generated a table with all the terms */
	private void _showAllTerms(MmiUri mmiUri, Model model, PrintWriter out, boolean debug) {
		out.printf(" All subjects in the ontology:<br/>%n"); 
		out.println("<table class=\"inline\" width=\"100%\">");
		out.printf("<tr>%n");
		out.printf("<th>URI</th>");
		out.printf("<th>Resolve</th>");
		if ( debug ) {
			out.printf("<th>_debug</th>");
		}
		//out.printf("<th>Name</th>%n");
		out.printf("</tr>%n");

		ResIterator iter = model.listSubjects();
		while (iter.hasNext()) {
			Resource elem = iter.nextResource();
			String elemUri = elem.getURI();
			if ( elemUri != null ) {
				String elemUriSlash = elemUri.replace('#' , '/');
				
				// generate anchor for the term using "id" in the row: 
				out.printf("<tr id=\"%s\">%n", elem.getLocalName());
				
				// Original URI (may be with # separator):
				out.printf("<td> <a href=\"%s\">%s</a> </td> %n", elemUri, elemUri);
				
				// resolve value with any # replaced with /
				out.printf("<td> <a href=\"%s\">resolve</a> </td> %n", elemUriSlash);
				
				if ( debug ) {
					out.printf("<td> <a href=\"%s?_debug\">_debug</a> </td> %n", elemUriSlash);
				}
				
				//out.printf("<td> %s </td> %n", elem.getLocalName());
				
				out.printf("</tr>%n");
			}
		}
		out.println("</table>");
	}
	
	
	/**
	 * Shows information about the requested term.
	 * @param mmiUri
	 * @param file
	 * @param out
	 * @param completePage 
	 * @throws IOException 
	 */
	void dispatchTerm(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, Model model, boolean completePage) throws IOException {
		
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		// construct URI of term.
		// First, try with "#" separator:
		String termUri = mmiUri.getTermUri(true, "#");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			// then, try with "/" separator
			termUri = mmiUri.getTermUri(true, "/");
			termRes = model.getResource(termUri);
		}
		
		PrintWriter out = response.getWriter();
		
		if ( termRes == null ) {
			out.println("   No resource found for URI: " +termUri);
			return;
		}
		
//		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
//		String label = labelRes == null ? null : ""+labelRes.getObject();
		
		if ( completePage ) {
			String fullRequestedUri = request.getRequestURL().toString();
			_startPage(request, response, fullRequestedUri);
		}
		
		out.println("<table class=\"inline\" width=\"100%\">");
		out.printf("<tr><th>%s</th></tr> %n", termRes);
		out.println("</table>");

		if ( true ) { // get all statements about the term
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			if (iter.hasNext()) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
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
						out.printf("<td><a href=\"%s\">%s</a></td>", prdUri, prdUri);
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
						out.printf("<td><a href=\"%s\">%s</a></td>", objUri, objUri);
					}
					else {
						out.printf("<td>%s</td>", obj.toString());
					}
					
					out.printf("</tr>%n");
				}
				
				out.println("</table>");
			}
		}
		
		if ( true ) { // test for subclasses
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
				out.printf("<tr>%n");
				out.printf("<th>Subclasses</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					out.printf("<tr>%n");
					
					Resource sjt = sta.getSubject();
					String sjtUri = sjt.getURI();

					if ( sjtUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", sjtUri, sjtUri);
					}
					else {
						out.printf("<td>%s</td>", sjt.toString());
					}

					out.printf("</tr>%n");
				}
				out.println("</table>");
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			if ( iter.hasNext() ) {
				out.println("<br/>");
				out.println("<table class=\"inline\" width=\"100%\">");
				out.printf("<tr>%n");
				out.printf("<th>Individuals</th>");
				out.printf("</tr>%n");
				while ( iter.hasNext() ) {
					Resource idv = (Resource) iter.next();
					
					out.printf("<tr>%n");
					
					String idvUri = idv.getURI();
					
					if ( idvUri != null ) {
						out.printf("<td><a href=\"%s\">%s</a></td>", idvUri, idvUri);
					}
					else {
						out.printf("<td>%s</td>", idv.toString());
					}
					
					out.printf("</tr>%n");
				}
			}
		}
		
	}		


}
