package org.mmisw.ont;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A helper to work with the database.
 * 
 * @author Carlos Rueda
 */
public class Db {
	
	private final Log log = LogFactory.getLog(Db.class);
	
	private final OntConfig ontConfig;

	// obtained from the config in the init() method
	private String aquaportalDatasource; 

	/** 
	 * Creates an instance of this helper. 
	 * Call {@link #init()} to initialize it.
	 * @param ontConfig Used at initialization.
	 */
	Db(OntConfig ontConfig) {
		this.ontConfig = ontConfig;
	}
	
	/** Basic initialization - required. */
	void init() throws Exception {
		log.debug("init called.");
		aquaportalDatasource = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_DATASOURCE); 
	}
	
	public Connection getConnection() throws SQLException, ServletException {
		Connection result = null;
		try {
			Context initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(aquaportalDatasource);
			if ( dataSource != null ) {
				result = dataSource.getConnection();
			}
			else {
				throw new ServletException("Failed to lookup datasource.");
			}
		}
		catch ( NamingException ex ) {
			throw new ServletException("Failed to lookup datasource.", ex);
		}
		return result;
    }
	
	
	/**
	 * Obtains the ontology by the given URI.
	 * @param ontologyUri
	 * @return
	 * @throws ServletException
	 */
	Ontology getOntology(String ontologyUri) throws ServletException {
		try {
			Connection _con = getConnection();
			Statement _stmt = _con.createStatement();

			if ( true ) {
				String query = 
					"select v.id, v.ontology_id, v.file_path " +
					"from v_ncbo_ontology v " +
					"where v.urn='" +ontologyUri+ "'";
				
				ResultSet rs = _stmt.executeQuery(query);
				
		        if ( rs.next() ) {
		        	Ontology ontology = new Ontology();
		        	ontology.id = rs.getString(1);
		        	ontology.ontology_id = rs.getString(2);
		        	ontology.file_path = rs.getString(3);
		        	
		        	try {
		        		URL uri_ = new URL(ontologyUri);
		        		ontology.filename = new File(uri_.getPath()).getName();
		        	}
		        	catch (MalformedURLException e) {
		        		// should not occur.
		        		log.debug("should not occur.", e);
		        	}
		        	return ontology;
		        }
			}
			else{
				String query = 
					"select v.id, v.ontology_id, v.file_path, f.filename " +
					"from v_ncbo_ontology v, ncbo_ontology_file f " +
					"where v.urn='" +ontologyUri+ "'" +
					"  and v.id = f.ontology_version_id";
				
				ResultSet rs = _stmt.executeQuery(query);
				
		        if ( rs.next() ) {
		        	Ontology ontology = new Ontology();
		        	ontology.id = rs.getString(1);
		        	ontology.ontology_id = rs.getString(2);
		        	ontology.file_path = rs.getString(3);
		        	ontology.filename = rs.getString(4);
		        	return ontology;
		        }
			}
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		
		return null;
	}

	
	List<Ontology> getOntologies() throws ServletException {
		List<Ontology> onts = new ArrayList<Ontology>();
		try {
			Connection _con = getConnection();
			Statement _stmt = _con.createStatement();

			// NOTE:
			//    v_ncbo_ontology.id  ==  ncbo_ontology_file.ontology_version_id
			//
			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename " +
				"from v_ncbo_ontology v, ncbo_ontology_file f " +
				"where v.id = f.ontology_version_id";
			
			ResultSet rs = _stmt.executeQuery(query);
			
	        while ( rs.next() ) {
	        	Ontology ontology = new Ontology();
	        	ontology.id = rs.getString(1);
	        	ontology.ontology_id = rs.getString(2);
	        	ontology.file_path = rs.getString(3);
	        	ontology.filename = rs.getString(4);
	        	onts.add(ontology);
	        }
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		
		return onts;
	}
}
