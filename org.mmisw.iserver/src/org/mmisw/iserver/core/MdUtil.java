package org.mmisw.iserver.core;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.iserver.gwt.client.vocabulary.Option;
import org.mmisw.ont.MmiUri;

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
class MdUtil {
	
	private static final Log log = LogFactory.getLog(MdUtil.class);
	
	/**
	 * Populates the AttrDef with the list of individuals from {@link Config.Prop#AUTHORITY_CLASS}.
	 * The options are sorted by getName() (ignoring case).
	 */
	static void readAuthorities(AttrDef authorityAttrDef, String authorityClassUri) {
		try {
			populateList(authorityAttrDef, authorityClassUri);
			Collections.sort(authorityAttrDef.getOptions(), new Comparator<Option>() {
				public int compare(Option o1, Option o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}
		catch (Exception e) {
			log.debug("Error trying to read: " +authorityClassUri+ ": " +e.getMessage(), e);
			authorityAttrDef.addOption(
					new Option("dummy", "dummy: (" +e.getMessage()+ ")")
			);
		}
	}

	/**
	 * Populates the AttrDef with the list of individuals from {@link Config.Prop#RESOURCE_TYPE_CLASS}.
	 * The options are sorted by getName() (ignoring case).
	 */
	static void readResourceTypes(AttrDef resourceTypeAttrDef, String resourceTypeClassUri) {
		try {
			populateList(resourceTypeAttrDef, resourceTypeClassUri);
			Collections.sort(resourceTypeAttrDef.getOptions(), new Comparator<Option>() {
				public int compare(Option o1, Option o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			});
		}
		catch (Exception e) {
			log.debug("Error trying to read: " +resourceTypeClassUri+ ": " +e.getMessage(), e);
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
		
		ExtendedIterator iter = ontModel.listIndividuals(classRes);
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
