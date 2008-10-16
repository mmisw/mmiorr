package org.mmisw.ont;

import java.io.StringWriter;
import java.util.List;

import javax.servlet.ServletException;

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
	
	
	private static Registry _registry;
	
	
	/**
	 * Initializes the registry.
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	static void initRegistry() throws ServletException {
		System.out.println("OntGraph.initRegistry called.");
		
		if ( _registry == null ) {
			_registry = doInitRegistry();
			System.out.println("\nOntGraph.initRegistry complete.");
		}
		else {
			System.out.println("\nOntGraph.initRegistry: already initialized.");
		}
	}
	
	static void reInitRegistry() throws ServletException {
		System.out.println("OntGraph.reInitRegistry called.");
		_registry = doInitRegistry();
		System.out.println("\nOntGraph.reInitRegistry complete.");
	}
	
	private static Registry doInitRegistry() throws ServletException {
		Registry registry = new Registry();
		List<Ontology> onts = Db.getOntologies();
		for ( Ontology ontology : onts ) {
			String full_path = "/Users/Shared/bioportal/resources/uploads/" 
				+ontology.file_path + "/" + ontology.filename;
		
			System.out.print("OntGraph.initRegistry: processing " +full_path+ "... ");
			System.out.flush();
			String absPath = "file:" + full_path;
			
			registry.addModel(absPath);
			System.out.println("LOADED");
		}
		return registry;
	}

	static Registry getRegistry() throws ServletException {
		System.out.println("OntGraph.getRegistry called.");
		
		if ( _registry == null ) {
			initRegistry();
		}
		
		return _registry;
	}

	
	
	static String getRDF(String sparqlQuery) {
		System.out.println("OntGraph.getRDF: query = " +sparqlQuery);

		Query query = QueryFactory.create(sparqlQuery);
		System.out.println("sparqlQuery " + sparqlQuery);

		QueryExecution qExec = QueryExecutionFactory.create(query, 
				_registry.getModel());
		Model model_ = qExec.execConstruct();
		StringWriter writer = new StringWriter();
		model_.getWriter().write(model_, writer, null);

		String result = writer.getBuffer().toString();
		System.out.println("RESULT:\n" + result);
		return result;
	}

	private OntGraph() {}
}
