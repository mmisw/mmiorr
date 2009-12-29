package org.mmisw.ont.graph;

import javax.servlet.ServletException;

import org.mmisw.ont.Ontology;
import org.mmisw.ont.sparql.QueryResult;


/**
 * Interface to handle the ontology graphs.
 * 
 * @author Carlos Rueda
 */
public interface IOntGraph {

	/** 
	 * Load the unversioned form of the last version of the ontologies?
	 * This is set to true. 
	 * (Can be set to false to get the original behavior, which was load the "versioned" form.)
	 */
	final boolean USE_UNVERSIONED = true;

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
	public void init() throws ServletException;
	
	/**
	 * Called by OntServlet when it is destroyed.
	 * @throws ServletException
	 */
	public void destroy() throws ServletException;
	
	/**
	 * Reinitializes the graph.
	 * @param withInference true to enable inference.
	 * @throws ServletException
	 */
	public void reinit(boolean withInference) throws ServletException;
	
	/**
	 * Executes a SPARQL query.
	 * 
	 * @param sparqlQuery
	 * @param form Only used for a "select" query.
	 * @return
	 * @throws Exception 
	 */
	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception ;


	/**
	 * Reindexes the graph.
	 * @param wait If true, then return only after indexing is completed.
	 *    If false, schedule an indexing task to run in the background
	 *    and return immediately.
	 * @throws ServletException
	 */
	public void reindex(boolean wait) throws ServletException;
	
	/**
	 * Loads the given model into the graph.
	 * 
	 * <p>
	 * If the user-specified graph is not given (null), then it is loaded into the default graph.
	 * 
	 * <p>
	 * Note that every ontology has its own associated graph, whose ID is &lt;ouri>, where ouri is the ontology's
	 * URI. All statements in the ontology are directly associated with this graph &lt;ouri>.
	 * 
	 * If a graphId is given, then the graph &lt;ouri> will be made a subGraphOf of graphId.
	 * Otherwise, no such subGraphOf property is created.
	 * 
	 * @param ontology The ontology to be loaded.
	 * 
	 * @param graphId User-specified graph as explained above. Can be null.
	 * 
	 * @throws Exception if there is some error  (for example, triple store
	 * 				server is down or timed out).
	 */
	public void loadOntology(Ontology ontology, String graphId) throws Exception;

}
