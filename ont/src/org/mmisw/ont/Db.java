package org.mmisw.ont;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;


/**
 * A helper to work with the database.
 * 
 * @author Carlos Rueda
 */
public class Db {

	// TODO: read this in a more dynamic way (from the servlet context perhaps)
	private static final String DATASOURCE_CONTEXT = "java:comp/env/jdbc/BioPortalDataSource";
	
	
	/** Basic initialization - required. */
	static void init() throws Exception {
		// nothing done here (yet).
	}
	
	static Connection getConnection() throws SQLException, ServletException {
		Connection result = null;
		try {
			Context initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup(DATASOURCE_CONTEXT);
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
	static Ontology getOntology(String ontologyUri) throws ServletException {
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
		        		// TODO Auto-generated catch block
		        		e.printStackTrace();
		        	}
		        	return ontology;
		        }
			}
			else{
				String query = 
					"select v.id, v.ontology_id, v.file_path, f.filename " +
					"from v_ncbo_ontology v, ncbo_ontology_file f " +
					"where v.urn='" +ontologyUri+ "' and v.id = f.id";
				
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
    
    private Db() {}

}
