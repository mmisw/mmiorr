package org.mmisw.orrclient.core.vine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ont.vocabulary.Vine;
import org.mmisw.orrclient.core.MdHelper;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;
import org.mmisw.orrportal.gwt.server.OrrConfig;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Creates a mapping ontology.
 *
 * @author Carlos Rueda
 */
public class MappingOntologyCreator {

	// TODO do we want to include owl:import <vine-uri>?  Set to false.
	private static final boolean ADD_VINE_IMPORT = false;

	// Use Vine.Statement for the reification. Set to true.
	// (false will use basic rdf:Statement and associated properties)
	private static final boolean USE_VINE_STATEMENT = true;

	private String namespaceRoot;

	private String orgAbbreviation;
	private String shortName;

	private String finalUri;

	private String finalShortName;


	private Map<String, String> values;


	private OntModel newOntModel;
	private String ns_;
	private String base_;

	private final Log log = LogFactory.getLog(MappingOntologyCreator.class);

	private Set<String> namespaces;
	private List<Mapping> mappings;

	private final String uniqueBaseName = _createUniqueBaseName();


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

		this.namespaces = mappingDataCreationInfo.getNamespaces();
		this.mappings = mappingDataCreationInfo.getMappings();

		String ontServiceUrl = OrrConfig.instance().ontServiceUrl;
		this.namespaceRoot = ontServiceUrl;
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
		String rdf = _getOntologyStringXml();


		// now save the RDF:
		File full_path = new File(OrrConfig.instance().voc2rdfDir, getPathOnServer());
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
		createVocabResult.setFullPath(full_path.getAbsolutePath());
	}

	private OntModel _createOntModel() {
		OntModel ontModel = JenaUtil2.createDefaultOntModel();

		// TODO: from the previous code based on OwlModel, ie.,
//		ontModel = new OwlModel(ontModel);
		// it seems the following "layer" is unnecesary. So, returning JenaUtil2.createDefaultOntModel()
		// directly should be fine.

		ontModel = ModelFactory.createOntologyModel(ontModel.getSpecification(), ontModel);

		return ontModel;
	}

	private void processCreateOntology() throws Exception {

		newOntModel = _createOntModel();
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

		// add any desired owl:imports
		_addImports(ont);

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


	/** adds an owl:import for each namespace in namespaces, if any */
	private void _addImports(Ontology ont) {

		if ( ADD_VINE_IMPORT ) {
			ont.addImport(ResourceFactory.createResource(JenaUtil2.removeTrailingFragment(Vine.NS)));
		}

		if ( namespaces == null ) {
			return;
		}

		for ( String namespace : namespaces ) {
			ont.addImport(ResourceFactory.createResource(JenaUtil2.removeTrailingFragment(namespace)));
		}
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
	 * @param r
	 * @param p
	 * @param o
	 */
	private void _createMapping(Resource r, Property p, RDFNode o, Map<String, String> md) {

		// the resource representing the statement (r,p,o):
		Resource stmtRsr;

		if ( USE_VINE_STATEMENT ) {
			// use vine:Statement
			stmtRsr = _createVineMappingStatement(newOntModel, r, p, o);
		}
		else {
			// use basic reification.
			Statement stmt = newOntModel.createStatement(r, p, o);
			newOntModel.add(stmt);
			stmtRsr = stmt.createReifiedStatement();
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
		Resource res;
		if ( USE_VINE_STATEMENT ) {
			res = model.createResource(Vine.Statement);
			res.addProperty(Vine.subject, s);
			res.addProperty(Vine.predicate, p);
			res.addProperty(Vine.object, o);
			// note, also add the direct mapping statement, which may be redundant if
			// appropriate reasoner rules over Vine properties are in place, but this
			// will be OK in general.
			model.add(s, p, o);
		}
		else {
			// this part was for some preliminary testing with basic RDF elements.
			res = model.createResource(RDF.Statement);
			res.addProperty(RDF.subject, s);
			res.addProperty(RDF.predicate, p);
			res.addProperty(RDF.object, o);
		}
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

	/** Removes unused namespace prefixes and returns the RDF/XML representation of
	 * the created ontology.
	 */
	private String _getOntologyStringXml() {
		JenaUtil2.removeUnusedNsPrefixes(newOntModel);
		return JenaUtil2.getOntModelAsString(newOntModel, "RDF/XML-ABBREV");
	}

	public String getPathOnServer() {
		return uniqueBaseName;
	}


	/**
	 * TODO createUniqueBaseName(): need a more robust way to get a unique name.
	 */
	private static String _createUniqueBaseName() {
		return String.valueOf(System.currentTimeMillis());

	}
}
