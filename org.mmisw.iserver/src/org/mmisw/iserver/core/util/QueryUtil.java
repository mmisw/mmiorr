package org.mmisw.iserver.core.util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ClassInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.IndividualInfo;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.OtherOntologyData;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.iserver.gwt.client.rpc.PropertyInfo;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData.ClassData;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A placeholder for preliminary functionality at the core.
 * 
 * @author Carlos Rueda
 */
public class QueryUtil {
	
	/** Query to obtain the individuals in a model */
	private static final String QUERY_PREFIXES =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#> "
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
	
	
	/**
	 * Gets the list of entities associated with the given ontology. 
	 * @param ontologyUri URI of the desired ontology.
	 * @return list of entities
	 * @throws Exception 
	 */
	public static List<EntityInfo> getEntities(String ontologyUri, OntModel ontModel) throws Exception {
		
		if ( ontModel == null ) {
			ontModel = loadModel(ontologyUri);
		}
		
		List<EntityInfo> entities = new ArrayList<EntityInfo>();
		
		// individuals:
		entities.addAll(_getIndividuals(null, ontModel, ontologyUri));

		// datatype properties:
		entities.addAll(_getProperties(DATATYPE_PROPERTIES_QUERY, null, ontModel, ontologyUri));

		// object properties:
		entities.addAll(_getProperties(OBJECT_PROPERTIES_QUERY, null, ontModel, ontologyUri));

		// classes:
		entities.addAll(_getClasses(null, ontModel, ontologyUri));
		
		return entities;
	}
	
	
	
	/**
	 * Populates the list of entities associated with the given ontology. 
	 * @param baseOntologyInfo
	 * @return the given argument
	 * @throws Exception 
	 */
	public static BaseOntologyInfo getEntities(BaseOntologyInfo baseOntologyInfo, OntModel ontModel) throws Exception {
		String ontologyUri = baseOntologyInfo.getUri();

		
		if ( ontModel == null ) {
			ontModel = loadModel(ontologyUri);
		}
		
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
		
		// TODO: the next search for SKOS relations is not complete; it's just an initial idea.
		boolean containSkos = false;
		for ( IndividualInfo individualInfo : individuals ) {
			List<PropValue> indivProps = individualInfo.getProps();
			for ( PropValue propValue: indivProps ) {
				if ( propValue.getPropName().matches(".*Match.*") ) {
					containSkos = true;
				}
			}
		}
		
		OntologyData ontologyData;
		
		// determine type of ontologyData to create
		
		// TODO NOTE: these are just heuristics to determine the ontologyData type:
		// Pending: use omv:useOntologyEngineeringTool for example.
		
		if ( classes.size() == 1 && individuals.size() > 0 && containDatatype ) {
			baseOntologyInfo.setType("vocabulary");
			ontologyData = _createVocabularyOntologyData(baseOntologyData);
		}
		else if ( containSkos ) {
			baseOntologyInfo.setType("mapping");
			ontologyData = _createMappingOntologyData(baseOntologyData);
		}
		else {
			baseOntologyInfo.setType("other");
			ontologyData = _createOtherOntologyData(baseOntologyData);
		}
		
		ontologyData.setBaseOntologyData(baseOntologyData);
		baseOntologyInfo.setOntologyData(ontologyData);
		
		return baseOntologyInfo;
	}
	
	
	/**
	 * It assigns the classInfo corresponding to the domain for each property.
	 * @param classes      List of known classes
	 * @param properties   Properties to be updated
	 */
	private static void _setDomainClassesForProperties(List<ClassInfo> classes,
			List<PropertyInfo> properties) {

		for ( PropertyInfo propertyInfo : properties ) {
			String domainClassUri = propertyInfo.getDomainUri();
			
			if ( domainClassUri == null ) {
				// I'm checking for null here to avoid a NPE with http://mmisw.org/ont/univmemphis/sensor
				// TODO Check why the domain uri has not been assigned for the propertyInfo
				continue;
			}
			
			// search corresponding classInfo in classes:
			ClassInfo domainClassInfo = null;
			
			for ( ClassInfo classInfo : classes ) {
				if ( domainClassUri.equals(classInfo.getUri()) ) {
					domainClassInfo = classInfo;
					break;
				}
			}
			
			if ( domainClassInfo != null ) {
				propertyInfo.setDomainClassInfo(domainClassInfo);
			}
		}
	}



	private static OntologyData _createVocabularyOntologyData(BaseOntologyData baseData) {
		VocabularyOntologyData ontologyData = new VocabularyOntologyData();
		
		ontologyData.setBaseOntologyData(baseData);
		
		
		Map<String, ClassData> classMap = new HashMap<String, ClassData>();
		
		List<PropertyInfo> properties = baseData.getProperties();		
		for ( PropertyInfo entity : properties ) {
			if ( ! entity.isDatatypeProperty() ) {
				continue;
			}

			String classUri = entity.getDomainUri();
			if ( classUri == null ) {
				continue;
			}
			
			ClassData classData = classMap.get(classUri);
			if ( classData == null ) {
				classData = new ClassData();
				classMap.put(classUri, classData);
				classData.setClassUri(classUri);
				classData.setClassInfo(entity.getDomainClassInfo());
				classData.setDatatypeProperties(new ArrayList<String>());
			}
			
			classData.getDatatypeProperties().add(entity.getLocalName());
		}
		
		// add the found classes and add corresponding individuals:

		List<ClassData> classes = new ArrayList<ClassData>();
		ontologyData.setClasses(classes);
		
		for ( String classUri : classMap.keySet() ) {
			ClassData classData = classMap.get(classUri);
			classes.add(classData);
			
			// add individuals whose type is classUri
			
			List<IndividualInfo> individuals = new ArrayList<IndividualInfo>();
			classData.setIndividuals(individuals);
			
			List<IndividualInfo> individualInfos = baseData.getIndividuals();
			for ( IndividualInfo individualInfo : individualInfos ) {
				String individualClass = individualInfo.getClassUri();
				if ( classUri.equals(individualClass) ) {
					individuals.add(individualInfo);
				}
			}
			
			
			// the following is an attempt to guess the column (ie., the datatype property that
			// was used as the 'key', so as to put that column as the first.
			// The strategy is to see what datatype property corresponds to rdfs:label
			
			// diffFlags[col] will be true if the corresponding column does not seem to coincide
			// with value of rdfs:label
			boolean[] diffFlags = null;    
			
			// but we do the check for a maximum of individuals:
			final int maxIndivs = 20;
			int indivNum = 0;
			for ( IndividualInfo individualInfo : individuals ) {

				// will contain the value of RDFS.label.getURI() if any:
				String rdfsLabelValue = null;
				
				Map<String, String> vals = new HashMap<String, String>();
				List<PropValue> props = individualInfo.getProps();
				for ( PropValue pv : props ) {
					
					if ( RDFS.label.getURI().equals(pv.getPropUri()) ) {
						rdfsLabelValue = pv.getValueName();
					}
					
					vals.put(pv.getPropName(), pv.getValueName());
				}
				
				if ( rdfsLabelValue == null ) {
					// do not continue making the check.
					break;
				}
				
				// let's ignore case, and replace spaces with underscores for purposes of
				// the comparison below
				rdfsLabelValue = rdfsLabelValue.toLowerCase().replace(' ', '_');
				
				List<String> datatypeProperties = classData.getDatatypeProperties();
				int numCols = datatypeProperties.size();
				if ( diffFlags == null ) {
					diffFlags = new boolean[numCols];
				}
				for ( int i = 0; i < numCols; i++ ) {
					String colValue = vals.get(datatypeProperties.get(i));
					if ( colValue != null ) {
						colValue = colValue.toLowerCase().replace(' ', '_');
					}
					diffFlags[i] = diffFlags[i] || !rdfsLabelValue.equals(colValue);
				}
				
				if ( ++indivNum >= maxIndivs ) {
					break;
				}
			}
			
			if ( diffFlags != null ) {
				// now, pick first column whose values coincided with rdfs:label:
				int foundColumn = -1;
				for ( int i = 0; i < diffFlags.length; i++ ) {
					if ( !diffFlags[i] ) {
						foundColumn = i;
						break;
					}
				}

				if ( foundColumn > 0 ) {
					// if we found the column, and that is not already the first, then
					// make it the first:
					List<String> datatypeProperties = classData.getDatatypeProperties();
					String keyColumnName = datatypeProperties.remove(foundColumn);
					datatypeProperties.add(0, keyColumnName);
				}
			}
		}
		
		
		return ontologyData;
	}



	private static OntologyData _createMappingOntologyData(BaseOntologyData baseOntologyData) {
		MappingOntologyData ontologyData = new MappingOntologyData();
		ontologyData.setBaseOntologyData(baseOntologyData);
		// TODO 
		return ontologyData;
	}

	private static OntologyData _createOtherOntologyData(BaseOntologyData baseOntologyData) {
		OtherOntologyData ontologyData = new OtherOntologyData();
		ontologyData.setBaseOntologyData(baseOntologyData);
		// TODO 
		return ontologyData;
	}



	/**
	 * Adds the individuals defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static List<IndividualInfo> _getIndividuals(List<IndividualInfo> entities,
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
					String localName = entityUri;
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
				String localName = entityUri;
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
					String localName = entityUri;
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
		Query query = QueryFactory.create(queryStr);
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
	 * Creates a default model, calls model.read(is, base) and returns the resulting model.
	 */
	public static OntModel loadModel(InputStream is, String base) {
		OntModel model = createDefaultOntModel();
		model.read(is, base);
		return model;
	}
	

	
	/**
	 * Loads a model first verifying the source text is in UTF-8.
	 * @param uriModel
	 * @return
	 * @throws Exception
	 */
	public static OntModel loadModel(String uriModel) throws Exception {
		
		URL url = new URL(uriModel);
		InputStream is = url.openStream();
		Utf8Util.verifyUtf8(is);
		
		OntModel model = createDefaultOntModel();
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		model.read(uriModel);
		return model;
	}

	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}
}

