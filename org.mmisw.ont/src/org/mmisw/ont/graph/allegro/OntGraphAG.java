package org.mmisw.ont.graph.allegro;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.Db;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntUtil;
import org.mmisw.ont.Ontology;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.graph.IOntGraph;
import org.mmisw.ont.sparql.QueryResult;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agbase.AllegroGraphSerializer;
import com.franz.agbase.RDFN3Serializer;
import com.franz.agbase.SPARQLQuery;
import com.franz.agbase.TriplesIterator;
import com.franz.agbase.ValueSetIterator;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.drexel.util.rdf.JenaUtil;


/**
 * Handles the ontology graphs using AllegroGraph.
 * 
 * @author Carlos Rueda
 */
public class OntGraphAG implements IOntGraph {

	/** Servlet resource containing the model with properties for inference purposes, N-triples format */
	@SuppressWarnings("unused")
	private static final String INF_PROPERTIES_MODEL_NAME_NT = "inf_properties.nt";
	

	private final Log log = LogFactory.getLog(OntGraphAG.class);
	
	private String serverHost;
	private int serverPort;
	private String tripleStoreDir;
	private String tripleStoreName;

	private final Db db;
	private final AdminDispatcher adminDispatcher;
	
	private String aquaUploadsDir;
	
	
	/**
	 * AllegroGraph connection. Usage idiom:
	 * <pre>
	 *   Ag ag = new Ag();
	 *   try {
	 *       ...
	 *   }
	 *   finally {
	 *       ag.end();
	 *   }
	 * </pre>
	 */
	private class Ag {
		AllegroGraphConnection ags;
		AllegroGraph ts;
		
		Ag() throws ServletException {
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
				ts = ags.access(tripleStoreName, tripleStoreDir);
				log.info("CONNECTED");
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
	 * Creates an instance of this helper.
	 * 
	 * @param ontConfig Used at initialization to obtain the "uploads" directory, where the
	 *        actual ontology files are located.
	 *        
	 * @param db The database helper.
	 */
	public OntGraphAG(OntConfig ontConfig, Db db, AdminDispatcher adminDispatcher) {
		this.db = db;
		this.adminDispatcher = adminDispatcher;
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
		Ag _ag = new Ag();
		try {
			log.info("AllegroGraph Server version = " +_ag.ags.getServerVersion());
			String[] idxFlavors = _ag.ts.getIndexFlavors();
			log.info(" Index flavors = " +(idxFlavors == null ? "null" : Arrays.asList(idxFlavors)));
			log.info(" unindexed threshold = " +_ag.ts.getUnindexedThreshold());
			log.info(" #unindexed triples = " +_ag.ts.getUnindexedTripleCount());
			log.info(" #triples = " +_ag.ts.numberOfTriples());
		}
		catch (AllegroGraphException e) {
			log.error("Error with AlegroGraph.", e);
			throw new ServletException("Error with AlegroGraph.", e);
		}
		finally {
			_ag.end();
		}

		log.info("init complete.");
	}
	
	/** nothing done here */
	public void destroy() throws ServletException {
		// nothing
	}


	public void reindex(boolean wait) throws ServletException {
		Ag _ag = new Ag();
		try {
			_reindex(_ag, wait);
		}
		finally {
			_ag.end();
		}
	}
	
	private void _reindex(Ag _ag, boolean wait) throws ServletException {
		log.info("reindex called. wait=" +wait);
		long start = System.currentTimeMillis();
		try {
			_ag.ts.indexAllTriples(wait);
			_ag.debugIndexInfo();
			log.info("reindex completed (" +AgUtils.elapsedTime(start)+ ")");
		}
		catch (AllegroGraphException e) {
			log.error("Error reindexing", e);
			throw new ServletException("Error reindexing", e);
		}
	}
	
	/**
	 * Reinitializes the graph.
	 * @param withInference NOT USED. 
	 * @throws ServletException
	 */
	public void reinit(boolean withInference) throws ServletException {
		log.info("reinit called. withInference=" +withInference);
		log.info("Creating connection to triple store ...");
		Ag _ag = new Ag();
		try {
			// populate the triple store:
			long start = System.currentTimeMillis();
			long numberOfTriples = _doReInitModel(_ag);
			log.debug("triple store populated (" +AgUtils.elapsedTime(start)+ ").  " +
					"#triples= " +numberOfTriples);
			
			// index:
			boolean wait = true;
			_reindex(_ag, wait);
		}
		finally {
			_ag.end();
		}
	}
	
	/**
	 * Re-Inits the graph.
	 * @return number of triples. -1 if some error happens while obtaining this number.
	 * @throws ServletException
	 */
	private long _doReInitModel(Ag _ag) throws ServletException {
		
		log.debug("clearing triple store...");
		try {
			_ag.ts.clear();
			log.debug("clearing triple store...done.  #triples= " +_ag.ts.numberOfTriples());
		}
		catch (AllegroGraphException e) {
			log.error("Error clearing triple store", e);
			throw new ServletException("Error clearing triple store", e);
		}
		
		// get the list of (latest-version) ontologies:
		final boolean allVersions = false;
		List<Ontology> onts = db.getAllOntologies(allVersions);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			
			log.debug("About to load the following " +onts.size()+ " ontologies: ");
			for ( Ontology ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}
		
		for ( Ontology ontology : onts ) {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph");
			try {
				// NOTE: the graphId here is null; the graph relationships are added below.
				String graphId = null;
				
				boolean clearGraphFirst = false;  // the triple store starts empty; see above
				
				_loadOntology(_ag, ontology, graphId, full_path, clearGraphFirst);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		// enable inferencing:
		_loadSupportingStatements(_ag);
		
		// load internal resources (graph relationships, etc.):
		try {
			_loadInternalResources(_ag);
		}
		catch (AllegroGraphException e) {
			log.error("Error loading internal resources", e);
			// but continue.
		}
		
		long numberOfTriples = -1;
		try {
			numberOfTriples = _ag.ts.numberOfTriples();
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
	private void _loadSupportingStatements(Ag _ag) {
		try {
			String[][] namespaces = adminDispatcher.getSupportingNamespaces();
			for ( String[] ns : namespaces ) {
				_ag.ts.registerNamespace(ns[0], ns[1]);
			}
			
			String[][] statements = adminDispatcher.getSupportingStatements();
			for ( String[] statement : statements ) {
				_ag.ts.addStatement(statement[0], statement[1], statement[2]);
				log.info("Added statement: " +statement[0]+ " " +statement[1]+ " " +statement[2]);
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
	private void _loadInternalResources(Ag _ag) throws AllegroGraphException {
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
				
				_ag.ts.addStatement(sbj, prd, obj);
				log.info("Added statement: " +stmt);
			}
		}		
	}

	public void loadOntology(Ontology ontology, String graphId) throws Exception {
		Ag _ag = new Ag();
		try {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph: " +(graphId == null ? "(default graph)" : graphId));
			boolean clearGraphFirst = true;
			_loadOntology(_ag, ontology, graphId, full_path, clearGraphFirst);
			
			// lauch indexing of new triples in the background and return:
			boolean wait = false;
			_ag.ts.indexNewTriples(wait);
		}
		finally {
			_ag.end();
		}

	}

	/**
	 * Loads an ontology to the given graph.
	 * 
	 * @param _ag
	 * @param ontology
	 * @param graphId User-specified graph. Can be null.
	 * @param full_path
	 * @param clearGraphFirst true to remove all statements associated with the graph directly associated with the
	 *        ontology.
	 *        
	 * @return the URI of the user-specified graphId, if any.
	 */
	private String _loadOntology(Ag _ag, Ontology ontology, String graphId, String full_path, boolean clearGraphFirst) {
		
		String graphUri = null;
		
		String ontologyUri = ontology.getUri();
		String serialization;
		
		if ( USE_UNVERSIONED ) {
			OntModel model = JenaUtil.loadModel(full_path, false);

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
					return null;
				}
				log.info("To load Ont-resolvable ontology in graph.");
			}
			else {
				serialization = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
				log.info("To load re-hosted ontology in graph.");
			}
		}
		else {
			String absPath = "file:" + full_path;
			OntModel model = JenaUtil.loadModel(full_path, false);
			serialization = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		}
		
		// now, update graph with model captured in serialization
		try {
			// this is the graph for the ontology itself. 
			// All the statements in the ontology are associated with this graph.
			String ownGraph = "<" +ontologyUri+ ">";
			
			if ( clearGraphFirst ) {
				// first, remove all statements associated with the graph:
				if ( log.isDebugEnabled() ) {
					log.debug("Removing all statements in graph " +ownGraph+ " ...");
				}
				_ag.ts.removeStatements(null, null, null, ownGraph);
			}
			
			// now, create the new graph:
			AgUtils.parseWithTiming(_ag.ts, true, serialization, ownGraph);
			
			
			// now, add the subGraphOf relationship if graphId != null
			if ( graphId != null ) {
				String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
				graphUri = adminDispatcher.getWellFormedGraphUri(graphId);
				_addSubGraph(_ag, ownGraphUri, graphUri);
			}
			
		}
		catch (AllegroGraphException e) {
			log.error("Error parsing/loading RDF in graph.", e);
		}
		
		return graphUri;
	}
	
	/**
	 * Updates the graphs resource and then the triple store.
	 * @param _ag
	 * @param subGraphUri  Assumed to be well-formed
	 * @param superGraphUri  Assumed to be well-formed
	 * @throws AllegroGraphException
	 */
	private void _addSubGraph(Ag _ag, String subGraphUri, String superGraphUri) throws AllegroGraphException {
		
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
				
				_ag.ts.addStatement(sbj, prd, obj);
				log.info("Added statement: " +stmt);
			}
		}		
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
		Ag _ag = new Ag();
		try {
			return _executeQuery(_ag, sparqlQuery, form);
		}
		finally {
			_ag.end();
		}
	}
	
	
	private QueryResult _executeQuery(Ag _ag, String sparqlQuery, String form) throws Exception {
		QueryResult queryResult = new QueryResult();

		SPARQLQuery sq = new SPARQLQuery();
		sq.setTripleStore(_ag.ts);
		sq.setQuery(sparqlQuery);
		sq.setIncludeInferred(true);
		
		boolean useRun = false;
		
		
		if ( form == null ) {
			form = "html";
		}

		if ( form.equalsIgnoreCase("owl") || form.equalsIgnoreCase("rdf") ) {
			useRun = true;
			sq.setResultsFormat("sparql-xml");
			// TODO: contentType should be sparql-related
			queryResult.setContentType("Application/rdf+xml");
		}
		// else: TODO what other formats are possible?
		
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
				// TODO N3 (using CSV for now)
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInCsv(log, valSetIter);
			}
			else if ( form.equalsIgnoreCase("nt") ) {
				// TODO NTriples (using CSV for now)
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInCsv(log, valSetIter);
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
			
			if ( form.equalsIgnoreCase("html") ) {
				queryResult.setContentType("text/html");
				res = AgUtils.getResultInHtml(log, tripleIter);
			}
			else if ( form.equalsIgnoreCase("n3") ) {
				queryResult.setContentType("text/plain");
				AllegroGraphSerializer serializer = new RDFN3Serializer();
				serializer.setDestination(null); // ie., to string to be returned by run()
				Object resObj = serializer.run(tripleIter);
				res = String.valueOf(resObj);
			}
			else if ( form.equalsIgnoreCase("nt") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInNTriples(log, tripleIter);
//				AllegroGraphSerializer serializer = new NTriplesSerializer();
//				serializer.setDestination(null); // ie., to string to be returned by run()
//				Object resObj = serializer.run(tripleIter);
//				res = String.valueOf(resObj);
			}
			else if ( form.equalsIgnoreCase("csv") ) {
				queryResult.setContentType("text/plain");
				res = AgUtils.getResultInCsv(log, tripleIter);
			}
			else if ( form.equalsIgnoreCase("json") ) {
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
			log.debug("SHOULD NOT HAPPEN: unexpected type of query");
			queryResult.setIsEmpty(false);
			queryResult.setResult("Internal error: unexpected type of query. Please report this bug.");
		}

		return queryResult;
	}

}
