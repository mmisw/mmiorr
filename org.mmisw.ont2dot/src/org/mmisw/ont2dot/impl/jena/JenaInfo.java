package org.mmisw.ont2dot.impl.jena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Collects info that will be used to generate the dot output.
 * Note: Some requests here to the Jena library may seem too low-level, but for
 * some reason some of the high-level calls I've tried don't return anything.
 * TODO look into this when time permits.
 * 
 * @author Carlos Rueda
 */
class JenaInfo {
	
	/** Missing resource in {@link OWL} */
    private  static final Resource OWL_NAMED_INDIVIDUAL = ResourceFactory.createProperty(OWL.NS, "NamedIndividual");

    private  static final Property OMV_VERSION = ResourceFactory.createProperty("http://omv.ontoware.org/2005/05/ontology#version");

	private static final Set<Resource> EMPTY_RESOURCE_SET = Collections.emptySet();

	private static final boolean PUT_ANY_FOR_MISSING_RANGES = true;
    
	private final Map<String, Resource> _classes = new HashMap<String, Resource>();

	// classURI -> direct super classes
	private final Map<String, Set<Resource>> _superClasses = new HashMap<String, Set<Resource>>();
	
	// classURI -> direct sub classes
	private final Map<String, Set<Resource>> _subClasses = new HashMap<String, Set<Resource>>();
	
	private final Map<Resource, Set<Resource>> _instances = new HashMap<Resource, Set<Resource>>();

	private final Set<Resource> _ontologies = new HashSet<Resource>();
	
	// clazz -> properties
	private final Map<Resource, Set<Resource>> _clazzProps = new HashMap<Resource, Set<Resource>>();
	
	// property -> domains
	private final Map<Resource, Set<Resource>> _propDomains = new HashMap<Resource, Set<Resource>>();
	// property -> ranges
	private final Map<Resource, Set<Resource>> _propRanges = new HashMap<Resource, Set<Resource>>();

	private final Map<Resource, Set<Statement>> _dataTypePropertyInstantiations = new HashMap<Resource, Set<Statement>>();

	
	private final Map<Resource, String> _rdfsLabels = new HashMap<Resource, String>();

	private final Map<String, DataRange> _dataRanges = new LinkedHashMap<String, DataRange>();
	private final Map<String, String> _dataRangeNames = new HashMap<String, String>();

	
	private final Map<String, Restriction> _restrictions = new LinkedHashMap<String, Restriction>();
	
	
	private final Map<String, UnionClass> _unionClasses = new LinkedHashMap<String, UnionClass>();
	
	
	private Set<Statement> _stmts = new HashSet<Statement>();
	
	
	//use label instead of local name?
	private boolean _useLabel = true;

	
	private String ontologyVersionInfo = null;

	
	JenaInfo(OntModel ontModel) {
		JenaInfo _info = this;
		
		_prepareInfoAboutOntology(ontModel);
		
		ExtendedIterator<UnionClass> unionClasses = ontModel.listUnionClasses();
		while ( unionClasses.hasNext() ) {
			UnionClass uclass = (UnionClass) unionClasses.next();
			String id = uclass.isAnon() ? uclass.getId().getLabelString() : uclass.getURI();
			_unionClasses.put(id , uclass);
//			System.err.println("XXXXXX UnionClass " +id+ " -> " +uclass);
		}
		
		StmtIterator iter = ontModel.listStatements();
		while ( iter.hasNext() ) {
			final Statement stmt = iter.nextStatement();
			
			final Resource sbj = stmt.getSubject();
			final Property prd = stmt.getPredicate();
			final RDFNode obj = stmt.getObject();
			
			if ( sbj.isAnon() ) {
				continue;
			}
			
			if ( prd.getNameSpace().equals(RDFS.getURI()) ) {
//				System.out.println(" ///////**** " +prd);

				if ( RDFS.label.equals(prd) ) {
					Literal objLit = (Literal) obj;
					_rdfsLabels.put(sbj, objLit.getLexicalForm());
					continue;
				}

				if ( RDFS.subPropertyOf.equals(prd)
				||   RDFS.seeAlso.equals(prd) 
				||   RDFS.comment.equals(prd) 
				) {
					continue;
				}
			}

			if ( OWL.inverseOf.equals(prd) ) {
				continue;
			}
			
			
			if ( RDFS.subClassOf.equals(prd)  ) {
				Resource objRsr = (Resource) obj;
				_info._putSubAndSuperClazz(sbj, objRsr);
			}
			
			else if ( RDF.type.equals(prd) ) {
				Resource objRsr = (Resource) obj;
				
				if ( OWL.Ontology.equals(objRsr) ) {
					_info._addOntology(sbj);
				}
				
				// TODO remove this temporary hard-coded filter
				if ( OWL.Ontology.equals(objRsr) 
				||   OWL.ObjectProperty.equals(objRsr)
				||   OWL.DatatypeProperty.equals(objRsr)
				||   OWL.SymmetricProperty.equals(objRsr)
				||   OWL.TransitiveProperty.equals(objRsr)
				||   OWL.FunctionalProperty.equals(objRsr)
				||   OWL.inverseOf.equals(objRsr)
				||   OWL.Class.equals(objRsr)
				||   OWL.AnnotationProperty.equals(objRsr)
				||   OWL_NAMED_INDIVIDUAL.equals(objRsr)
				) {
					continue;
				}
				
				_info._putInstance(sbj, objRsr);
			}
			
			else {
				
				if ( RDFS.domain.equals(prd) ) {
					Resource objRsr = (Resource) obj;
					_putDomain(sbj, objRsr);
				}
				else if ( RDFS.range.equals(prd) ) {
					Resource objRsr = (Resource) obj;
					_putRange(sbj, objRsr);
				}
					
				
				// other kind of relationship.
				
				if ( obj.isLiteral() ) {
					_putDataTypePropertyInstantiation(sbj, stmt);
				}
				else {
					// uncategorized statement
					_info._addStatement(stmt);
				}
			}
		}
		
		if ( PUT_ANY_FOR_MISSING_RANGES ) {
			// put RDFS.Resource as range for all properties with given domain
			// but missing range:
			for ( Resource prop : _propDomains.keySet() ) {
				if ( ! _propRanges.containsKey(prop) ) {
					_putRange(prop , RDFS.Resource);
				}
			}
		}
		
		
		int nextDataRangeIndex = 1;
		ExtendedIterator<?> dataRanges = ontModel.listDataRanges();
		while ( dataRanges.hasNext() ) {
			DataRange dataRange = (DataRange) dataRanges.next();
			String id = dataRange.isAnon() ? dataRange.getId().getLabelString() : dataRange.getURI();
			_dataRanges.put(id , dataRange);
			String dataRangeName = "DR_" + String.valueOf(nextDataRangeIndex++);
			_dataRangeNames.put(id, dataRangeName );
		}
		

		
		ExtendedIterator<?> restrs = ontModel.listRestrictions();
		while ( restrs.hasNext() ) {
			Restriction restr = (Restriction) restrs.next();
			String id = restr.isAnon() ? restr.getId().getLabelString() : restr.getURI();
			_restrictions.put(id , restr);
//			System.err.println("XXXXXX Restriction " +id+ " -> " +restr);
		}
	}
	
	private void _prepareInfoAboutOntology(OntModel ontModel) {
		
		ontologyVersionInfo = null;
		
		ExtendedIterator<Ontology> onts = ontModel.listOntologies();
		if ( onts == null || ! onts.hasNext() ) {
			return;
		}

		List<Ontology> list = new ArrayList<Ontology>();
		while ( onts.hasNext() ) {
			Ontology ontology = onts.next();
			list.add(ontology);
		}
		
		if ( list.size() == 0 ) {
			return;
		}
		
		boolean includeOntUri = list.size() > 1 ;
		StringBuffer sb = new StringBuffer();
		String newLine = "";
		for ( Ontology ontology : list ) {
			String ontUri = ontology.getURI();
			System.err.println("_prepareInfoAboutOntology: ontUri: " +ontUri);
			String versionFrom = null;
			String versionValue = null;
			RDFNode node = ontology.getPropertyValue(OMV_VERSION);
			if ( node != null ) {
				versionFrom = "(from omv:version)";
				versionValue = node.toString();
			}
			else {
				versionValue = ontology.getVersionInfo();
				if ( versionValue != null ) {
					versionFrom = "(from owl:versionInfo)";
				}
			}
			if ( versionValue == null ) {
				versionValue = "(info not found)";
				versionFrom = "";
			}
			if ( includeOntUri ) {
				sb.append(ontUri+ " ");
			}
			sb.append("Version: " +versionValue + " " + versionFrom + newLine);
			newLine = "\n";
		}
		
		ontologyVersionInfo = sb.toString();
	}

	
	/** Info about the version of the ontology(ies), maybe multiple \n-separated lines */
	public String getOntologyVersionInfo() {
		return ontologyVersionInfo;
	}

	public Map<String, DataRange> getDataRanges() {
		return _dataRanges;
	}
	
	/**
	 * Sets whether the rdfs:label should be used to label property edges instead of the local name
	 * of the property.
	 * @param useLabel
	 */
	public void setUseLabel(boolean useLabel) {
		this._useLabel = useLabel;
	}



	private void _putDataTypePropertyInstantiation(Resource sbj, Statement stmt) {
		Set<Statement> stmts = _dataTypePropertyInstantiations.get(sbj);
		if ( stmts == null ) {
			stmts = new HashSet<Statement>();
			_dataTypePropertyInstantiations.put(sbj, stmts);
		}
		stmts.add(stmt);
	}
	
	public Set<Statement> getDataTypePropertyInstantiations(Resource sbj) {
		return _dataTypePropertyInstantiations.get(sbj);
	}

	
	private void _putClazz(Resource clazz) {
		if ( ! _classes.containsKey(clazz.getURI()) ) {
			_classes.put(clazz.getURI(), clazz);
		}
	}

	private void _putSubAndSuperClazz(Resource subClazz, Resource superClazz) {
		_putSubClazz(subClazz, superClazz);
		_putSuperClazz(subClazz, superClazz);
		
		_putClazz(subClazz);
		_putClazz(superClazz);
	}

	private void _putSubClazz(Resource subClazz, Resource superClazz) {
		Set<Resource> subs = _subClasses.get(superClazz.getURI());
		if ( subs == null ) {
			subs = new HashSet<Resource>();
			_subClasses.put(superClazz.getURI(), subs);
		}
		subs.add(subClazz);
	}
	
	private void _putSuperClazz(Resource subClazz, Resource superClazz) {
		Set<Resource> supers = _superClasses.get(subClazz.getURI());
		if ( supers == null ) {
			supers = new HashSet<Resource>();
			_superClasses.put(subClazz.getURI(), supers);
		}
		supers.add(superClazz);
	}
	
	private void _putInstance(Resource instance, Resource clazz) {
		Set<Resource> clazzes = _instances.get(instance);
		if ( clazzes == null ) {
			clazzes = new HashSet<Resource>();
			_instances.put(instance, clazzes);
			clazzes.add(clazz);
		}
		_putClazz(clazz);
	}

	
	private void _putClazzProperty(Resource clazz, Resource prop) {
		Set<Resource> props = _clazzProps.get(clazz);
		if ( props == null ) {
			props = new HashSet<Resource>();
			_clazzProps.put(clazz, props);
		}
		props.add(prop);
		_putClazz(clazz);
	}

	
	private void _putDomain(Resource prop, Resource domain) {
		Set<Resource> domains = _propDomains.get(prop);
		if ( domains == null ) {
			domains = new HashSet<Resource>();
			_propDomains.put(prop, domains);
		}
		
		if ( domain.isAnon() ) {
			String id = domain.getId().getLabelString();
			UnionClass unionClass = _unionClasses.get(id);
			List<RDFNode> operands = unionClass.getOperands().asJavaList();
			for ( RDFNode node : operands ) {
				if ( node instanceof Resource ) {
					Resource rsr = (Resource) node;
					if ( ! rsr.isAnon() ) {
						// rsr should be a class
						domains.add(rsr);
						_putClazzProperty(rsr, prop);
					}
					else {
						// TODO: it's a "complex" operand -- need generic mechanism
						// to handle this case.
					}
				}
			}
			
		}
		else {
			domains.add(domain);
			_putClazzProperty(domain, prop);
		}
	}

	private void _putRange(Resource prop, Resource range) {
		Set<Resource> ranges = _propRanges.get(prop);
		if ( ranges == null ) {
			ranges = new HashSet<Resource>();
			_propRanges.put(prop, ranges);
		}
		ranges.add(range);
	}

	
	public Collection<Resource> getClazzes() {
		return _classes.values();
	}
	
	public Resource getClass(String classUri) {
		return _classes.get(classUri);
	}
	
	public Collection<Resource> getInstances() {
		return _instances.keySet();
	}

	public Set<Resource> getTypes(Resource instance) {
		return _instances.get(instance);
	}
	
	/** never null */
	public Collection<Resource> getSubClasses(Resource superClazz) {
		Set<Resource> set = _subClasses.get(superClazz.getURI());
		return set != null ? set : EMPTY_RESOURCE_SET;
	}

	/** never null */
	public Collection<Resource> getSuperClasses(Resource subClazz) {
		Set<Resource> set = _superClasses.get(subClazz.getURI());
		return set != null ? set : EMPTY_RESOURCE_SET;
	}
	
	/**
	 * Says if a class is present in a class tree
	 * @param clazz  class to check
	 * @param rootClass Root class of the tree
	 * @return true iff class is rootClass or a descendent.
	 */
	public boolean presentInTree(Resource clazz, Resource rootClass) {
		// to prevent possible circular subClassOf:
		Set<Resource> checkedClasses = new HashSet<Resource>();
		return _presentInTree(clazz, rootClass, checkedClasses);
	}
	private boolean _presentInTree(Resource clazz, Resource rootClass, Set<Resource> checkedClasses) {
		if ( clazz.equals(rootClass) ) {
			return true;
		}
		if ( checkedClasses.contains(rootClass) ) {
			return false;
		}
		checkedClasses.add(rootClass);
		for (Resource subClass : getSubClasses(rootClass) ) {
			if ( _presentInTree(clazz, subClass, checkedClasses) ) {
				return true;
			}
		}
		return false;
	}

	public Collection<Resource> getAllProperties() {
		Set<Resource> allProps = new HashSet<Resource>();
		for ( Set<Resource> props : _clazzProps.values() ) {
			allProps.addAll(props);
		}
		return allProps;
	}
	
	public Collection<Resource> getProperties(Resource clazz) {
		return _clazzProps.get(clazz);
	}
	
	public Set<Resource> getDomains(Resource prop) {
		Set<Resource> domains = _propDomains.get(prop);
		return domains != null ? domains : EMPTY_RESOURCE_SET;
	}

	/**
	 * All the ranges of a given property.
	 * Note that RDFS.Resource is given as range for a property with given domain
	 * but missing range.
	 * 
	 * @param prop
	 * @return
	 */
	public Set<Resource> getRanges(Resource prop) {
		Set<Resource> ranges = _propRanges.get(prop);
		return ranges != null ? ranges : EMPTY_RESOURCE_SET;
	}

	private void _addOntology(Resource sbj) {
		_ontologies.add(sbj);		
	}

	public boolean containsOntology(Resource rsr) {
		return _ontologies.contains(rsr);
	}

	private void _addStatement(Statement stmt) {
		_stmts.add(stmt);
	}

	public Set<Statement> getStatements() {
		return _stmts;
	}

	public String getLabel(Resource resource) {
		String label = _useLabel ? _rdfsLabels.get(resource) : null;
		if ( label == null ) {
			label = resource.getLocalName();
		}
		return label;
	}

	public String getDataRangeName(String id) {
		return _dataRangeNames.get(id);
	}

	public Map<String, Restriction> getRestrictions() {
		return _restrictions;
	}
}


