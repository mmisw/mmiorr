package org.mmisw.ont.graph.allegro;

import java.net.URISyntaxException;
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
import org.mmisw.ont.graph.IOntGraph;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.sparql.Sparql;
import org.mmisw.ont.util.Util;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agbase.AllegroGraphSerializer;
import com.franz.agbase.RDFN3Serializer;
import com.franz.agbase.SPARQLQuery;
import com.franz.agbase.TriplesIterator;
import com.franz.agbase.ValueSetIterator;
import com.franz.agjena.AllegroGraphGraphMaker;
import com.franz.agjena.AllegroGraphModel;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

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
	
	/** SKOS properties */
	private static final String[][] SKOS_PROPS = {
			{ "!skos:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos:exactMatch", "!rdf:type", "!owl:Symmetric" },
			
			{ "!skos:closeMatch", "!rdf:type", "!owl:Symmetric" },

			{ "!skos:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos:broadMatch", "!owl:inverseOf", "!skos:narrowMatch" },

			{ "!skos:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
			
			{ "!skos:relatedMatch", "!rdf:type", "!owl:Symmetric" },
	};


	/** Servlet resource containing the rules for inference purposes */
	private static final String INF_RULES_NAME = "inf_rules.txt";

	private final Log log = LogFactory.getLog(OntGraphAG.class);
	
	private String serverHost;
	private int serverPort;
	private String tripleStoreDir;
	private String tripleStoreName;

	private final Db db;
	
	private String aquaUploadsDir;
	
	
	/**
	 * AllegroGraph connection. Use idiom:
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
	public OntGraphAG(OntConfig ontConfig, Db db) {
		this.db = db;
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
				_loadOntology(_ag, ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		// enable inferencing:
		_loadSkosProperties(_ag);
		
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
	 * load the skos properties model
	 */
	private void _loadSkosProperties(Ag _ag) {
		try {
			_ag.ts.registerNamespace("skos","http://www.w3.org/2008/05/skos#");
			for (int i = 0; i < SKOS_PROPS.length; i++) {
				_ag.ts.addStatement(SKOS_PROPS[i][0], SKOS_PROPS[i][1], SKOS_PROPS[i][2]);
				log.info("Added statement: " +SKOS_PROPS[i][0]+ " " +SKOS_PROPS[i][1]+ " " +SKOS_PROPS[i][2]);
			}
		}
		catch (AllegroGraphException e) {
			log.error("Error adding statements to graph.", e);
		}
	}

	
	/**
	 * Create reasoner and InfModel.
	 * @return the created InfModel
	 */
	private InfModel _createInfModel(Model model) {
		String rulesSrc = Util.getResource(log, INF_RULES_NAME);
		if ( rulesSrc == null ) {
			return null;
		}
		log.info("Creating InfModel with rules:\n\t" +rulesSrc.replaceAll("\n", "\n\t"));
		List<?> rules = Rule.parseRules(rulesSrc);
		Reasoner reasoner = new GenericRuleReasoner(rules);
		InfModel im = ModelFactory.createInfModel(reasoner, model);
		return im;
	}

	
	/**
	 * Loads the given model into the graph.
	 * @param ontology
	 */
	public void loadOntology(Ontology ontology) throws Exception {
		Ag _ag = new Ag();
		try {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph");
			_loadOntology(_ag, ontology, full_path);
		}
		finally {
			_ag.end();
		}

	}

	private void _loadOntology(Ag _ag, Ontology ontology, String full_path) {
		
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
					return;
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
			Object graph = "<" +ontologyUri+ ">";
			AgUtils.parseWithTiming(_ag.ts, true, serialization, graph);
		}
		catch (AllegroGraphException e) {
			log.error("Error parsing/loading RDF in graph.", e);
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

	
	@SuppressWarnings("unused")
	private QueryResult executeQuery2(String sparqlQuery, String form) throws Exception {
		Ag _ag = new Ag();
		try {
			Model model = _getModel(_ag);

			if ( false ) {
				// NOTE: this is disabled--very poor performance
				//
				// Create Jena inference model
				// TODO: there is perhaps a mechanism to encode the rules in the triple store
				// so it's not necessary to associate them every time a query is issued.
				//
				long start = System.currentTimeMillis();
				model = _createInfModel(model);
				log.debug("Inference model created in " +AgUtils.elapsedTime(start));
			}
			
			QueryResult queryResult = Sparql.executeQuery(model, sparqlQuery, form);
			return queryResult;
		}
		finally {
			_ag.end();
		}
	}

	private Model _getModel(Ag _ag) throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("getModel: getting model from graph..");
		}
		
	    GraphMaker maker = new AllegroGraphGraphMaker(_ag.ts);
	    Graph defaultGraph = maker.getGraph();
	    Model model = new AllegroGraphModel(defaultGraph);
	    
	    if ( log.isDebugEnabled() ) {
	    	log.debug("Getting model from graph... Done.");
	    }
	    
		return model;
	}

}
