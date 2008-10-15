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
	
	
	private static Registry REGISTRY;
	
	
	/**
	 * Initializes the registry.
	 * Does nothing if already initialized.
	 * @throws ServletException
	 */
	static void initRegistry() throws ServletException {
		System.out.println("OntGraph.initRegistry called.");
		
		if ( REGISTRY == null ) {
			REGISTRY = doInitRegistry();
			System.out.println("\nOntGraph.initRegistry complete.");
		}
		else {
			System.out.println("\nOntGraph.initRegistry: already initialized.");
		}
	}
	
	static void reInitRegistry() throws ServletException {
		System.out.println("OntGraph.reInitRegistry called.");
		REGISTRY = doInitRegistry();
		System.out.println("\nOntGraph.reInitRegistry complete.");
	}
	
	private static Registry doInitRegistry() throws ServletException {
		Registry registry = new Registry();
		List<Ontology> onts = Db.getOntologies();
		for ( Ontology ontology : onts ) {
			String full_path = "/Users/Shared/bioportal/resources/uploads/" 
				+ontology.file_path + "/" + ontology.filename;
		
			System.out.print("OntGraph.initRegistry: processing " +full_path+ "...");
			System.out.flush();
			String absPath = "file:" + full_path;
			
			REGISTRY.addModel(absPath);
			System.out.print("[loaded]");
		}
		return registry;
	}

	static Registry getRegistry() throws ServletException {
		System.out.println("OntGraph.getRegistry called.");
		
		if ( REGISTRY == null ) {
			initRegistry();
		}
		
		return REGISTRY;
	}

	
	
	static String getRDF(String SPARQLQuery) {
		System.out.println("OntGraph.getRDF: query = " +SPARQLQuery);

		Query query = QueryFactory.create(SPARQLQuery);
		System.out.println("SPARQLQuery " + SPARQLQuery);

		QueryExecution qExec = QueryExecutionFactory.create(query, REGISTRY
				.getModel());
		Model model_ = qExec.execConstruct();
		StringWriter writer = new StringWriter();
		model_.getWriter().write(model_, writer, null);

		String result = writer.getBuffer().toString();
		System.out.println("RESULT: " + result);
		return result;
	}

	private OntGraph() {}
}
