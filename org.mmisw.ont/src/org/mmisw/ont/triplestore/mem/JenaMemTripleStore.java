package org.mmisw.ont.triplestore.mem;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.triplestore.jena.JenaTripleStore;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Triple store implementation based on a Jena Model in memory.
 * 
 * @author Carlos Rueda
 */
public class JenaMemTripleStore extends JenaTripleStore {
	
	private final Log log = LogFactory.getLog(JenaMemTripleStore.class);
	
	/** the corresponding inference model after a _doInitModel(true) call. */
	private InfModel _infModel;
	
	/** the model with all the ontologies */
	private Model _model;
	
	
	/**
	 * @param ontConfig Used at initialization to obtain the "uploads" directory, where the
	 *        actual ontology files are located.
	 *        
	 * @param db The database helper.
	 */
	public JenaMemTripleStore(Db db) {
		super(db);
		log.debug(getClass().getSimpleName()+ " instance created.");
	}

	@Override
	protected void _createModel() {
		_model = ModelFactory.createDefaultModel();
		log.debug("_createModel: model created.");
	}


	@Override
	protected void _setInfModelNull() {
		_infModel = null;
	}
	
	/**
	 * 1) load the skos properties model into the base model _model
	 * 2) create reasoner and InfModel.
	 * @return the created InfModel
	 */
	@Override
	protected void _createInfModel() {
		//
		// 1) load the skos properties model into the base model _model:
		//
		String propsSrc = Util.getResource(INF_PROPERTIES_MODEL_NAME_N3);
		if ( propsSrc == null ) {
			return;
		}
		
		Model propsModel = ModelFactory.createDefaultModel();
		StringReader sr = new StringReader(propsSrc);
		propsModel.read(sr, "dummyBase", "N3");
		_model.add(propsModel);
		log.info("_createInfModel: Added properties model:\n\t" +propsSrc.replaceAll("\n", "\n\t"));

		
		//
		// 2) create reasoner and InfModel:
		//
		String rulesSrc = Util.getResource(INF_RULES_NAME);
		if ( rulesSrc == null ) {
			return;
		}
		log.info("_createInfModel: Creating InfModel with rules:\n\t" +rulesSrc.replaceAll("\n", "\n\t"));
		List<Rule> rules = Rule.parseRules(rulesSrc);
		Reasoner reasoner = new GenericRuleReasoner(rules);
		_infModel = ModelFactory.createInfModel(reasoner, _model);
	}

	
	@Override
	protected InfModel _getInfModel() {
		return _infModel;
	}

	@Override
	protected Model _getModel() {
		return _model;
	}

}
