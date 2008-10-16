package org.mmisw.ont;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
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
	
	
	private static final String VERSION = "0.1.0 (2008-10-16)";
	private static final String TITLE = "MMI Ontology URI resolver. Version " +VERSION;


	private static final String SPARQL_EXAMPLE = "CONSTRUCT  { ?s ?p ?o } where{?s ?p ?o. } LIMIT 20";

	
	private final Log log = LogFactory.getLog(UriResolver.class);

	public void init() throws ServletException {
		log.info(TITLE+ ": initializing");
		try {
			Db.init();
		} 
		catch (Exception e) {
			throw new ServletException("Cannot init db", e);
		}
	
		OntGraph.initRegistry();
		
//		String logFilePath = getServletContext().getInitParameter("ont.app.logfilepath");
//		log.info("logFilePath = " +logFilePath);
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// first, see if there are any testing requests to dispatch 
		
		// show request info?
		if ( _yes(request, "showreq")  ) {
			_showReq(request, response);
		} 
		
		// dispatch list of ontologies?
		else if ( _yes(request, "list")  ) {
			_doListOntologies(request, response);
		}
		
		// dispatch a sparql-query?
		else if ( _yes(request, "sparql")  ) {
			_doSparqlQuery(request, response);
		}
		
		
		// reload graph?
		else if ( _yes(request, "_reload")  ) {
			OntGraph.reInitRegistry();
		}
		
		// dispatch a db-query?
		else if ( _yes(request, "dbquery")  ) {
			_doDbQuery(request, response);
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
			if ( !file.exists() || !file.canRead() || file.isDirectory() ) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, 
						request.getRequestURI()+ ": not found");
				return;
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
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		// if the "info" parameter is included, show some info about the URI parse
		// and the ontology from the database (but do not serve the contents)
		boolean info = _yes(request, "info");
		PrintWriter out = null;    // only used iff info == true.
		
		if ( info ) {
			// start the response page:
			response.setContentType("text/html");
			out = response.getWriter();
	        out.println("<html>");
	        out.println("<head>");
	        out.println("<title>" +TITLE+ "</title>");
	        out.println("</head>");
	        out.println("<body>");
			out.println("<b>" +TITLE+ "</b><br/><br/>");
			out.println(" Full requested URI: <code>" + fullRequestedUri + "</code> <br/><br/>");
		}
		
		// parse the given URI:
		final String requestedUri = request.getRequestURI();
		final String contextPath = request.getContextPath();
		MmiUri mmiUri = null;
		try {
			mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
		}
		catch (URISyntaxException e) {
			if ( info ) {
				out.println("<font color=\"red\">ERROR: " +e.getReason()+ "</font><br/>");
		        out.println("</body>");
		        out.println("</html>");
				return true;   // dispatched here.
			}
			return false;   // not dispatched here.
		}
		
		String ontologyUri = mmiUri.getOntologyUri();
	
		if ( info  ) {
			// show the parse result:
			String authority = mmiUri.getAuthority();
			String topic = mmiUri.getTopic();
			String term = mmiUri.getTerm();
			out.println("<br/>Parse result: OK<br/>");
			out.println("<pre>");
			out.println("       Ontology URI: " + ontologyUri);
			out.println("          authority: " + authority);
			out.println("              topic: " + topic);
			out.println("               Term: " + term);
			out.println("</pre>");
		}

		// obtain info about the ontology:
    	Ontology ontology = Db.getOntology(ontologyUri);
		
		if ( ontology == null ) {
			if ( info  ) {
				out.println("<br/>Database result: ");
				out.println("<font color=\"red\">ERROR: Ontology not found by the given URI.</font> <br/>");
		        out.println("</body>");
		        out.println("</html>");
				return true;    // dispatched.
			}
			else {
				return false;   // not dispatched here.
			}
		}
		
		// prepare info about the path to the file on disk:
		String full_path = "/Users/Shared/bioportal/resources/uploads/" 
			+ontology.file_path + "/" + ontology.filename;
		File file = new File(full_path);

		if ( info  ) {
			// report the db info and whether the file can be read or not:
			out.println("<br/>Database result: ");
			out.println(" Ontology entry FOUND: <br/>");
			out.println("<pre>");
			out.println("                 id: " + ontology.id);
			out.println("        ontology_id: " + ontology.ontology_id);
			out.println("          file_path: " + ontology.file_path);
			out.println("           filename: " + ontology.filename);
			out.println("</pre>");
			out.println(" Full path: <code>" + full_path + "</code> <br/>");
			out.println(" Can read full path: <code>" + file.canRead() + "</code> <br/>");
			
			_showTermInfo(mmiUri, file, out);
		}
		else {
			if ( file.canRead() ) {
				// respond with the contents of the file with contentType set to RDF+XML 
				response.setContentType("Application/rdf+xml");
				FileInputStream is = new FileInputStream(file);
				ServletOutputStream os = response.getOutputStream();
				IOUtils.copy(is, os);
				os.close();
			}
			else {
				// respond with a NotFound error:
				String msg = full_path+ ": internal error: uploaded file ";
				msg += file.exists() ? "exists but cannot be read." : "not found.";
				msg += "Please, report this bug.";
				response.sendError(HttpServletResponse.SC_NOT_FOUND, msg); 
			}
		}
		
		return true;   // dispatched here.
	}
	

	/**
	 * Shows information about the requested term, if any.
	 * @param mmiUri
	 * @param file
	 * @param out
	 */
	private void _showTermInfo(MmiUri mmiUri, File file, PrintWriter out) {
		String term = mmiUri.getTerm();
		if ( term.length() == 0 || ! file.canRead() ) {
			return;  // nothing to show
		}

		// first, load file:
		String uriFile = file.toURI().toString();
		Model model = _loadModel(uriFile);

		// construct URI of term:
		String termUri = mmiUri.getTermUri(true, "#");
		Resource termRes = model.getResource(termUri);

		if ( termRes == null ) {
			termUri = mmiUri.getTermUri(true, "/");
			termRes = model.getResource(termUri);
		}
		
		if ( termRes == null ) {
			out.println("   No resource found for URI: " +termUri);
			return;
		}
		
		com.hp.hpl.jena.rdf.model.Statement labelRes = termRes.getProperty(RDFS.label);
		String label = labelRes == null ? "null" : ""+labelRes.getObject();
		
		out.println("<pre>");
		out.println("   term resource: " +termRes);
		out.println("           label: " +label);
		out.println("    getLocalName: " +termRes.getLocalName());
		

		if ( true ) { // get all about the term
			out.println("\n    All about: " +termRes.getURI());
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			while (iter.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
				out.println("      " 
						+PrintUtil.print(sta.getPredicate().getURI())
						+ "   "
						+PrintUtil.print(sta.getObject().toString())
				);
			}
		}
		
		if ( true ) { // test for subclasses
			out.println("\n    subclasses of : " +termRes.getURI());
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			while (iter.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
				out.println(" - " + PrintUtil.print(sta.getSubject().getURI()));
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			out.println("  Individuals:");
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			while ( iter.hasNext() ) {
				Resource indiv = (Resource) iter.next();
				out.println("    " +indiv.getLocalName());
			}
		}
	}		

	/**
	 * Loads a model.
	 * @param uriModel
	 * @return
	 */
	private Model _loadModel(String uriModel) {
		Model model = JenaUtil.loadModel(uriModel, false);
		
		return model;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void _showReq(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Show req</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<pre>");
        
        out.println("request.getRequestURL()         = " + request.getRequestURL()  );
        out.println("request.getRequestURI()         = " + request.getRequestURI()  );
        out.println("request.getQueryString()        = " + request.getQueryString()  );
        
        out.println("request.getParameterMap()       = " + request.getParameterMap()  );
		Map<String, String[]> params = _getParams(request);
		for ( String key: params.keySet() ) {
			out.println("    " +key+ " => " + Arrays.asList(params.get(key))  );	
		}
        
        out.println("request.getContextPath()        = " + request.getContextPath() ); 
        out.println("request.getMethod()             = " + request.getMethod()  ); 
        out.println("request.getPathInfo()           = " + request.getPathInfo()  ); 
        out.println("request.getPathTranslated()     = " + request.getPathTranslated()  ); 
        out.println("request.getRemoteUser()         = " + request.getRemoteUser()  );
        out.println("request.getRequestedSessionId() = " + request.getRequestedSessionId()  );
        out.println("request.getServletPath()        = " + request.getServletPath()  );
        out.println("request.getAttributeNames()     = " + request.getAttributeNames()  ); 
        out.println("request.getCharacterEncoding()  = " + request.getCharacterEncoding()  );
        out.println("request.getContentLength()      = " + request.getContentLength()  );
        out.println("request.getContentType()        = " + request.getContentType()  );
        out.println("request.getProtocol()           = " + request.getProtocol()  );
        out.println("request.getRemoteAddr()         = " + request.getRemoteAddr()  );
        out.println("request.getRemoteHost()         = " + request.getRemoteHost()  ); 
        out.println("request.getScheme()             = " + request.getScheme()  );
        out.println("request.getServerName()         = " + request.getServerName()  );
        out.println("request.getServerPort()         = " + request.getServerPort()  );
        out.println("request.isSecure()              = " + request.isSecure()  ); 
        
        out.println("request. headers             = ");
        Enumeration hnames = request.getHeaderNames();
        while ( hnames.hasMoreElements() ) {
        	Object hname = hnames.nextElement();
        	out.print("        " +hname+ " : ");
        	Enumeration hvals = request.getHeaders(hname.toString());
        	String sep = "";
            while ( hvals.hasMoreElements() ) {
            	Object hval = hvals.nextElement();
				out.println(hval + sep);
				sep = "  ;  ";
            }
            out.println();
        }        

        out.println("</pre>");
        out.println("</body>");
        out.println("</html>");
	}


	
	private void _doDbQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Connection _con = Db.getConnection();
			Statement _stmt = _con.createStatement();
			String table = _getParam(request, "table", "ncbo_ontology");
			int limit = Integer.parseInt(_getParam(request, "limit", "500"));

			String query = "select * from " +table+ "  limit " +limit;
			
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
	        out.println("<tr>");
	        for (int i = 0; i < cols; i++) {
	        	out.println("<th>");
	        	out.println(md.getColumnLabel(i+1));
	            out.println("</th>");
	        }
	        out.println("</tr>");

	        while ( rs.next() ) {
	        	out.println("<tr>");
	        	for (int i = 0; i < cols; i++) {
		        	out.println("<td>");
		        	out.println(rs.getObject(i+1));
		            out.println("</td>");
	        	}
	        	out.println("</tr>");
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private void _doSparqlQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = _getParam(request, "sparql", "");
		if ( query.length() == 0 ) {
			query = SPARQL_EXAMPLE;
		}
		String result = OntGraph.getRDF(query);
		
		response.setContentType("Application/rdf+xml");
		StringReader is = new StringReader(result);
		ServletOutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		os.close();
	}
	
	private void _doListOntologies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Connection _con = Db.getConnection();
			Statement _stmt = _con.createStatement();
			String table = "v_ncbo_ontology";
			int limit = Integer.parseInt(_getParam(request, "limit", "500"));

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
	
	
	@SuppressWarnings("unchecked")
	private Map<String, String[]> _getParams(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		return params;
	}

	/** @returns true iff the given param is defined in the request
	 * AND either no value is associated OR none of the values is equal to "n".
	 */
	private boolean _yes(HttpServletRequest request, String param) {
		List<String> values = _paramValues(request, param);
		return values != null && ! values.contains("n");
	}

	/** @returns the list of values associated to the given param.
	 * null if the param is not included in the request.
	 */
	private List<String> _paramValues(HttpServletRequest request, String param) {
		Map<String, String[]> params = _getParams(request);
		String[] vals = params.get(param);
		if ( null == vals ) {
			return null;
		}
		List<String> list = Arrays.asList(params.get(param));
		return list;
	}

	private String _getParam(HttpServletRequest request, String param, String defaultValue) {
		Map<String, String[]> params = _getParams(request);
		String[] array = params.get(param);
		if ( array == null || array.length == 0 ) {
			return defaultValue;
		}
		// return last value in the array: 
		return array[array.length -1];
	}

}
