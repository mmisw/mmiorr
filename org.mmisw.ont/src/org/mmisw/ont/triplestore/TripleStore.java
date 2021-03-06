package org.mmisw.ont.triplestore;

import javax.servlet.ServletException;

import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntServlet;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.admin.AdminDispatcher;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.sparql.QueryResult;
import org.mmisw.ont.triplestore.allegro.AgTripleStore;
import org.mmisw.ont.triplestore.allegro4.Ag4TripleStore;
import org.mmisw.ont.triplestore.mem.JenaMemTripleStore;
import org.mmisw.ont.triplestore.tdb.JenaTbdTripleStore;
import org.mmisw.ont.triplestore.virtuoso.JenaVirtuosoTripleStore;

/**
 * Proxy to the actual implementation of {@link ITripleStore}.
 * 
 * The {@link #init()} method in this class instantiates the actual implementation, 
 * once the configuration information is available.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * <p>
 * Thread-safety: This class is not strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers. 
 * 
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
	
	/**
	 * Instantiates the actual implementation based on configuration parameters and calls
	 * {@link ITripleStore#init()} on it.
	 */
	public void init() throws ServletException {
		if ( _impl == null ) {
			_impl = _createTripleStoreInstance();
		}
		
		_impl.init();
	}
	
	/**
	 * Creates the actual {@link ITripleStore} implementation based on configuration parameters.
	 */
	@SuppressWarnings("deprecation")
	private ITripleStore _createTripleStoreInstance() {
		
		//
		// If an AllegroGraph server version is given, then use corresponding AG implementation.
		//
		String agraphVersion = OntConfig.Prop.AGRAPH_VERSION.getValue();
		boolean useAllegroGraph = agraphVersion != null && agraphVersion.trim().length() > 0;
		if ( useAllegroGraph  ) {
			agraphVersion = agraphVersion.trim();
			if ( agraphVersion.startsWith("4.") ) {
				return new Ag4TripleStore(_db, _adminDispatcher);
			}
			else if ( agraphVersion.startsWith("3.") ) {
				return new AgTripleStore(_db, _adminDispatcher);
			}
		}

		//
		// If VIRTUOSO_HOST is given, then use the Virtuoso implementation.
		//
		String virtuosoHost = OntConfig.Prop.VIRTUOSO_HOST.getValue();
		boolean useVirtuoso = virtuosoHost != null && virtuosoHost.trim().length() > 0;
		if ( useVirtuoso  ) {
			return new JenaVirtuosoTripleStore(_db, _adminDispatcher);
		}
		
		//
		// If the JENA_TDB_DIR is given, then use the JenaTbd implementation.
		//
		String jenaTdbDir = OntConfig.Prop.JENA_TDB_DIR.getValue();
		boolean useJenaTdb = jenaTdbDir != null && jenaTdbDir.trim().length() > 0;
		if ( useJenaTdb  ) {
			return new JenaTbdTripleStore(_db);
		}

		//
		// Otherwise, use the memory based implementation.
		// 
		if ( true ) {  // new JenaMem implementation
			return new JenaMemTripleStore(_db);
		}
		else {
			// TODO: remove this previous version of the memory-based impl when JenaMem is tested.
			return new org.mmisw.ont.triplestore.mem.OntGraphMem(_db);
		}
	}

	public void destroy() throws ServletException {
		_impl.destroy();
	}

	public QueryResult executeQuery(String sparqlQuery, boolean infer, String form) throws Exception {
		return _impl.executeQuery(sparqlQuery, infer, form);
	}

	public void loadOntology(OntologyInfo ontology, String graphId) throws Exception {
		_impl.loadOntology(ontology, graphId);
	}

	public void reindex(boolean wait) throws ServletException {
		_impl.reindex(wait);
	}

	public void reinit() throws ServletException {
		_impl.reinit();
	}

	public void clear() throws ServletException {
		_impl.clear();
	}
	
	public void removeOntology(OntologyInfo ontology) throws Exception {
		_impl.removeOntology(ontology);
	}

}
