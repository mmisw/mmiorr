package org.mmisw.ont;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;

/**
 * Handles the metadata attributes for the ontologies stored in the
 * MMI Registry.
 * 
 * @author Carlos Rueda
 */
public class MdHelper {
	private static String title = "Metadata";
	
	// Examples: preferredPrefix(DC.NS) == "dc";
	private static Map<String,String> preferredPrefix = new HashMap<String,String>();
	
	static {
		preferredPrefix.put(DC.NS, "dc");
		preferredPrefix.put(Omv.NS, "omv");
		preferredPrefix.put(OmvMmi.NS, "omvmmi");
	}

	public static String getPreferredPrefix(String namespace) {
		return preferredPrefix.get(namespace);
	}


	
	/** A single attribute definition */
	static class AttrDef {
		
		final Property[] props;

		/** 
		 * The properties that provide for the same attribute.
		 * The first element takes precedence.
		 */
		public AttrDef(Property... props) {
			assert props.length > 0;
			this.props = props;
		}
		
	}
	
	private static AttrDef[] attrDefs = {
		
		new AttrDef(DC.title),
		new AttrDef(DC.creator),
		new AttrDef(DC.subject, Omv.hasDomain),
		new AttrDef(DC.description, Omv.description),
		new AttrDef(DC.publisher),
		new AttrDef(DC.contributor, Omv.hasContributor),
		new AttrDef(DC.date, Omv.creationDate),
		new AttrDef(DC.identifier, Omv.uri),
		new AttrDef(DC.source, OmvMmi.origVocUri),

//		new AttrDef(DC.coverage),
//		new AttrDef(DC.format),
//		new AttrDef(DC.language),
//		new AttrDef(DC.relation),
//		new AttrDef(DC.rights),
//		new AttrDef(DC.type),
		
		
		
		new AttrDef(Omv.hasPriorVersion),
		
		
		
		new AttrDef(OmvMmi.shortNameUri),
		new AttrDef(OmvMmi.contact),
		new AttrDef(OmvMmi.contactRole),
		new AttrDef(OmvMmi.accessStatus),

		new AttrDef(OmvMmi.accessStatusDate),
		new AttrDef(OmvMmi.licenseCode),
		new AttrDef(OmvMmi.licenseReference),
		new AttrDef(OmvMmi.licenseAsOfDate),
		new AttrDef(OmvMmi.temporaryMmiRole),
		new AttrDef(OmvMmi.agreedMmiRole),
		new AttrDef(OmvMmi.creditRequired),
		new AttrDef(OmvMmi.creditConditions),
		new AttrDef(OmvMmi.creditCitation),

		new AttrDef(OmvMmi.origVocUri),
		new AttrDef(OmvMmi.origVocManager),
		new AttrDef(OmvMmi.origVocDocumentationUri),
		new AttrDef(OmvMmi.origVocDescriptiveName),
		new AttrDef(OmvMmi.origVocVersionId),
		new AttrDef(OmvMmi.origVocKeywords),

		new AttrDef(OmvMmi.origVocSyntaxFormat),
		new AttrDef(OmvMmi.origMaintainerCode),
	};
	
	private static Map<String,AttributeValue> _initAttributes(Map<String,AttributeValue> attributes) {
		for ( AttrDef attrDef : attrDefs ) {
			Property dcProp = attrDef.props[0];
			attributes.put(dcProp.getLocalName(), new AttributeValue(attrDef));
		}
		return attributes;	
	}
	
	/**
	 * attributes that can/should be associated
	 */
	private Map<String,AttributeValue> attributes;
	
	
	/** 
	 * Creates an ontology metadata helper.
	 */
	public MdHelper() {
		attributes = _initAttributes(new LinkedHashMap<String,AttributeValue>());	
	}
	
	public String getTitle() {
		return title;
	}

	/** 
	 * Gets all the attributes
	 * @return All the attributes
	 */
	public Collection<AttributeValue> getAttributes() {
		return attributes.values();
	}
	
	
	/**
	 * Updates the given model with the non-empty-valued attributes in this
	 * object.
	 * 
	 * @param ontModel The model to be updated.
	 */
	public void updateModel(OntModel ontModel) {

		// see that at least one attribute has a value associated
		boolean hasValues = false;
		for ( AttributeValue attr : getAttributes() ) {
			String values = attr.getValue();
			if ( values.length() > 0 ) {
				hasValues = true;
				break;
			}
		}
		
		if ( !hasValues ) {
			System.out.println(this.getClass().getName()+ ": no metadata to update in model...");
			return;
		}
		
		System.out.println(this.getClass().getName()+ ": updating metadata in model ...");
		OwlModel newOntModel = new OwlModel(ontModel);
		Ontology ontolgy = newOntModel.createOntology(JenaUtil.getURIForBase(""));
		
		for ( AttributeValue attr : getAttributes() ) {
			String value = attr.getValue();
			if ( value.length() > 0 ) {
				ontolgy.addProperty(attr.attrDef.props[0], value);
			}
		}
	}

	
	/**
	 * Updates the attributes in this object using the metadata in the 
	 * given model.
	 * 
	 * @param model The model to read metadata from.
	 */
	public void updateAttributesFromModel(Model ontModel) {

		System.out.println(this.getClass().getName()+ ": updating attributes with model metadata ...");
		
		Resource ontRes = JenaUtil.getFirstIndividual(ontModel, OWL.Ontology);
		
		if ( ontRes == null ) {
			return;
		}
		
		for ( AttrDef attrDef : attrDefs ) {
			Property dcProp = attrDef.props[0];
			String value = JenaUtil.getValue(ontRes, dcProp);
			if (value == null) {
				continue;
			}
			//	value = JenaUtil.getBaseURI(ontModel);
			
			AttributeValue attr = attributes.get(dcProp.getLocalName());
			if ( attr != null ) {
				attr.setValue(value);
			}
			
		}
		System.out.println();
	}

	
	/**
	 * A metadata attribute with associated value. 
	 */
	public static class AttributeValue {
		private AttrDef attrDef;
		private String value = "";
		
		public AttributeValue(AttrDef attrDef) {
			this.attrDef = attrDef;
		}

		public String getNamespace() {
			return attrDef.props[0].getNameSpace();
		}
		
		public String getLabel() {
			return attrDef.props[0].getLocalName();
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value.trim();
		}
		
	}
	

}
