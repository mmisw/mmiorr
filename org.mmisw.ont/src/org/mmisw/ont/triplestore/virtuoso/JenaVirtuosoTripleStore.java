package org.mmisw.ont.triplestore.virtuoso;

import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.UnversionedConverter;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.sparql.Sparql;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.triplestore.TsUtil;
import org.mmisw.ont.util.OntUtil;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Triple store implementation based on Virtuoso.
 * 
 * <p>
 * TODO Under preliminary testing.
 * 
 * @author Carlos Rueda
 */
public class JenaVirtuosoTripleStore implements ITripleStore {

	private final Log log = LogFactory.getLog(JenaVirtuosoTripleStore.class);
	
	
	private final Db db;
	private final AdminDispatcher adminDispatcher;
	private String aquaUploadsDir;
	
	
	private String _host;
	private String _username;
	private String _password;
	
	/**
	 * Connection. Usage idiom:
	 * <pre>
	 *   _Conn _conn = new _Conn();
	 *   try {
	 *       ...
	 *   }
	 *   finally {
	 *       _conn.end();
	 *   }
	 * </pre>
	 */
	private class _Conn {
		private VirtGraph _graph;
		private Model _model;
		
		/** connects to the default graph */
		_Conn() throws ServletException {
			log.info("Connecting to triple store...");
			try {
				_graph = new VirtGraph(_host, _username, _password);
				_model = ModelFactory.createModelForGraph(_graph);
			} 
			catch (Throwable e) {
				throw new ServletException("Error connecting to triple store server.", e);
			}
			log.info("CONNECTION OPEN");
		}

		/** connects to a particular graph */
		_Conn(String graphName) throws ServletException {
			log.info("Connecting to triple store, graphName=" +graphName);
			try {
				_graph = new VirtGraph(graphName, _host, _username, _password);
				_model = ModelFactory.createModelForGraph(_graph);
			} 
			catch (Throwable e) {
				throw new ServletException("Error connecting to triple store server.", e);
			}
			log.info("CONNECTION OPEN");
		}
		
		void debugIndexInfo() {
			if ( log.isDebugEnabled() ) {
				log.debug("_graph name = " +_graph.getGraphName());
				log.debug("_graph size = " +_graph.size());
			}			
		}

		void end() throws ServletException {
			if ( _graph != null ) {
				_graph.close();
			}
			_graph = null;
			_model = null;
			log.info("CONNECTION CLOSED");
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param db The database helper.
	 */
	public JenaVirtuosoTripleStore(Db db, AdminDispatcher adminDispatcher) {
		this.db = db;
		this.adminDispatcher = adminDispatcher;
		log.debug(getClass().getSimpleName()+ " instance created.");
	}
	

	/** nothing done here */
	public void destroy() throws ServletException {
	}

	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		_Conn _conn = new _Conn();
		try {
			return _executeQuery(_conn, sparqlQuery, form);
		}
		finally {
			_conn.end();
		}
	}

	private QueryResult _executeQuery(_Conn _conn, String sparqlQuery, String form) throws Exception {
		log.debug(" _executeQuery called.");
		
		Query query = QueryFactory.create(sparqlQuery);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(query, _conn._graph);
		
		try {
			QueryResult queryResult = Sparql.executeQuery(query, vqe, sparqlQuery, form);
			return queryResult;
		}
		finally {
			vqe.close();
		}
	}

	/**
	 * Initializes some internal parameters from OntConfig and tests connection
	 * with the server.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		log.info("init called.");
			
		_host = OntConfig.Prop.VIRTUOSO_HOST.getValue();
		_username = OntConfig.Prop.VIRTUOSO_USERNAME.getValue();
		_password = OntConfig.Prop.VIRTUOSO_PASSWORD.getValue();
		aquaUploadsDir = OntConfig.Prop.AQUAPORTAL_UPLOADS_DIRECTORY.getValue();

		// test that we can connect to the server
		_Conn _conn = new _Conn();
		try {
			_conn.debugIndexInfo();
		}
		finally {
			_conn.end();
		}

		log.info("init complete.");
	}

	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
		log.info("Loading: " +full_path+ " in graph: " +(graphId == null ? "(default graph)" : graphId));
		final boolean clearGraphFirst = true;
		
		_Conn _conn = new _Conn();
		try {
			_loadOntology(_conn, ontology, graphId, full_path, clearGraphFirst);
		}
		finally {
			_conn.end();
		}

	}

	/**
	 * Loads an ontology to the given graph.
	 * 
	 * @param _conn
	 * @param ontology
	 * @param graphId User-specified graph. Can be null.
	 * @param full_path
	 * @param clearGraphFirst true to remove all statements associated with the graph directly associated with the
	 *        ontology.
	 */
	private void _loadOntology(_Conn _conn, OntologyInfo ontology, String graphId, String full_path, boolean clearGraphFirst) {
		
		String graphUri;
		
		String ontologyUri = ontology.getUri();
		OntModel model;
		
		if ( USE_UNVERSIONED ) {
			model = JenaUtil2.loadModel("file:" +full_path, false);

			if ( OntUtil.isOntResolvableUri(ontologyUri) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					model = UnversionedConverter.getUnversionedModel(model, mmiUri);
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return ;
				}
				log.info("To load Ont-resolvable ontology in graph.");
			}
			else {
				log.info("To load re-hosted ontology in graph.");
			}
		}
		else {
			model = JenaUtil2.loadModel("file:" +full_path, false);
		}
		
		///////////////////////////////////////////////////////////////
		// now, update triple store with model captured 
		///////////////////////////////////////////////////////////////
		
		// add the model to the given triple store in the given connection,
		// which should be the "default" graph.
		if ( log.isDebugEnabled() ) {
			log.debug("Loading model in default graph in triple store...");
		}
		_conn._model.add(model);
		if ( log.isDebugEnabled() ) {
			log.debug("Loading model in default graph in triple store... Done.");
		}

		// below, we add the model to its "ownGraph" 
		
		try {
			// 'ownGraph' is the graph for the ontology itself. 
			// All the statements in the ontology are associated with this graph.
			final String ownGraph = ontologyUri;
			
			_Conn _connGraph = new _Conn(ownGraph);
			try {
				if ( clearGraphFirst ) {
					// remove all statements associated with the graph:
					if ( log.isDebugEnabled() ) {
						log.debug("Removing all statements in graph " +ownGraph+ " ...");
					}
					_connGraph._graph.clear();
				}
				
				// now, create the new graph:
				if ( log.isDebugEnabled() ) {
					log.debug("Loading model in graph '" +ownGraph+ "' in triple store...");
				}
				_connGraph._model.add(model);
				if ( log.isDebugEnabled() ) {
					log.debug("Loading model in graph '" +ownGraph+ "' in triple store... Done");
				}
				
			}
			finally {
				_connGraph.end();
			}
			
			
			// add the graph statement to the graphs resource:
			String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
			adminDispatcher.newGraph(ownGraphUri);
			
			// now, add the subGraphOf relationship if graphId != null
			if ( graphId != null ) {
				graphUri = adminDispatcher.getWellFormedGraphUri(graphId);
				_addSubGraph(_conn, ownGraphUri, graphUri);
			}
			
		}
		catch (Exception e) {
			log.error("Error parsing/loading RDF in graph.", e);
		}
		
	}
	
	
	/**
	 * Updates the graphs resource and then the triple store.
	 * @param _conn
	 * @param subGraphUri  Assumed to be well-formed
	 * @param superGraphUri  Assumed to be well-formed
	 */
	private void _addSubGraph(_Conn _conn, String subGraphUri, String superGraphUri)  {
		
		// first, update the graphs resource:
		List<Statement> statements = adminDispatcher.newSubGraph(subGraphUri, superGraphUri);
		
		// then, update the triple store with the corresponding statements:
		if ( statements != null ) {
			for (Statement  stmt : statements) {
				_conn._model.add(stmt);
				log.info("Added statement: " +stmt);
			}
		}		
	}

	/** TODO nothing done here at the moment */
	public void reindex(boolean wait) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * Clears the triple store.
	 * @throws ServletException
	 */
	public void clear() throws ServletException {
		log.info("clear called.");
		log.info("Creating connection to triple store ...");
		long start = System.currentTimeMillis();
		_Conn _conn = new _Conn();
		try {
			_doClear(_conn);
		}
		finally {
			_conn.end();
		}
		log.info("clear completed (" +TsUtil.elapsedTime(start)+ ")");
	}

	/**
	 * Clears the triple store
	 * @throws ServletException
	 */
	private void _doClear(_Conn _conn) throws ServletException {
		log.debug("clearing triple store...");
		try {
			// clear the given graph
			_conn._graph.clear();
			log.debug("graph " +_conn._graph.getGraphName()+ " cleared.  #triples= " +_conn._graph.size());
			// and the all the named graphs:
			_clearAllGraphs();
		}
		catch (Exception e) {
			log.error("Error clearing triple store", e);
			throw new ServletException("Error clearing triple store", e);
		}
	}

	/** 
	 * clears all the graphs associated with the registered ontologies 
	 */
	private void _clearAllGraphs() throws ServletException {
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		
		final int numOnts = onts.size();
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			log.debug("About to clear the following " +numOnts+ " graphs: ");
			for ( OntologyInfo ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}

		int nn = 0;
		for ( OntologyInfo ontology : onts ) {
			nn++;
			if ( log.isDebugEnabled() ) {
				log.debug("CLEARING: " +nn+ "/" +numOnts);
			}
			try {
				_Conn _conn2 = new _Conn();
				try {
					_clearGraph(_conn2, ontology);
				}
				finally {
					_conn2.end();
				}
			}
			catch (Throwable ex) {
				log.warn("Error clearing graph for ontology: " +ontology.getUri()+ " (CONTINUING...)", ex);
			}
		}

	}
	
	/**
	 * Clears the graph corresponding to the given ontology.
	 * 
	 * @param _conn
	 * @param ontology
	 * @param graphId User-specified graph. Can be null.
	 * @param full_path
	 * @throws Exception 
	 */
	private void _clearGraph(_Conn _conn, OntologyInfo ontology) throws Exception {
		
		String ontologyUri = ontology.getUri();
		
		if ( USE_UNVERSIONED ) {
			if ( OntUtil.isOntResolvableUri(ontologyUri) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return ;
				}
				log.info("To clear graph for Ont-resolvable ontology.");
			}
			else {
				log.info("To clear graph for for re-hosted ontology.");
			}
		}
		
		// 'ownGraph' is the graph for the ontology itself. 
		// All the statements in the ontology are associated with this graph.
		final String ownGraph = ontologyUri;
		
		try {
			_Conn _connGraph = new _Conn(ownGraph);
			try {
				_connGraph._graph.clear();
				if ( log.isDebugEnabled() ) {
					log.debug("graph " +_connGraph._graph.getGraphName()+ " cleared.  #triples= " +_conn._graph.size());
				}
			}
			finally {
				_connGraph.end();
			}
		}
		catch (Exception e) {
			log.warn("Error clearing graph " +ownGraph, e);
		}
		
		//////////////////////////////////////////////////////////////////////
		// ii) remove all statements having ownGraph as subject (in particular,
		// subGraphOf relationships and the typeOf-graph statement will be removed):
		//
		String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
		_removeAllStatementsForSubject(_conn, ownGraphUri);
	}


	/**
	 * Reinitializes the triple store.
	 * @throws ServletException
	 */
	public void reinit() throws ServletException {
		log.info("reinit called.");
		log.info("Creating connection to triple store ...");
		_Conn _conn = new _Conn();
		try {
			// clear and populate the triple store:
			_doReInit(_conn);
		}
		finally {
			_conn.end();
		}
	}
	
	
	/**
	 * Re-Inits the triple store.
	 * @return number of triples. -1 if some error happens while obtaining this number.
	 * @throws ServletException
	 */
	private void _doReInit(_Conn _conn) throws ServletException {
		clear();
		_loadAllOntologies(_conn);
	}
	
	
	private void _loadAllOntologies(_Conn _conn) throws ServletException {
		long start = System.currentTimeMillis();
		long numberOfTriples = _doLoadAllOntologies(_conn);
		log.debug("triple store populated (" +TsUtil.elapsedTime(start)+ ").  " +
				"#triples= " +numberOfTriples);
	}
	
	
	
	private long _doLoadAllOntologies(_Conn _conn) throws ServletException {
		// get the list of (latest-version) ontologies:
		final boolean allVersions = false;
		List<OntologyInfo> onts = db.getAllOntologies(allVersions);
		final int numOnts = onts.size();
		
		if ( log.isDebugEnabled() ) {
			log.debug("Using unversioned ontologies: " +USE_UNVERSIONED);
			log.debug("About to load the following " +numOnts+ " ontologies: ");
			for ( OntologyInfo ontology : onts ) {
				log.debug(ontology.getOntologyId()+ " :: " +ontology.getUri());
			}
		}
		
		int nn = 0;
		for ( OntologyInfo ontology : onts ) {
			if ( log.isDebugEnabled() ) {
				log.debug("LOADING: " +nn+ "/" +numOnts);
			}
			String full_path = aquaUploadsDir+ "/" +ontology.getFilePath() + "/" + ontology.getFilename();
			log.info("Loading: " +full_path+ " in graph");
			try {
				// NOTE: the graphId here is null; the graph relationships are added below.
				String graphId = null;
				
				boolean clearGraphFirst = false;  // the triple store starts empty; see above
				
				_Conn _conn2 = new _Conn();
				try {
					_loadOntology(_conn2, ontology, graphId, full_path, clearGraphFirst);
				}
				finally {
					_conn2.end();
				}
			}
			catch (Throwable ex) {
				log.error("Error loading ontology: " +full_path+ " " +
						" ontology.getUri= " +ontology.getUri()+ " (CONTINUING...)", ex);
			}
		}
		
		// enable inferencing:
		// In AgTripleStore, we call: _loadSupportingStatements(_conn);
		// TODO something to do with Virtuoso explicitly to enable inferencing?
		
		// load internal resources (graph relationships, etc.):
		try {
			_loadInternalResources(_conn);
		}
		catch (Exception e) {
			log.error("Error loading internal resources", e);
			// but continue.
		}
		
		long numberOfTriples = -1;
		try {
			numberOfTriples = _conn._graph.size();
		}
		catch (Exception e) {
			log.error("Error getting number of triples", e);
			// but continue.
		}
		return numberOfTriples;
	}
	
	/**
	 * load internal resources (graphs, etc) in the triple store.
	 * @throws AllegroGraphException 
	 */
	private void _loadInternalResources(_Conn _conn) throws Exception {
		log.info("_loadInternalResources called.");
		List<Statement> statements = adminDispatcher.getInternalStatements();
		
		// then, update the triple store with the corresponding statements:
		if ( statements != null ) {
			for (Statement  stmt : statements) {
				_conn._model.add(stmt);
				log.info("Added statement: " +stmt);
			}
		}		
	}



	public void removeOntology(OntologyInfo ontology) throws Exception {
		_Conn _conn = new _Conn();
		try {
			log.info("Removing: id=" +ontology.getId()+ " of ontologyId=" +ontology.getOntologyId()+ " ...");
			_removeOntology(_conn, ontology);
		}
		finally {
			_conn.end();
		}

	}

	/**
	 * i) removes all statements associated with the "proper" graph (ie., the
	 * graph whose URI is the same as the ontology URI); 
	 * <p>
	 * If the ontology URI will be completely gone (ie., no previous version is available), then:
	 * <p>
	 * ii) removes all statements having the "proper" graph as subject (in particular, subGraphOf 
	 * relationships and the typeOf-graph statement will be removed).
	 * 
	 * @param _conn
	 * @param ontology
	 * @throws Exception 
	 *        
	 */
	private void _removeOntology(_Conn _conn, OntologyInfo ontology) throws Exception {
		
		String ontologyUri = ontology.getUri();
		
		if ( USE_UNVERSIONED ) {

			if ( OntUtil.isOntResolvableUri(ontologyUri) ) {
				MmiUri mmiUri;
				try {
					mmiUri = new MmiUri(ontologyUri);
					ontologyUri = mmiUri.copyWithVersion(null).getOntologyUri();
				}
				catch (URISyntaxException e) {
					log.error("shouldn't happen", e);
					return ;
				}
				log.debug("About to remove Ont-resolvable ontology from graph.");
			}
			else {
				log.debug("About to remove re-hosted ontology from graph.");
			}
		}
		// Else: nothing -- just keep the given ontologyUri.
		
		// now, update graph:

		// this is the graph for the ontology itself. 
		// All the statements in the ontology are associated with this graph.
		String ownGraph = ontologyUri;
		
		_Conn _conn2 = new _Conn(ownGraph);
		try {
			//////////////////////////////////////////////////////////////////////
			// i) first, remove all statements associated with the ownGraph:
			//
			if ( log.isDebugEnabled() ) {
				log.debug("Removing all statements in graph " +ownGraph+ " ...");
			}
			_conn2._graph.clear();
		}
		catch (Exception e) {
			log.error("Error removing ontology statements from graph " +ownGraph, e);
			return;
		}
		finally {
			_conn2.end();
		}

		// if any, get latest version that may remain:
		OntologyInfo latestOntology = null;
		try {
			latestOntology = db.getRegisteredOntologyLatestVersion(ontologyUri);
		}
		catch (ServletException e) {
			log.warn("Warning: error while trying to retrieve existing version of ontology. Ignoring error.", e);
		}
			
		if ( latestOntology != null ) {
			// there still is an existing ontology version. So, no need
			// for more updates, ie., any existing subGraphOf statements will remain valid.
			log.debug("_removeOntology: No need to remove subGraphOf statements");
			
			// FIXME but we need to load the contents of the latest version that remains
			// ...
			
			return;
		}

		// here: ontologyUri completely gone.

		//////////////////////////////////////////////////////////////////////
		// ii) remove all statements having ownGraph as subject (in particular,
		// subGraphOf relationships and the typeOf-graph statement will be removed):
		//
		String ownGraphUri = adminDispatcher.getWellFormedGraphUri(ownGraph);
		_removeAllStatementsForSubject(_conn, ownGraphUri);
		
	}

	
	/**
	 * Removes all statements for a given subject.
	 * Updates the graphs resource and then the triple store.
	 * @param _conn
	 * @param subGraphUri  Assumed to be well-formed
	 * @throws AllegroGraphException
	 */
	private void _removeAllStatementsForSubject(_Conn _conn, String subGraphUri) throws Exception {
		
		log.debug("_removeAllStatementsForSubject: " +subGraphUri);
		
		// remove the statements from the graphs resource:
		adminDispatcher.removeAllStatementsFromSubject(subGraphUri);
		
		Resource subGraphRes = ResourceFactory.createResource(subGraphUri);
		// then, update the triple store with the corresponding statements:
		_conn._model.removeAll(subGraphRes, null, null);
	}

}
