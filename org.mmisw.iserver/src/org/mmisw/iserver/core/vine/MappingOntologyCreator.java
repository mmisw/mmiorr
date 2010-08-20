package org.mmisw.iserver.core.vine;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.MdHelper;
import org.mmisw.iserver.core.ServerConfig;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ont.vocabulary.Vine;

import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;

/**
 * Creates a mapping ontology.
 * 
 * @author Carlos Rueda
 */
public class MappingOntologyCreator {

	private String namespaceRoot;
	
	private String orgAbbreviation;
	private String shortName;

	private String finalUri;
	
	private String finalShortName;


	private Map<String, String> values;
	
	
	private OwlModel newOntModel;
	private String ns_;
	private String base_;

	private final Log log = LogFactory.getLog(MappingOntologyCreator.class);
	

	
	/**
	 * TODO createUniqueBaseName(): need a more robust way to get a unique name. 
	 */
	private static String _createUniqueBaseName() {
		return String.valueOf(System.currentTimeMillis());

	}
	private final String uniqueBaseName = _createUniqueBaseName();


	private List<Mapping> mappings;

	
	
	/**
	 * 
	 * @param createOntologyInfo      Metadata for the ontology
	 * @param mappingDataCreationInfo    Data for the ontology
	 */
	public MappingOntologyCreator(
			CreateOntologyInfo createOntologyInfo,
			MappingDataCreationInfo mappingDataCreationInfo
	)  {
		
		Map<String, String> values = createOntologyInfo.getMetadataValues();
		
		this.mappings = mappingDataCreationInfo.getMappings();
		
		this.namespaceRoot = ServerConfig.Prop.ONT_SERVICE_URL.getValue();
		this.orgAbbreviation = createOntologyInfo.getAuthority();
		this.shortName = createOntologyInfo.getShortName();
		
		this.values = values;
		
		if ( log.isDebugEnabled() ) {
			log.debug("!!!!!!!!!!!!!!!! MappingOntologyCreator: metadata values = " +values+ "\n" +
					" number of mappings = " +mappings.size());
		}

		if ( orgAbbreviation == null ) {
			orgAbbreviation = values.get(OmvMmi.origMaintainerCode.getURI());
		}
		
		if ( shortName == null ) {
			shortName = values.get(Omv.acronym.getURI());
		}

	}
	

	public void createOntology(CreateOntologyResult createVocabResult) {
		if ( log.isDebugEnabled() ) {
			log.debug("MappingOntologyCreator.createOntology");
		}
		
		//
		// generate RDF:
		//
		setFinalUri();
		try {
			processCreateOntology();
		}
		catch (Exception e) {
			String error = e.getMessage();
			log.error(error, e);
			createVocabResult.setError(error);
			return;
		}
		String rdf = getOntologyStringXml();
		

		// now save the RDF:
		String full_path = ServerConfig.Prop.ONTMD_VOC2RDF_DIR.getValue() + getPathOnServer();
		try {
			FileWriter os = new FileWriter(full_path);
			os.write(rdf);
			os.close();
		}
		catch (IOException ex) {
			String msg = "Error writing generated RDF file: " +full_path; 
			log.error(msg, ex);
			createVocabResult.setError(msg);
			return;
		}

		// OK:
		createVocabResult.setFullPath(full_path);
	}
	
	private void processCreateOntology() throws Exception {

		newOntModel = new OwlModel(JenaUtil.createDefaultOntModel());
		ns_ = JenaUtil2.appendFragment(finalUri);
		base_ = JenaUtil2.removeTrailingFragment(finalUri);
		
		// set NS prefixes:
		newOntModel.setNsPrefix("", ns_);
		Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
		for ( String uri : preferredPrefixMap.keySet() ) {
			String prefix = preferredPrefixMap.get(uri);
			newOntModel.setNsPrefix(prefix, uri);
		}
		
		Ontology ont = newOntModel.createOntology(base_);
		if ( log.isDebugEnabled() ) {
			log.debug("New ontology created with namespace " + ns_ + " base " + base_);
		}

		// Indicate VINE as the engineering tool:
		ont.addProperty(Omv.usedOntologyEngineeringTool, OmvMmi.vine);
		
		// Import the VINE ontology to provide associated semantics:
		ont.addImport(ResourceFactory.createResource(JenaUtil2.removeTrailingFragment(Vine.NS)));
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		for ( String uri : values.keySet() ) {
			String value = values.get(uri);
			if ( value.trim().length() > 0 ) {
				Property prop = uriPropMap.get(uri);
				if ( prop == null ) {
					log.warn("No property found for uri='" +uri+ "'");
					continue;
				}
				ont.addProperty(prop, value.trim());
			}
		}
		
		// set Omv.uri from final
		ont.addProperty(Omv.uri, finalUri);
		
		// TODO set Omv.acronym from something
		ont.addProperty(Omv.acronym, "ACRONYM");
		
		
		String fullTitle = values.get("fullTitle");
		if ( fullTitle != null ) {
			ont.addProperty(Omv.name, fullTitle);
		}
		
		String creator = values.get("creator");
		if ( creator != null ) {
			ont.addProperty(Omv.hasCreator, creator);
		}
		
		String briefDescription = values.get("briefDescription");
		if ( briefDescription != null ) {
			ont.addProperty(Omv.description, briefDescription);
		}
		
		if ( orgAbbreviation != null && orgAbbreviation.trim().length() > 0 ) {
			ont.addProperty(OmvMmi.origMaintainerCode, orgAbbreviation.trim());
		}
		
		createContents();
	}


	private void createContents() {
		
		for ( Mapping mapping : mappings ) {
			String left = mapping.getLeft();
			String relation = mapping.getRelation();
			String right = mapping.getRight();
			
			Resource r = newOntModel.createResource(left);
			Property p = newOntModel.createProperty(relation);
			RDFNode o = newOntModel.createResource(right);
			
			Map<String, String> md = mapping.getMetadata();

			_createMapping(r, p, o, md);
		}
		
	}

	/**
	 * Creates and adds to the model the mapping statement.
	 * @param mapping
	 * @param r
	 * @param p
	 * @param o
	 */
	private void _createMapping(Resource r, Property p, RDFNode o, Map<String, String> md) {
		
		// the resource representing the statement (r,p,o):
		Resource stmtRsr;
		
		if ( false ) { 
			// use basic reification.
			Statement stmt = newOntModel.createStatement(r, p, o);
			newOntModel.add(stmt);
			stmtRsr = stmt.createReifiedStatement();
		}
		else {
			// use vine:Statement
			stmtRsr = _createVineMappingStatement(newOntModel, r, p, o);
		}
		
		if ( md != null ) {
			for ( String uri : md.keySet() ) {
				String value = md.get(uri);
				Property prop = newOntModel.createProperty(uri);
				stmtRsr.addProperty(prop, value);
			}
		}
	}


	private static Resource _createVineMappingStatement(Model model, Resource s, Property p, RDFNode o) {
		Resource res = model.createResource(Vine.Statement);
		res.addProperty(Vine.subject, s);
		res.addProperty(Vine.predicate, p);
		res.addProperty(Vine.object, o);
//		res.addProperty(DC.date, ISO8601Date.getCurrentDate());
		return res;
	}


	private void setFinalUri() {

		// If given, ontologyUri will take precedence (see VocabPanel)
		String ontologyUri = values.get("ontologyUri");
		
		if ( ontologyUri != null ) {
			log.debug("Using given ontologyUri and finalUri: " +ontologyUri);
			finalUri = ontologyUri.replaceAll("(/|\\\\)+$", "");
		}
		else {
			// remove any trailing slashes
			namespaceRoot = namespaceRoot.replaceAll("(/|\\\\)+$", "");  
			
			//
			// replace any colon (:) in the pieces that go to the ontology URI
			// with underscores (_):
			//
			
			String orgAbbrev = orgAbbreviation.replaceAll("\\s+", "").replace(':', '_');
			
			finalUri = namespaceRoot + "/" + orgAbbrev;
			
			
			finalShortName = shortName.toLowerCase().replace(':', '_');
			finalUri += "/" + finalShortName;
		}
		
		// see createProperties()
		
		if ( log.isDebugEnabled() ) {
			log.debug("setFinalUri: " +finalUri);
		}
	}


	public String getOntologyStringXml() {
		return JenaUtil2.getOntModelAsString(newOntModel, "RDF/XML-ABBREV");
	}

	public String getPathOnServer() {
		return uniqueBaseName;
	}

}