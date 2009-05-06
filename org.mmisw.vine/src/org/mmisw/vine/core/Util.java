package org.mmisw.vine.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;
import org.mmisw.vine.gwt.client.rpc.IndividualInfo;
import org.mmisw.vine.gwt.client.rpc.OntologyInfo;
import org.mmisw.vine.gwt.client.rpc.PropValue;

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

/**
 * A placeholder for preliminary functionality at the core.
 * 
 * @author Carlos Rueda
 */
public class Util {
	
	/** Query to obtain the individuals in a model */
	private static final String INDIVIDUALS_QUERY =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
		"SELECT ?instance " +
		"WHERE { ?class rdf:type owl:Class ." +
		"        ?instance rdf:type ?class . }"
	;
	
	/** Query to obtain the properties in a model */
	private static final String PROPERTIES_QUERY =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
		"SELECT ?prop " +
		"WHERE {       { ?prop rdf:type rdf:Property }" +
		        "UNION { ?prop rdf:type owl:DatatypeProperty  }" +
		        "UNION { ?prop rdf:type owl:ObjectProperty } }"
	;
	
	/** Query to obtain the classes in a model */
	private static final String CLASSES_QUERY =
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
		"SELECT ?class " +
		"WHERE { ?class rdf:type owl:Class . }"
	;
	
	/** Query template to obtain all properties associated with an entity */
	private static final String PROPS_QUERY_TEMPLATE =
		"SELECT ?prop ?value " +
		"WHERE { <{E}> ?prop ?value . }"
	;
	
	public static OntologyInfo getEntities(OntologyInfo ontologyInfo) {
		String ontologyUri = ontologyInfo.getUri();
		OntModel ontModel = loadModel(ontologyUri);

		List<EntityInfo> entities = new ArrayList<EntityInfo>();
		
		// individuals:
		addIndividuals(entities, ontModel, ontologyUri);

		// properties:
		addProperties(entities, ontModel, ontologyUri);

		// classes:
		addClasses(entities, ontModel, ontologyUri);

		ontologyInfo.setEntities(entities);
		return ontologyInfo;
	}
	
	
	/**
	 * Adds the individuals defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static void addIndividuals(List<EntityInfo> entities,
			OntModel ontModel, String ontologyUri) {
		
		Query query = QueryFactory.create(INDIVIDUALS_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is ontologyUri a prefix of entityUri?
				if ( entityUri != null && entityUri.indexOf(ontologyUri) == 0 ) {
					IndividualInfo entityInfo = new IndividualInfo();
					String localName = entityUri.substring(ontologyUri.length());
					entityInfo.setLocalName(localName);
					
					_addProps(entityUri, entityInfo, ontModel);
					
					entities.add(entityInfo);
				}
			}
		}
		
	}

	/**
	 * Adds the properties defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static void addProperties(List<EntityInfo> entities,
			OntModel ontModel, String ontologyUri) {

		Query query = QueryFactory.create(PROPERTIES_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is ontologyUri a prefix of entityUri?
				if ( entityUri != null && entityUri.indexOf(ontologyUri) == 0 ) {
					IndividualInfo entityInfo = new IndividualInfo();
					String localName = entityUri.substring(ontologyUri.length());
					entityInfo.setLocalName(localName);
					
					_addProps(entityUri, entityInfo, ontModel);
					
					entities.add(entityInfo);
				}
			}
		}
		
		
	}


	/**
	 * Adds the classes defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static void addClasses(List<EntityInfo> entities,
			OntModel ontModel, String ontologyUri) {

		Query query = QueryFactory.create(CLASSES_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is ontologyUri a prefix of entityUri?
				if ( entityUri !=null && entityUri.indexOf(ontologyUri) == 0 ) {
					IndividualInfo entityInfo = new IndividualInfo();
					String localName = entityUri.substring(ontologyUri.length());
					entityInfo.setLocalName(localName);
					
					_addProps(entityUri, entityInfo, ontModel);
					
					entities.add(entityInfo);
				}
			}
		}
		
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

	
	

	/** see JenaUtil2 */
	private static final String FRAG_SEPARATOR = "/" ;

	private static String getURIForBase(String uri) {
		return uri.replaceAll(FRAG_SEPARATOR + "+$", "");
	}
	
	private static OntModel loadModel(String uriModel) {
		OntModel model = createDefaultOntModel();
		uriModel = getURIForBase(uriModel);
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

