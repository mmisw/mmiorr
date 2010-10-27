package org.mmisw.ont.triplestore.allegro;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.triplestore.TsUtil;
import org.mmisw.ont.util.OntUtil;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agbase.AllegroGraphSerializer;
import com.franz.agbase.NTriplesSerializer;
import com.franz.agbase.RDFN3Serializer;
import com.franz.agbase.SPARQLQuery;
import com.franz.agbase.TriplesIterator;
import com.franz.agbase.ValueSetIterator;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * Triple store implementation using AllegroGraph.
 * 
 * @author Carlos Rueda
 */
public class AgTripleStore implements ITripleStore {

	/** Servlet resource containing the model with properties for inference purposes, N-triples format */
	@SuppressWarnings("unused")
	private static final String INF_PROPERTIES_MODEL_NAME_NT = "inf_properties.nt";
	

	private final Log log = LogFactory.getLog(AgTripleStore.class);
	
	private String serverHost;
	private int serverPort;
	private String tripleStoreDir;
	private String tripleStoreName;

	private final Db db;
	private final AdminDispatcher adminDispatcher;
	
	private String aquaUploadsDir;
	
	
	/**
	 * Connection. Usage idiom:
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
		AllegroGraphConnection ags;
		AllegroGraph ts;
		
		/** Open a connection using access */
		_Conn() throws ServletException {
			this(false);
		}
		
		/** If renew is true, opens a connection using renew(), so the triple store is recreated 
		 * Otherwise, it uses access()
		 */
		_Conn(boolean renew) throws ServletException {
			log.info("Connecting to triple store...");
			try {
				ags = new AllegroGraphConnection();
				ags.setHost(serverHost);
				ags.setPort(serverPort);
				ags.enable();
			} 
			catch (Throwable e) {
				throw new ServletException("Error connecting to triple store server.", e);
			}

			try {
				if ( renew ) {
					ts = ags.renew(tripleStoreName, tripleStoreDir);
					log.info("CONNECTION OPEN WITH renew");
				}
				else {
					ts = ags.access(tripleStoreName, tripleStoreDir);					
					log.info("CONNECTION OPEN");
				}
			}
			catch (AllegroGraphException e) {
				throw new ServletException("Error accessing triple store.", e);
			}
		}

		void debugIndexInfo() {
			if ( log.isDebugEnabled() ) {
				try {
					log.debug("getChunkSize = " +ags.getChunkSize());
					log.debug("getDefaultExpectedResources = " +ags.getDefaultExpectedResources());
					log.debug("getTimeout = " +ags.getTimeout());
					log.debug("getUnindexedThreshold = " +ts.getUnindexedThreshold());
					log.debug("getUnmergedThreshold = " +ts.getUnmergedThreshold());
					log.debug("Unmerged Chunks: " + ts.getUnmergedCount());
					log.debug("Unindexed triples: " + ts.getUnindexedTripleCount());
				}
				catch (AllegroGraphException e) {
					log.debug("error while getting some triple store values", e);
				}
			}			
		}

		void end() throws ServletException {
			if ( ts != null ) {
				try {
					ts.closeTripleStore();
				} 
				catch (AllegroGraphException e) {
					throw new ServletException("Unable to close triple store.", e);
				}
				ts = null;
			}
			if (ags != null) {
				ags.disable();
				ags = null;
			}
			log.info("CONNECTION CLOSED");
		}
	}

	
	/**
	 * Constructor.
	 * 
	 * @param db The database helper.
	 */
	public AgTripleStore(Db db, AdminDispatcher adminDispatcher) {
		this.db = db;
		this.adminDispatcher = adminDispatcher;
		log.debug(getClass().getSimpleName()+ " instance created.");
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
		tripleStoreDir = OntConfig.Prop.AGRAPH_TS_DIR.getValue();
		tripleStoreName = OntConfig.Prop.AGRAPH_TS_NAME.getValue();
		aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();

		// test that we can connect to the server
		_Conn _conn = new _Conn();
		try {
			log.info("AllegroGraph Server version = " +_conn.ags.getServerVersion());
			String[] idxFlavors = _conn.ts.getIndexFlavors();
			log.info(" Index flavors = " +(idxFlavors == null ? "null" : Arrays.asList(idxFlavors)));
			log.info(" unindexed threshold = " +_conn.ts.getUnindexedThreshold());
			log.info(" #unindexed triples = " +_conn.ts.getUnindexedTripleCount());
			log.info(" #triples = " +_conn.ts.numberOfTriples());
		}
		catch (AllegroGraphException e) {
			log.error("Error with AlegroGraph.", e);
			throw new ServletException("Error with AlegroGraph.", e);
		}
		finally {
			_conn.end();
		}

		log.info("init complete.");
	}
	
	/** nothing done here */
	public void destroy() throws ServletException {
		// nothing
	}


	public void reindex(boolean wait) throws ServletException {
		_Conn _conn = new _Conn();
		try {
			_reindex(_conn, wait);
		}
		finally {
			_conn.end();
		}
	}
	
	private void _reindex(_Conn _conn, boolean wait) throws ServletException {
		log.info("reindex called. wait=" +wait);
		long start = System.currentTimeMillis();
		try {
			_conn.ts.indexAllTriples(wait);
			_conn.debugIndexInfo();
			log.info("reindex completed (" +TsUtil.elapsedTime(start)+ ")");
		}
		catch (AllegroGraphException e) {
			log.error("Error reindexing", e);
			throw new ServletException("Error reindexing", e);
		}
	}
	
	/**
	 * Reinitializes the triple store.
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
			log.debug("triple store populated (" +TsUtil.elapsedTime(start)+ ").  " +
					"#triples= " +numberOfTriples);
			
			// index:
			boolean wait = true;
			_reindex(_conn, wait);
		}
		finally {
			_conn.end();
		}
	}
	
	/**
	 * Clears the triple store.
	 * @throws ServletException
	 */
	public void clear() throws ServletException {
		log.info("clear called. Creating connection with call to renew() on triple store ...");
		_Conn _conn = new _Conn(true);
		try {
			log.debug("clear done.  #triples= " +_conn.ts.numberOfTriples());
		}
		catch (AllegroGraphException e) {
			log.error("Error clearing triple store", e);
			throw new ServletException("Error clearing triple store", e);
		}
		finally {
			_conn.end();
		}
	}
	
	
	/**
	 * Re-Inits the triple store. This starts by calling {@link #clear()} and then
	 * re-loading all the ontologies.
	 * 
	 * @return number of triples. -1 if some error happens while obtaining this number.
	 * @throws ServletException
	 */
	private long _doReInit(_Conn _conn) throws ServletException {
		
		clear();
		
		// get the list of (latest-version) ontologies:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			log.debug("About to load the following " +onts.size()+ " ontologies: ");
			for ( OntologyInfo ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}
		
		for ( OntologyInfo ontology : onts ) {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph");
			try {
				// NOTE: the graphId here is null; the graph relationships are added below.
				String graphId = null;
				
				boolean clearGraphFirst = false;  // the triple store starts empty; see above
				
				_loadOntology(_conn, ontology, graphId, full_path, clearGraphFirst);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		// enable inferencing:
		_loadSupportingStatements(_conn);
		
		// load internal resources (graph relationships, etc.):
		try {
			_loadInternalResources(_conn);
		}
		catch (AllegroGraphException e) {
			log.error("Error loading internal resources", e);
			// but continue.
		}
		
		long numberOfTriples = -1;
		try {
			numberOfTriples = _conn.ts.numberOfTriples();
		}
		catch (AllegroGraphException e) {
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
		try {
			for ( Entry<String, String> ns : adminDispatcher.getSupportingNamespaces().entrySet() ) {
				_conn.ts.registerNamespace(ns.getKey(), ns.getValue());
				log.info("namespace registered: " +ns.getKey()+ " : " +ns.getValue());
			}
			
			for ( String[] statement : AgSupport.SUPPORTING_STATEMENTS ) {
				_conn.ts.addStatement(statement[0], statement[1], statement[2]);
				log.info("statement added: " +statement[0]+ " " +statement[1]+ " " +statement[2]);
			}
		}
		catch (AllegroGraphException e) {
			log.error("Error adding statements to graph.", e);
		}
	}

	
	/**
	 * load internal resources (graphs, etc) in the triple store.
	 * @throws AllegroGraphException 
	 */
	private void _loadInternalResources(_Conn _conn) throws AllegroGraphException {
		log.info("_loadInternalResources called.");
		List<Statement> statements = adminDispatcher.getInternalStatements();
		
		// then, update the triple store with the corresponding statements:
		if ( statements != null ) {
			for (Statement  stmt : statements) {
				String sbj = stmt.getSubject().getURI();
				String prd = stmt.getPredicate().getURI();
				String obj = ((Resource) stmt.getObject()).getURI();
	
				sbj = '<' + sbj + '>';
				prd = '<' + prd + '>';
				obj = '<' + obj + '>';
				
				_conn.ts.addStatement(sbj, prd, obj);
				log.info("Added statement: " +stmt);
			}
		}		
	}

	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		_Conn _conn = new _Conn();
		try {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph: " +(graphId == null ? "(default graph)" : graphId));
			boolean clearGraphFirst = true;
			_loadOntology(_conn, ontology, graphId, full_path, clearGraphFirst);
			
			// launch indexing of new triples in the background and return:
			boolean wait = false;
			_conn.ts.indexNewTriples(wait);
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
	 * @param graphId User-specified graph. Can be null.
	 * @param full_path
	 * @param clearGraphFirst true to remove all statements associated with the graph directly associated with the
	 *        ontology.
	 */
	private void _loadOntology(_Conn _conn, OntologyInfo ontology, String graphId, String full_path, boolean clearGraphFirst) {
		
		String graphUri;
		
		String ontologyUri = ontology.getUri();
		String serialization;
		
		if ( USE_UNVERSIONED ) {
			OntModel model = JenaUtil2.loadModel("file:" +full_path, false);

			if ( OntUtil.isOntResolvableUri(ontologyUri) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					OntModel unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
					
					serialization = JenaUtil2.getOntModelAsString(unversionedModel, "RDF/XML-ABBREV");
					
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return ;
				}
				log.info("To load Ont-resolvable ontology in graph.");
			}
			else {
				serialization = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
				log.info("To load re-hosted ontology in graph.");
			}
		}
		else {
			OntModel model = JenaUtil2.loadModel("file:" +full_path, false);
			serialization = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		}
		
		///////////////////////////////////////////////////////////////
		// now, update graph with model captured in serialization
		///////////////////////////////////////////////////////////////
		
		try {
			// 'ownGraph' is the graph for the ontology itself. 
			// All the statements in the ontology are associated with this graph.
			final String ownGraph = "<" +ontologyUri+ ">";
			
			if ( clearGraphFirst ) {
				// remove all statements associated with the graph:
				if ( log.isDebugEnabled() ) {
					log.debug("Removing all statements in graph " +ownGraph+ " ...");
				}
				_conn.ts.removeStatements(null, null, null, ownGraph);
			}
			
			// now, create the new graph:
			AgUtils.parseWithTiming(_conn.ts, true, serialization, ownGraph);
			
			
			// add the graph statement to the graphs resource:
			String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
			adminDispatcher.newGraph(ownGraphUri);
			
			// now, add the subGraphOf relationship if graphId != null
			if ( graphId != null ) {
				graphUri = adminDispatcher.getWellFormedGraphUri(graphId);
				_addSubGraph(_conn, ownGraphUri, graphUri);
			}
			
		}
		catch (AllegroGraphException e) {
			log.error("Error parsing/loading RDF in graph.", e);
		}
		
	}
	
	
	/**
	 * Updates the graphs resource and then the triple store.
	 * @param _conn
	 * @param subGraphUri  Assumed to be well-formed
	 * @param superGraphUri  Assumed to be well-formed
	 * @throws AllegroGraphException
	 */
	private void _addSubGraph(_Conn _conn, String subGraphUri, String superGraphUri) throws AllegroGraphException {
		
		// first, update the graphs resource:
		List<Statement> statements = adminDispatcher.newSubGraph(subGraphUri, superGraphUri);
		
		// then, update the triple store with the corresponding statements:
		if ( statements != null ) {
			for (Statement  stmt : statements) {
				String sbj = stmt.getSubject().getURI();
				String prd = stmt.getPredicate().getURI();
				String obj = ((Resource) stmt.getObject()).getURI();
	
				sbj = '<' + sbj + '>';
				prd = '<' + prd + '>';
				obj = '<' + obj + '>';
				
				_conn.ts.addStatement(sbj, prd, obj);
				log.info("Added statement: " +stmt);
			}
		}		
	}

	public void removeOntology(OntologyInfo ontology) throws Exception {
		_Conn _conn = new _Conn();
		try {
			log.info("Removing: id=" +ontology.getId()+ " of ontologyId=" +ontology.getOntologyId()+ " ...");
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
	 * If the ontology URI will be completely gone (ie., no previous version is available), then:
	 * <p>
	 * ii) removes all statements having the "proper" graph as subject (in particular, subGraphOf 
	 * relationships and the typeOf-graph statement will be removed).
	 * 
	 * @param _conn
	 * @param ontology
	 * @throws Exception 
	 *        
	 */
	private void _removeOntology(_Conn _conn, OntologyInfo ontology) throws Exception {
		
		String ontologyUri = ontology.getUri();
		
		if ( USE_UNVERSIONED ) {

			if ( OntUtil.isOntResolvableUri(ontologyUri) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return ;
				}
				log.debug("About to remove Ont-resolvable ontology from graph.");
			}
			else {
				log.debug("About to remove re-hosted ontology from graph.");
			}
		}
		// Else: nothing -- just keep the given ontologyUri.
		
		// now, update graph:

		// this is the graph for the ontology itself. 
		// All the statements in the ontology are associated with this graph.
		String ownGraph = "<" +ontologyUri+ ">";
		
		try {
			//////////////////////////////////////////////////////////////////////
			// i) first, remove all statements associated with the ownGraph:
			//
			if ( log.isDebugEnabled() ) {
				log.debug("Removing all statements in graph " +ownGraph+ " ...");
			}
			_conn.ts.removeStatements(null, null, null, ownGraph);
		}
		catch (AllegroGraphException e) {
			log.error("Error removing ontology statements from graph.", e);
			return;
		}
		
		// if any, get latest version that may remain:
		OntologyInfo latestOntology = null;
		try {
			latestOntology = db.getRegisteredOntologyLatestVersion(ontologyUri);
		}
		catch (ServletException e) {
			log.warn("Warning: error while trying to retrieve existing version of ontology. Ignoring error.", e);
		}
			
		if ( latestOntology != null ) {
			// there still is an existing ontology version. So, no need
			// for more updates, ie., any existing subGraphOf statements will remain valid.
			log.debug("_removeOntology: No need to remove subGraphOf statements");
			
			// FIXME but we need to load the contents of the latest version that remains
			// ...
			
			return;
		}

		// here: ontologyUri completely gone.

		//////////////////////////////////////////////////////////////////////
		// ii) So, remove all statements having ownGraph as subject (in particular,
		// subGraphOf relationships and the typeOf-graph statement will be removed):
		//
		String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
		_removeAllStatementsForSubject(_conn, ownGraphUri);
		
	}

	/**
	 * Removes all statements for a given subject.
	 * Updates the graphs resource and then the triple store.
	 * @param _conn
	 * @param subGraphUri  Assumed to be well-formed
	 * @throws AllegroGraphException
	 */
	private void _removeAllStatementsForSubject(_Conn _conn, String subGraphUri) throws AllegroGraphException {
		
		log.debug("_removeAllStatementsForSubject: " +subGraphUri);
		
		// remove the statements from the graphs resource:
		adminDispatcher.removeAllStatementsFromSubject(subGraphUri);
		
		// then, update the triple store with the corresponding statements:
		_conn.ts.removeStatements("<" +subGraphUri+ ">", null, null);
		
	}

	
	
	/**
	 * Executes a SPARQL query.
	 * 
	 * @param sparqlQuery
	 * @param form Only used for a "select" query.
	 * @return
	 * @throws Exception 
	 */
	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		_Conn _conn = new _Conn();
		try {
			return _executeQuery(_conn, sparqlQuery, form);
		}
		finally {
			_conn.end();
		}
	}
	
	
	private QueryResult _executeQuery(_Conn _conn, String sparqlQuery, String form) throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("_executeQuery called.  form=" +form);
		}
		
		if ( form == null ) {
			form = "html";
		}
		
		QueryResult queryResult = new QueryResult();

		SPARQLQuery sq = new SPARQLQuery();
		sq.setTripleStore(_conn.ts);
		sq.setQuery(sparqlQuery);
		sq.setIncludeInferred(true);
		
		boolean useRun = false;
		
		// NOTE about AG bugs: 
		// 1) SPARQLQuery.run throws an exception! argh!
		// 2) the AG serializers are not working (not even in the examples provided by them)

		// because of 1), commenting out the following so SPARQLQuery.run() is not called below.
//		if ( form.equalsIgnoreCase("owl") || form.equalsIgnoreCase("rdf") ) {
//			useRun = true;
//			sq.setResultsFormat("sparql-xml");
//			// TODO: contentType should be sparql-related
//			queryResult.setContentType("Application/rdf+xml");
//		}
//		// else: TODO what other formats are possible?

		
		if ( useRun ) {
			String res = sq.run();
			queryResult.setIsEmpty(res.trim().length() == 0);
			queryResult.setResult(res);
			
			return queryResult;
		}
		
		// use Jena to determine what kind of query this is (AG doesn't seem to provide an operation for this)
		// so we call the appropriate execution method:
		Query query = QueryFactory.create(sparqlQuery);

		// only one of these results is captured
		TriplesIterator tripleIter = null;
		ValueSetIterator valSetIter = null;
		Boolean askResult = null;

		
		queryResult.setContentType("text/plain");

		// SELECT
		if ( query.isSelectType() ) {
			valSetIter = sq.select();
		}
		// DESCRIBE
		else if ( query.isDescribeType() ) {
			tripleIter = sq.describe();
		}
		// CONSTRUCT
		else if ( query.isConstructType() ) {
			tripleIter = sq.construct();
		}
		// ASK
		else if ( query.isAskType() ) {
			askResult = Boolean.valueOf(sq.ask());
		}

		if ( valSetIter != null ) {
			queryResult.setIsEmpty(! valSetIter.hasNext());
			String res;
			
			if ( form.equalsIgnoreCase("html") ) {
				queryResult.setContentType("text/html");
				res = AgUtils.getResultInHtml(log, valSetIter);
			}
			else if ( form.equalsIgnoreCase("n3") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInN3(log, valSetIter);
			}
			else if ( form.equalsIgnoreCase("nt") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInNTriples(log, valSetIter);
			}
			else if ( form.equalsIgnoreCase("csv") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInCsv(log, valSetIter);
			}
			else if ( form.equalsIgnoreCase("json") ) {
				queryResult.setContentType("application/json");
				res = AgUtils.getResultInJson(log, valSetIter);
			}
			else {
				queryResult.setContentType("text/html");
				res = AgUtils.getResultInHtml(log, valSetIter);
			}
			
			queryResult.setResult(res);
		}
		else if ( tripleIter != null ) {
			queryResult.setIsEmpty(! tripleIter.hasNext());
			
			// TODO: NOTE: the AG serializers are not working (not even in the examples provided by them)
			
			String res = null;
			
			if ( form.equalsIgnoreCase("owl") || form.equalsIgnoreCase("rdf") ) {
				queryResult.setContentType("Application/rdf+xml");
				res = AgUtils.getResultInRdf(log, tripleIter);
			}
			else if ( form.equalsIgnoreCase("html") ) {
				queryResult.setContentType("text/html");
				res = AgUtils.getResultInHtml(log, tripleIter);
			}
			else if ( form.equalsIgnoreCase("n3") ) {
				queryResult.setContentType("text/plain");
				if ( true ) {
					res = AgUtils.getResultInN3(log, tripleIter);
				}
				else {
					// AG serializers do not work.
					AllegroGraphSerializer serializer = new RDFN3Serializer();
					serializer.setDestination(null); // ie., to string to be returned by run()
					Object resObj = serializer.run(tripleIter);
					res = String.valueOf(resObj);
				}
			}
			else if ( form.equalsIgnoreCase("nt") ) {
				queryResult.setContentType("text/plain");
				if ( true ) {
					res = AgUtils.getResultInNTriples(log, tripleIter);
				}
				else {
					// AG serializers do not work.
					AllegroGraphSerializer serializer = new NTriplesSerializer();
					serializer.setDestination(null); // ie., to string to be returned by run()
					Object resObj = serializer.run(tripleIter);
					res = String.valueOf(resObj);
				}
			}
			else if ( form.equalsIgnoreCase("csv") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInCsv(log, tripleIter);
			}
			else if ( form.equalsIgnoreCase("json") ) {
				// FIXME 261: JSON output format not honored  
				// Returning CSV for the moment.
				res = AgUtils.getResultInCsv(log, tripleIter);
				queryResult.setContentType("text/plain");
				
//				queryResult.setContentType("application/json");
//				res = AgUtils.getResultInJson(log, tripleIter);
			}
			else {
				queryResult.setContentType("text/html");
				res = AgUtils.getResultInHtml(log, tripleIter);
			}
			
			queryResult.setResult(res);
		}
		else if ( askResult != null ) {
			queryResult.setIsEmpty(false);
			queryResult.setResult(askResult.toString());
		}
		else {
			log.warn("SHOULD NOT HAPPEN: unexpected type of query");
			queryResult.setIsEmpty(false);
			queryResult.setResult("Internal error: unexpected type of query. Please report this bug.");
		}

		return queryResult;
	}

}
