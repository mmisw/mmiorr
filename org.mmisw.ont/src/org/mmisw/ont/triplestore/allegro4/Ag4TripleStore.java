/**
 * 
 */
package org.mmisw.ont.triplestore.allegro4;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.ont.client.util.HttpUtil.HttpResponse;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.triplestore.TsUtil;
import org.mmisw.ont.util.OntUtil;
import org.mmisw.ont.util.Util;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;

import com.franz.agraph.jena.AGGraph;
import com.franz.agraph.jena.AGGraphMaker;
import com.franz.agraph.jena.AGModel;
import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Triple store implementation using AllegroGraph 4.
 * 
 * @author Carlos Rueda
 */
public class Ag4TripleStore implements ITripleStore {

	private final Log log = LogFactory.getLog(Ag4TripleStore.class);

	private String serverHost;
	private int serverPort;
	// private String tripleStoreDir; AG4: Not needed
	private String tripleStoreName;
	
	private String username;
	private String password;
	
	private HttpUtil.Credentials credentials;

	private String tripleStoreUrl;

	private final Db db;
	private final AdminDispatcher adminDispatcher;

	private String aquaUploadsDir;

	/**
	 * Connection. Usage idiom:
	 * 
	 * <pre>
	 *   _Conn _conn = new _Conn();
	 *   try {
	 *       ...
	 *   }
	 *   finally {
	 *       _conn.end();
	 *   }
	 * </pre>
	 */
	private class _Conn {
		/*
		 * TODO AG4: not sure yet what of the following should strictly be
		 * around
		 */
		AGServer _server;
		AGCatalog _catalog;
		AGRepositoryConnection ags;
		AGGraphMaker _maker;
		AGGraph _graph;
		AGModel _model;

		// AllegroGraph ts;
		AGRepository ts;

		/** Open a connection using access */
		_Conn() throws ServletException {
			this(false);
		}

		/**
		 * If renew is true, opens a connection using renew(), so the triple
		 * store is recreated Otherwise, it uses access()
		 */
		_Conn(boolean renew) throws ServletException {
			log.info("Connecting to triple store...");
			try {
				_server = new AGServer(serverHost + ":" + serverPort, username,
						password);
				_catalog = _server.getCatalog();
			}
			catch (Throwable e) {
				throw new ServletException(
						"Error connecting to triple store server.", e);
			}

			try {
				if (renew) {
					_catalog.deleteRepository(tripleStoreName);

				}
				ts = _catalog.createRepository(tripleStoreName);
				ags = ts.getConnection();
				_maker = new AGGraphMaker(ags);
				_graph = _maker.getGraph();
				_model = new AGModel(_graph);

			}
			catch (RepositoryException e) {
				throw new ServletException("Error accessing triple store.", e);
			}

			if (renew) {
				log.info("CONNECTION OPEN WITH renew");
			}
			else {
				log.info("CONNECTION OPEN");
			}

		}

		void end() throws ServletException {
			try {
				if (ts != null) {
					// ts.closeTripleStore();
					ts.close();
				}
				ts = null;
				if (ags != null) {
					// ags.disable();
					ags.close();
					ags = null;
				}
				log.info("CONNECTION CLOSED");
			}
			catch (RepositoryException e) {
				throw new ServletException("Unable to close triple store.", e);
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param db
	 *            The database helper.
	 */
	public Ag4TripleStore(Db db, AdminDispatcher adminDispatcher) {
		this.db = db;
		this.adminDispatcher = adminDispatcher;
		log.debug(getClass().getSimpleName() + " instance created.");
	}

	/**
	 * Initializes some internal parameters from OntConfig and tests connection
	 * with the allegroGraph server and opening the triple store.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		log.info("init called.");

		serverHost = OntConfig.Prop.AGRAPH_HOST.getValue();
		serverPort = Integer.parseInt(OntConfig.Prop.AGRAPH_PORT.getValue());
		// tripleStoreDir = OntConfig.Prop.AGRAPH_TS_DIR.getValue(); AG4: Not
		// needed
		tripleStoreName = OntConfig.Prop.AGRAPH_TS_NAME.getValue();
		aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();
		
		username = OntConfig.Prop.AGRAPH_USERNAME.getValue();
		password = OntConfig.Prop.AGRAPH_PASSWORD.getValue();
		
		_setCredentials();

		tripleStoreUrl = serverHost + ":" + serverPort + "/repositories/"
				+ tripleStoreName;

		// test that we can connect to the server
		_Conn _conn = new _Conn();
		try {
			// log.info("AllegroGraph Server version = "
			// +_conn.ags.getServerVersion());
			log.info("AllegroGraph Server version = "
					+ _conn._server.getVersion());
			// String[] idxFlavors = _conn.ts.getIndexFlavors();
			List<String> idxFlavors = _conn.ags.listIndices();
			// log.info(" Index flavors = " +(idxFlavors == null ? "null" :
			// Arrays.asList(idxFlavors)));
			log.info(" Index flavors = "
					+ (idxFlavors == null ? "null" : idxFlavors));

			// log.info(" unindexed threshold = "
			// +_conn.ts.getUnindexedThreshold());
			// log.info(" #unindexed triples = "
			// +_conn.ts.getUnindexedTripleCount());

			// log.info(" #triples = " +_conn.ts.numberOfTriples());
			log.info(" #triples = " + _conn.ags.size());
		}
		catch (RepositoryException e) {
			log.error("Error with AlegroGraph.", e);
			throw new ServletException("Error with AlegroGraph.", e);
		}
		catch (OpenRDFException e) {
			log.error("Error with AlegroGraph.", e);
			throw new ServletException("Error with AlegroGraph.", e);
		}
		finally {
			_conn.end();
		}

		log.info("init complete.");
	}

	/**
	 * Sets the credentials object.
	 */
	private void _setCredentials() {
		credentials = new HttpUtil.Credentials();
		try {
			// serverHost is expected to include a protocol; get the
			// host for the credential via a URL:
			credentials.host = new URL(serverHost).getHost();
		}
		catch (MalformedURLException e) {
			// should not happen
			log.warn("Parsing of URL failed for serverHost: " + serverHost, e);
			credentials.host = serverHost;
		}
		credentials.port = serverPort;
		credentials.username = username;
		credentials.password = password;
		log.info("Credentials set to: " + credentials);
	}

	/** nothing done here */
	public void destroy() throws ServletException {
		// nothing
	}

	/**
	 * Does nothing -- no need to reindex in AG4
	 */
	public void reindex(boolean wait) throws ServletException {
		log.info("reindex( " + wait
				+ ") called but ignored -- not needed in AG 4.");
	}

	/**
	 * Reinitializes the triple store.
	 * 
	 * @throws ServletException
	 */
	public void reinit() throws ServletException {
		log.info("reinit called.");
		log.info("Creating connection to triple store ...");
		_Conn _conn = new _Conn();
		try {
			// populate the triple store:
			long start = System.currentTimeMillis();
			long numberOfTriples = _doReInit(_conn);
			log.debug("triple store populated (" + TsUtil.elapsedTime(start)
					+ ").  " + "#triples= " + numberOfTriples);
		}
		finally {
			_conn.end();
		}
	}

	/**
	 * Clears the triple store.
	 * 
	 * @throws ServletException
	 */
	public void clear() throws ServletException {
		log.info("clear called. Creating connection with call to renew() on triple store ...");
		_Conn _conn = new _Conn(true);
		try {
			log.debug("clear done.  #triples= " + _conn.ags.size());
		}
		catch (RepositoryException e) {
			log.error("Error clearing triple store", e);
			throw new ServletException("Error clearing triple store", e);
		}
		finally {
			_conn.end();
		}
	}

	/**
	 * Re-Inits the triple store. This starts by calling {@link #clear()} and
	 * then re-loading all the ontologies.
	 * 
	 * @return number of triples. -1 if some error happens while obtaining this
	 *         number.
	 * @throws ServletException
	 */
	private long _doReInit(_Conn _conn) throws ServletException {

		clear();

		// get the list of (latest-version) ontologies:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);

		if (log.isDebugEnabled()) {
			log.debug("Using unversioned ontologies: " + USE_UNVERSIONED);
			log.debug("About to load the following " + onts.size()
					+ " ontologies: ");
			for (OntologyInfo ontology : onts) {
				log.debug(ontology.getOntologyId() + " :: "
								+ ontology.getUri());
			}
		}

		for (OntologyInfo ontology : onts) {
			String full_path = aquaUploadsDir + "/" + ontology.getFilePath()
					+ "/" + ontology.getFilename();
			log.info("Loading: " + full_path + " in graph");
			try {
				// NOTE: the graphId here is null; the graph relationships are
				// added below.
				String graphId = null;

				boolean clearGraphFirst = false; // the triple store starts
				// empty; see above

				_loadOntology(_conn, ontology, graphId, full_path,
						clearGraphFirst);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " + full_path
						+ " (continuing..)", ex);
			}
		}

		// statements for inferencing:
		_loadSupportingStatements(_conn);

		// load internal resources (graph relationships, etc.):
		_loadInternalResources(_conn);

		long numberOfTriples = -1;
		try {
			// numberOfTriples = _conn.ts.numberOfTriples();
			numberOfTriples = _conn.ags.size();
		}
		catch (RepositoryException e) {
			log.error("Error getting number of triples", e);
			// but continue.
		}
		return numberOfTriples;
	}

	/**
	 * load supporting statements for inference
	 */
	private void _loadSupportingStatements(_Conn _conn) {
		log.info("Loading supporting statements to allegrograph triplestore");
			for (Entry<String, String> ns : adminDispatcher.getSupportingNamespaces().entrySet()) {
				try {
					_conn.ags.setNamespace(ns.getKey(), ns.getValue());
					log.info("namespace registered: " + ns.getKey() + " : "
							+ ns.getValue());
				}
				catch (RepositoryException e) {
					log.error("Error adding namespace.", e);
				}
			}

			for (String[] statement : AgSupport.SUPPORTING_STATEMENTS) {
				Resource s = _conn._model.createResource(statement[0]);
				Property p = _conn._model.createProperty(statement[1]);
				Resource o = _conn._model.createResource(statement[2]);
				_conn._model.add(s, p, o);
				log.info("statement added: " + statement[0] + " "
						+ statement[1] + " " + statement[2]);
			}
	}

	/**
	 * load internal resources (graphs, etc) in the triple store.
	 */
	private void _loadInternalResources(_Conn _conn) {
		log.info("_loadInternalResources called.");
		
		 List<Statement> statements = adminDispatcher.getInternalStatements();
		
//		 OLD
//		 // then, update the triple store with the corresponding statements:
//		 if ( statements != null ) {
//			 for (Statement stmt : statements) {
//				 String sbj = stmt.getSubject().getURI();
//				 String prd = stmt.getPredicate().getURI();
//				 String obj = ((Resource) stmt.getObject()).getURI();
//
//				 sbj = '<' + sbj + '>';
//				 prd = '<' + prd + '>';
//				 obj = '<' + obj + '>';
//
//				 _conn.ts.addStatement(sbj, prd, obj);
//				 log.info("Added statement: " +stmt);
//			 }
//		 }
		
		 // New AG4:
		 _conn._model.add(statements);
		 
	}

	public void loadOntology(OntologyInfo ontology, String graphId)
			throws Exception {
		_Conn _conn = new _Conn();
		try {
			String full_path = aquaUploadsDir + "/" + ontology.getFilePath()
					+ "/" + ontology.getFilename();
			log.info("Loading: " + full_path + " in graph: "
					+ (graphId == null ? "(default graph)" : graphId));
			boolean clearGraphFirst = true;
			_loadOntology(_conn, ontology, graphId, full_path, clearGraphFirst);

			// launch indexing of new triples in the background and return:
			// NO: indexing not needed in AG 4
			// boolean wait = false;
			// _conn.ts.indexNewTriples(wait);
		}
		finally {
			_conn.end();
		}

	}

	/**
	 * Loads an ontology to the given graph.
	 * 
	 * @param _conn
	 * @param ontology
	 * @param graphId
	 *            User-specified graph. Can be null.
	 * @param full_path
	 * @param clearGraphFirst
	 *            true to remove all statements associated with the graph
	 *            directly associated with the ontology.
	 */
	private void _loadOntology(_Conn _conn, OntologyInfo ontology,
			String graphId, String full_path, boolean clearGraphFirst) {

		String graphUri;

		String ontologyUri = ontology.getUri();
		OntModel model;
//		String serialization;   Not used in the AG4 impl

		if (USE_UNVERSIONED) {
			model = JenaUtil2.loadModel("file:" + full_path, false);

			if (OntUtil.isOntResolvableUri(ontologyUri)) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					OntModel unversionedModel = UnversionedConverter
							.getUnversionedModel(model, mmiUri);

//					serialization = JenaUtil2.getOntModelAsString(
//							unversionedModel, "RDF/XML-ABBREV");
					
					// AG$: we don;t use the serialization; but here we need to update
					// the model reference to be unversionedModel:
					model = unversionedModel;

					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return;
				}
				log.info("To load Ont-resolvable ontology in graph.");
			}
			else {
//				serialization = JenaUtil2.getOntModelAsString(model,
//						"RDF/XML-ABBREV");
				log.info("To load re-hosted ontology in graph.");
			}
		}
		else {
			model = JenaUtil2.loadModel("file:" + full_path, false);
//			serialization = JenaUtil2.getOntModelAsString(model,
//					"RDF/XML-ABBREV");
		}

		// /////////////////////////////////////////////////////////////
		// now, update graph with model captured in serialization
		// /////////////////////////////////////////////////////////////

		try {
			// 'ownGraph' is the graph for the ontology itself.
			// All the statements in the ontology are associated with this
			// graph.
			final String ownGraph = "<" + ontologyUri + ">";

			if (clearGraphFirst) {
				// remove all statements associated with the graph:
				if (log.isDebugEnabled()) {
					log.debug("Removing all statements in graph " + ownGraph
							+ " ...");
				}
				// OLD AG3: _conn.ts.removeStatements(null, null, null, ownGraph);
				// New AG4:
				_removeStatementsFromDefaultGraph(ownGraph);
				_clearGraph(ownGraph);
			}

			// now, create the new graph:
			// OLD AG3: AgUtil.parseWithTiming(_conn.ts, true, serialization,
			// ownGraph);
			_addModelToGraph(_conn, model, ontologyUri);

			// add the graph statement to the graphs resource:
			String ownGraphUri = adminDispatcher
					.getWellFormedGraphUri(ownGraph);
			adminDispatcher.newGraph(ownGraphUri);

			// now, add the subGraphOf relationship if graphId != null
			if (graphId != null) {
				graphUri = adminDispatcher.getWellFormedGraphUri(graphId);
				_addSubGraph(_conn, ownGraphUri, graphUri);
			}

		}
		catch (Exception e) {
			log.error("Error parsing/loading RDF in graph.", e);
		}

	}

	/**
	 * Updates the graphs resource and then the triple store.
	 * 
	 * @param _conn
	 * @param subGraphUri
	 *            Assumed to be well-formed
	 * @param superGraphUri
	 *            Assumed to be well-formed
	 * @throws AllegroGraphException
	 */
	private void _addSubGraph(_Conn _conn, String subGraphUri,
			String superGraphUri)  {

		// first, update the graphs resource:
		List<Statement> statements = adminDispatcher.newSubGraph(subGraphUri,
				superGraphUri);

		// then, update the triple store with the corresponding statements:
		if (statements != null) {
			// OLD AG3 way:
			// for (Statement stmt : statements) {
			// String sbj = stmt.getSubject().getURI();
			// String prd = stmt.getPredicate().getURI();
			// String obj = ((Resource) stmt.getObject()).getURI();
			//	
			// sbj = '<' + sbj + '>';
			// prd = '<' + prd + '>';
			// obj = '<' + obj + '>';
			//				
			// _conn.ts.addStatement(sbj, prd, obj);
			// log.info("Added statement: " +stmt);
			// }
			
			// New AG4:
			_conn._model.add(statements);
			log.info("Added statements: " + statements.size());
		}
	}

	public void removeOntology(OntologyInfo ontology) throws Exception {
		_Conn _conn = new _Conn();
		try {
			log.info("Removing: id=" + ontology.getId() + " of ontologyId="
					+ ontology.getOntologyId() + " ...");
			_removeOntology(_conn, ontology);
		}
		finally {
			_conn.end();
		}

	}

	/**
	 * i) removes all statements associated with the "proper" graph (ie., the
	 * graph whose URI is the same as the ontology URI);
	 * <p>
	 * If the ontology URI will be completely gone (ie., no previous version is
	 * available), then:
	 * <p>
	 * ii) removes all statements having the "proper" graph as subject (in
	 * particular, subGraphOf relationships and the typeOf-graph statement will
	 * be removed).
	 * 
	 * @param _conn
	 * @param ontology
	 * @throws Exception
	 * 
	 */
	private void _removeOntology(_Conn _conn, OntologyInfo ontology)
			throws Exception {

		String ontologyUri = ontology.getUri();

		if (USE_UNVERSIONED) {

			if (OntUtil.isOntResolvableUri(ontologyUri)) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return;
				}
				log
						.debug("About to remove Ont-resolvable ontology from graph.");
			}
			else {
				log.debug("About to remove re-hosted ontology from graph.");
			}
		}
		// Else: nothing -- just keep the given ontologyUri.

		// now, update graph:

		// this is the graph for the ontology itself.
		// All the statements in the ontology are associated with this graph.
		String ownGraph = "<" + ontologyUri + ">";

		try {
			////////////////////////////////////////////////////////////////////
			// //
			// i) first, remove all statements associated with the ownGraph:
			//
			if (log.isDebugEnabled()) {
				log.debug("Removing all statements in graph " + ownGraph
						+ " ...");
			}
			// OLD AG3: _conn.ts.removeStatements(null, null, null, ownGraph);
			
			// New AG4:
			_removeStatementsFromDefaultGraph(ownGraph);
			_clearGraph(ownGraph);
		}
		catch (Exception e) {
			log.error("Error removing ontology statements from graph.", e);
			return;
		}

		// if any, get latest version that may remain:
		OntologyInfo latestOntology = null;
		try {
			latestOntology = db.getRegisteredOntologyLatestVersion(ontologyUri);
		}
		catch (ServletException e) {
			log
					.warn(
							"Warning: error while trying to retrieve existing version of ontology. Ignoring error.",
							e);
		}

		if (latestOntology != null) {
			/*
			 * there still is an existing ontology version.
			 * Existing subGraphOf statements will remain valid.
			 * Just load the found latest ontology:
			 */
			log.debug("_removeOntology: Loading latestOntology after removal");
			loadOntology(latestOntology, null);

			return;
		}

		// here: ontologyUri completely gone.

		// ////////////////////////////////////////////////////////////////////
		// ii) So, remove all statements having ownGraph as subject (in
		// particular,
		// subGraphOf relationships and the typeOf-graph statement will be
		// removed):
		//
		String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
		_removeAllStatementsForSubject(_conn, ownGraphUri);

	}

	/**
	 * Removes all statements for a given subject. Updates the graphs resource
	 * and then the triple store.
	 * 
	 * @param _conn
	 * @param subGraphUri
	 *            Assumed to be well-formed
	 * @throws Exception 
	 */
	private void _removeAllStatementsForSubject(_Conn _conn, String subGraphUri)
			throws Exception {

		log.debug("_removeAllStatementsForSubject: " + subGraphUri);

		// remove the statements from the graphs resource:
		adminDispatcher.removeAllStatementsFromSubject(subGraphUri);

		// then, update the triple store by removing corresponding statements:
		// OLD AG3: _conn.ts.removeStatements("<" + subGraphUri + ">", null, null);
		// New AG4:
		String subj = "<" + subGraphUri + ">";
		_doUpdate(
				"delete { " + subj + " ?p ?o .} " +
				"where  { " + subj + " ?p ?o .}"
		);

	}

	/**
	 * Executes a SPARQL query.
	 * 
	 * @param sparqlQuery
	 * @param infer With inference?
	 * @param form
	 *            Only used for a "select" query.
	 * @return
	 * @throws Exception
	 */
	public QueryResult executeQuery(String query, boolean infer, String form) throws Exception {

		final QueryResult queryResult = new QueryResult();

		/*
		 * Make the request to the AllegroGraph HTTP endpoint.
		 */
		query = URLEncoder.encode(query, "UTF-8");
		String urlRequest = tripleStoreUrl + "?infer=" +infer+ "&query=" + query;
		String accept = AgUtil.mimeType(form);
		if (log.isDebugEnabled()) {
			log.debug("Making http get request: " +urlRequest+ " Accept: " +accept);
		}
		HttpResponse httpResponse = HttpUtil.httpGet(urlRequest, accept);
        if (log.isDebugEnabled()) {
            log.debug(_debugHttpResponse(httpResponse));
        }

        /*
         * Fix 300:No suitable response format available.
         * If error returned is 406 Not Acceptable, retry with "application/rdf+xml".
         * If this also fails then retry with "text/plain". 
         */
        if ( 406 == httpResponse.statusCode ) {
    		accept = "application/rdf+xml";
    		if (log.isDebugEnabled()) {
    			log.debug("Got 406 Not Acceptable. Trying with Accept: " + accept + " Request: " +urlRequest);
    		}
    		httpResponse = HttpUtil.httpGet(urlRequest, accept);
            if (log.isDebugEnabled()) {
                log.debug(_debugHttpResponse(httpResponse));
            }

            /*
             * 406 Not Acceptable got again?
             * Then, just retry with "text/plain":
             */
            if ( 406 == httpResponse.statusCode ) {
        		accept = "text/plain";
        		if (log.isDebugEnabled()) {
        			log.debug("Got 406 Not Acceptable. Trying with Accept: " + accept + " Request: " +urlRequest);
        		}
        		httpResponse = HttpUtil.httpGet(urlRequest, accept);
                if (log.isDebugEnabled()) {
                    log.debug(_debugHttpResponse(httpResponse));
                }
            }
        }

        /*
         * Note that AgUtil.mimeType returns a CSV format for form="html"
         * The following checks that case to convert the returned CSV into HTML.
         * (the regex is for application/processed-csv and text/csv)
         */
        if ("html".equals(form) && accept.matches(".*\\Wcsv$")
		&& httpResponse.statusCode == HttpServletResponse.SC_OK ) {
			String html = Util.csv2html(httpResponse.body);
			queryResult.setResult(html);
			queryResult.setContentType("text/html");
		}
		else {
			queryResult.setResult(httpResponse.body);
			queryResult.setContentType(httpResponse.contentType);
		}

		return queryResult;
	}
	
	private static String _debugHttpResponse(HttpResponse httpResponse) {
        String body = httpResponse.body;
        if (body.length() > 300) {
                body = body.substring(0, 256) + " ... " + body.substring(body.length() - 44);
        }
        return "httpResponse: statusCode: " +httpResponse.statusCode+ "\n" +
                  "statusLine: " +httpResponse.statusLine+ "\n" +
                  "contentType: " +httpResponse.contentType+ "\n" +
                  "body: " +body+ "\n"
        ;
	}

	/**
	 * Adds the statements in an OntModel to a graph in the triple store.
	 * 
	 * @param _conn
	 * @param ontologyUri
	 *            Graph URI
	 * @param model
	 *            Model containing the statements.
	 */
	private void _addModelToGraph(_Conn _conn, OntModel model,
			String ontologyUri) {
		AGGraph agGraph = _conn._maker.createGraph(ontologyUri);
		AGModel agModel = new AGModel(agGraph);
		agModel.add(model);

		// finally, add the new stuff into the triple store:
		_conn._model.add(agModel);
	}

	/**
	 * Removes all statements in the given graph from the default graph.
	 * 
	 * @param graph The graph whose statements are to be removed from the default graph.
	 */
	private void _removeStatementsFromDefaultGraph(String graph) throws Exception {
        /*
         * first, get the statements in the given graph:
         */
        String query = "select ?s ?p ?o" +
                        " from " + graph +
                        " where { ?s ?p ?o }";
        if ( log.isDebugEnabled() ) {
                log.debug("Executing query to get statements in graph: " + query);
        }
        QueryResult qr = executeQuery(query, false, "json");
        String result = qr.getResult();

        /*
         * remove them from the default graph: Use a "delete data" statement, which
         * requires explicit triples (no template or variables):
         */
        JSONTokener jsonParser = new JSONTokener(result);
        JSONObject jsonObj = new JSONObject(jsonParser);
//      JSONArray names = jsonObj.getJSONArray("names");
        JSONArray values = jsonObj.getJSONArray("values");
        if ( values.length() > 0 ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Preparing query to delete " +values.length() + " triples from default graph");
            }
        	StringBuffer sb = new StringBuffer("delete data {\n");
	        for (int i = 0; i < values.length(); i++ ) {
	                JSONArray t = (JSONArray) values.get(i);
	                sb.append(t.get(0).toString() + " " + t.get(1) + " " + t.get(2) + " . \n");
	        }
	        sb.append("}");
	        String delQuery = sb.toString();
	        _doUpdate(delQuery);
        }
        else {
            if ( log.isDebugEnabled() ) {
                log.debug("No statements in graph: " + graph);
            }
        }
	}

	/**
	 * Clears the given graph, ie., removes all associated statements from the given graph.
	 */
	private void _clearGraph(String graph) throws Exception {
		_doUpdate("clear graph " + graph);
	}
	
	/**
	 * Makes a SPARL Update request.
	 * 
	 * @param update
	 * @throws Exception
	 */
	private void _doUpdate(String update) throws Exception {
		if (log.isDebugEnabled()) {
			String mu = update;
			if (update.length() > 300) {
				mu = mu.subSequence(0, 128) + " ... " + mu.substring(update.length() - 100);
			}
			log.debug("update: " + mu);
		}

		final String encUpdate = URLEncoder.encode(update, "UTF-8");

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("update", encUpdate);

		String urlRequest = tripleStoreUrl + "?update=" + encUpdate;

		if (log.isDebugEnabled()) {
			log.debug("_doUpdate: Making request...");
		}
		HttpResponse httpResponse = HttpUtil.httpPost(credentials, urlRequest, vars);
		if (log.isDebugEnabled()) {
			log.debug("_doUpdate: httpResponse = " + httpResponse);
		}
	}

}
