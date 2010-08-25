package org.mmisw.ont.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.Db;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntServlet.Request;
import org.mmisw.ont.vocabulary.Rdfg;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.drexel.util.rdf.JenaUtil;



/**
 * Dispatcher of admin-related operations.
 * 
 * @author Carlos Rueda
 */
public class AdminDispatcher {
	
	private static File internalDir;
	private static File graphsFile;

	
	private static final String[][] SUPPORTING_NAMESPACES = new String[][] {
			{ "skos", "http://www.w3.org/2004/02/skos/core#" }, 
			{ "skos2", "http://www.w3.org/2008/05/skos#" }, 
			{ "rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/" }, 
		};
		

	
	private static final String[][] SUPPORTING_STATEMENTS = {
		
			//////// SKOS
			{ "!skos:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos:exactMatch", "!rdf:type", "!owl:Symmetric" },
			{ "!skos:closeMatch", "!rdf:type", "!owl:Symmetric" },
			{ "!skos:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos:broadMatch", "!owl:inverseOf", "!skos:narrowMatch" },
			{ "!skos:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos:relatedMatch", "!rdf:type", "!owl:Symmetric" },
			
			//////// SKOS2
			{ "!skos2:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos2:exactMatch", "!rdf:type", "!owl:Symmetric" },
			{ "!skos2:closeMatch", "!rdf:type", "!owl:Symmetric" },
			{ "!skos2:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos2:broadMatch", "!owl:inverseOf", "!skos2:narrowMatch" },
			{ "!skos2:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
			{ "!skos2:relatedMatch", "!rdf:type", "!owl:Symmetric" },
			
			//////// RDFG
			{ "!rdfg:subGraphOf", "!rdf:type", "!owl:TransitiveProperty" },

	};

	
	private final Log log = LogFactory.getLog(AdminDispatcher.class);
	
	private Db db;
	
	public AdminDispatcher(Db db) {
		this.db = db;
	}

	
	/**
	 * Gets prefix-namespace pairs for some required namespaces
	 * 
	 * @return the namespaces.
	 */
	public String[][] getSupportingNamespaces() {
		return SUPPORTING_NAMESPACES;
	}

	/**
	 * Gets the list of supporting statements
	 * 
	 * @return the statements.
	 */
	public String[][] getSupportingStatements() {
		return SUPPORTING_STATEMENTS;
	}

	/**
	 * Initializes some internal resources.
	 * 
	 * @throws ServletException
	 */
	public void init() throws ServletException {
		log.info("init called.");
		
		internalDir = new File(OntConfig.Prop.ONT_INTERNAL_DIR.getValue());
		graphsFile = new File(internalDir, "graphs.rdf");
		
		log.info("internalDir: " +internalDir);
		log.info("graphsFile: " +graphsFile);

		// create graphs file if not already created:
		getGraphsModel();
	}
	
	
	/**
	 * Responds an RDF with registered users. Every user URI will be *versioned* with the current time.
	 */
	public void getUsersRdf(Request req) throws ServletException, IOException {
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
		
		req.response.setContentType("application/rdf+xml");
		ServletOutputStream os = req.response.getOutputStream();
		IOUtils.write(result, os);
		os.close();
	}

	
	

	/**
	 * Adds a new graph to the internal graphs resource.
	 * 
	 * @param graphUri    URI of the graph. Assumed to be well-formed.
	 */
	public void newGraph(String graphUri) {
		
		Model model = getGraphsModel();
		
		Resource subGraphRes = ResourceFactory.createResource(graphUri);
		
		Statement stmt = ResourceFactory.createStatement(subGraphRes, RDF.type, Rdfg.Graph);
		model.add(stmt);
		log.debug("newGraph: added statement: " +stmt);
		
		try {
			updateGraphsFile(model);
		}
		catch (Exception e) {
			log.error("Cannot write out to file " +graphsFile, e);
		}
	}
	
	
	/**
	 * Updates the internal graphs resource.
	 * 
	 * @param subGraphUri    URI of subject of the subGraphOf property. Assumed to be well-formed.
	 * @param superGraphUri  URI of object of the subGraphOf property. Assumed to be well-formed.
	 * 
	 * @return corresponding statements suitable to update the main graph.
	 */
	public List<Statement> newSubGraph(String subGraphUri, String superGraphUri) {
		
		Model model = getGraphsModel();
		
		List<Statement> statements = new ArrayList<Statement>();
		
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
			updateGraphsFile(model);
		}
		catch (Exception e) {
			log.error("Cannot write out to file " +graphsFile, e);
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
		
		Model model = getGraphsModel();
		
		Resource subGraphRes = ResourceFactory.createResource(subjectUri);
		
		if ( log.isDebugEnabled() ) {
			log.debug("removeAllStatementsFromSubject: " +subGraphRes);
		}
		
		model.removeAll(subGraphRes, null, null);
		try {
			updateGraphsFile(model);
		}
		catch (Exception e) {
			log.error("removeAllStatementsFromSubject: Cannot write out to file " +graphsFile, e);
		}
	}

	
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
	 * @return corresponding statements suitable to update the main graph.
	 */
	public List<Statement> getInternalStatements() {
		
		Model model = getGraphsModel();
		if ( model == null ) {
			return null;
		}
		
		List<Statement> statements = new ArrayList<Statement>();
		
		StmtIterator stmsIter = model.listStatements();
		while ( stmsIter.hasNext() ) {
			Statement stmt = stmsIter.nextStatement();
			statements.add(stmt);
		}
		
		return statements;
	}
	


	/**
	 * Gets the current contents of the graphs file. If the file does not exist, it is created
	 * with an initial contents.
	 * 
	 * @return the model for the graphs file.
	 */
	private Model getGraphsModel() {
		
		Model model = JenaUtil.createDefaultRDFModel();
		boolean createFile = false;
		
		if ( ! graphsFile.isFile() ) {
			if ( ! internalDir.isDirectory() ) {
				if ( ! internalDir.mkdirs() ) {
					log.error("Cannot create directory " +internalDir);
					return null;
				}
			}
			createFile = true;
		}
		
		if ( createFile ) {
			String[][] namespaces = getSupportingNamespaces();
			for ( String[] ns : namespaces ) {
				model.setNsPrefix(ns[0], ns[1]);
			}
			try {
				model.write(new FileOutputStream(graphsFile), "RDF/XML-ABBREV");
				log.info(graphsFile+ ": model created.");
			}
			catch (FileNotFoundException e) {
				log.error("Cannot write out to file " +graphsFile, e);
				return null;
			}
		}
		else {
			model.read(graphsFile.toURI().toString());
			log.info(graphsFile+ ": model read in.");
		}
		
		return model;
	}

	private void updateGraphsFile(Model model) throws Exception {
		String rdf = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		_writeRdfToFile(rdf, graphsFile);
		log.info(graphsFile+ ": model updated.");
	}
	
	private static void _writeRdfToFile(String rdf, File reviewedFile) throws Exception {
		PrintWriter os = new PrintWriter(reviewedFile, "UTF-8");
		StringReader is = new StringReader(rdf);
		IOUtils.copy(is, os);
		os.flush();
	}

}
