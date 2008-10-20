package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.sparql.SparqlDispatcher;
import org.mmisw.ont.util.Accept;
import org.mmisw.ont.util.Util;
import org.mmisw.ont.util.Accept.Entry;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.drexel.util.rdf.JenaUtil;


/**
 * The "ont" service to resolve ontology URIs.
 * 
 * @author Carlos Rueda
 */
public class UriResolver extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
	private static final String VERSION = "0.1.1 (20081018)";
	private static final String TITLE = "MMI Ontology URI resolver. Version " +VERSION;

	private final Log log = LogFactory.getLog(UriResolver.class);

	private final OntConfig ontConfig = new OntConfig();
	private final Db db = new Db(ontConfig);
	private final OntGraph ontGraph = new OntGraph(ontConfig, db);
	
	private final SparqlDispatcher sparqlDispatcher = new SparqlDispatcher(ontGraph);

	
	private enum OntFormat { RDFXML, N3 };

	public void init() throws ServletException {
		log.info(TITLE+ ": initializing");
		
		try {
			ontConfig.init(getServletConfig());
			db.init();
			ontGraph.init();
		} 
		catch (Exception ex) {
			log.error("Cannot initialize: " +ex.getMessage(), ex);
			throw new ServletException("Cannot initialize", ex);
		}
	}
	
	public void destroy() {
		log.info(TITLE+ ": destroy called.\n\n");
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// first, see if there are any testing requests to dispatch 
		
		// show request info?
		if ( Util.yes(request, "showreq")  ) {
			Util.showReq(request, response);
		} 
		
		// dispatch list of ontologies?
		else if ( Util.yes(request, "list")  ) {
			_doListOntologies(request, response);
		}
		
		// dispatch a sparql-query?
		else if ( Util.yes(request, "sparql")  ) {
			sparqlDispatcher.execute(request, response);
		}
		
		
		// reload graph?
		else if ( Util.yes(request, "_reload")  ) {
			ontGraph.reinit();
		}
		
		// dispatch a db-query?
		else if ( Util.yes(request, "dbquery")  ) {
			Util.doDbQuery(request, response, db);
		}
		
		// resolve URI?
		else if ( _resolveUri(request, response)  ) {
			// OK, no more to do here.
		}
		
		// Else, try to resolve the requested resource.
		// Note, since I'm using <url-pattern>/*</url-pattern> in web.xml, *everything* 
		// gets dispatched through this servlet, so I have to resolve other possible resources.
		else {
			String path = request.getPathTranslated();
			File file = new File(path);
			if ( !file.canRead() || file.isDirectory() ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Other resource: " +path+ ": not found or cannot be read");
				}
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");

				return;
			}
			
			String mime = getServletContext().getMimeType(path);
			if ( mime != null ) {
				response.setContentType(mime);
			}
			
			if ( log.isDebugEnabled() ) {
				log.debug(path+ ": FOUND. " +
						(mime != null ? "Mime type set to: " +mime : "No Mime type set.")
				);
			}

			FileInputStream is = new FileInputStream(file);
			ServletOutputStream os = response.getOutputStream();
			IOUtils.copy(is, os);
			os.close();
		}
	}

	/**
	 * Resolves the ontology identified by its URI as indicated by <code>request.getRequestURL()</code>.
	 * 
	 * <p>
	 * This is the main operation in this servlet.
	 * 
	 * @param request
	 * @param response
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 * 
	 * @throws ServletException 
	 * @throws IOException 
	 */
	private boolean _resolveUri(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		// if the "info" parameter is included, show some info about the URI parse
		// and the ontology from the database (but do not serve the contents)
		boolean info = Util.yes(request, "info");
		if ( info ) {
			_resolveUriInfo(request, response);
			return true;
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			// Not dispatched here; allow caller to dispatch in any other convenient way:
			if ( log.isDebugEnabled() ) {
				log.debug("MMI URI not well formed: " +e.getMessage());
			}
			return false;   
		}
		
		////////////////////////////////////////////////////////////////////////////////
		//    Dereferencing rules
		////////////////////////////////////////////////////////////////////////////////
		
		// Dereferencing is done according to the "accept" header and the topic extension.

		// The response type depends of the following elements:
		String topicExt = mmiUri.getTopicExtension();
		Accept accept = new Accept(request);
		
		String dominating = accept.getDominating();
		
		if ( log.isDebugEnabled() ) {
			log.debug("===== Starting dereferencing ====== ");
			log.debug("  accept entries:");
			List<Entry> entries = accept.getEntries();
			for ( Entry entry : entries ) {
				log.debug("      " +entry);
			}
			log.debug("  dominating entry: " +dominating);
			log.debug("topicExt = " +topicExt);
		}

		
		if ( topicExt.length() == 0                  // No ontology extension? 
		||   topicExt.equalsIgnoreCase(".owl")       // OR extension is .owl
		||   topicExt.equalsIgnoreCase(".rdf")       // OR extension is .rdf
		) {
			// dereferenced according to content negotiation as:
			
			// (a) an OWL document (if Accept: application/rdf+xml dominates)
			if ( "application/rdf+xml".equalsIgnoreCase(dominating) ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
			}
			
			// (a.1) firefox doesn't explicitly say "application/rdf+xml" and I guess this
			// is also the case with other standard browsers. In particula, my firefox sends:
			//     text/html
			//     application/xhtml+xml	
			//     application/xml; q = 0.9
			//      */*; q = 0.8
			// Since the extension is not ".html", I would consider the following case:
			//
			else if ( accept.contains("application/xml") ) {
				return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
			}
			
			// (b) an HTML document (if Accept: text/html but not application/rdf+xml)
			else if ( accept.contains("text/html") ) {
				
				return _resolveUriHtml(request, response, mmiUri);
			}
			
			// (c) an HTML document (if Accept: text/html, application/rdf+xml or Accept: */*)
			else if ( accept.contains("text/html") ||
					  accept.contains("application/rdf+xml") ||
					  accept.contains("*/*")
			) {
				
				if ( topicExt.equalsIgnoreCase(".owl") ) {
					return _resolveUriOntFormat(request, response, mmiUri, OntFormat.RDFXML);
				}
				else {
					return _resolveUriHtml(request, response, mmiUri);
				}
			}
			
			// (d) an OWL document with referenced style sheet (if no Accept)
			else if ( accept.isEmpty() ) {
				// TODO accept list empty
				// arbitrarely returning in HTML:
				log.warn("Case (d): \"accept\" list is empty. " +
						"'OWL document with referenced style sheet' Not implemented yet." +
						" Returning HTML temporarily.");
				
				return _resolveUriHtml(request, response, mmiUri);
			}
			
			
			// Else: arbitrarely returning in HTML:
			else {
				log.warn("Default case: Returning HTML.");
				return _resolveUriHtml(request, response, mmiUri);
			}
		}
		
		// Else: ontology extension (other than .owl or .rdf) included:
		
		// .html:
		else if ( topicExt.equalsIgnoreCase(".html") ) {
			return _resolveUriHtml(request, response, mmiUri);
		}
			
		// .n3:
		else if ( topicExt.equalsIgnoreCase(".n3") ) {
			return _resolveUriOntFormat(request, response, mmiUri, OntFormat.N3);
		}
			
		// .pdf:
		else if ( topicExt.equalsIgnoreCase(".pdf") ) {
			// TODO .pdf Not implemented yet.
			// arbitrarely returning in HTML:
			log.warn("PDF format requested, but not implemented yet.");
			return false;   // handle this by saying "not dispatched here."
		}
			
		return false;   // not dispatched here.
	}
	
	/**
	 * Helper method to dispatch a request with response in the givenontology format.
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 */
	// TODO dispatch N3 format
	private boolean _resolveUriOntFormat(HttpServletRequest request, HttpServletResponse response, 
			MmiUri mmiUri, OntFormat ontFormat) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriOntFormat: starting response.");
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		// obtain info about the ontology:
    	Ontology ontology = db.getOntology(ontologyUri);
		
    	if ( ontology == null ) {
    		// if topic has no extension, try with ".owl"
    		if ( mmiUri.getTopic().indexOf('.') < 0 ) {
    			String withExt = mmiUri.getOntologyUriWithTopicExtension(".owl");
    			ontology = db.getOntology(withExt);
    			if ( ontology != null ) {
    				ontologyUri = withExt;
    			}
    		}
    	}
    	
		if ( ontology == null ) {
			return false;   // not dispatched here.
		}
		
		// prepare info about the path to the file on disk:
		String full_path = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY) 
			+ "/" +ontology.file_path + "/" + ontology.filename;
		File file = new File(full_path);

		if ( file.canRead() ) {
			String term = mmiUri.getTerm();
			
			// Term included?
			if ( term.length() > 0 ) {
				String uriFile = file.toURI().toString();
				Model model = JenaUtil.loadModel(uriFile, false);

				// TODO "text/html" for now
				String termContents = _resolveTerm(request, mmiUri, model);
				StringReader is = new StringReader(termContents);
				response.setContentType("text/html");
				ServletOutputStream os = response.getOutputStream();
				IOUtils.copy(is, os);
				os.close();
			}
			
			// No term included:
			else {
				// respond with the contents of the file with contentType set to RDF+XML 
				response.setContentType("Application/rdf+xml");
				FileInputStream is = new FileInputStream(file);
				ServletOutputStream os = response.getOutputStream();
				IOUtils.copy(is, os);
				os.close();
			}
		}
		else {
			// This should not happen.
			// Log the error and respond with a NotFound error:
			String msg = full_path+ ": internal error: uploaded file ";
			msg += file.exists() ? "exists but cannot be read." : "not found.";
			msg += "Please, report this bug.";
			log.error(msg, null);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, msg); 
		}
		
		return true;   // dispatched here.
	}


	/**
	 * Helper method to dispatch an "HTML" request.
	 * 
	 * @return true for dispatch completed here; false otherwise.
	 */
	private boolean _resolveUriHtml(HttpServletRequest request, HttpServletResponse response, MmiUri mmiUri) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriHtml: starting response.");
		}
		
		// TODO dispatch HTML response.
		
		// Temporarily using the "?info" strategy for preliminary testing
		if ( true ) {
			_resolveUriInfo(request, response);
		}
		
		return true;
	}

	
	/**
	 * Helper method to dispatch an "?info" request.
	 * The dispatch is always completed here.
	 */
	private void _resolveUriInfo(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveUriInfo: starting 'info' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/html");
		out = response.getWriter();
		out.println("<html>");
		out.println("<head>");
		out.println("<title>" +TITLE+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<b>" +TITLE+ "</b><br/><br/>");
		out.println(" Full requested URI: <code>" + fullRequestedUri + "</code> <br/><br/>");
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
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
		String term = mmiUri.getTerm();
		out.println("Parse result: OK<br/>");
		out.println("<pre>");
		out.println("       Ontology URI: " + ontologyUri);
		out.println("          authority: " + authority);
		out.println("              topic: " + topic);
		out.println("               Term: " + term);
		out.println("</pre>");

		// obtain info about the ontology:
    	Ontology ontology = db.getOntology(ontologyUri);
		
    	out.println("<br/>Database result:<br/> ");
		
    	if ( ontology == null ) {
    		out.println(ontologyUri+ ": <font color=\"red\">Not found.</font> <br/>");

    		// if topic has no extension, try with ".owl"
    		if ( mmiUri.getTopic().indexOf('.') < 0 ) {
    			out.println("Trying with .owl extension... <br/>");

    			String withExt = mmiUri.getOntologyUriWithTopicExtension(".owl");
    			ontology = db.getOntology(withExt);
    			if ( ontology != null ) {
    				out.println(withExt+ ": <font color=\"green\">Found.</font> <br/>");
    				ontologyUri = withExt;
    			}
    			else {
    				out.println(withExt+ ": <font color=\"red\">Not found.</font> <br/>");
    			}
    		}
    	}
    	
		if ( ontology == null ) {
			out.println("</body>");
			out.println("</html>");
			return;
		}
		
		// prepare info about the path to the file on disk:
		String full_path = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY) 
			+ "/" +ontology.file_path + "/" + ontology.filename;
		File file = new File(full_path);

		// report the db info and whether the file can be read or not:
		out.println(" Ontology entry FOUND: <br/>");
		out.println("<pre>");
		out.println("                 id: " + ontology.id);
		out.println("        ontology_id: " + ontology.ontology_id);
		out.println("          file_path: " + ontology.file_path);
		out.println("           filename: " + ontology.filename);
		out.println("</pre>");
		out.println(" Full path: <code>" + full_path + "</code> ");
		out.println(" Can read it: <code>" + file.canRead() + "</code> <br/>");

		if ( file.canRead() ) {
			out.println("<br/>");

			String uriFile = file.toURI().toString();
			Model model = JenaUtil.loadModel(uriFile, false);

			if ( mmiUri.getTerm().length() > 0 ) {
				_showTermInfo(mmiUri, model, out);
			}
			else {
				_showAllTerms(mmiUri, model, out);
			}
		}
	}
	
	
	private void _showAllTerms(MmiUri mmiUri, Model model, PrintWriter out) {
		out.printf(" All subjects in the model:<br/>%n"); 
		out.println("<table class=\"inline\">");
		out.printf("<tr>%n");
		out.printf("<th>Subject</th> <th>Info</th> <th>Name</th>%n");
		out.printf("</tr>%n");

		ResIterator iter = model.listSubjects();
		while (iter.hasNext()) {
			Resource elem = iter.nextResource();
			String elemUri = elem.getURI();
			if ( elemUri != null ) {
				String elemUriSlash = elemUri.replace('#' , '/');
				out.printf("<tr>%n");
				out.printf("<td> <a href=\"%s\">%s</a> </td> %n", elemUriSlash, elemUriSlash); 
				out.printf("<td> <a href=\"%s?info\">info</a> </td> %n", elemUriSlash); 
				out.printf("<td> %s </td> %n", elem.getLocalName()); 
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
		
		// construct URI of term with "#" separator
		String termUri = mmiUri.getTermUri(true, "#");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			// try with "/" separator
			termUri = mmiUri.getTermUri(true, "/");
			termRes = model.getResource(termUri);
		}
		
		if ( termRes == null ) {
			out.println("   No resource found for URI: " +termUri);
			return;
		}
		
		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
		String label = labelRes == null ? null : ""+labelRes.getObject();
		
		out.println("<pre>");
		out.println("   term resource: " +termRes);
		out.println("           label: " +label);
		out.println("    getLocalName: " +termRes.getLocalName());
		

		if ( true ) { // get all about the term
			out.println("\n    All about: " +termRes.getURI());
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			while (iter.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
				out.printf("      %30s   %s%n", 
						PrintUtil.print(sta.getPredicate().getURI()),
						PrintUtil.print(sta.getObject().toString())
				);
			}
		}
		
		if ( true ) { // test for subclasses
			out.println("\n    Subclasses of : " +termRes.getURI());
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					out.println("  " + PrintUtil.print(sta.getSubject().getURI()));
				}
			}
			else {
				out.println("        (none)");
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			out.println("    Individuals:");
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			while ( iter.hasNext() ) {
				Resource indiv = (Resource) iter.next();
				out.println("        " +indiv.getURI());
			}
		}
		
		out.println("</pre>");
	}		

	private String _resolveTerm(HttpServletRequest request, MmiUri mmiUri, Model model) {
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		// construct URI of term with "#" separator
		String termUri = mmiUri.getTermUri(true, "#");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			// try with "/" separator
			termUri = mmiUri.getTermUri(true, "/");
			termRes = model.getResource(termUri);
		}
		
		if ( termRes == null ) {
			return null; // Not found.
		}
		
		StringWriter strWriter = new StringWriter();
		PrintWriter out = new PrintWriter(strWriter);
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" +termUri+ "</title>");
        out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<i>temporary response</i>");
		
		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
		String label = labelRes == null ? null : ""+labelRes.getObject();
		
		out.println("<pre>");
		out.println("   term resource: " +termRes);
		out.println("           label: " +label);
		out.println("    getLocalName: " +termRes.getLocalName());
		

		if ( true ) { // get all about the term
			out.println("\n    All about: " +termRes.getURI());
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			while (iter.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
				out.printf("      %30s   %s%n", 
						PrintUtil.print(sta.getPredicate().getURI()),
						PrintUtil.print(sta.getObject().toString())
				);
			}
		}
		
		if ( true ) { // test for subclasses
			out.println("\n    Subclasses of : " +termRes.getURI());
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					out.println("  " + PrintUtil.print(sta.getSubject().getURI()));
				}
			}
			else {
				out.println("        (none)");
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			out.println("    Individuals:");
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			while ( iter.hasNext() ) {
				Resource indiv = (Resource) iter.next();
				out.println("        " +indiv.getURI());
			}
		}
		
        out.println("</pre>");
        out.println("</body>");
        out.println("</html>");
        
        return strWriter.toString();
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}
	
	
	private void _doListOntologies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Connection _con = db.getConnection();
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
	        out.println("<table class=\"inline\">");

			
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
	}

}
