package org.mmisw.ont.triplestore.jena;

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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

/**
 * Base class for triple store implementations based on Jena Models.
 * 
 * @author Carlos Rueda
 */
public abstract class JenaTripleStore implements ITripleStore {
	
	private final Log log = LogFactory.getLog(JenaTripleStore.class);
	
	/** Servlet resource containing the model with properties for inference purposes, N3 format */
	protected static final String INF_PROPERTIES_MODEL_NAME_N3 = "inf_properties.n3";
	

	/** Servlet resource containing the rules for inference purposes */
	protected static final String INF_RULES_NAME = "inf_rules.txt";


	private final Db db;
	
	private String aquaUploadsDir;

	
	/**
	 * Base constructor.
	 * 
	 * @param db The database helper.
	 */
	protected JenaTripleStore(Db db) {
		this.db = db;
	}

	
	/** 
	 * Creates the main model with all the ontologies.
	 * Subclasses decide how to create this model.
	 */
	protected abstract void _createModel();
	
	/** 
	 * Creates the corresponding inference model after a _doInitModel(true) call. 
	 * Subclasses decide how to create this model.
	 */
	protected abstract void _createInfModel();

	/**
	 * Getter to the main model.
	 */
	protected abstract Model _getModel();
	
	/**
	 * Getter to the inference model.
	 */
	protected abstract InfModel _getInfModel();
	
	/**
	 * Should nullify the inference model so {@link #_getInfModel()} should return null after this. 
	 */
	protected abstract void _setInfModelNull() ;

	
	/**
	 * Gets the size of the main model.
	 */
	private long _getModelSize() {
		return _getModel().size();
	}

	/**
	 * Gets the model in place for updates. If this object has been initialized with
	 * inference, then the corresponding InfModel is returned; otherwise the raw model.
	 */
	private Model _getEffectiveModel()  {
		InfModel infModel = _getInfModel();
		return infModel != null ? infModel : _getModel();
	}
	
	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		log.debug(getClass().getSimpleName()+ " executeQuery called.");
		QueryResult queryResult = Sparql.executeQuery(_getEffectiveModel(), sparqlQuery, form);
		return queryResult;
	}



	/**
	 * Called by {@link #init()} as initial step during init of this object.
	 * This method should always be called even if overriden.
	 */
	protected void _preInit() throws ServletException {
		aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();
		log.info("_preInit: aquaUploadsDir: " +aquaUploadsDir);
	}

	/**
	 * Called by {@link #init()} as a second step during init of this object.
	 * In this class, this method calls {@link #_doReInit(boolean)}.
	 * 
	 * <p>
	 * A subclass may completely override this method to perform a different init sequence.
	 */
	protected void _mainInit() throws ServletException {
		final boolean withInference = true;
		log.info("_mainInit called. withInference=" +withInference);
		_doReInit(withInference);
		log.info("_mainInit: complete.");
	}
	
	/**
	 * Calls <code>{{@link #_preInit()}; {@link #_mainInit()};}</code> if this object has not been
	 * initialized yet; otherwise, does nothing.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		if ( _getModel() == null ) {
			_preInit();
			_mainInit();
			log.info("init complete.");
		}
		else {
			log.debug("init: already initialized (withInference = " +(_getInfModel() != null)+ ")");
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
	 * Reinitializes the triple store.
	 * @param withInference true to enable inference.
	 * @throws ServletException
	 */
	public void reinit(boolean withInference) throws ServletException {
		log.info("reinit called. withInference=" +withInference);
		_doReInit(withInference);
		log.info("reinit complete.");
	}
	
	/**
	 * In this class, this method resets the models (by calling {@link #_createModel()}
	 * and {@link #_createInfModel()}) and loads all latest versions of the ontologies 
	 * as returned by {@link org.mmisw.ont.db.Db#getAllOntologies(boolean) with false argument}.
	 * 
	 * <p>
	 * A subclass may do some preliminary preparation and then call super._doReInit(withInference).
	 * 
	 * @param withInference
	 * @throws ServletException
	 */
	protected void _doReInit(boolean withInference) throws ServletException {
		if ( true ) {
			_doReInit2(withInference); // new impl
		}
		else {
			_doReInit1(withInference);
		}
	}
	
	/**
	 * Inits the _model and, if withInference is true, also the _infModel.
	 * @param withInference true to create the inference model
	 * @throws ServletException
	 */
	private void _doReInit1(boolean withInference) throws ServletException {
		_setInfModelNull();  // make sure loadOntology(ontology) below does not use _infModel
		
		_createModel();
		
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
			log.info("Loading: " +full_path+ " in triple store;  uri=" +ontology.getUri());
			try {
				_loadOntology(ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		log.info("size of base model: " +_getModelSize());
		
		if ( withInference ) {
			log.info("starting creation of inference model...");
			long startTime = System.currentTimeMillis();
			_createInfModel();
			if ( _getInfModel() != null ) {
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
			_setInfModelNull();
		}

		if ( false && log.isDebugEnabled() ) {
			log.debug("_listStatements:");
			StmtIterator iter = _getModel().listStatements();
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
	 * Version 2: creates first the InfModel and then add all the ontologies to
	 * this InfMode.
	 * Inits the _model and, if withInference is true, also the _infModel.
	 * @param withInference true to create the inference model
	 * @throws ServletException
	 */
	private void _doReInit2(boolean withInference) throws ServletException {
		
		_createModel();
		if ( withInference ) {
			log.info("_doInitModel2: starting creation of inference model...");
			long startTime = System.currentTimeMillis();
			_createInfModel();
			if ( _getInfModel() != null ) {
				long endTime = System.currentTimeMillis();
				log.info("_doInitModel2: creation of inference model completed successfully. (" +(endTime-startTime)+ " ms)");
			}
			else {
				// Log.error messages have been already generated.
			}
		}
		else {
			_setInfModelNull();
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
			log.info("Loading: " +full_path+ " in triple store;  uri=" +ontology.getUri());
			try {
				_loadOntology(ontology, full_path);
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " (continuing..)", ex);
			}
		}
		
		log.info("size of base model: " +_getModelSize());
	}
	
	/**
	 * Loads the given model into the triple store.
	 * If inference is enabled, then it updates the corresponding inference model.
	 * @param ontology
	 * @param graphId IGNORED
	 */
	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
		log.info("Loading: " +full_path+ " in triple store;  uri=" +ontology.getUri());
		_loadOntology(ontology, full_path);
	}

	private void _loadOntology(OntologyInfo ontology, String full_path) {
		final Model model2update = _getEffectiveModel();
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
				log.info("Ont-resolvable ontology loaded in triple store.");
			}
			else {
				model2update.add(model);
				log.info("Re-hosted ontology loaded in triple store.");
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
