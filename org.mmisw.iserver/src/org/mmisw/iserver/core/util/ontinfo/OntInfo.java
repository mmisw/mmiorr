package org.mmisw.iserver.core.util.ontinfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.core.util.Skos;
import org.mmisw.iserver.core.util.Skos2;
import org.mmisw.iserver.core.util.ontype.OntTypeUtil;
import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.ClassInfo;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.IndividualInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyType;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.iserver.gwt.client.rpc.PropertyInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.ont.vocabulary.Vine;
import org.mmisw.ont.vocabulary.Vine20071128;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Based on SPARQL queries.
 * 
 * @author Carlos Rueda
 */
class OntInfo extends BaseOntInfo {
	
//	private static final Log log = LogFactory.getLog(OntQuery.class);


	public BaseOntologyInfo getEntities(BaseOntologyInfo baseOntologyInfo, OntModel ontModel
	) throws Exception {
		
		
		String ontologyUri = baseOntologyInfo.getUri();

		// individuals:
		List<IndividualInfo> individuals = _getIndividuals(null, ontModel, ontologyUri);

		
		// datatype properties:
		List<PropertyInfo> properties = _getDatatypeProperties(null, ontModel, ontologyUri);
		for ( PropertyInfo propertyInfo : properties ) {
			propertyInfo.setDatatypeProperty(true);
		}

		// add object properties
		_getObjectProperties(properties, ontModel, ontologyUri);

		
		// classes:
		List<ClassInfo> classes = _getClasses(null, ontModel, ontologyUri);

		_setDomainClassesForProperties(classes, properties);
		
		BaseOntologyData baseOntologyData = new BaseOntologyData();
		baseOntologyData.setIndividuals(individuals);
		baseOntologyData.setProperties(properties);
		baseOntologyData.setClasses(classes);
		
		// now, determine the type of ontology data to be created:

		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri);
		
		baseOntologyInfo.setType(ontype);

		OntologyData ontologyData;
		
		switch ( ontype ) {
			case MAPPING:
				List<Mapping> mappings = _getMappings(null, ontModel);
				ontologyData = _createMappingOntologyData(baseOntologyData, mappings, individuals);
				break;
				
			case VOCABULARY:
				ontologyData = _createVocabularyOntologyData(baseOntologyData);
				break;
				
			case OTHER:
				ontologyData = _createOtherOntologyData(baseOntologyData);
				break;
				
			default:
				throw new IllegalStateException();
		}
		
		ontologyData.setBaseOntologyData(baseOntologyData);
		baseOntologyInfo.setOntologyData(ontologyData);
		
		return baseOntologyInfo;
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
		
		for ( Individual ind : ontModel.listIndividuals().toList() ) {
			
			if ( ind.isAnon() ) {
				continue;
			}
			String entityUri = ind.getURI();
			if ( entityUri == null ) {
				continue;
			}
			
			OntClass ontClass = ind.getOntClass(true);
			if ( ontClass == null || ! ontClass.isURIResource() ) {
				continue;
			}
			String classUri = ontClass.getURI();

				
			IndividualInfo entityInfo = new IndividualInfo();
			entityInfo.setUri(entityUri);
			entityInfo.setClassUri(classUri);

			String localName = _getLocalName(entityUri, ontologyUri);
			entityInfo.setLocalName(localName);

			_addProps(entityUri, entityInfo, ontModel);
			entities.add(entityInfo);
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
		
		Resource s = ResourceFactory.createResource(entityUri);
		StmtIterator stmts = ontModel.listStatements(s, null, (RDFNode) null);
		for ( Statement stmt : stmts.toList() ) {
		
			RDFNode rdfNode = stmt.getObject();
			if ( rdfNode.isAnon() ) {
				continue;
			}
			Property prop = stmt.getPredicate();
			
			// ...
			
			String propName = prop.getLocalName();
			String propUri = prop.getURI();
			String valueName = null;
			String valueUri = null;
			
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

			PropValue pv = new PropValue(propName, propUri, valueName, valueUri);
			entityInfo.getProps().add(pv);

		}
		
	}

	
	/**
	 * Adds the datatype properties defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static List<PropertyInfo> _getDatatypeProperties(
			List<PropertyInfo> entities,
			OntModel ontModel, String ontologyUri
	) {

		if ( entities == null ) {
			entities = new ArrayList<PropertyInfo>();
		}
		
		ExtendedIterator<DatatypeProperty> props = ontModel.listDatatypeProperties();
		while ( props.hasNext() ) {
			DatatypeProperty prop = props.next();
			PropertyInfo entityInfo = _createPropertyInfo(prop, ontologyUri, ontModel);
			if ( entityInfo != null ) {
				entities.add(entityInfo);
			}
		}
		
		return entities;
	}

	/**
	 * Adds the datatype properties defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private static List<PropertyInfo> _getObjectProperties(
			List<PropertyInfo> entities,
			OntModel ontModel, String ontologyUri
	) {
		
		if ( entities == null ) {
			entities = new ArrayList<PropertyInfo>();
		}
		
		ExtendedIterator<ObjectProperty> props = ontModel.listObjectProperties();
		while ( props.hasNext() ) {
			ObjectProperty prop = props.next();
			PropertyInfo entityInfo = _createPropertyInfo(prop, ontologyUri, ontModel);
			if ( entityInfo != null ) {
				entities.add(entityInfo);
			}
		}
		
		return entities;
	}
	
	private static PropertyInfo _createPropertyInfo(OntProperty prop, String ontologyUri, OntModel ontModel) {
		if ( prop.isAnon() ) {
			return null;
		}
		String entityUri = prop.getURI();
		
		OntResource domain = prop.getDomain();
		if ( domain == null ) {
			// TODO some appropriate inferred domain class could be assigned.
			return null;
		}
		if ( ! domain.isURIResource() ) {
			return null;
		}
		String domainUri = domain.getURI();
		
		PropertyInfo entityInfo = new PropertyInfo();
		entityInfo.setUri(entityUri);
		entityInfo.setDomainUri(domainUri);

		String localName = _getLocalName(entityUri, ontologyUri);
		entityInfo.setLocalName(localName);

		_addProps(entityUri, entityInfo, ontModel);
		return entityInfo;
	}

	
	/**
	 * Adds the classes defined in the model to the given list.
	 * @param entities
	 * @param ontModel
	 * @param ontologyUri
	 */
	private List<ClassInfo> _getClasses(List<ClassInfo> entities,
			OntModel ontModel, String ontologyUri) {
		
		if ( entities == null ) {
			entities = new ArrayList<ClassInfo>();
		}
		
		for ( OntClass ind : ontModel.listNamedClasses().toList() ) {
			
			if ( ind.isAnon() ) {
				continue;
			}
			String classUri = ind.getURI();
			if ( classUri == null ) {
				continue;
			}
				
			ClassInfo entityInfo = new ClassInfo();
			entityInfo.setUri(classUri);

			String localName = _getLocalName(classUri, ontologyUri);
			entityInfo.setLocalName(localName);

			_addProps(classUri, entityInfo, ontModel);
			entities.add(entityInfo);
		}
		
		return entities;
	}


	/**
	 * Gets the list of Vine mappings in the model.
	 * @param ontModel
	 */
	private static List<Mapping> _getMappings(List<Mapping> mappings, OntModel ontModel) {
		
		if ( mappings == null ) {
			mappings = new ArrayList<Mapping>();
		}
		
		// all the mapping statements in the model. 
		// below we check for both reified and non-reified mapping-related statements.
		StmtIterator vineStmts = ontModel.listStatements();
		
		// collect all found reified Vine statements in this map:
		Map<StmtKey,Set<Resource>> stmtRsrsMap = new HashMap<StmtKey,Set<Resource>>();
		
		while ( vineStmts.hasNext() ) {
			Statement stmt = vineStmts.nextStatement();
			Property prd = stmt.getPredicate();
			
			// Vine "subject" arbitrarily chosen to check whether we're seeing a Vine statement:
			if ( _isVineSubject(prd) ) {
				//
				// Yes, add the reified statement to our map:
				//
				Resource stmtRsr = stmt.getSubject();
				StmtKey stmtKey = _getStmtKey(stmtRsr);
				_getSetForStmtKey(stmtRsrsMap, stmtKey).add(stmtRsr);
			}
			
			// else: to capture cases not using reification, see if the predicate is
			// one of the SKOS xxxMatch properties. Try the two SKOS namespaces:
			else if ( prd.getNameSpace().equals(Skos.NS)  ||  prd.getNameSpace().equals(Skos2.NS)) {
				if ( prd.getLocalName().endsWith("Match") ) {
					//
					// Yes, it's a simple statement with a xxxxMatch predicate:
					//
					StmtKey stmtKey = _getStmtKey(stmt);
					if ( stmtKey != null ) {
						// note, we add the key even if creating an associated empy set:
						_getSetForStmtKey(stmtRsrsMap, stmtKey);
					}
				}
			}
		}
		
		// now process all the collected StmtKeys:
		for ( StmtKey stmtKey : stmtRsrsMap.keySet() ) {
			
			// create and add the mapping to the list:
			Mapping mapping = new Mapping(stmtKey.getSubject(), stmtKey.getPredicate(), stmtKey.getObject());
			mappings.add(mapping);
			
			// get the associated resources (reified statements) if any, to collect the metadata:
			Map<String,String> md = new HashMap<String,String>();
			for ( Resource stmtRsr : stmtRsrsMap.get(stmtKey) ) {
				_getMetadataForReified(stmtRsr, md);
				//
				// TODO Note that later elements in this loop will overwrite common metadata properties.
				// To be more concrete, if the (s,p,o) mapping happens to appear in two or more
				// reified statements, each with its associated metadata, we are not *aggregating*
				// such metadata (for example, rdfs:comment properties may be multiple), but
				// *overwriting*, EVEN in the same reified statement, actually.
			}
			
			// assign the metadata map only if non-empty:
			if ( md.size() > 0 ) {
				mapping.setMetadata(md);
			}
		}
		
		return mappings;
	}

	
	/** 
	 * Gets the set in stmtRsrsMap for the given stmtKey.
	 * If the key has not been inserted in the map, it is inserted
	 * with a new empty set associated.
	 * 
	 * @param stmtRsrsMap
	 * @param stmtKey
	 */
	private static Set<Resource> _getSetForStmtKey(
			Map<StmtKey, Set<Resource>> stmtRsrsMap, StmtKey stmtKey
	) {
		Set<Resource> stmtRsrs = stmtRsrsMap.get(stmtKey);
		if ( stmtRsrs == null ) {
			stmtRsrsMap.put(stmtKey, stmtRsrs = new HashSet<Resource>());
		}
		return stmtRsrs;
	}

	/** 
	 * Gets the key (s,p,o) for a simple (non-reified) statement.
	 * Returns null if not all components are defined.
	 */
	private static StmtKey _getStmtKey(Statement stmt) {
		Resource sjt = stmt.getSubject();
		Property prd = stmt.getPredicate();
		
		String left = sjt.isAnon() ? null : sjt.getURI();
		String rel = prd.isAnon() ? null : prd.getURI(); 
		String right = _getValueAsString(stmt.getObject());
		
		if ( left != null && rel != null && right != null ) {
			return new StmtKey(left, rel, right);
		}
		return null;
	}

	
	
	/** 
	 * Gets the key (s,p,o) for a given (reified) statement.
	 * Returns null if not all components are defined.
	 */
	private static StmtKey _getStmtKey(Resource stmtRsr) {
		String left = null;
		String rel = null;
		String right = null;
		
		for ( StmtIterator myProps = stmtRsr.listProperties(); myProps.hasNext(); ) {
			Statement myStmt = myProps.nextStatement();
			Property myProp = myStmt.getPredicate();
			
			if ( _isVineSubject(myProp) ) {
				left = _getValueAsString(myStmt.getObject());
			}
			else if ( _isVinePredicate(myProp) ) {
				rel = _getValueAsString(myStmt.getObject());
			}
			else if ( _isVineObject(myProp) ) {
				right = _getValueAsString(myStmt.getObject());
			}
			
			if ( left != null && rel != null && right != null ) {
				return new StmtKey(left, rel, right);
			}
		}
		
		return null;
	}
	
	
	
	
	private static boolean _isVineSubject(Property myProp) {
		return Vine.subject.equals(myProp) 
		|| Vine20071128.subject.equals(myProp)
		|| RDF.subject.equals(myProp)
		;
	}
	private static boolean _isVinePredicate(Property myProp) {
		return Vine.predicate.equals(myProp) 
		|| Vine20071128.predicate.equals(myProp)
		|| RDF.predicate.equals(myProp)
		;
	}
	private static boolean _isVineObject(Property myProp) {
		return Vine.object.equals(myProp) 
		|| Vine20071128.object.equals(myProp)
		|| RDF.object.equals(myProp)
		;
	}

	/** 
	 * Creates a mapping from the given (reified) statement.
	 * Metadata is assigned if any. 
	 */
	private static void _getMetadataForReified(Resource stmtRsr, Map<String,String> md) {
		
		// traverse all the properties associated with stmtRsr
		
		for ( StmtIterator myProps = stmtRsr.listProperties(); myProps.hasNext(); ) {
			Statement myStmt = myProps.nextStatement();
			Property myProp = myStmt.getPredicate();
			
			if ( RDFS.comment.equals(myProp) 
			||   Vine.confidence.equals(myProp)  ||  Vine20071128.confidence.equals(myProp)  
			) {
				// OK; these are the ONLY expected metadata properties per mapping (as of 2010-08-23)
				String propUri = myProp.getURI();
				String propValue = _getValueAsString(myStmt.getObject());
				md.put(propUri, propValue);
			}
			
			// Else: just IGNORE.
			
			
/////////////////////////////////////////////////////////////////////////////////////////////////			
//			// Note that the properties below are possible here, but we can just ignore them
//			// for purposes of collecting the metadata:
//			else if ( RDF.type.equals(myProp) ) {
//				// The value would be vine:Statement, something that we already know.
//			}
//			else if ( Vine.subject.equals(myProp) || Vine20071128.subject.equals(myProp)
//			||   Vine.predicate.equals(myProp) || Vine20071128.predicate.equals(myProp)
//			||   Vine.object.equals(myProp) || Vine20071128.object.equals(myProp) 
//			) {
//				// ok, the corresponding triple element has already 
//				// been obtained for the StmtKey elsewhere
//			}
//			else {
//				// other properties?? but this case should NOT not happen.
//				// NOTE: this of course assumes the following setting: Vine-created ontologies are
//				// to be handled only by Vine for purposes of creating new versions. 
//			}
/////////////////////////////////////////////////////////////////////////////////////////////////			
			
		}
	}

	private static String _getValueAsString(RDFNode node) {
		if (node instanceof Literal) {
			Literal lit = (Literal) node;
			return lit.getLexicalForm();
		}
		else {
			return ((Resource) node).getURI();
		}
	}


}