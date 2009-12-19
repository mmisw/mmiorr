package org.mmisw.ont.graph;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.Db;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.Ontology;
import org.mmisw.ont.graph.allegro.OntGraphAG;
import org.mmisw.ont.graph.mem.OntGraphMem;
import org.mmisw.ont.sparql.QueryResult;

/**
 * Proxy to the actual implementation of {@link IOntGraph}.
 * @author Carlos Rueda
 */
public class OntGraph implements IOntGraph {
	
	private static final Log log = LogFactory.getLog(OntGraph.class);
	
	private IOntGraph _impl;
	private OntConfig _ontConfig;
	private Db _db;

	/**
	 * Creates the proxy.
	 * {@link #init()} instantiates the actual implementation, once the configuration
	 * information is available.
	 * 
	 * @param ontConfig Used at initialization to obtain the "uploads" directory, where the
	 *        actual ontology files are located.
	 *        
	 * @param db The database helper.
	 */
	public OntGraph(OntConfig ontConfig, Db db) {
		this._ontConfig = ontConfig;
		this._db = db;
	}
	
	public void init() throws ServletException {
		if ( _impl == null ) {
			//
			// If the AllegroGraph server host is given, then use the OntGraphAG implementation;
			// otherwise, use the OntGraphMem implementation:
			//
			// TODO: other parameters can be used to determine the actual implementation and/or
			// particular settings.
			//
			String serverHost = OntConfig.Prop.AGRAPH_HOST.getValue();
			boolean useAllegroGraph = serverHost != null && serverHost.trim().length() > 0;

			if ( useAllegroGraph  ) {
				log.info("createOntGraph: Using AllegroGraph");
				_impl = new OntGraphAG(_ontConfig, _db);
			}
			else {
				log.info("createOntGraph: Using OntGraphMem");
				_impl = new OntGraphMem(_ontConfig, _db);
			}
		}
		
		_impl.init();
	}
	
	public void destroy() throws ServletException {
		_impl.destroy();
	}

	public QueryResult executeQuery(String sparqlQuery, String form) throws Exception {
		return _impl.executeQuery(sparqlQuery, form);
	}

	public void loadOntology(Ontology ontology) throws Exception {
		_impl.loadOntology(ontology);
	}

	public void reindex(boolean wait) throws ServletException {
		_impl.reindex(wait);
	}

	public void reinit(boolean withInference) throws ServletException {
		_impl.reinit(withInference);
	}

}
