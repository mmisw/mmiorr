package org.mmisw.ont;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmi.ont.util.Registry;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;


/**
 * A helper to handle the "big" graph of all existing ontologies.
 * 
 * @author Carlos Rueda
 */
public class OntGraph {
	
	private final Log log = LogFactory.getLog(OntGraph.class);

	private final OntConfig ontConfig;
	private final Db db;
	
	private Registry _registry;

	
	/**
	 * Creates an instance of this helper.
	 * @param ontConfig Used at initialization.
	 * @param db The database helper.
	 */
	OntGraph(OntConfig ontConfig, Db db) {
		this.ontConfig = ontConfig;
		this.db = db;
	}

	
	/**
	 * Initializes the graph.
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	void initRegistry() throws ServletException {
		log.info("initRegistry called.");
		
		if ( _registry == null ) {
			_registry = doInitRegistry();
			log.info("initRegistry complete.");
		}
		else {
			log.debug("initRegistry: already initialized.");
		}
	}
	
	void reInitRegistry() throws ServletException {
		log.info("reInitRegistry called.");
		_registry = doInitRegistry();
		log.info("reInitRegistry complete.");
	}
	
	private Registry doInitRegistry() throws ServletException {
		String aquaUploadsDir = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY);
		
		Registry registry = new Registry();
		List<Ontology> onts = db.getOntologies();
		for ( Ontology ontology : onts ) {
			String full_path = aquaUploadsDir 
				+ "/" +ontology.file_path + "/" + ontology.filename;
		
			log.info("init registry: loading " +full_path);
			String absPath = "file:" + full_path;
			registry.addModel(absPath);
		}
		return registry;
	}

	Registry getRegistry() throws ServletException {
		if ( _registry == null ) {
			initRegistry();
		}
		return _registry;
	}

	
	
	public String getRDF(String sparqlQuery) {
		log.debug("getRDF: query string = [" +sparqlQuery+ "]");

		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExec = QueryExecutionFactory.create(query, 
				_registry.getModel());
		Model model_ = qExec.execConstruct();
		StringWriter writer = new StringWriter();
		model_.getWriter().write(model_, writer, null);

		String result = writer.getBuffer().toString();
		log.debug("getRDF: result = [" +result+ "]");
		return result;
	}

}
