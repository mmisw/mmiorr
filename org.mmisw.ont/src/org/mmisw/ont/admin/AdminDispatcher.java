package org.mmisw.ont.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntRequest;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.util.ServletUtil;
import org.mmisw.ont.vocabulary.Rdfg;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Dispatcher of admin-related operations.
 * 
 * <p>
 * This singleton maintains a "graphs" file with appropriate control of concurrency access
 * with respect to the running virtual machine where the Ont service is running.
 * It is assumed that the file is not modified externally.
 * 
 * <p>
 * All public methods, except {@link #init(Db)}, will throw {@link IllegalStateException}
 * if the instance has not been initialized.
 * 
 * @author Carlos Rueda
 */
@ThreadSafe
public class AdminDispatcher {
	
	/** the singleton instance of this class */
	private static AdminDispatcher instance = null;
	
	/**
	 * Creates the singleton instance of this class.
	 * {@link #init()} should be called for the actual initialization of the returned object.
	 * 
	 * @param db
	 *           used to retrieve user information
	 *           
	 * @throw IllegalArgumentException if argument is null
	 * @throw IllegalStateException if already created
	 */
	public static AdminDispatcher createInstance(Db db) {
		if ( db == null ) {
			throw new IllegalArgumentException();
		}
		if ( instance != null ) {
			throw new IllegalStateException("instance already created");
		}
		instance = new AdminDispatcher(db);
		return instance;
	}
	
	/** unmodifiable map of prefix-namespace pairs for some required namespaces. */
	@SuppressWarnings("serial")
	private static final Map<String,String> SUPPORTING_NAMESPACES = Collections.unmodifiableMap(
			new HashMap<String, String>() {{ 
				put("skos", "http://www.w3.org/2004/02/skos/core#");
				put("skos2", "http://www.w3.org/2008/05/skos#");
				put("rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/");
			}}
	);
	
	private final Log log = LogFactory.getLog(AdminDispatcher.class);
	
	/** 
	 * Used to retrieve user information.
	 */
	private final Db db;
	
	/** set at {@link #init()} time */
	private File internalDir;
	
	/** set at {@link #init()} time */
	private File graphsFile;

	
	/**
	 * Initializes internal resources.
	 * 
	 * <p>
	 * Any errors are logged out as warnings. This is due to the fact that
	 * these internal resources (currently a model -backed by a file- with
	 * graphs and subGraphOf relations) are only partially supported in the
	 * overall ORR system. So, errors here are not considered critical in this sense. 
	 */
	public void init()  {
		log.info("init called.");
		
		internalDir = new File(OntConfig.Prop.ONT_INTERNAL_DIR.getValue());
		graphsFile = new File(internalDir, "graphs.rdf");
		
		log.info("internalDir: " +internalDir);
		log.info("graphsFile: " +graphsFile);

		// create graphs file if not already created:
		_getGraphsModel();
	}
	
	
	/**
	 * Gets prefix-namespace pairs for some required namespaces.
	 * The returned map is unmodifiable.
	 * 
	 * @return the namespaces.
	 */
	public Map<String,String> getSupportingNamespaces() {
		return SUPPORTING_NAMESPACES;
	}


	/**
	 * Responds an RDF with registered users. 
	 * Every user URI will be *versioned* with the current time.
	 * 
	 * <p>
	 * NOTE: RDF structure is preliminary.
	 */
	public void getUsersRdf(OntRequest req) throws ServletException, IOException {
		final String MMIORR_NS = "http://mmisw.org/ont/mmi/mmiorr/";
		
		log.debug("getUsersRdf called.");
		
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String version = sdf.format(date);

		final String users_ns = OntConfig.Prop.ONT_SERVICE_URL.getValue()+ "/mmiorr-internal/" +version+ "/users/";
		
		final Model model = ModelFactory.createDefaultModel();
		final Resource userClass = model.createResource( MMIORR_NS + "User" );
		model.setNsPrefix("mmiorr", MMIORR_NS);
		model.setNsPrefix("", users_ns);
		
		final String[][] fieldPropNames = {
				{ "username",  "hasUserName" },
				{ "firstname", "hasFirstName" },
				{ "lastname",  "hasLastName" },
				{ "email",     "hasEmail" },
				{ "date_created", "hasDateCreated" },
		};
		
		List<Map<String, String>> list = db.getAllUserInfos();
		for (Map<String, String> user : list) {
			
			String username = user.get("username");
			if ( username == null || username.length() == 0 ) {
				continue;
			}
			
			// avoid spaces in the local name
			username = username.replaceAll("\\s", "_");
			
			Resource userInstance = model.createResource( users_ns + username );
			// type:
			model.add(userInstance, RDF.type, userClass);
			for (String[] fieldPropName : fieldPropNames ) {
				String propValue = user.get(fieldPropName[0]);
				if ( propValue == null || propValue.length() == 0 ) {
					continue;
				}
				Property propUri = model.createProperty( MMIORR_NS , fieldPropName[1] );
				if ( "hasDateCreated".equals(fieldPropName[1]) ) {
					model.add(userInstance, propUri, propValue,  XSDDatatype.XSDdateTime);
				}
				else {
					model.add(userInstance, propUri, propValue, XSDDatatype.XSDstring);
				}
			}
		}
		
		String result = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		ServletUtil.writeResponseRdfXml(req.response, result);
	}

	/**
	 * Adds a new graph to the internal graphs resource.
	 * 
	 * @param graphUri    
	 *            URI of the graph. Assumed to be well-formed.
	 */
	public void newGraph(String graphUri) {
		synchronized (this) {
			Model model = _getGraphsModel();
			Resource subGraphRes = ResourceFactory.createResource(graphUri);
			Statement stmt = ResourceFactory.createStatement(subGraphRes, RDF.type, Rdfg.Graph);
			model.add(stmt);
			log.debug("newGraph: added statement: " +stmt);
			try {
				_updateGraphsFile(model);
			}
			catch (Exception e) {
				log.error("Cannot write out to file " +graphsFile, e);
			}
		}
	}
	
	
	/**
	 * Creates a new subGraphOf relation.
	 * Updates the internal graphs resource.
	 * 
	 * @param subGraphUri    URI of subject of the subGraphOf property. Assumed to be well-formed.
	 * @param superGraphUri  URI of object of the subGraphOf property. Assumed to be well-formed.
	 * 
	 * @return corresponding statements suitable to update the main graph.
	 */
	public List<Statement> newSubGraph(String subGraphUri, String superGraphUri) {
		List<Statement> statements = new ArrayList<Statement>();
		synchronized (this) {
			Model model = _getGraphsModel();

			Resource subGraphRes = ResourceFactory.createResource(subGraphUri);
			Resource superGraphRes = ResourceFactory.createResource(superGraphUri);

			statements.add(ResourceFactory.createStatement(subGraphRes, RDF.type, Rdfg.Graph));
			statements.add(ResourceFactory.createStatement(superGraphRes, RDF.type, Rdfg.Graph));
			statements.add(ResourceFactory.createStatement(subGraphRes, Rdfg.subGraphOf, superGraphRes));

			for (Statement stmt : statements) {
				model.add(stmt);
				log.debug("newSubGraph: added statement: " +stmt);
			}

			try {
				_updateGraphsFile(model);
			}
			catch (Exception e) {
				log.error("Cannot write out to file " +graphsFile, e);
			}
		}
		
		return statements;
	}
	
	/**
	 * Removes all statements for a given subject in the internal graphs resource 
	 * 
	 * @param subjectUri    
	 *                    URI of subject. Assumed to be well-formed.
	 */
	public void removeAllStatementsFromSubject(String subjectUri) {
		synchronized (this) {
			Model model = _getGraphsModel();

			Resource subGraphRes = ResourceFactory.createResource(subjectUri);

			if ( log.isDebugEnabled() ) {
				log.debug("removeAllStatementsFromSubject: " +subGraphRes);
			}

			model.removeAll(subGraphRes, null, null);
			try {
				_updateGraphsFile(model);
			}
			catch (Exception e) {
				log.error("removeAllStatementsFromSubject: Cannot write out to file " +graphsFile, e);
			}
		}
	}

	/**
	 * Removes any leading/trailing angle brackets to the given string.
	 * If the resulting string, <i>RES</i>, contains only word characters or hyphens, then
	 * it is used as a local name to create and return the full URI 
	 * <i>ONT_SERVICE_URL</i>/mmiorr-internal/graphs/<i>RES</i>.
	 * Otherwise, <i>RES</i> is returned.
	 * 
	 * @param uri
	 * @return The resulting string.
	 */
	public String getWellFormedGraphUri(String uri) {
		// remove any leading/trailing angle brackets:
		uri = uri.trim().replaceAll("^<+|>+$", "");
		
		if ( uri.matches("(\\w|-)+") ) {
			// uri is just word characters or the hyphen: use this as local name for the
			// final absolute URI:
			if ( uri.matches("^\\d.*") ) {
				uri = "_" +uri;
			}
			uri = OntConfig.Prop.ONT_SERVICE_URL.getValue() + "/mmiorr-internal/graphs/" +uri;
		}
		
		return uri;
	}

	
	/**
	 * Gets the statements in the internal resources.
	 * 
	 * @return corresponding statements suitable to update the triple store.
	 *         null if some error happens, in such a case warnings are logged. 
	 *         NOTE: This behavior is subject to change as more overall support for graphs is
	 *         implemented.
	 */
	public List<Statement> getInternalStatements() {
		Model model = null;
		synchronized (this) {
			model = _getGraphsModel();
		}
		if ( model == null ) {
			return null;
		}
		return model.listStatements().toList();
	}
	

	/**
	 * {@link #init()} does the actual initialization.
	 */
	private AdminDispatcher(Db db) {
		this.db = db;
	}


	/**
	 * Gets the current contents of the graphs file. If the file does not exist, it is created
	 * with an initial contents.
	 * 
	 * <p>
	 * Note: caller is responsible for any necessary synchronization.
	 * 
	 * @return the model for the graphs file. 
	 *         null if some error happens, in such a case warnings are logged. 
	 *         NOTE: This behavior is subject to change as more overall support for graphs is
	 *         implemented.
	 */
	private Model _getGraphsModel() {
		
		Model model = JenaUtil2.createDefaultRDFModel();
		boolean createFile = false;
		
		if ( ! graphsFile.isFile() ) {
			if ( ! internalDir.isDirectory() ) {
				if ( ! internalDir.mkdirs() ) {
					log.warn("Cannot create directory " +internalDir);
					return null;
				}
			}
			createFile = true;
		}
		
		if ( createFile ) {
			// brand new graphs file.
			//
			// save a model with just the supporting namespaces:
			for ( Entry<String, String> ns : getSupportingNamespaces().entrySet() ) {
				model.setNsPrefix(ns.getKey(), ns.getValue());
			}
			try {
				model.write(new FileOutputStream(graphsFile), "RDF/XML-ABBREV");
				log.info(graphsFile+ ": model created.");
			}
			catch (FileNotFoundException e) {
				log.warn("Cannot write out to file " +graphsFile, e);
				return null;
			}
		}
		else {
			model.read(graphsFile.toURI().toString());
			log.info(graphsFile+ ": model read in.");
		}
		
		return model;
	}

	/**
	 * Updates the graphs file. 
	 * <p>
	 * Note: caller is responsible for any necessary synchronization.
	 * 
	 * @param model
	 * @throws Exception
	 */
	private void _updateGraphsFile(Model model) throws Exception {
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		_writeRdfToFile(rdf, graphsFile);
		log.info(graphsFile+ ": model updated.");
	}
	
	/**
	 * Writes the given string into the given file in UTF-8.
	 * <p>Note: caller is responsible for any necessary synchronization.
	 */
	private static void _writeRdfToFile(String str, File file) throws Exception {
		PrintWriter os = new PrintWriter(file, "UTF-8");
		try {
			IOUtils.write(str, os);
			os.flush();
		}
		finally {
			IOUtils.closeQuietly(os);
		}
	}
}
