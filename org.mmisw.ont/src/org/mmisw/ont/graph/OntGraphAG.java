package org.mmisw.ont.graph;

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
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.sparql.Sparql;
import org.mmisw.ont.util.Util;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agjena.AllegroGraphGraphMaker;
import com.franz.agjena.AllegroGraphModel;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.ontology.OntModel;
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
	private static final String INF_PROPERTIES_MODEL_NAME_NT = "inf_properties.nt";

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
			ags = new AllegroGraphConnection();
			ags.setHost(serverHost);
			ags.setPort(serverPort);
			
			try {
				ags.enable();
			} 
			catch (Exception e) {
				throw new ServletException("Error connecting to triple store server.", e);
			}

			try {
				ts = ags.access(tripleStoreName, tripleStoreDir);
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
	OntGraphAG(OntConfig ontConfig, Db db) {
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
		log.info("reindex called. wait=" +wait);
		Ag _ag = new Ag();
		long start = System.currentTimeMillis();
		try {
			_ag.ts.indexAllTriples(wait);
			_ag.debugIndexInfo();
		}
		catch (AllegroGraphException e) {
			log.error("Error reindexing", e);
			throw new ServletException("Error reindexing", e);
		}
		finally {
			_ag.end();
		}
		
		log.info("reindex completed (" +AgUtils.elapsedTime(start)+ ")");
		
	}
	
	/**
	 * Reinitializes the graph.
	 * @param withInference NOT USED. 
	 * @throws ServletException
	 */
	public void reinit(boolean withInference) throws ServletException {
		log.info("reinit called. withInference=" +withInference);
		Ag _ag = new Ag();
		try {
			long start = System.currentTimeMillis();
			long numberOfTriples = _doReInitModel(_ag);
			log.debug("reinit completed (" +AgUtils.elapsedTime(start)+ ").  " +
					"#triples= " +numberOfTriples);
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
			
			log.debug("About to load the following ontologies: ");
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
		
		//
		// 1) load the skos properties model into the base model _model:
		//
		String propsSrc = Util.getResource(log, INF_PROPERTIES_MODEL_NAME_NT);
		if ( propsSrc == null ) {
			return;
		}
		
		// now, update graph with model captured in serialization
		try {
			AgUtils.parseWithTiming(_ag.ts, false, propsSrc);
		}
		catch (AllegroGraphException e) {
			log.error("Error parsing/loading RDF in graph.", e);
		}

		log.info("Added properties model:\n\t" +propsSrc.replaceAll("\n", "\n\t"));

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
		String serialization;
		
		if ( USE_UNVERSIONED ) {
			OntModel model = JenaUtil.loadModel(full_path, false);

			if ( OntUtil.isOntResolvableUri(ontology.getUri()) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontology.getUri());
					OntModel unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
					
					serialization = JenaUtil2.getOntModelAsString(unversionedModel, "RDF/XML-ABBREV");
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
			AgUtils.parseWithTiming(_ag.ts, true, serialization);
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
