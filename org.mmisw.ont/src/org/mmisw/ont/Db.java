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
	
	// obtained in the init() methos
	private DataSource dataSource;
	

	/** 
	 * Creates an instance of this helper. 
	 * Call {@link #init()} to initialize it.
	 * @param ontConfig Used at initialization.
	 */
	Db(OntConfig ontConfig) {
		this.ontConfig = ontConfig;
	}
	
	/** 
	 * Required initialization.
	 */
	void init() throws Exception {
		log.debug("init called.");
		aquaportalDatasource = ontConfig.getProperty(OntConfig.Prop.AQUAPORTAL_DATASOURCE); 
		
		try {
			Context initialContext = new InitialContext();
			dataSource = (DataSource) initialContext.lookup(aquaportalDatasource);
			if ( dataSource == null ) {
				throw new ServletException("Failed to lookup datasource.");
			}
		}
		catch ( NamingException ex ) {
			throw new ServletException("Failed to lookup datasource.", ex);
		}

	}
	
	public Connection getConnection() throws SQLException, ServletException {
		Connection result = dataSource.getConnection();
		return result;
    }
	
	
	/**
	 * Obtains the ontology by the given URI.
	 * 
	 * This uses the onotologyUri given. To try other file extensions,
	 * use {@link #getOntologyWithExts(MmiUri, String[])}.
	 * 
	 * @param ontologyUri
	 * @return
	 * @throws ServletException
	 * @throws SQLException 
	 */
	Ontology getOntology(String ontologyUri) throws ServletException {
		Connection _con = null;
		try {
			_con = getConnection();
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
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * Gets an ontology by trying the original ontology URI,
	 * and the file extensions "", ".owl", and ".rdf" in sequence until successful
	 * or returning null if none of these tries works.
	 * 
	 * @param mmiUri
	 * @param foundUri If not null, the URI that was success is stored at foundUri[0]. 
	 * 
	 * @throws ServletException 
	 */
	Ontology getOntologyWithExts(MmiUri mmiUri, String[] foundUri) throws ServletException {
		// try with given URI:
		String ontologyUri = mmiUri.getOntologyUri();
		Ontology ontology = this.getOntology(ontologyUri);
		if ( ontology != null ) {
			if ( foundUri != null ) {
				foundUri[0] = ontologyUri;
			}
			return ontology;
		}
		
		// try with a different extension, including no extension:
		String[] exts = { "", ".owl", ".rdf" };
		String topicExt = mmiUri.getTopicExtension();
		for (String ext : exts ) {
			if ( ! ext.equalsIgnoreCase(topicExt) ) {
				String withNewExt = mmiUri.getOntologyUriWithTopicExtension(ext);
				ontology = this.getOntology(withNewExt);
				if ( ontology != null ) {
					if ( foundUri != null ) {
						foundUri[0] = withNewExt;
					}
					return ontology;
				}
			}
		}

		return ontology;
	}	
	


	
	List<Ontology> getOntologies() throws ServletException {
		List<Ontology> onts = new ArrayList<Ontology>();
		Connection _con = null;
		try {
			_con = getConnection();
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
		finally {
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}
	
		return onts;
	}
}
