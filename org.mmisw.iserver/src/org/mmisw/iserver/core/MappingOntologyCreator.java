package org.mmisw.iserver.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.JenaUtil2;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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
		
		log.info("!!!!!!!!!!!!!!!! MappingOntologyCreator: values = " +values);

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
		log.info("New ontology created with namespace " + ns_ + " base " + base_);

		// Indicate VINE as the engineering tool:
		ont.addProperty(Omv.usedOntologyEngineeringTool, OmvMmi.vine);
		
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
			newOntModel.add(r, p, o);
		}
		
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
		
		log.info("setFinalUri: " +finalUri);
	}


	public String getOntologyStringXml() {
		return JenaUtil2.getOntModelAsString(newOntModel, "RDF/XML-ABBREV");
	}

	public String getPathOnServer() {
		return uniqueBaseName;
	}

}