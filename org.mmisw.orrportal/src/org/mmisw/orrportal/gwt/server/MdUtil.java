package org.mmisw.orrportal.gwt.server;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.MmiUri;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.Option;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * Some utils related with metadata
 * 
 * @author Carlos Rueda
 */
@Deprecated
class MdUtil {
	
	private static final Log log = LogFactory.getLog(MdUtil.class);
	
	/**
	 * Populates the AttrDef with the list of individuals from {@link PortalConfig.Prop#AUTHORITY_CLASS}.
	 * The options are sorted by getName() (ignoring case).
	 */
	static void readAuthorities(AttrDef authorityAttrDef) {
		String classUri = PortalConfig.Prop.AUTHORITY_CLASS.getValue();
		try {
			populateList(authorityAttrDef, classUri);
			Collections.sort(authorityAttrDef.getOptions(), new Comparator<Option>() {
				public int compare(Option o1, Option o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}
		catch (Exception e) {
			log.debug("Error trying to read: " +classUri+ ": " +e.getMessage(), e);
			authorityAttrDef.addOption(
					new Option("dummy", "dummy: (" +e.getMessage()+ ")")
			);
		}
	}

	/**
	 * Populates the AttrDef with the list of individuals from {@link PortalConfig.Prop#RESOURCE_TYPE_CLASS}.
	 * The options are sorted by getName() (ignoring case).
	 */
	static void readResourceTypes(AttrDef resourceTypeAttrDef) {
		String classUri = PortalConfig.Prop.RESOURCE_TYPE_CLASS.getValue();
		try {
			populateList(resourceTypeAttrDef, classUri);
			Collections.sort(resourceTypeAttrDef.getOptions(), new Comparator<Option>() {
				public int compare(Option o1, Option o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}
		catch (Exception e) {
			log.debug("Error trying to read: " +classUri+ ": " +e.getMessage(), e);
			resourceTypeAttrDef.addOption(
					new Option("dummy", "dummy: (" +e.getMessage()+ ")")
			);
		}
	}
	
	

	private static void populateList(AttrDef attrDef, String classUri) throws URISyntaxException {
		
		MmiUri classMmiUri = new MmiUri(classUri);
		String ontologUri = classMmiUri.getOntologyUri();
		String className = classMmiUri.getTerm();
		
		log.debug("reading: " +classMmiUri.getTermUri()+ " individuals to populate " +className+ " list");

		// read the ontology:
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.read(ontologUri);
		
		// add options here termporarily
		List<Option> list = new ArrayList<Option>();
		
		Resource classRes = ResourceFactory.createResource(classMmiUri.getTermUri());
		
		ExtendedIterator<Individual> iter = ontModel.listIndividuals(classRes);
		while ( iter.hasNext() ) {
			Resource idv = (Resource) iter.next();
			String idvName = idv.getLocalName();
			String idvUri = idv.getURI();
			
			String label = idvName+ " - " +idvUri;
			
			Option option = new Option(idvName, label);
			option.setUri(idvUri);
			
			// TODO: provide more information for each option
			list.add(option);
		}
		
		attrDef.getOptions().clear();
		attrDef.getOptions().addAll(list);
		
		log.debug("read: " +attrDef.getOptions().size()+ " individuals");
	}
	

	private MdUtil() { }
}
