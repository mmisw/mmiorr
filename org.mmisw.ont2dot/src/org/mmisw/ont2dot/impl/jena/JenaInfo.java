package org.mmisw.ont2dot.impl.jena;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
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

	private static final Set<Resource> EMPTY_RESOURCE_SET = Collections.emptySet();
    
	private final Map<String, Resource> _classes = new HashMap<String, Resource>();

	private final Map<String, Set<Resource>> _superClasses = new HashMap<String, Set<Resource>>();
	
	private final Map<Resource, Set<Resource>> _instances = new HashMap<Resource, Set<Resource>>();

	private final Set<Resource> _ontologies = new HashSet<Resource>();
	
	// clazz -> properties
	private final Map<Resource, Set<Resource>> _clazzProps = new HashMap<Resource, Set<Resource>>();
	
	// property -> domains
	private final Map<Resource, Set<Resource>> _propsDomains = new HashMap<Resource, Set<Resource>>();
	// property -> ranges
	private final Map<Resource, Set<Resource>> _propRanges = new HashMap<Resource, Set<Resource>>();

	private final Map<Resource, Set<Statement>> _dataTypePropertyInstantiations = new HashMap<Resource, Set<Statement>>();

	
	private final Map<Resource, String> _rdfsLabels = new HashMap<Resource, String>();

	private final Map<String, DataRange> _dataRanges = new LinkedHashMap<String, DataRange>();
	private final Map<String, String> _dataRangeNames = new HashMap<String, String>();

	
	private final Map<String, Restriction> _restrictions = new LinkedHashMap<String, Restriction>();
	
	
	private Set<Statement> _stmts = new HashSet<Statement>();
	
	
	//use label instead of local name?
	private boolean _useLabel = true;


	
	JenaInfo(OntModel ontModel) {
		JenaInfo _info = this;
		
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
				_info._putSuperClazz(sbj, objRsr);
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
//			System.out.println("XXXXXX " +id);
		}

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

	private void _putSuperClazz(Resource subClazz, Resource superClazz) {
		Set<Resource> supers = _superClasses.get(subClazz.getURI());
		if ( supers == null ) {
			supers = new HashSet<Resource>();
			_superClasses.put(subClazz.getURI(), supers);
		}
		supers.add(superClazz);
		
		_putClazz(subClazz);
		_putClazz(superClazz);
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
		Set<Resource> domains = _propsDomains.get(prop);
		if ( domains == null ) {
			domains = new HashSet<Resource>();
			_propsDomains.put(prop, domains);
		}
		domains.add(domain);
		_putClazzProperty(domain, prop);
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
	
	public Collection<Resource> getInstances() {
		return _instances.keySet();
	}

	public Set<Resource> getTypes(Resource instance) {
		return _instances.get(instance);
	}
	
	public Collection<Resource> getSuperClasses(Resource subClazz) {
		return _superClasses.get(subClazz.getURI());
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
		Set<Resource> domains = _propsDomains.get(prop);
		return domains != null ? domains : EMPTY_RESOURCE_SET;
	}

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


