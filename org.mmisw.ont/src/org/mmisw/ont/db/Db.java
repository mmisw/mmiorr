package org.mmisw.ont.db;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntServlet;
import org.mmisw.ont.OntologyInfo;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.util.OntUtil;


/**
 * A (singleton) helper to work with the aquaportal database.
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
@ThreadSafe
public class Db {
	
	private final Log log = LogFactory.getLog(Db.class);
	

	// obtained from the config in the init() method
	private String aquaportalDatasource; 
	
	// obtained in the init() method
	private DataSource dataSource;
	

	/** 
	 * Creates the instance of this helper. 
	 * Call {@link #init()} to initialize it.
	 * @param ontConfig Used at initialization.
	 */
	public Db(OntConfig ontConfig) {
	}
	
	/** 
	 * Required initialization.
	 */
	public void init() throws Exception {
		log.info("init called.");
		aquaportalDatasource = OntConfig.Prop.AQUAPORTAL_DATASOURCE.getValue(); 
		
		Context initialContext = null;
		try {
			initialContext = new InitialContext();
			dataSource = (DataSource) initialContext.lookup(aquaportalDatasource);
			if ( dataSource == null ) {
				throw new Exception("Failed to lookup datasource: " +aquaportalDatasource);
			}
		}
		catch ( NamingException ex ) {
			throw new Exception("Failed to lookup datasource: " +aquaportalDatasource, ex);
		}
		
		if ( initialContext != null ) {
			// release context resources:
			try {
				initialContext.close();
			}
			catch (NamingException ignore) {
			}
		}

	}
	
	/**
	 * Establishes a connection to the aquaportal datasource.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection result = dataSource.getConnection();
		return result;
    }
	
	/**
	 * Calls stmt.close() if stmt is not null.
	 * Calls connection.close() if the connection is not not null and is not already closed. 
	 * Any {@link SQLException} during the above operations is logged as 
	 * a warning and absorbed.
	 *  
	 * @param stmt
	 *          The statement to close. Can be null.
	 * @param connection
	 *          The connection to close. Can be null.
	 */
	public void closeStatementAndConnection(Statement stmt, Connection connection) {
		if ( stmt != null ) {
			try {
				stmt.close();
			}
			catch (SQLException e) {
				log.warn("error closing SQL statment", e);
			}
		}
		
		if ( connection != null ) {
			try {
				if ( ! connection.isClosed() ) {
					connection.close();
				}
			}
			catch (SQLException e) {
				log.warn("error closing connection", e);
			}
		}
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
	public OntologyInfo getOntologyVersion(final String ontologyUri, final String version) throws ServletException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename " +
				"from v_ncbo_ontology v, ncbo_ontology_file f  " +
				"where v.id = f.ontology_version_id" +
				"  and v.urn='" +ontologyUri+ "'" +
				"  and v.version_number='" +version+ "'";

			ResultSet rs = _stmt.executeQuery(query);

			if ( rs.next() ) {
				OntologyInfo ontology = new OntologyInfo();
				ontology.setUri(ontologyUri);
				ontology.setId(rs.getString(1));
				ontology.setOntologyId(rs.getString(2));
				ontology.setFilePath(rs.getString(3));

				ontology.setFilename(rs.getString(4));
				
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
			closeStatementAndConnection(_stmt, _con);
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
	public OntologyInfo getOntology(String ontologyUri) throws ServletException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

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
				OntologyInfo ontology = new OntologyInfo();
				ontology.setId(rs.getString(1));
				ontology.setOntologyId(rs.getString(2));
				ontology.setFilePath(rs.getString(3));
				
				ontology.setFilename(rs.getString(4));
				
				ontology.setUri(ontologyUri);

				return ontology;
			}
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			closeStatementAndConnection(_stmt, _con);
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
	public OntologyInfo getOntologyWithExts(MmiUri mmiUri, String[] foundUri) throws ServletException {
		// try with given URI:
		String ontologyUri = mmiUri.getOntologyUri();
		if ( log.isDebugEnabled() ) {
			log.debug("getOntologyWithExts: given URI=" +ontologyUri);
		}
		OntologyInfo ontology = this.getOntology(ontologyUri);
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
	public List<OntologyInfo> getOntologyVersions(MmiUri mmiUri) throws ServletException {
		
		List<OntologyInfo> onts = new ArrayList<OntologyInfo>();
		
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
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

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
				OntologyInfo ontology = new OntologyInfo();
				ontology.setId(rs.getString(1));
				ontology.setOntologyId(rs.getString(2));
				ontology.setFilePath(rs.getString(3));
				
				String ontologyUri = rs.getString(4);
				ontology.setUri(ontologyUri);

				ontology.setFilename(rs.getString(5));
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
			closeStatementAndConnection(_stmt, _con);
		}
		
		return onts;
	}
	
	
	
	public OntologyInfo getMostRecentOntologyVersion(MmiUri mmiUri) throws ServletException {
		List<OntologyInfo> onts = getOntologyVersions(mmiUri);
		if ( onts.size() == 0 ) {
			return null;
		}
		OntologyInfo ont = onts.get(0);
		return ont;
	}
	
	
	/**
	 * Gets the latest version of a registered ontology
	 * 
	 * @param potentialOntUri. The URI that will be used to try to find a corresponding registered
	 *                     ontology. If this is an "ont resolvable" uri, any explicit version is
	 *                     ignored.
	 * @return the ontology if found; null if not found.
	 * @throws ServletException
	 */
	public OntologyInfo getRegisteredOntologyLatestVersion(String potentialOntUri) throws ServletException  {
		log.debug("getRegisteredOntologyLatestVersion: " +potentialOntUri);
		OntologyInfo ontology = null;
		if ( OntUtil.isOntResolvableUri(potentialOntUri) ) {
			try {
				MmiUri mmiUri = new MmiUri(potentialOntUri);
				// ignore version:
				mmiUri = mmiUri.copyWithVersion(null);
				ontology = getMostRecentOntologyVersion(mmiUri);
			}
			catch (URISyntaxException e) {
				// Not an MmiUri. Just try to use the argument as given:
				ontology = getOntology(potentialOntUri);
			}
		}
		else {
			ontology = getOntology(potentialOntUri);
		}
		
		return ontology;
	}


	/**
	 * Returns the list of all ontologies in the database.
	 * 
	 * @return
	 * @throws ServletException
	 * @deprecated Use {@link #getAllOntologies(boolean)}
	 */
	// TODO Remove this deprecated method
	@Deprecated
	List<OntologyInfo> getOntologies() throws ServletException {
		List<OntologyInfo> onts = new ArrayList<OntologyInfo>();
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

			// NOTE:
			//    v_ncbo_ontology.id  ==  ncbo_ontology_file.ontology_version_id
			//
			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename, v.urn " +
				"from v_ncbo_ontology v, ncbo_ontology_file f " +
				"where v.id = f.ontology_version_id";
			
			ResultSet rs = _stmt.executeQuery(query);
			
	        while ( rs.next() ) {
	        	OntologyInfo ontology = new OntologyInfo();
	        	ontology.setId(rs.getString(1));
	        	ontology.setOntologyId(rs.getString(2));
	        	ontology.setFilePath(rs.getString(3));
	        	ontology.setFilename(rs.getString(4));
	        	ontology.setUri(rs.getString(5));
	        	onts.add(ontology);
	        }
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			closeStatementAndConnection(_stmt, _con);
		}
	
		return onts;
	}

	
	/**
	 * Gets the list of registered ontologies.
	 * Always with URIs in *versioned* form for the full-hosted entries.
	 * 
	 * @param allVersions true to report ALL versions of all ontologies;
	 *                    false to return just the lastest versions
	 * @return
	 * @throws ServletException
	 */
	public List<OntologyInfo> getAllOntologies(boolean allVersions) throws ServletException {
		
		List<OntologyInfo> onts = new ArrayList<OntologyInfo>();
		Map<String,OntologyInfo> mostRecent = new LinkedHashMap<String,OntologyInfo>();
		
		// If allVersions==true, we add to onts immediately; otherwise
		// we keep track of the most recent ontology for each ontology_id in mostRecent.
		// See below.
		
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

			// note that this is sorted in increasing ontology_id,version_number
			String query = 
				"select v.id, v.ontology_id, v.file_path, f.filename, v.urn, v.display_label " +
				"from v_ncbo_ontology v, ncbo_ontology_file f " +
				"where v.id = f.ontology_version_id " +
				"order by v.ontology_id, v.version_number";

			ResultSet rs = _stmt.executeQuery(query);
			
	        while ( rs.next() ) {

	        	OntologyInfo ontology = new OntologyInfo();
	        	ontology.setId(rs.getString(1));
	        	ontology.setOntologyId(rs.getString(2));
	        	ontology.setFilePath(rs.getString(3));
	        	ontology.setFilename(rs.getString(4));
	        	ontology.setUri(rs.getString(5));
	        	ontology.setDisplayLabel(rs.getString(6));
	        	
	        	if ( allVersions ) {
	        		// just add item immediately:
	        		onts.add(ontology);
	        	}
	        	else {
	        		// "update" the most recent item for this ontology_id
	        		mostRecent.put(ontology.getOntologyId(), ontology);
	        	}
	        }

	        if ( allVersions ) {
	        	// just return the list
	        	return onts;
	        }
	        else {
	        	// add all the most recent items:
	        	for ( OntologyInfo ontology : mostRecent.values() ) {
	        		onts.add(ontology);
	        	}
	        	return onts;
	        }
		} 
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			closeStatementAndConnection(_stmt, _con);
		}
	}
	
	/**
	 * Gets info about a user.
	 * @param username
	 * @return
	 */
	public Map<String,String> getUserInfo(String username) throws ServletException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

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
			closeStatementAndConnection(_stmt, _con);
		}
	}
	
	/**
	 * Gets all registered users.
	 * @return
	 */
	public List<Map<String,String>> getAllUserInfos() throws ServletException {
		// to format date_created appropriately (xsd dateTime)
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = getConnection();
			_stmt = _con.createStatement();

			String query = 
				"select id, username, email, firstname, lastname, phone, date_created " +
				"from ncbo_user " +
				"order by date_created " ;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			while ( rs.next() ) {
				ResultSetMetaData md = rs.getMetaData();
				Map<String,String> props = new LinkedHashMap<String,String>();
			
				for ( int i = 1, count = md.getColumnCount(); i <= count; i++ ) {
					String value = rs.getString(i);
					if ( value != null ) {
						String colName = md.getColumnName(i);
						
						if ( colName.equals("date_created") ) {
							Timestamp date_created = rs.getTimestamp(i);
							value = sdf.format(date_created);
						}
						
						props.put(colName , value);
					}
				}
				
	        	list.add(props);
	        }
			
			return list;
		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			closeStatementAndConnection(_stmt, _con);
		}
	}

}
