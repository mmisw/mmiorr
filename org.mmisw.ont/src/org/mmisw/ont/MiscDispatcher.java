package org.mmisw.ont;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
 * Helper class to dispatch some miscelaneous, ad hoc requests.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class MiscDispatcher {
	
	private final Log log = LogFactory.getLog(MiscDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	
	MiscDispatcher(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	/**
	 * List the registered ontologies.
	 */
	void listOntologies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection _con = null;
		try {
			_con = db.getConnection();
			Statement _stmt = _con.createStatement();
			String table = "v_ncbo_ontology";
			int limit = Integer.parseInt(Util.getParam(request, "limit", "500"));

			String query = 
				"select id, ontology_id, user_id, urn " +
				"from " +table+ "  limit " +limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<head> <link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\"> </head>");
	        out.println("<title>" +query+ "</title>");
	        out.println("</head>");
	        out.println("<body>");
	        out.println("<code>" +query+ "</code>");
	        out.println("<table class=\"inline\" width=\"100%\">");

			
	        ResultSetMetaData md = rs.getMetaData();
	        int cols = md.getColumnCount();
	        int idxUrn = -1;
	        out.println("<tr>");
	        for (int i = 0; i < cols; i++) {
	        	out.println("<th>");
	        	String colLabel = md.getColumnLabel(i+1);
				out.println(colLabel );
	            out.println("</th>");
	            if ( "urn".equals(colLabel) ) {
	            	idxUrn = i;
	            }
	        }
	        out.println("</tr>");

	        while ( rs.next() ) {
	        	out.println("<tr>");
	        	for (int i = 0; i < cols; i++) {
		        	out.println("<td>");
		        	Object val = rs.getObject(i+1);
		        	if ( val != null ) {
		        		if ( idxUrn == i ) {
		        			out.println("<a href=\"" +val+ "\">" +val+ "</a>");
		        		}
		        		else {
		        			out.println(val);
		        		}
		        	}
		            out.println("</td>");
	        	}
	        	out.println("</tr>");
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
	}

	/**
	 * List all vocabularies (not mappings)
	 */
	void listVocabularies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean unverAndVer = Boolean.valueOf(Util.getParam(request, "uv", "false"));
		String limit = Util.getParam(request, "limit", "");
		if ( limit.length() > 0 ) {
			limit = " limit " +limit;
		}
		String table = "v_ncbo_ontology";
		
		Connection _con = null;
		try {
			_con = db.getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select urn, display_label " +
				"from " +table+ limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/plain");
	        PrintWriter out = response.getWriter();

	        while ( rs.next() ) {
	        	String ontologyUri = rs.getString(1);
	        	String display_label = rs.getString(2);
	        	
	        	try {
	        		MmiUri mmiUri = new MmiUri(ontologyUri);

	        		// discard mapping ontologies:
	        		String topic = mmiUri.getTopic().toLowerCase();
	        		if ( topic.matches(".*_map($|_.*)") ) {
	        			// discard mapping ontology
	        			System.out.println("_doListVocabularies: mapping ontology discarded");
	        			continue;
	        		}

	        		String unversionedOntologyUri = mmiUri.copyWithVersion(null).toString();

	        		// always add unversioned one:
	        		out.println(unversionedOntologyUri+ " , " +display_label);
	        		
	        		if ( unverAndVer ) {    
	        			// add also the versioned one:
	        			out.println(ontologyUri+ " , " +display_label);
	        		}
	        	}
	    		catch (URISyntaxException e) {
	    			// Shouldn't happen.
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    			continue;
	    		}
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
	}

	/**
	 * List all mappings.
	 */
	void listMappings(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		boolean unverAndVer = Boolean.valueOf(Util.getParam(request, "uv", "false"));
		String limit = Util.getParam(request, "limit", "");
		if ( limit.length() > 0 ) {
			limit = " limit " +limit;
		}
		String table = "v_ncbo_ontology";
		
		Connection _con = null;
		try {
			_con = db.getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select urn, display_label " +
				"from " +table+ limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/plain");
	        PrintWriter out = response.getWriter();

	        while ( rs.next() ) {
	        	String ontologyUri = rs.getString(1);
	        	String display_label = rs.getString(2);
	        	
	        	try {
	        		MmiUri mmiUri = new MmiUri(ontologyUri);

	        		// only mapping ontologies:
	        		String topic = mmiUri.getTopic().toLowerCase();
	        		if ( ! topic.matches(".*_map($|_.*)") ) {
	        			// discard non-mapping ontology
	        			System.out.println("_doListMappings: non-mapping ontology discarded");
	        			continue;
	        		}

	        		String unversionedOntologyUri = mmiUri.copyWithVersion(null).toString();

	        		// always add unversioned one:
	        		out.println(unversionedOntologyUri+ " , " +display_label);
	        		
	        		if ( unverAndVer ) {    
	        			// add also the versioned one:
	        			out.println(ontologyUri+ " , " +display_label);
	        		}
	        	}
	    		catch (URISyntaxException e) {
	    			// Shouldn't happen.
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    			continue;
	    		}
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
	}

	
	/**
	 * Helper method to dispatch a "_lpath" request.
	 * The dispatch is always completed here.
	 */
	void resolveGetLocalPath(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveGetPath: starting '_path' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/plain");
		out = response.getWriter();
		
		// parse the given URI:
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri);
		}
		catch (URISyntaxException e) {
			out.println("ERROR: " +e.getReason());
			return;
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		// obtain info about the ontology:
		String[] foundUri = { null };
    	Ontology ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
    	if ( ontology == null ) {
    		out.println("ERROR: " +ontologyUri+ ": Not found.");
    		return;
    	}

		
		// just return the path, without any further checks:
    	File file = UriResolver._getFullPath(ontology, ontConfig, log);

    	out.println(file.getAbsolutePath());
	}
	

	/**
	 * Helper method to dispatch a "_versions" request.
	 * The dispatch is always completed here.
	 */
	void resolveGetVersions(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveGetVersions: starting '_versions' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/plain");
		out = response.getWriter();
		
		// parse the given URI:
		MmiUri mmiUri;
		try {
			mmiUri = new MmiUri(fullRequestedUri);
		}
		catch (URISyntaxException e) {
			out.println("ERROR: " +e.getReason());
			return;
		}
		
		List<Ontology> onts = db.getOntologyVersions(mmiUri);
		
		for ( Ontology ontology : onts ) {
			
			// report the URI:
			out.println(ontology.getUri());
			
		}
	}
	

	/**
	 * Helper method to dispatch a "_debug" request.
	 * The dispatch is always completed here.
	 */
	void resolveUriDebug(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriInfo: starting '_debug' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +UriResolver.FULL_TITLE+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<b>" +UriResolver.FULL_TITLE+ "</b><br/><br/>");
		out.println(" Full requested URI: <code>" + fullRequestedUri + "</code> <br/><br/>");
		
		// parse the given URI:
		MmiUri mmiUri;
		try {
			mmiUri = new MmiUri(fullRequestedUri);
		}
		catch (URISyntaxException e) {
			out.println("<font color=\"red\">ERROR: " +e.getReason()+ "</font><br/>");
			out.println("</body>");
			out.println("</html>");
			return;
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		// show the parse result:
		String authority = mmiUri.getAuthority();
		String topic = mmiUri.getTopic();
		String version = mmiUri.getVersion();
		String term = mmiUri.getTerm();
		out.println("Parse result: OK<br/>");
		out.println("<pre>");
		out.println("       Ontology URI: " + ontologyUri);
		out.println("          authority: " + authority);
		out.println("            version: " + (version != null ? version : "(not given)"));
		out.println("              topic: " + topic);
		out.println("               Term: " + term);
		out.println("</pre>");

		
		// report something about the available versions:
		out.println("Available versions:");
		out.println("<pre>");
		for ( Ontology ont : db.getOntologyVersions(mmiUri) ) {
			out.println("   " +ont.getUri());
		}
		out.println("</pre>");
		
		
		
		// obtain info about the ontology:
		String[] foundUri = { null };
    	Ontology ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
    	out.println("<br/>Database result:<br/> ");
		
    	if ( ontology != null ) {
			out.println(foundUri[0]+ ": <font color=\"green\">Found.</font> <br/>");
    	}
    	else {
    		out.println(ontologyUri+ ": <font color=\"red\">Not found.</font> <br/>");
    		out.println("</body>");
    		out.println("</html>");
    		return;
    	}

		
		// prepare info about the path to the file on disk:
    	File file = UriResolver._getFullPath(ontology, ontConfig, log);

		// report the db info and whether the file can be read or not:
		out.println(" Ontology entry FOUND: <br/>");
		out.println("<pre>");
		out.println("                 id: " + ontology.id);
		out.println("        ontology_id: " + ontology.ontology_id);
		out.println("          file_path: " + ontology.file_path);
		out.println("           filename: " + ontology.filename);
		out.println("</pre>");
		out.println(" Full path: <code>" + file.getAbsolutePath() + "</code> ");
		out.println(" Can read it: <code>" + file.canRead() + "</code> <br/>");

		if ( file.canRead() ) {
			out.println("<br/>");

			String uriFile = file.toURI().toString();
			Model model = JenaUtil.loadModel(uriFile, false);

			if ( mmiUri.getTerm().length() > 0 ) {
				_showTermInfo(mmiUri, model, out);
			}
			else {
				_showAllTerms(mmiUri, model, out, true);
			}
		}
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
	 */
	private void _showTermInfo(MmiUri mmiUri, Model model, PrintWriter out) {
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		// construct URI of term.
		// First, try with "/" separator:
		String termUri = mmiUri.getTermUri("/");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			out.println("<br/>Term URI: " +termUri+ " Not found");
			// then, try with "#" separator
			termUri = mmiUri.getTermUri("#");
			termRes = model.getResource(termUri);
		}
		
		if ( termRes == null ) {
			out.println("<br/>Term URI: " +termUri+ " Not found");
			out.println("   No resource found for URI: " +termUri);
			return;
		}
		else {
			out.println("<br/>Term URI: " +termUri+ " FOUND");
		}
		
//		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
//		String label = labelRes == null ? null : ""+labelRes.getObject();
		
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
