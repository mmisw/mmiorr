package org.mmisw.ont.triplestore;

import javax.servlet.ServletException;

import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.sparql.QueryResult;


/**
 * Interface to a triple store.
 * 
 * @author Carlos Rueda
 */
public interface ITripleStore {

	/** 
	 * Load the unversioned form of the last version of the ontologies?
	 * This is set to true. 
	 * (Can be set to false to get the original behavior, which was load the "versioned" form.)
	 */
	final boolean USE_UNVERSIONED = true;

	/**
	 * Initializes the triple store.
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
	 * Reinitializes the triple store.
	 * As part of this, re-loads all latest versions of the ontologies. 
	 * @throws ServletException
	 */
	public void reinit() throws ServletException;
	
	/**
	 * Clears the triple store.
	 * @throws ServletException
	 */
	public void clear() throws ServletException;
	
	/**
	 * Executes a SPARQL query.
	 * 
	 * @param sparqlQuery
	 * @param infer With inference?
	 * @param form Only used for a "select" query.
	 * @return
	 * @throws Exception 
	 */
	public QueryResult executeQuery(String sparqlQuery, boolean infer, String form) throws Exception ;


	/**
	 * Reindexes the triple store.
	 * @param wait If true, then return only after indexing is completed.
	 *    If false, schedule an indexing task to run in the background
	 *    and return immediately.
	 * @throws ServletException
	 */
	public void reindex(boolean wait) throws ServletException;
	
	/**
	 * Loads the given model into the triple store.
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
	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception;

	
	/**
	 * Removes an ontology from the triple store.
	 * This means, 
	 * i) removes all statements associated with the "proper" graph (ie., the
	 * graph whose URI is the same as the ontology URI); 
	 * ii) removes all subGraphOf relationships with the "proper" graph as subject.
	 * 
	 * @param ontology
	 */
	public void removeOntology(OntologyInfo ontology) throws Exception ;
}
