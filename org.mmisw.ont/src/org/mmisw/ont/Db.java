package org.mmisw.ont;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	}
	
	/** 
	 * Required initialization.
	 */
	void init() throws Exception {
		log.debug("init called.");
		aquaportalDatasource = OntConfig.Prop.AQUAPORTAL_DATASOURCE.getValue(); 
		
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
	 * Obtains basic ontology info by the given URI and version.
	 * 
	 * @param ontologyUri The ontology URI as exactly stored in the database.
	 * @param version desired version
	 * @return
	 * @throws ServletException
	 * @throws SQLException 
	 */
	Ontology getOntologyVersion(String ontologyUri, String version) throws ServletException {
		Connection _con = null;
		try {
			_con = getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename " +
				"from v_ncbo_ontology v, ncbo_ontology_file f  " +
				"where v.id = f.ontology_version_id" +
				"  and v.urn='" +ontologyUri+ "'" +
				"  and v.version_number='" +version+ "'";

			ResultSet rs = _stmt.executeQuery(query);

			if ( rs.next() ) {
				Ontology ontology = new Ontology();
				ontology.id = rs.getString(1);
				ontology.ontology_id = rs.getString(2);
				ontology.file_path = rs.getString(3);

				ontology.filename = rs.getString(4);
				
				// TODO Remove this OLD way to determine filename
//				try {
//					URL uri_ = new URL(ontologyUri);
//					ontology.filename = new File(uri_.getPath()).getName();
//				}
//				catch (MalformedURLException e) {
//					// should not occur.
//					log.debug("should not occur.", e);
//				}
				
				return ontology;
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
					log.warn("Error closing connection", e);
				}
			}
		}
		
		return null;
	}
	

	
	/**
	 * Obtains basic ontology info by the given URI.
	 * 
	 * <p>
	 * Note: As of August, 2009, because on the "re-hosting" capability, the ontology URI is
	 * no longer unique in the database, ie., there may be multiple versions associated with the 
	 * same ontology URI. Now, this method returns the most recent version associated
	 * with the given URI.
	 * 
	 * <p>
	 * This uses the ontologyUri given. To try other file extensions,
	 * use {@link #getOntologyWithExts(MmiUri, String[])}.
	 * 
	 * @param ontologyUri The ontology URI as exactly stored in the database.
	 * @return
	 * @throws ServletException
	 * @throws SQLException 
	 */
	Ontology getOntology(String ontologyUri) throws ServletException {
		Connection _con = null;
		try {
			_con = getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename " +
				"from v_ncbo_ontology v, ncbo_ontology_file f  " +
				"where v.id = f.ontology_version_id" +
				"  and v.urn='" +ontologyUri+ "'  " +
				"order by v.version_number desc";  // -> to get most recent version first.

			if ( log.isDebugEnabled() ) {
				log.debug("Executing query: " +query);
			}
			
			ResultSet rs = _stmt.executeQuery(query);

			if ( rs.next() ) {
				Ontology ontology = new Ontology();
				ontology.id = rs.getString(1);
				ontology.ontology_id = rs.getString(2);
				ontology.file_path = rs.getString(3);
				
				ontology.filename = rs.getString(4);
				
				ontology.setUri(ontologyUri);

				return ontology;
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
					log.warn("Error closing connection", e);
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * Gets an ontology by trying the original ontology URI, and then, if that fails,
	 * with the file extensions "", ".owl", and ".rdf" in sequence until successful
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
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyWithExts: given URI=" +ontologyUri);
		}
		Ontology ontology = this.getOntology(ontologyUri);
		if ( ontology != null ) {
			if ( foundUri != null ) {
				foundUri[0] = ontologyUri;
			}
			return ontology;
		}
		
		// TODO Remove the following trials with various extensions as we are NOT storing
		// URIs with extension in the database, AND mmiUri.getOntologyUri() above is always
		// with NO extension.
		
		// try with a different extension, including no extension:
		String[] exts = { "", ".owl", ".rdf" };
		String topicExt = mmiUri.getExtension();
		for (String ext : exts ) {
			if ( ! ext.equalsIgnoreCase(topicExt) ) {
				String withNewExt = mmiUri.getOntologyUriWithExtension(ext);
				if ( log.isDebugEnabled() ) {
					log.debug("getOntologyWithExts: withNewExt=" +withNewExt);
				}
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
	

	/**
	 * Gets the list of versions of a given ontology according to the corresponding
	 * mmiUri identification, which is used to create the query:
	 * <ul>
	 *    <ul> Use wildcard "%" for the version 
	 *    
	 *    <ul> If allExts is true, search for topic with NO extension, but also allow
	 *       the same topic with any extension ".%" (Note: the dot is important to
	 *       avoid getting topics that have the topic in question as suffix).
	 * </ul>
	 * 
	 * <p>
	 * Note that the term component in the given URI is ignored.
	 * 
	 * <p>
	 * The elements are sorted such that the first element is the most recent version
	 * available.
	 * 
	 * @param mmiUri the base URI to create the version wilcard.
	 * 
	 * @return list of ontologies with exactly the same given mmiUri except for the
	 *          version component.
	 *          
	 * @throws ServletException
	 */
	List<Ontology> getOntologyVersions(MmiUri mmiUri) throws ServletException {
		
		List<Ontology> onts = new ArrayList<Ontology>();
		
		if ( mmiUri.getVersion() != null && log.isDebugEnabled() ) {
			log.debug("getOntologyVersions: " +mmiUri.getVersion()+ ": version component will be ignored.");
		}
		
		// get ontologyUriPattern to do the "like" query:
		String ontologyUriPattern;
		try {
			// use "%" for the version:
			MmiUri mmiUriPatt = mmiUri.copyWithVersionNoCheck("%");
			ontologyUriPattern = mmiUriPatt.getOntologyUri();
		}
		catch (URISyntaxException e) {
			// should not occur.
			log.debug("should not occur.", e);
			return onts;
		}
		
		// to be added to the condition is the query string below:
		// Add an "or" condition to allow extensions, ".%":
		// TODO remove this ".%" condition as we are no storing URIs with extensions in the database.
		String or_with_dot_ext = "or v.urn like '" +ontologyUriPattern+ ".%' ";
		
		// ok, now run the "like" query
		Connection _con = null;
		try {
			_con = getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select v.id, v.ontology_id, v.file_path, v.urn, f.filename " +
				"from v_ncbo_ontology v, ncbo_ontology_file f " +
				"where v.id = f.ontology_version_id " +
				"  and ( v.urn like '" +ontologyUriPattern+ "' " +
				         or_with_dot_ext +
				"      ) " +
				"order by v.urn desc";

			if ( log.isDebugEnabled() ) {
				log.debug("Versions query: " +query);
			}
			
			ResultSet rs = _stmt.executeQuery(query);

			while ( rs.next() ) {
				Ontology ontology = new Ontology();
				ontology.id = rs.getString(1);
				ontology.ontology_id = rs.getString(2);
				ontology.file_path = rs.getString(3);
				
				String ontologyUri = rs.getString(4);
				ontology.setUri(ontologyUri);

				ontology.filename = rs.getString(5);
				onts.add(ontology);
				
				// TODO Remove this OLD way to determine filename
//				try {
//					URL uri_ = new URL(ontologyUri);
//					ontology.filename = new File(uri_.getPath()).getName();
//					onts.add(ontology);
//				}
//				catch (MalformedURLException e) {
//					// should not occur.
//					log.debug("should not occur.", e);
//				}
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
					log.warn("Error closing connection", e);
				}
			}
		}
		
		return onts;
	}
	
	
	
	Ontology getMostRecentOntologyVersion(MmiUri mmiUri) throws ServletException {
		List<Ontology> onts = getOntologyVersions(mmiUri);
		if ( onts.size() == 0 ) {
			return null;
		}
		Ontology ont = onts.get(0);
		return ont;
	}

	/**
	 * Returns the list of the latest versions of all ontologies in the database.
	 * 
	 * @return
	 * @throws ServletException
	 */
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
				"select v.id, v.ontology_id, v.file_path, f.filename, v.urn " +
				"from v_ncbo_ontology v, ncbo_ontology_file f " +
				"where v.id = f.ontology_version_id";
			
			ResultSet rs = _stmt.executeQuery(query);
			
	        while ( rs.next() ) {
	        	Ontology ontology = new Ontology();
	        	ontology.id = rs.getString(1);
	        	ontology.ontology_id = rs.getString(2);
	        	ontology.file_path = rs.getString(3);
	        	ontology.filename = rs.getString(4);
	        	ontology.setUri(rs.getString(5));
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
					log.warn("Error closing connection", e);
				}
			}
		}
	
		return onts;
	}

	/**
	 * Gets info about a user.
	 * @param username
	 * @return
	 */
	public Map<String,String> getUserInfo(String username) throws ServletException {
		Connection _con = null;
		try {
			_con = getConnection();
			Statement _stmt = _con.createStatement();

			String query = 
				"select id, username, email, firstname, lastname, phone, date_created " +
				"from ncbo_user " +
				"where username = '" +username+ "'";
			
			ResultSet rs = _stmt.executeQuery(query);
			
			if ( rs.next() ) {
				ResultSetMetaData md = rs.getMetaData();
				Map<String,String> props = new LinkedHashMap<String,String>();
			
				for ( int i = 1, count = md.getColumnCount(); i <= count; i++ ) {
					String value = rs.getString(i);
					if ( value != null ) {
						props.put(md.getColumnName(i), value);
					}
				}
				
	        	return props;
	        }
	        else {
	        	return null;
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
					log.warn("Error closing connection", e);
				}
			}
		}
	}
}
