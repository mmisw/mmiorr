package org.mmisw.orrclient.core.util.ontinfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.core.util.OntServiceUtil;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ClassInfo;
import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.IndividualInfo;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyType;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrclient.gwt.client.rpc.PropertyInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Based on SPARQL queries.
 * 
 * @deprecated refactoring in progress -- should use OntInfo instead
 * @author Carlos Rueda
 */
@Deprecated
class OntInfoOld extends BaseOntInfo {
	
	private static final Log log = LogFactory.getLog(OntInfoOld.class);
	
	private static final String QUERY_PREFIXES =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
		"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
		"PREFIX skos2: <http://www.w3.org/2008/05/skos#>\n"
	;
	
	/** Query to obtain the individuals in a model */
	private static final String INDIVIDUALS_QUERY = 
		QUERY_PREFIXES +
		"SELECT ?instance ?class " +
		"WHERE {    { ?class rdf:type owl:Class }" +
		"     UNION { ?class rdf:type rdfs:Class }  ." +
		"        ?instance rdf:type ?class . }"
	;
	
	/** Query to obtain the datatype properties in a model */
	private static final String DATATYPE_PROPERTIES_QUERY =
		QUERY_PREFIXES +
		"SELECT ?prop ?domain " +
		"WHERE { ?prop rdf:type owl:DatatypeProperty. " +
		        "?prop rdfs:domain ?domain ." +
		     " }"
	;
	
	/** Query to obtain the object properties in a model */
	private static final String OBJECT_PROPERTIES_QUERY =
		QUERY_PREFIXES +
		"SELECT ?prop ?domain " +
		"WHERE { ?prop rdf:type owl:ObjectProperty . " +
		        "?prop rdfs:domain ?domain ." +
		     " }"
	;
	
	/** Query to obtain the classes in a model */
	private static final String CLASSES_QUERY =
		QUERY_PREFIXES +
		"SELECT ?class " +
		"WHERE {    { ?class rdf:type owl:Class }" +
		"     UNION { ?class rdf:type rdfs:Class }  . }"
	;
	
	/** Query template to obtain all properties associated with an entity */
	private static final String PROPS_QUERY_TEMPLATE =
		"SELECT ?prop ?value " +
		"WHERE { <{E}> ?prop ?value . }"
	;
	
	/** Query to get the SKOS relations in a model */
	private static final String SKOS_QUERY = 
		QUERY_PREFIXES +
		"SELECT ?left ?rel ?right \n" +
		"WHERE {    { ?left ?rel ?right  } . \n" +
		"           {      { ?left skos:broadMatch ?right } \n" +
		"            UNION { ?left skos:closeMatch ?right } \n" +
		"            UNION { ?left skos:exactMatch ?right } \n" +
		"            UNION { ?left skos:narrowMatch ?right } \n" +
		"            UNION { ?left skos:relatedMatch ?right } \n" +
		"            UNION { ?left skos2:broadMatch ?right } \n" +
		"            UNION { ?left skos2:closeMatch ?right } \n" +
		"            UNION { ?left skos2:exactMatch ?right } \n" +
		"            UNION { ?left skos2:narrowMatch ?right } \n" +
		"            UNION { ?left skos2:relatedMatch ?right } \n" +
		"           } .\n" +
		"}" 
	;
	
	
	/**
	 * Populates the list of entities associated with the given ontology. 
	 * @param baseOntologyInfo
	 * @return the given argument
	 * @throws Exception 
	 */
	public BaseOntologyInfo getEntities(BaseOntologyInfo baseOntologyInfo, OntModel ontModel) throws Exception {
		String ontologyUri = baseOntologyInfo.getUri();

		// individuals:
		List<IndividualInfo> individuals = _getIndividuals(null, ontModel, ontologyUri);

		// datatype properties:
		List<PropertyInfo> properties = _getProperties(DATATYPE_PROPERTIES_QUERY, null, ontModel, ontologyUri);
		for ( PropertyInfo propertyInfo : properties ) {
			propertyInfo.setDatatypeProperty(true);
		}
		boolean containDatatype = properties.size() > 0;

		// add object properties
		_getProperties(OBJECT_PROPERTIES_QUERY, properties, ontModel, ontologyUri);
		
		// classes:
		List<ClassInfo> classes = _getClasses(null, ontModel, ontologyUri);

		_setDomainClassesForProperties(classes, properties);
		
		BaseOntologyData baseOntologyData = new BaseOntologyData();
		baseOntologyData.setIndividuals(individuals);
		baseOntologyData.setProperties(properties);
		baseOntologyData.setClasses(classes);
		
		// now, determine the type of ontology data to be created:
		
		OntologyData ontologyData;
		
		if ( OntServiceUtil.isOntResolvableUri(ontologyUri) ) {
			//
			// apply the ad hoc rules to determine type of ontology only if 
			// the ontologyUri is resolvable by the Ont service.
			//
			
			
			List<Mapping> mappings = _getSkosRelations(null, ontModel);
			boolean containSkos = mappings.size() > 0 || _containsSkos(ontModel, individuals);
		
			// determine type of ontologyData to create
			
			// TODO NOTE: these are just heuristics to determine the ontologyData type:
			// Pending: use omv:useOntologyEngineeringTool for example.
			
			if ( containSkos ) {
				baseOntologyInfo.setType(OntologyType.MAPPING);
				ontologyData = _createMappingOntologyData(baseOntologyData, mappings, individuals);
			}
			else if ( classes.size() == 1 && individuals.size() > 0 && containDatatype ) {
				baseOntologyInfo.setType(OntologyType.VOCABULARY);
				ontologyData = _createVocabularyOntologyData(baseOntologyData);
			}
			else {
				baseOntologyInfo.setType(OntologyType.OTHER);
				ontologyData = _createOtherOntologyData(baseOntologyData);
			}
		}
		else {
			// otherwise (the URI is not Ont resolvable), always create the "other" type of ontology data
			baseOntologyInfo.setType(OntologyType.OTHER);
			ontologyData = _createOtherOntologyData(baseOntologyData);
		}
		
		ontologyData.setBaseOntologyData(baseOntologyData);
		baseOntologyInfo.setOntologyData(ontologyData);
		
		return baseOntologyInfo;
	}
	
	
	// TODO: the next search for SKOS relations is not complete; it's just an initial idea.
	private static boolean _containsSkos(OntModel ontModel, List<IndividualInfo> individuals) {
		// try looking into the individuals:
		for ( IndividualInfo individualInfo : individuals ) {
			List<PropValue> indivProps = individualInfo.getProps();
			for ( PropValue propValue: indivProps ) {
				if ( propValue.getPropName().matches(".*Match.*") ) {
					return true;
				}
			}
		}
		return false;
	}



	/**
	 * Adds the individuals defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private List<IndividualInfo> _getIndividuals(List<IndividualInfo> entities,
			OntModel ontModel, String ontologyUri) {
		
		if ( entities == null ) {
			entities = new ArrayList<IndividualInfo>();
		}
		
		Query query = QueryFactory.create(INDIVIDUALS_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			
			String entityUri = null;
			String classUri = null;
			
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());

				RDFNode rdfNode = sol.get(varName);
				
				if ( rdfNode.isAnon() ) {
					continue;
				}
				
				String varValue = String.valueOf(rdfNode);
				
				if ( varValue == null ) {
					continue;
				}
				
				if ( varName.equalsIgnoreCase("instance") ) {
					entityUri = varValue;
				}
				else if ( varName.equalsIgnoreCase("class") ) {
					classUri = varValue;
				}
				else {
					throw new AssertionError();
				}
			}
			
			if ( entityUri != null ) {
			
				IndividualInfo entityInfo = new IndividualInfo();
				entityInfo.setUri(entityUri);
				entityInfo.setClassUri(classUri);
				
				// is ontologyUri a prefix of entityUri?
				if ( entityUri.indexOf(ontologyUri) == 0 ) {
					String localName = entityUri.substring(ontologyUri.length());
					localName = localName.replaceAll("^/+", "");
					entityInfo.setLocalName(localName);
				}
				else {
					// use the given entityUri as the local name.
					// Note that the query is made against the ontology, so every entity
					// found there should be included.
					String localName = _getLocalName(entityUri);
					entityInfo.setLocalName(localName);
				}
				
				if ( entityInfo != null ) {
					_addProps(entityUri, entityInfo, ontModel);
					entities.add(entityInfo);
				}
			}
		}
		
		return entities;
	}
	
	/**
	 * Adds the properties defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static List<PropertyInfo> _getProperties(
			String propertiesQuery,
			List<PropertyInfo> entities,
			OntModel ontModel, String ontologyUri) {

		if ( entities == null ) {
			entities = new ArrayList<PropertyInfo>();
		}
		
		Query query = QueryFactory.create(propertiesQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			
			String entityUri = null;
			String domainUri = null;
			
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());

				RDFNode rdfNode = sol.get(varName);
				
				if ( rdfNode.isAnon() ) {
					continue;
				}
				
				String varValue = String.valueOf(rdfNode);
				
				if ( varValue == null ) {
					continue;
				}
				
				if ( varName.equalsIgnoreCase("prop") ) {
					entityUri = varValue;
				}
				else if ( varName.equalsIgnoreCase("domain") ) {
					domainUri = varValue;
				}
				else {
					throw new AssertionError();
				}
			}
			
			if ( entityUri == null ) {
				continue;
			}
				
			PropertyInfo entityInfo = new PropertyInfo();
			entityInfo.setUri(entityUri);
			entityInfo.setDomainUri(domainUri);


			// is ontologyUri a prefix of entityUri?
			if ( entityUri.indexOf(ontologyUri) == 0 ) {
				String localName = entityUri.substring(ontologyUri.length());
				localName = localName.replaceAll("^/+", "");
				entityInfo.setLocalName(localName);
			}
			else {
				// use the given entityUri as the local name.
				// Note that the query is made against the ontology, so every entity
				// found there should be included.
				String localName = _getLocalName(entityUri);
				entityInfo.setLocalName(localName);
			}

			if ( entityInfo != null ) {
				_addProps(entityUri, entityInfo, ontModel);
				entities.add(entityInfo);
			}

		}
		
		return entities;
	}


	/**
	 * Adds the classes defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static List<ClassInfo> _getClasses(List<ClassInfo> entities,
			OntModel ontModel, String ontologyUri) {

		if ( entities == null ) {
			entities = new ArrayList<ClassInfo>();
		}
		
		Query query = QueryFactory.create(CLASSES_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				
				RDFNode rdfNode = sol.get(varName);
				
				if ( rdfNode.isAnon() ) {
					continue;
				}
				
				String entityUri = String.valueOf(rdfNode);
				if ( entityUri == null ) {
					continue;
				}
				
				ClassInfo entityInfo = new ClassInfo();
				entityInfo.setUri(entityUri);
				
				// is ontologyUri a prefix of entityUri?
				if ( entityUri.indexOf(ontologyUri) == 0 ) {
					String localName = entityUri.substring(ontologyUri.length());
					localName = localName.replaceAll("^/+", "");
					entityInfo.setLocalName(localName);
				}
				else {
					// use the given entityUri as the local name.
					// Note that the query is made against the ontology, so every entity
					// found there should be included.
					String localName = _getLocalName(entityUri);
					entityInfo.setLocalName(localName);
				}
				
				if ( entityInfo != null ) {
					_addProps(entityUri, entityInfo, ontModel);
					entities.add(entityInfo);
				}

			}
		}
		
		return entities;
	}


	
	/**
	 * Adds PropValue's to the entityInfo
	 * @param entityUri
	 * @param entityInfo
	 * @param ontModel
	 */
	private static void _addProps(String entityUri, EntityInfo entityInfo, OntModel ontModel) {
		String queryStr = PROPS_QUERY_TEMPLATE.replaceAll("\\{E\\}", entityUri);
		
		// added the try-catch-warn below while further investigating  
		// http://code.google.com/p/mmisw/issues/detail?id=253
		// It happened that the CSV has a record spanning two rows; the second row started in the
		// middle of a comment, which was taken as the URI of the entity.
		Query query;
		try {
			query = QueryFactory.create(queryStr);
		}
		catch ( RuntimeException ex ) {
			log.warn("_addProps: entityUri=[" +entityUri+ "] queryStr=[" +queryStr+ "]", ex);
			throw ex;
		}
		
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			
			String propName = null, propUri = null;
			String valueName = null, valueUri = null;
			
			while ( varNames.hasNext() ) {
				String varName = varNames.next().toString();
				RDFNode rdfNode = sol.get(varName);
				
				if ( rdfNode.isAnon() ) {
					continue;
				}
				
				if ( varName.equals("prop") ) {
					if ( rdfNode.isResource() ) {
						Resource r = (Resource) rdfNode;
						propName = r.getLocalName();
						propUri = r.getURI();
					}
					else {
						propName = rdfNode.toString();
						
						// if propName looks like a URL, associate the link also:
						try {
							new URL(propName);
							propUri = propName;
						}
						catch (MalformedURLException ignore) {
						}
					}
				}
				else if ( varName.equals("value") ) {
					if ( rdfNode.isResource() ) {
						Resource r = (Resource) rdfNode;
						valueName = r.getLocalName();
						valueUri = r.getURI();
					}
					else {
						valueName = rdfNode.toString();
						// if valueName looks like a URL, associate the link also:
						try {
							new URL(valueName);
							valueUri = valueName;
						}
						catch (MalformedURLException ignore) {
						}
					}
				}
			}
			
			PropValue pv = new PropValue(propName, propUri, valueName, valueUri);
			entityInfo.getProps().add(pv);
		}
	}

	
	/**
	 * Determines if there are SKOS relations used (as predicates) in the model.
	 * @param ontModel
	 */
	private static List<Mapping> _getSkosRelations(List<Mapping> mappings, OntModel ontModel) {
		
		if ( mappings == null ) {
			mappings = new ArrayList<Mapping>();
		}
		
		Query query = QueryFactory.create(SKOS_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			
			QuerySolution sol = results.nextSolution();
			
			String left = null;
			String rel = null;
			String right = null;
			
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				RDFNode rdfNode = sol.get(varName);
				
				if ( rdfNode.isAnon() ) {
					continue;
				}
				
				String varValue = String.valueOf(rdfNode);
				if ( varValue == null ) {
					continue;
				}
				
				if ( "left".equals(varName) ) {
					left = varValue;
				}
				else if ( "rel".equals(varName) ) {
					rel = varValue;
				}
				else if ( "right".equals(varName) ) {
					right = varValue;
				}
			}
			
			if ( left != null && rel != null && right != null ) {
				mappings.add(new Mapping(left, rel, right));
			}
		}
		
		return mappings;
	}


}

