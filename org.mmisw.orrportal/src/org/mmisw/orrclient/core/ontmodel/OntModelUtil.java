package org.mmisw.orrclient.core.ontmodel;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.orrclient.core.MdHelper;

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
		String ns_ = model.getNsPrefixURI("");
		String base_ = JenaUtil2.removeTrailingFragment(ontUri);

		// set NS prefixes:
		if (ns_ != null) {
			newOntModel.setNsPrefix("", ns_);
		}
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

	private OntModelUtil() {}
}
