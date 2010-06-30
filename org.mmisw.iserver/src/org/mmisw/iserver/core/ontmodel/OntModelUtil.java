package org.mmisw.iserver.core.ontmodel;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.MdHelper;
import org.mmisw.ont.JenaUtil2;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Some utilities to create and update OntModel objects.
 * 
 * @author Carlos Rueda
 */
public class OntModelUtil {

	private static final Log log = LogFactory.getLog(OntModelUtil.class);
	

	/**
	 * Creates a basic OntModel with an Ontology resource whose URI is the
	 * given URI.
	 * 
	 * @param ontUri URI for the Ontology resource.
	 * @param model Base model; can be null.
	 * @return the created OntModel
	 */
	public static OntModel createOntModel(String ontUri, OntModel model) {
		
		if ( model == null ) {
			model = createDefaultOntModel();
		}
		OntModelImpl newOntModel = new OntModelImpl(model.getSpecification(), model);
		String ns_ = JenaUtil2.appendFragment(ontUri);
		String base_ = JenaUtil2.removeTrailingFragment(ontUri);
		
		// set NS prefixes:
		newOntModel.setNsPrefix("", ns_);
		Map<String, String> preferredPrefixMap = MdHelper.getPreferredPrefixMap();
		for ( String uri : preferredPrefixMap.keySet() ) {
			String prefix = preferredPrefixMap.get(uri);
			newOntModel.setNsPrefix(prefix, uri);
		}

		newOntModel.createOntology(base_);
		if ( log.isDebugEnabled() ) {
			log.debug("New ontology created with namespace " + ns_ + " base " + base_);
		}

		return newOntModel;
	}
	
	/**
	 * Gets the first Ontology associated with the base model of the given model.
	 * <p>
	 * 
	 * See <a href="http://jena.sourceforge.net/ontology/#metadata">this jena doc</a>
	 * 
	 * @param ontModel
	 * @return the found Ontology or null.
	 */
	public static Ontology getOntology(OntModel ontModel) {
		
		OntModel mBase = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, ontModel.getBaseModel() );

		Ontology ont = null;
		
		ExtendedIterator<Ontology> iter = mBase.listOntologies();
		if ( iter.hasNext() ) {
			ont = (Ontology) iter.next();
		}
		
		if ( log.isDebugEnabled()  &&  iter.hasNext() ) {
			Ontology ont2 = (Ontology) iter.next();
			log.debug("WARNING: more than one Ontology resource in OntModel. " +
					"Second found: " +ont2.getURI()
			);
		}

		if ( log.isDebugEnabled() ) {
			if ( ont != null ) {
				log.debug("Returning Ontology with URI: " +ont.getURI());
			}
			else {
				log.debug("No Ontology found in OntModel");
			}
		}

		return ont;
	}
	
	
	
	/**
	 * Adds properties to the (first) ontology resource in the given ontModel.
	 * @param ontModel
	 * @param propValues (propUri, PropValue) pairs
	 * @return ontModel
	 */
	public static OntModel addProperties(OntModel ontModel, Map<String, String> propValues) {
		return setOrAddProperties(ontModel, false, propValues);
	}
	
	/**
	 * Sets properties to the (first) ontology resource, if any, in the given ontModel.
	 * @param ontModel
	 * @param propValues (propUri, PropValue) pairs
	 * @return ontModel
	 */
	public static OntModel setProperties(OntModel ontModel, Map<String, String> values) {
		return setOrAddProperties(ontModel, true, values);
	}
	
	
	private static OntModel setOrAddProperties(OntModel ontModel, boolean doSet, Map<String, String> values) {
		ExtendedIterator<Ontology> iter = ontModel.listOntologies();
		if ( iter.hasNext() ) {
			Ontology ont = (Ontology) iter.next();
			for ( String uri : values.keySet() ) {
				String value = values.get(uri);
				if ( value.trim().length() > 0 ) {
					Property prop = ResourceFactory.createProperty(uri);
					if ( doSet  &&  ont.getPropertyValue(prop) != null ) {
						ont.removeAll(prop);
					}
					ont.addProperty(prop, value.trim());
				}
			}
		}
		return ontModel;
	}
	
	
	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}

	public static OntModel loadModel(String uriModel, boolean processImports) {
		OntModel model = null;
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		model = createDefaultOntModel();
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(processImports);
		model.read(uriModel);
		return model;
	}


	private OntModelUtil() {}
}
