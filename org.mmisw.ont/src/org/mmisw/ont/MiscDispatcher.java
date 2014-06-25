package org.mmisw.ont;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Helper class to dispatch some miscelaneous, ad hoc requests.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * <p>
 * Thread-safety: This class is not strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers. 
 * 
 * @author Carlos Rueda
 */
public class MiscDispatcher {
	
	private static final String SEP = " , ";
	

	private static final Pattern MAPPING_TOPIC_PATTERN = Pattern.compile("^map_.*|.*_map($|_.*)");
	
	private static boolean _isMappingAccordingToTopic(String topic) {
		Matcher matcher = MAPPING_TOPIC_PATTERN.matcher(topic);
		return null != matcher && matcher.matches();
	}

	private final Log log = LogFactory.getLog(MiscDispatcher.class);
	
	private OntConfig ontConfig;
	private Db db;
	
	MiscDispatcher(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	/**
	 * List the registered ontologies for a given authority.
	 * 
	 * @param request
	 * @param response
	 * @param authority
	 * @param outFormat  TODO use it
	 * 
	 * @return true iff dispatch completed here.
	 */
	boolean listOntologiesForAuthority(HttpServletRequest request, HttpServletResponse response, 
			String authority, String outFormat) 
	throws ServletException, IOException {
		
		// get the list of (latest-version) ontologies:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		
		class Item {
			String uri;
			String name;
			String version;
		}
		
		List<Item> selected = new ArrayList<Item>();
		
		for (OntologyInfo ontology : onts) {
			try {
				MmiUri mmiUri = new MmiUri(ontology.getUri());
				if ( authority.equalsIgnoreCase(mmiUri.getAuthority())) {
					Item item = new Item();
					item.name = ontology.getDisplayLabel();
					item.version = mmiUri.getVersion();
					item.uri = mmiUri.copyWithVersion(null).getOntologyUri();
					selected.add(item);
				}
			}
			catch (URISyntaxException ignore) {
			} 
		}

		log.info("listOntologiesForAuthority: " +authority+ " = " +selected.size()+ " entries.");

		if (selected.size() == 0 ) {
			return false; // not dispatched
		}
		
		// order by descending version
		Collections.sort(selected, new Comparator<Item>() {
			public int compare(Item o1, Item o2) {
				return - o1.version.compareToIgnoreCase(o2.version);
			}
		});
		
		// TODO use the given outForm. For now, always dispatching as html
		
		response.setContentType("text/html");
		response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head> <link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\"> </head>");
        out.println("<title>" +"Authority: " +authority+ "</title>");
        out.println("</head>");
        out.println("<body>");
        
        
        out.println("<br/>");
        out.println("<div align=\"center\">");
        
        
        // ontology table
        
        out.println("<table class=\"inline2\">");
        // header
    	out.println("<tr>");
    	out.println("<td colspan=3 align=\"center\">Registered ontologies under authority: <b>" +authority+ "</b></td>");
        out.println("</tr>");

        out.println("<tr>");
    	out.println("<th>URI</th>");
    	out.println("<th>Name</th>");
    	out.println("<th>Version</th>");
        out.println("</tr>");

        for (Item item : selected) {
        	String displayLabel = item.name;
        	String version = item.version;
        	String unversionedUri = item.uri;
        	
        	out.println("<tr>");
        	
        	out.println("<td>");
			out.println("<a href=\"" +unversionedUri+ "\">" +unversionedUri+ "</a>");
            out.println("</td>");

            out.println("<td>");
            out.println(displayLabel);
            out.println("</td>");

            out.println("<td>");
            out.println(version);
            out.println("</td>");
            
	        out.println("</tr>");
			
		}
        
        out.println("</table>");
        out.println("<font color=\"gray\" size=\"-2\"><br/>" +OntVersion.getFullTitle()+ "</font>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
        
        return true;
	}

	/**
	 * List the registered ontologies.
	 */
	void listOntologies(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();
			String table = "v_ncbo_ontology";
			int limit = Integer.parseInt(Util.getParam(request, "limit", "500"));

			String query = 
				"select id, ontology_id, user_id, urn " +
				"from " +table+ "  limit " +limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/html");
			response.setHeader("Access-Control-Allow-Origin", "*");
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
			db.closeStatementAndConnection(_stmt, _con);
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
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();

			String query = 
				"select urn, display_label " +
				"from " +table+ limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/plain");
			response.setHeader("Access-Control-Allow-Origin", "*");
	        PrintWriter out = response.getWriter();

	        while ( rs.next() ) {
	        	String ontologyUri = rs.getString(1);
	        	String display_label = rs.getString(2);
	        	
	        	try {
	        		MmiUri mmiUri = new MmiUri(ontologyUri);

	        		// discard mapping ontologies:
	        		String topic = mmiUri.getTopic().toLowerCase();
	        		if (_isMappingAccordingToTopic(topic)) {
	        			// discard mapping ontology
	        			if (log.isDebugEnabled()) {
	        				log.debug("listVocabularies: mapping ontology discarded: " + ontologyUri);
	        			}
	        			continue;
	        		}

	        		String unversionedOntologyUri = mmiUri.copyWithVersion(null).toString();

	        		// always add unversioned one:
	        		out.println(unversionedOntologyUri+ SEP +display_label);
	        		
	        		if ( unverAndVer ) {    
	        			// add also the versioned one:
	        			out.println(ontologyUri+ SEP +display_label);
	        		}
	        	}
	    		catch (URISyntaxException e) {
	    			// ignore: ontologyUri is not an MMiUri; besides, the mechanism
	    			// to properly determine the type of vacbulary still needs to be 
	    			// properly implemented.
//	    			log.error("Shouldn't happen", e);
	    			continue;
	    		}
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			db.closeStatementAndConnection(_stmt, _con);
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
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();

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
	        		if (! _isMappingAccordingToTopic(topic)) {
	        			// discard non-mapping ontology
	        			if (log.isDebugEnabled()) {
	        				log.debug("listMappings: non-mapping ontology discarded: " +ontologyUri);
	        			}
	        			continue;
	        		}

	        		String unversionedOntologyUri = mmiUri.copyWithVersion(null).toString();

	        		// always add unversioned one:
	        		out.println(unversionedOntologyUri+ SEP +display_label);
	        		
	        		if ( unverAndVer ) {    
	        			// add also the versioned one:
	        			out.println(ontologyUri+ SEP +display_label);
	        		}
	        	}
	    		catch (URISyntaxException e) {
	    			// ignore: ontologyUri is not an MMiUri; besides, the mechanism
	    			// to properly determine the type of vacbulary still needs to be 
	    			// properly implemented.
//	    			log.error("Shouldn't happen", e);
	    			continue;
	    		}
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			db.closeStatementAndConnection(_stmt, _con);
		}
	}
	
	
	
	
	/**
	 * List all ontologies (versioned form) for use by the orrclient module.
	 */
	void listAll(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // helps propagate version_status from previous version to newer version when undefined in newer one
        Map<String, String> version_statuses = new HashMap<String, String>();

		final String sep = "'|'";
		
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();

			String query = 
				"select o.urn, o.display_label, o.user_id, o.contact_name, o.version_number, " +
				"       o.date_created, u.username, o.ontology_id, o.version_status " +
				"from v_ncbo_ontology o, ncbo_user u " +
				"where o.user_id = u.id " +
				"order by o.ontology_id, o.version_number";
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/plain");
	        PrintWriter out = response.getWriter();

	        while ( rs.next() ) {
	        	String ontologyUri = rs.getString(1);
	        	String display_label = rs.getString(2);
	        	String user_id = rs.getString(3);
	        	String contact_name = rs.getString(4);
	        	String version_number = rs.getString(5);
	        	String date_created = rs.getString(6);
	        	String username = rs.getString(7);
	        	String ontology_id = rs.getString(8);
	        	String version_status = rs.getString(9);
                if (version_status == null || version_status.trim().length() == 0) {
                    if (version_statuses.containsKey(ontology_id)) {
                        version_status = version_statuses.get(ontology_id);
                    }
                    else {
                        version_status = "undefined";
                    }
                }
                version_statuses.put(ontology_id, version_status);

	        	// mapping or vocabulary?
	        	// TODO: a more robust mechanism to determine the type of an ontology. 
	        	
	        	// assume "vocabulary"
	        	String type = "vocabulary";
	        	
	        	try {
	        		MmiUri mmiUri = new MmiUri(ontologyUri);

	        		String topic = mmiUri.getTopic().toLowerCase();
	        		if (_isMappingAccordingToTopic(topic)) {
	        			type = "mapping";
	        		}
	        	}
	    		catch (URISyntaxException e) {
	    			// <re-host> initially, exception ignored as "shouldn't happen"
	    			//
	    			//log.error("Shouldn't happen", e);
	    			// continue;
	    			//
	    			// but now, it could be a re-hosted ontology, so let fall through
	    		}
	    		
	    		// always VERSIONED form:
	    		out.println(
	    				"'"
	    				+ ontologyUri
	    				+ sep +display_label
	    				+ sep +type
	    				+ sep +user_id
	    				+ sep +contact_name
	    				+ sep +version_number
	    				+ sep +date_created
	    				+ sep +username
	    				+ sep +ontology_id
	    				+ sep +version_status
	    				+ "'"
	    		);
	    		
	        }

		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			db.closeStatementAndConnection(_stmt, _con);
		}
	}


	/**
	 * List all ontologies (only latest versions) in json.
	 */
	void listOnts(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get the list of (latest-version) ontologies:
        final boolean allVersions = false;
        List<OntologyInfo> onts = db.getAllOntologies(allVersions);

        List<JSONObject> list = new ArrayList<JSONObject>();

        for (OntologyInfo ontology : onts) {
            Map<String,String> map = new LinkedHashMap<String, String>();
            map.put("uri",     ontology.getUri());
            map.put("name",    ontology.getDisplayLabel());
            map.put("author",  ontology.getAuthor());
            map.put("version", ontology.getVersion());
            map.put("version_status", ontology.getVersionStatus());

            try {
                MmiUri mmiUri = new MmiUri(ontology.getUri());
                map.put("uri",     mmiUri.copyWithVersion(null).clone().getOntologyUri());
            }
            catch (URISyntaxException ignore) {
            }

            JSONObject obj = new JSONObject(map);
            list.add(obj);
        }

        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter out = response.getWriter();
        JSONArray result = new JSONArray(list);
        out.println(result.toString());
	}


	
	/**
	 * Helper method to dispatch a "_lpath" request.
	 * The dispatch is always completed here.
	 */
	void resolveGetLocalPath(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_resolveGetPath: starting '_lpath' response.");
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
    	OntologyInfo ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
    	if ( ontology == null ) {
    		out.println("ERROR: " +ontologyUri+ ": Not found.");
    		return;
    	}

		
		// just return the path, without any further checks:
    	File file = OntServlet.getFullPath(ontology, ontConfig, log);

    	out.println(file.getAbsolutePath());
	}
	

	/**
	 * Helper method to report the version of this service
	 * The dispatch is always completed here.
	 */
	void reportOntVersion(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		final String ontVersion = OntVersion.getVersion();
		if ( log.isDebugEnabled() ) {
			log.debug("reportOntVersion: reporting: " +ontVersion);
		}
		
		response.setContentType("text/plain");
		response.setHeader("Access-Control-Allow-Origin", "*");
		PrintWriter out = response.getWriter();
    	out.println(ontVersion);
	}
	
	
	/**
	 * Helper method to report the aquaportal rest url
	 * The dispatch is always completed here.
	 */
	void reportAquaportalRestUrl(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		final String aquaportalRestUrl = OntConfig.Prop.AQUAPORTAL_REST_URL.getValue();
		if ( log.isDebugEnabled() ) {
			log.debug("reportAquaportalRestUrl: reporting: " +aquaportalRestUrl);
		}
		
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println(aquaportalRestUrl);
	}
	
	
	/**
	 * Helper method to dispatch a "_csv" request.
	 * The dispatch is always completed here.
	 */
	void resolveGetCsv(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		
		if ( log.isDebugEnabled() ) {
			log.debug("resolveGetCsv: starting '_csv' response.");
		}
		
		final String fullRequestedUri = request.getRequestURL().toString();
		
		PrintWriter out = null; 
		

		// start the response page:
		response.setContentType("text/plain");
		response.setHeader("Access-Control-Allow-Origin", "*");
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
		
		
		// the ontology that is found and corresponding URI
		OntologyInfo ontology = null;
		String ontologyUri = null;
		
		String version = mmiUri.getVersion();
		if ( version == null || version.equals(MmiUri.LATEST_VERSION_INDICATOR) ) {
			ontology = db.getMostRecentOntologyVersion(mmiUri);
			if ( ontology != null ) {
				ontologyUri = ontology.getUri();
			}
		}
		else {
			String[] foundUri = { null };
			ontology = db.getOntologyWithExts(mmiUri, foundUri);
	    	if ( ontology != null ) {
	    		ontologyUri = foundUri[0];
	    	}
		}

    	if ( ontology == null ) {
    		out.println("ERROR: " +mmiUri.getOntologyUri()+ ": Not found.");
    		return;
    	}


    	// get the CSV corresponding to the found URI
    	log.debug("getOntologyInfoFromRegistry: foundUri=" +ontologyUri);

    	String destPathCsv;
		try {
			destPathCsv = new URL(ontologyUri).getPath();
		}
		catch (MalformedURLException e) {
			log.error("shouldn't happen", e);
			out.println("ERROR: shouldn't happen: " +e.getMessage());
			return;
		}
		
		destPathCsv = destPathCsv.replaceAll("/|\\\\", "_") + ".csv";
		File fileCsv = new File(OntConfig.Prop.AQUAPORTAL_VOC2RDF_DIR.getValue() + destPathCsv);

		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyInfoFromRegistry: fileCsv=" +fileCsv+ " exists=" +fileCsv.exists());
		}

		if ( ! fileCsv.exists() ) {
			out.println("ERROR: CSV of " +ontologyUri + " does not exist");
			return;
		}


		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(new FileInputStream(fileCsv)));
			IOUtils.copy(is, out);
			out.flush();
		}
		catch (IOException e) {
			out.println("ERROR: error reading CSV: " +e.getMessage()); 
		}
		finally {
			if ( is != null ) {
				try {
					is.close();
				}
				catch(IOException ignore) {
				}
			}
		}

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
		
		List<OntologyInfo> onts = db.getOntologyVersions(mmiUri);
		
		for ( OntologyInfo ontology : onts ) {
			
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
		out.println("<title>" +OntVersion.getFullTitle()+ "</title>");
		out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
		out.println("</head>");
		out.println("<body>");
		out.println("<b>" +OntVersion.getFullTitle()+ "</b><br/><br/>");
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
		for ( OntologyInfo ont : db.getOntologyVersions(mmiUri) ) {
			out.println("   " +ont.getUri());
		}
		out.println("</pre>");
		
		
		
		// obtain info about the ontology:
		String[] foundUri = { null };
    	OntologyInfo ontology = db.getOntologyWithExts(mmiUri, foundUri);
		
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
    	File file = OntServlet.getFullPath(ontology, ontConfig, log);

		// report the db info and whether the file can be read or not:
		out.println(" Ontology entry FOUND: <br/>");
		out.println("<pre>");
		out.println("                 id: " + ontology.getId());
		out.println("        ontology_id: " + ontology.getOntologyId());
		out.println("          file_path: " + ontology.getFilePath());
		out.println("           filename: " + ontology.getFilename());
		out.println("</pre>");
		out.println(" Full path: <code>" + file.getAbsolutePath() + "</code> ");
		out.println(" Can read it: <code>" + file.canRead() + "</code> <br/>");

		if ( file.canRead() ) {
			out.println("<br/>");

			String uriFile = file.toURI().toString();
			Model model = JenaUtil2.loadModel(uriFile, false);

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
			ExtendedIterator<Individual> iter = ontModel.listIndividuals(termRes);
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
