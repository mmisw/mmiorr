package org.mmisw.ontmd.gwt.server;

import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.MmiUri;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

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
	 */
	static void readAuthorities(AttrDef authorityAttrDef) {
		String classUri = Config.Prop.AUTHORITY_CLASS.getValue();
		try {
			populateList(authorityAttrDef, classUri);
		}
		catch (Exception e) {
			authorityAttrDef.addOption(
					new Option("dummy", "dummy: (" +e.getMessage()+ ")")
			);
		}
	}

	/**
	 * Populates the AttrDef with the list of individuals from {@link Config.Prop#RESOURCE_TYPE_CLASS}.
	 */
	static void readResourceTypes(AttrDef mainClassAttrDef) {
		String classUri = Config.Prop.RESOURCE_TYPE_CLASS.getValue();
		try {
			populateList(mainClassAttrDef, classUri);
		}
		catch (Exception e) {
			mainClassAttrDef.addOption(
					new Option("dummy", "dummy: (" +e.getMessage()+ ")")
			);
		}
	}
	
	

	private static void populateList(AttrDef mainClassAttrDef,
				String classUri) throws URISyntaxException {
		
		MmiUri classMmiUri = new MmiUri(classUri);
		String ontologUri = classMmiUri.getOntologyUri();
		String className = classMmiUri.getTerm();
		
		log.debug("reading: " +classMmiUri.getTermUri()+ " individuals to populate " +className+ " list");

		// read the ontology:
		OntModel ontModel = ModelFactory.createOntologyModel();
		ontModel.read(ontologUri);
		
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
			mainClassAttrDef.addOption(option);
		}
	}
	

	private MdUtil() { }
}
