package org.mmisw.ont;

import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;


/**
 * A helper to handle the "big" graph of all existing ontologies.
 * 
 * @author Carlos Rueda
 */
public class OntGraph {
	
	private final Log log = LogFactory.getLog(OntGraph.class);

	private final OntConfig ontConfig;
	private final Db db;
	
	private Model _model;
	
	private String aquaUploadsDir;

	
	/**
	 * Creates an instance of this helper.
	 * @param ontConfig Used at initialization.
	 * @param db The database helper.
	 */
	OntGraph(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	public Model getModel() {
		return _model;
	}


	/**
	 * Initializes the graph.
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	void init() throws ServletException {
		log.info("init called.");
		
		aquaUploadsDir = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY);
		
		if ( _model == null ) {
			_doInitModel();
			log.info("init complete.");
		}
		else {
			log.debug("init: already initialized.");
		}
	}
	
	/**
	 * Reinitializes the graph.
	 * @throws ServletException
	 */
	void reinit() throws ServletException {
		log.info("reinit called.");
		_doInitModel();
		log.info("reinit complete.");
	}
	
	private void _doInitModel() throws ServletException {
		_model = ModelFactory.createDefaultModel();
		List<Ontology> onts = db.getOntologies();
		for ( Ontology ontology : onts ) {
			String full_path = aquaUploadsDir 
				+ "/" +ontology.file_path + "/" + ontology.filename;
		
			log.info("init: loading: " +full_path);
			String absPath = "file:" + full_path;
			
			try {
				
				_model.read(absPath, "", null);
				
//				// TODO processImports: true or false?
//				boolean processImports = false;
//				Model model_ = JenaUtil.loadModel(absPath, processImports);
//				_model.add(model_);
				
				log.info("added " + absPath + " to model");
			} 
			catch (Exception e) {
				log.error("Unable to add " + absPath + " to model");
			}
		}

		if ( log.isDebugEnabled() ) {
			_listStatements();
		}
	}

	
	private void _listStatements() {
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
