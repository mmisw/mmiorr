package org.mmisw.ont.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.Db;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntUtil;
import org.mmisw.ont.Ontology;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.sparql.Sparql;

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

import edu.drexel.util.rdf.JenaUtil;

//
// Copy of OntGraph.java 894 11/25/09 7:43 PM
// except that it implements the new interface IOntGraph for refactoring purposes
// while developing other implementations.
//

/**
 * Handles the "big" graph of all registered ontologies.
 * 
 * @author Carlos Rueda
 */
public class OntGraphMem implements IOntGraph {
	
	private final Log log = LogFactory.getLog(OntGraphMem.class);
	
	/** Servlet resource containing the model with properties for inference purposes */
	private static final String INF_PROPERTIES_MODEL_NAME = "inf_properties.n3";
	
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
	 * @param ontConfig Used at initialization to obtain the "uploads" directory, where the
	 *        actual ontology files are located.
	 *        
	 * @param db The database helper.
	 */
	OntGraphMem(OntConfig ontConfig, Db db) {
		this.db = db;
	}

	/**
	 * Gets the model containing the graph. If the graph has been initialized with
	 * inference, then the corresponding InfModel is returned; otherwise the raw model.
	 * 
	 * @return the model as described.
	 */
	private Model _getModel() throws Exception {
		return _infModel != null ? _infModel : _model;
	}
	
	/**
	 * Returns null in this implementation.
	 */
	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		QueryResult queryResult = Sparql.executeQuery(_getModel(), sparqlQuery, form);
		return queryResult;
	}



	/**
	 * Initializes the graph with all latest version of the ontologies 
	 * as returned by {@link org.mmisw.ont.Db#getAllOntologies(boolean) with false argument}.
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
			final boolean withInference = true;
			log.info("init called. withInference=" +withInference);
			
			aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();
			_doInitModel(withInference);
			log.info("init complete.");
		}
		else {
			log.debug("init: already initialized (withInference = " +(_infModel != null)+ ")");
		}
	}

	/**
	 *  Nohing done in this implementation.
	 */
	public void reindex(boolean wait) throws ServletException {
		
	}
	

	/**
	 * Reinitializes the graph.
	 * @param withInference true to enable inference.
	 * @throws ServletException
	 */
	public void reinit(boolean withInference) throws ServletException {
		log.info("reinit called. withInference=" +withInference);
		_doInitModel(withInference);
		log.info("reinit complete.");
	}
	
	/**
	 * Inits the _model and, if withInference is true, also the _infModel.
	 * @param withInference true to create the inference model
	 * @throws ServletException
	 */
	private void _doInitModel(boolean withInference) throws ServletException {
		_infModel = null;  // make sure loadOntology(ontology) below does not use _infModel
		
		_model = ModelFactory.createDefaultModel();
		
		// get the list of (latest-version) ontologies:
		// fixed Issue 223: ontology graph with all versions
		// now using new correct method to obtain the latest versions:
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
				_loadOntology(ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		log.info("size of base model: " +_model.size());
		
		if ( withInference ) {
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
		}
		else {
			_infModel = null;
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
	 * helper method to retrieve the contents of a resource in the classpath .
	 */
	private String _getResource(String resourceName) {
		InputStream infRulesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
		if ( infRulesStream == null ) {
			log.error(resourceName+ ": resource not found -- check classpath");
			return null;
		}
		StringWriter output = new StringWriter();
		try {
			IOUtils.copy(infRulesStream, output);
			return output.toString();
		}
		catch (IOException e) {
			log.error(resourceName+ ": cannot read resource", e);
			return null;
		}
		finally {
			IOUtils.closeQuietly(infRulesStream);
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
		String propsSrc = _getResource(INF_PROPERTIES_MODEL_NAME);
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
		String rulesSrc = _getResource(INF_RULES_NAME);
		if ( rulesSrc == null ) {
			return null;
		}
		log.info("Creating InfModel with rules:\n\t" +rulesSrc.replaceAll("\n", "\n\t"));
		List<?> rules = Rule.parseRules(rulesSrc);
		Reasoner reasoner = new GenericRuleReasoner(rules);
		InfModel im = ModelFactory.createInfModel(reasoner, _model);
		return im;
	}

	/**
	 * Loads the given model into the graph.
	 * If inference is enabled, then it updates the corresponding inference model.
	 * @param ontology
	 */
	public void loadOntology(Ontology ontology) throws Exception {
		String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
		log.info("Loading: " +full_path+ " in graph");
		_loadOntology(ontology, full_path);
	}

	private void _loadOntology(Ontology ontology, String full_path) {
		final Model model2update = _infModel != null ? _infModel : _model;
		if ( USE_UNVERSIONED ) {
			OntModel model = JenaUtil.loadModel(full_path, false);

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
}
