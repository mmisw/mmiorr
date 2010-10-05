package org.mmisw.ont.triplestore;

import javax.servlet.ServletException;

import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.triplestore.allegro.AgTripleStore;
import org.mmisw.ont.triplestore.mem.JenaMemTripleStore;
import org.mmisw.ont.triplestore.mem.OntGraphMem;
import org.mmisw.ont.triplestore.tdb.TripleStoreJenaTbd;

/**
 * Proxy to the actual implementation of {@link ITripleStore}.
 * @author Carlos Rueda
 */
public class TripleStore implements ITripleStore {
	
	private ITripleStore _impl;
	private Db _db;
	
	private AdminDispatcher _adminDispatcher;

	
	/**
	 * Creates the proxy.
	 * {@link #init()} instantiates the actual implementation, once the configuration
	 * information is available.
	 * 
	 * @param db The database helper.
	 */
	public TripleStore(Db db, AdminDispatcher adminDispatcher) {
		this._db = db;
		this._adminDispatcher = adminDispatcher;
	}
	
	public void init() throws ServletException {
		if ( _impl == null ) {
			_impl = _createTripleStoreInstance();
		}
		
		_impl.init();
	}
	
	/**
	 * Creates the actual implementation based on {@link ITripleStore}.
	 */
	private ITripleStore _createTripleStoreInstance() {
		//
		// If the JENA_TDB_DIR is given, then use the JenaTbd implementation.
		// Otherwise, if the AllegroGraph server host is given, then use the AG implementation.
		// Otherwise, use the memory based implementation.
		//
		
		String jenaTdbDir = OntConfig.Prop.JENA_TDB_DIR.getValue();
		boolean useJenaTdb = jenaTdbDir != null && jenaTdbDir.trim().length() > 0;
		if ( useJenaTdb  ) {
			return new TripleStoreJenaTbd(_db, jenaTdbDir);
		}

		String agraphHost = OntConfig.Prop.AGRAPH_HOST.getValue();
		boolean useAllegroGraph = agraphHost != null && agraphHost.trim().length() > 0;
		if ( useAllegroGraph  ) {
			return new AgTripleStore(_db, _adminDispatcher);
		}
		
		// Else: use a memory-based implementation:
		if ( true ) {  // new JenaMem implementation
			return new JenaMemTripleStore(_db);
		}
		else {
			// TODO: remove this when JenaMem tested.
			return new OntGraphMem(_db);
		}
	}

	public void destroy() throws ServletException {
		_impl.destroy();
	}

	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		return _impl.executeQuery(sparqlQuery, form);
	}

	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		_impl.loadOntology(ontology, graphId);
	}

	public void reindex(boolean wait) throws ServletException {
		_impl.reindex(wait);
	}

	public void reinit(boolean withInference) throws ServletException {
		_impl.reinit(withInference);
	}

	public void removeOntology(OntologyInfo ontology) throws Exception {
		_impl.removeOntology(ontology);
	}

}
