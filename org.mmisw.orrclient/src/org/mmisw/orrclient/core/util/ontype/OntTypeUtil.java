package org.mmisw.orrclient.core.util.ontype;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ont.vocabulary.Vine;
import org.mmisw.ont.vocabulary.Vine20071128;
import org.mmisw.orrclient.gwt.client.rpc.OntologyType;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Determines the "type" of an ontology for purposes of dispatching
 * appropriate handling.
 * 
 * @author Carlos Rueda
 */
public class OntTypeUtil {
	
	private static final Log log = LogFactory.getLog(OntTypeUtil.class);
	
	
	/**
	 * Maps from string to OntologyType
	 * @param type
	 * @return
	 */
	public static OntologyType map(String type) {
		for ( OntologyType ot : OntologyType.values() ) {
			if ( ot.toString().equalsIgnoreCase(type) ) {
				return ot;
			}
		}
		return OntologyType.OTHER;
	}
	
	/**
	 * Determines the type of the given model.
	 * 
	 * @param ontModel
	 * @param ontologyUri
	 * @param dtProps
	 *         If not null, used when determining if the model is a vocabulary:
	 *         check that all provided dtProps are contained in the defined 
	 *         datatype properties in the model.
	 * @return
	 * @throws Exception
	 */
	public static OntologyType determineType(OntModel ontModel, String ontologyUri,
			Set<Property> dtProps
	) throws Exception {
		
		Ontology ont = _getOntology(ontModel, ontologyUri);
		
		// mapping?
		if ( _isMapping(ontModel, ont) ) {
			return OntologyType.MAPPING;
		}
		
		// vocabulary?
		if ( _isVocabulary(ontModel, ont, dtProps) ) {
			return OntologyType.VOCABULARY;
		}
		
		return OntologyType.OTHER;
	}
	

	/**
	 * Obtains the Ontology of the given model.
	 * 
	 * @param ontModel 
	 *            The model
	 * @param ontologyUri 
	 *            If not null, returns ontModel.getOntology(ontologyUri).
	 *            Otherwise, it follows <a href="http://jena.sourceforge.net/ontology/#metadata"
	 *            >this Jena guidance</a>.
	 *            Basically, it returns the first reported ontology in the base model of the 
	 *            given model.
	 */
	private static Ontology _getOntology(OntModel ontModel, String ontologyUri) throws Exception {
		if ( ontologyUri != null ) {
			return ontModel.getOntology(ontologyUri);
		}
		
		ExtendedIterator<Ontology> ontsIter = ontModel.listOntologies();
		List<Ontology> list = ontsIter.toList();
		
		if ( log.isDebugEnabled() ) {
			log.debug(" Ontologies in given model: " +list);
		}
		if ( list.size() == 1 ) {
			return list.get(0);
		}
		
		OntModel mBase = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM, ontModel.getBaseModel() 
		);
		ontsIter = mBase.listOntologies();
		list = ontsIter.toList();
		if ( log.isDebugEnabled() ) {
			log.debug(" Ontologies in given model: " +list);
		}
		return list.size() > 0 ? list.get(0) : null;
	}
	
	
	/** Is the model a Vine-created mapping? */
	private static boolean _isMapping(OntModel ontModel, Ontology ont) {
		String ontUriMsg = "ontUri=?";
		if ( ont != null ) {
			ontUriMsg = "ontUri=" +ont.getURI();
		}
		
		if ( ont != null ) {
			// try the Omv.usedOntologyEngineeringTool property:
			RDFNode val = ont.getPropertyValue(Omv.usedOntologyEngineeringTool);
			if ( OmvMmi.vine.getURI().equals(_getValueAsString(val)) ) {
				if ( log.isDebugEnabled() ) {
					log.debug("_isMapping: " +ontUriMsg+ " --> true (Omv.usedOntologyEngineeringTool)" );
				}
				return true;
			}
		}

		// try examining the contents: check for statements having Vine.subject or 
		// Vine20071128.subject as predicate:
		if (ontModel.contains(null, Vine.subject, (RDFNode) null) ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_isMapping: " +ontUriMsg+ " --> true (Vine.subject)" );
			}
			return true;
		}
		if (ontModel.contains(null, Vine20071128.subject, (RDFNode) null) ) {
			if ( log.isDebugEnabled() ) {
				log.debug("_isMapping: " +ontUriMsg+ " --> true (Vine20071128.subject)" );
			}
			return true;
		}
		
		return false;
	}

	
	/** 
	 * Is the model a voc2rdf-created mapping? 
	 * 
	 * @param ontModel
	 * @param ont
	 * @param dtProps
	 *         If not null, check that all provided dtProps are contained in the defined 
	 *         datatype properties in the model.
	 * @return
	 */
	private static boolean _isVocabulary(OntModel ontModel, Ontology ont,
			Set<Property> dtProps
	) {
		String ontUriMsg = "ontUri=?";
		if ( ont != null ) {
			ontUriMsg = "ontUri=" +ont.getURI();
		}

		if ( ont != null ) {
			// try the Omv.usedOntologyEngineeringTool property:
			RDFNode val = ont.getPropertyValue(Omv.usedOntologyEngineeringTool);
			if ( OmvMmi.voc2rdf.getURI().equals(_getValueAsString(val)) ) {
				if ( log.isDebugEnabled() ) {
					log.debug("_isVocabulary: " +ontUriMsg+ " --> true (Omv.usedOntologyEngineeringTool)" );
				}
				return true;
			}
		}
		
		// try examining the contents:
		ExtendedIterator<OntClass> nc = ontModel.listNamedClasses();
		List<OntClass> classes = nc.toList();
		if ( classes.size() != 1 ) {
			return false;
		}
		
		OntClass cls = classes.get(0);
		ExtendedIterator<Individual> inds = ontModel.listIndividuals(cls);
		List<Individual> individuals = inds.toList();
		if ( individuals.size() == 0 ) {
			return false;
		}
		
		if ( dtProps != null ) {
			// check that all provided dtProps are contained in the defined datatype properties
			// in the model:
			List<DatatypeProperty> props = ontModel.listDatatypeProperties().toList();
			
			for ( Property dtProp : dtProps ) {
				String dtPropUri = dtProp.getURI();
				boolean contained = false;
				for ( DatatypeProperty datatypeProp : props ) {
					if ( datatypeProp.getURI().equals(dtPropUri) ) {
						contained = true;
						break;
					}
				}
				if ( ! contained ) {
					return false;
				}
			}
			
			if ( log.isDebugEnabled() ) {
				log.debug("_isVocabulary: " +ontUriMsg+ " --> true (all datatype properties defined in the ontology)" );
			}
			return true;
			
		}
		else {
			ExtendedIterator<DatatypeProperty> props = ontModel.listDatatypeProperties();
			boolean containDatatype = props.hasNext();
			if ( containDatatype  ) {
				if ( log.isDebugEnabled() ) {
					log.debug("_isVocabulary: " +ontUriMsg+ " --> true (defines datatype)" );
				}
				return true;
			}
		}
		
		return false;	
	}
	
	
	private static String _getValueAsString(RDFNode node) {
		if ( node == null ) {
			return null;
		}
		else if ( node instanceof Literal ) {
			Literal lit = (Literal) node;
			return lit.getLexicalForm();
		} 
		else {
			return ((Resource) node).getURI();
		}
	}



	private OntTypeUtil() {}
}

