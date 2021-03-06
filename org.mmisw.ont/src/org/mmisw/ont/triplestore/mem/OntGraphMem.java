package org.mmisw.ont.triplestore.mem;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.sparql.Sparql;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.util.OntUtil;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

//
// Copy of org.mmisw.ont.OntGraph.java 894 11/25/09 7:43 PM
// except that it implements the new interface IOntGraph (initially for refactoring purposes)
// while developing other implementations.
//

/**
 * Handles the "big" graph of all registered ontologies.
 * 
 * @author Carlos Rueda
 */
@Deprecated
public class OntGraphMem implements ITripleStore {
	
	private final Log log = LogFactory.getLog(OntGraphMem.class);
	
	/** Servlet resource containing the model with properties for inference purposes, N3 format */
	private static final String INF_PROPERTIES_MODEL_NAME_N3 = "inf_properties.n3";
	

	/** Servlet resource containing the rules for inference purposes */
	private static final String INF_RULES_NAME = "inf_rules.txt";

	/** the corresponding inference model after a _doInitModel(true) call. */
	private InfModel _infModel;
	
	
	private final Db db;
	
	/** the model with all the ontologies */
	private Model _model;
	
	
	private String aquaUploadsDir;

	
	/**
	 * Creates an instance of this helper.
	 * 
	 * @param db The database helper.
	 */
	public OntGraphMem(Db db) {
		this.db = db;
		log.debug(getClass().getSimpleName()+ " instance created.");
	}

	/**
	 * Gets the model containing the graph.  If this object has been initialized with
	 * inference and infer is true, then the corresponding InfModel is returned; otherwise the raw model.
	 * 
	 * @return the model as described.
	 */
	private Model _getModel(boolean infer) throws Exception {
		if (_infModel == null || !infer) {
			return _model;
		}
		else {
			return _infModel;
		}
	}
	
	public QueryResult executeQuery(String sparqlQuery, boolean infer, String form) throws Exception {
		QueryResult queryResult = Sparql.executeQuery(_getModel(infer), sparqlQuery, form);
		return queryResult;
	}



	/**
	 * Initializes the graph with all latest version of the ontologies 
	 * as returned by {@link org.mmisw.ont.db.Db#getAllOntologies(boolean) with false argument}.
	 * 
	 * <p>
	 * Inference is enabled by default. If inference should be disabled,
	 * call {@link #reinit(boolean)} with a false argument.
	 * 
	 * <p>
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		if ( _model == null ) {
			log.info("init called.");
			
			aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();
			_doInitModel();
			log.info("init complete.");
		}
		else {
			log.debug("init: already initialized.");
		}
	}

	/** nothing done here */
	public void destroy() throws ServletException {
		// nothing
	}

	/**
	 *  Nothing done in this implementation.
	 */
	public void reindex(boolean wait) throws ServletException {
		
	}
	
	/**
	 * Clears the triple store.
	 * @throws ServletException
	 */
	public void clear() throws ServletException {
		// TODO just most obvious implementation -- NOT TESTED
		_model = ModelFactory.createDefaultModel();
		_infModel = _createInfModel();
	}
	
	/**
	 * Reinitializes the graph.
	 * @throws ServletException
	 */
	public void reinit() throws ServletException {
		log.info("reinit called.");
		_doInitModel();
		log.info("reinit complete.");
	}
	
	private void _doInitModel() throws ServletException {
		if ( true ) {
			_doInitModel2();
		}
		else {
			_doInitModel1();
		}
	}

	/**
	 * Version 2: creates first the InfModel and then add all the ontologies to
	 * this InfMode.
	 * Inits the _model and also the _infModel.
	 * @throws ServletException
	 */
	private void _doInitModel2() throws ServletException {
		
		_model = ModelFactory.createDefaultModel();
		log.info("_doInitModel2: starting creation of inference model...");
		long startTime = System.currentTimeMillis();
		_infModel = _createInfModel();
		if ( _infModel != null ) {
			long endTime = System.currentTimeMillis();
			log.info("_doInitModel2: creation of inference model completed successfully. (" +(endTime-startTime)+ " ms)");
		}
		else {
			// Log.error messages have been already generated.
		}
		
		// get the list of (latest-version) ontologies:
		// fixed Issue 223: ontology graph with all versions
		// now using new correct method to obtain the latest versions:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			
			log.debug("About to load the following ontologies: ");
			for ( OntologyInfo ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}
		
		for ( OntologyInfo ontology : onts ) {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph;  uri=" +ontology.getUri());
			try {
				_loadOntology(ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		log.info("size of base model: " +_model.size());
	}
	
	/**
	 * Inits the _model and also the _infModel.
	 * @throws ServletException
	 */
	private void _doInitModel1() throws ServletException {
		_infModel = null;  // make sure loadOntology(ontology) below does not use _infModel
		
		_model = ModelFactory.createDefaultModel();
		
		// get the list of (latest-version) ontologies:
		// fixed Issue 223: ontology graph with all versions
		// now using new correct method to obtain the latest versions:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			
			log.debug("About to load the following ontologies: ");
			for ( OntologyInfo ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}
		
		for ( OntologyInfo ontology : onts ) {
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph;  uri=" +ontology.getUri());
			try {
				_loadOntology(ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		log.info("size of base model: " +_model.size());
		
		log.info("starting creation of inference model...");
		long startTime = System.currentTimeMillis();
		_infModel = _createInfModel();
		if ( _infModel != null ) {
			long endTime = System.currentTimeMillis();
			log.info("creation of inference model completed successfully. (" +(endTime-startTime)+ " ms)");

			// this takes time -- do not do it for now
			//log.info("estimated size of inference model: " +_infModel.size());
		}
		else {
			// Log.error messages have been already generated.
		}

		if ( false && log.isDebugEnabled() ) {
			log.debug("_listStatements:");
			StmtIterator iter = _model.listStatements();
			while (iter.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
				Resource sjt = sta.getSubject();
				log.debug("      " + 
						PrintUtil.print(sjt)
						+ "   " +
						PrintUtil.print(sta.getPredicate().getURI())
						+ "   " +
						PrintUtil.print(sta.getObject().toString())
				);
			}
		}
	}
	

	/**
	 * 1) load the skos properties model into the base model _model
	 * 2) create reasoner and InfModel.
	 * @return the created InfModel
	 */
	private InfModel _createInfModel() {
		//
		// 1) load the skos properties model into the base model _model:
		//
		String propsSrc = Util.getResource(INF_PROPERTIES_MODEL_NAME_N3);
		if ( propsSrc == null ) {
			return null;
		}
		
		Model propsModel = ModelFactory.createDefaultModel();
		StringReader sr = new StringReader(propsSrc);
		propsModel.read(sr, "dummyBase", "N3");
		_model.add(propsModel);
		log.info("Added properties model:\n\t" +propsSrc.replaceAll("\n", "\n\t"));

		
		//
		// 2) create reasoner and InfModel:
		//
		String rulesSrc = Util.getResource(INF_RULES_NAME);
		if ( rulesSrc == null ) {
			return null;
		}
		log.info("Creating InfModel with rules:\n\t" +rulesSrc.replaceAll("\n", "\n\t"));
		List<Rule> rules = Rule.parseRules(rulesSrc);
		Reasoner reasoner = new GenericRuleReasoner(rules);
		InfModel im = ModelFactory.createInfModel(reasoner, _model);
		return im;
	}

	/**
	 * Loads the given model into the graph.
	 * If inference is enabled, then it updates the corresponding inference model.
	 * @param ontology
	 * @param graphId IGNORED
	 */
	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
		log.info("Loading: " +full_path+ " in graph;  uri=" +ontology.getUri());
		_loadOntology(ontology, full_path);
	}

	private void _loadOntology(OntologyInfo ontology, String full_path) {
		final Model model2update = _infModel != null ? _infModel : _model;
		if ( USE_UNVERSIONED ) {
			OntModel model = JenaUtil2.loadModel("file:" +full_path, false);

			if ( OntUtil.isOntResolvableUri(ontology.getUri()) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontology.getUri());
					OntModel unversionedModel = UnversionedConverter.getUnversionedModel(model, mmiUri);
					model2update.add(unversionedModel);
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return;
				}
				log.info("Ont-resolvable ontology loaded in graph.");
			}
			else {
				model2update.add(model);
				log.info("Re-hosted ontology loaded in graph.");
			}
		}
		else {
			String absPath = "file:" + full_path;
			try {
				model2update.read(absPath, "", null);
			} 
			catch (Exception e) {
				log.error("Unable to add " + absPath + " to model");
			}
		}
		
	}

	/**
	 * NOT IMPLEMENTED (focus is on OntGrapghAG implementation)
	 */
	public void removeOntology(OntologyInfo ontology) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
