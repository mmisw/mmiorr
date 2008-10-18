package org.mmisw.ont;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmi.ont.util.Registry;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;


/**
 * Handles the "big" graph of all existing ontologies.
 * 
 * <p>
 * Currently, it uses main memory.
 * 
 * 
 * @author Carlos Rueda
 */
class OntGraph {
	
	private static final Log log = LogFactory.getLog(OntGraph.class);

	private static Registry _registry;
	
	
	/**
	 * Initializes the registry.
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	static void initRegistry() throws ServletException {
		log.debug("initRegistry called.");
		
		if ( _registry == null ) {
			_registry = doInitRegistry();
			log.debug("initRegistry complete.");
		}
		else {
			log.debug("initRegistry: already initialized.");
		}
	}
	
	static void reInitRegistry() throws ServletException {
		log.debug("reInitRegistry called.");
		_registry = doInitRegistry();
		log.debug("reInitRegistry complete.");
	}
	
	private static Registry doInitRegistry() throws ServletException {
		Registry registry = new Registry();
		List<Ontology> onts = Db.getOntologies();
		for ( Ontology ontology : onts ) {
			String full_path = "/Users/Shared/bioportal/resources/uploads/" 
				+ontology.file_path + "/" + ontology.filename;
		
			log.debug("init registry: loading " +full_path+ "...");
			String absPath = "file:" + full_path;
			registry.addModel(absPath);
			log.debug("... LOADED.");			
		}
		return registry;
	}

	static Registry getRegistry() throws ServletException {
		log.debug("getRegistry called.");
		
		if ( _registry == null ) {
			initRegistry();
		}
		
		return _registry;
	}

	
	
	static String getRDF(String sparqlQuery) {
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

	private OntGraph() {}
}
