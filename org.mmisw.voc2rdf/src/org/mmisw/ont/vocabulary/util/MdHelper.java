package org.mmisw.ont.vocabulary.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrDef;
import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrGroup;

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

	/**
	 * Examples: getPreferredPrefix(DC.NS) == "dc".
	 * @param namespace
	 * @return
	 */
	public static String getPreferredPrefix(String namespace) {
		return preferredPrefix.get(namespace);
	}

	
	////////////////////////////////////////
	/////// AttrDef
	
	// map: propUri -> Property
	private static Map<String,Property> uriPropMap = new HashMap<String,Property>();
	
	public static Map<String, Property> getUriPropMap() {
		return uriPropMap;
	}

	private static AttrDef createAttrDef(Property prop) {
		AttrDef attrDef = new AttrDef(prop.getURI(), prop.getNameSpace(), prop.getLocalName());
		uriPropMap.put(prop.getURI(), prop);
		return attrDef;
	}
	
	private static AttrDef createAttrDef(Property prop, boolean required) {
		AttrDef attrDef = createAttrDef(prop);
		attrDef.setRequired(required);
		return attrDef;
	}
	
	private static AttrDef createAttrDef(Property prop, boolean required, boolean internal, String... options) {
		AttrDef attrDef = createAttrDef(prop);
		attrDef.setRequired(required);
		attrDef.setInternal(internal);
		attrDef.setOptions(options);
		attrDef.setExample(options.length > 0 ? options[0] : "");
		return attrDef;
	}
	
	private static AttrGroup[] attrGroups = {
		new AttrGroup("General",
			new AttrDef[] {
				createAttrDef(OmvMmi.origMaintainerCode, true, false, 
						"mmi", "argo", "q2o", "cf", "gcmd", "ioosdif", "*"
				), 
				createAttrDef(Omv.name, true).setExample("Project X Parameters"),
				createAttrDef(Omv.hasCreator, true).setExample("John Smith"),
				createAttrDef(Omv.hasDomain),
				createAttrDef(Omv.description, true).setExample("parameters used in project X"),
				
				
				createAttrDef(Omv.acronym, true, false, 
						"parameter",
						"sensorType",
						"platform types",
						"units",
						"keyword",
						"organization",
						"process",
						"missingflag",
						"qualityflag",
						"featureType",
						"GeographicFeature"
				), 

				
				//createAttrDef(DC.publisher),
				
				createAttrDef(Omv.hasContributor),
				createAttrDef(Omv.creationDate).setExample(org.mmi.util.ISO8601Date.getCurrentDateBasicFormat()),
				createAttrDef(Omv.uri),
				createAttrDef(OmvMmi.origVocUri),

				createAttrDef(Omv.hasPriorVersion),
				createAttrDef(OmvMmi.shortNameUri),
				createAttrDef(OmvMmi.contact),
				createAttrDef(OmvMmi.contactRole),
				createAttrDef(OmvMmi.accessStatus),
				createAttrDef(OmvMmi.accessStatusDate),
				
//				createAttrDef(DC.coverage),
//				createAttrDef(DC.format),
//				createAttrDef(DC.language),
//				createAttrDef(DC.relation),
//				createAttrDef(DC.rights),
//				createAttrDef(DC.type),

			}
		),
		new AttrGroup("Usage/License/Permissions",
			new AttrDef[] {
				createAttrDef(OmvMmi.licenseCode),
				createAttrDef(OmvMmi.licenseReference),
				createAttrDef(OmvMmi.licenseAsOfDate),
				createAttrDef(OmvMmi.temporaryMmiRole),
				createAttrDef(OmvMmi.agreedMmiRole),
				createAttrDef(OmvMmi.creditRequired),
				createAttrDef(OmvMmi.creditConditions),
				createAttrDef(OmvMmi.creditCitation),
			}
		),
		new AttrGroup("Original source",
			new AttrDef[] {
				createAttrDef(OmvMmi.origVocUri),
				createAttrDef(OmvMmi.origVocManager),
				createAttrDef(OmvMmi.origVocDocumentationUri),
				createAttrDef(OmvMmi.origVocDescriptiveName),
				createAttrDef(OmvMmi.origVocVersionId),
				createAttrDef(OmvMmi.origVocKeywords),
				createAttrDef(OmvMmi.origVocSyntaxFormat),
			}
		),
	};

	
	public static AttrGroup[] getAttrGroups() {
		return attrGroups;
	}
	
	
	private static AttrDef[] attrDefs = {
		
	};
	
	private static Map<String,AttributeValue> _initAttributes(Map<String,AttributeValue> attributes) {
		for ( AttrDef attrDef : attrDefs ) {
			attributes.put(attrDef.getLocalName(), new AttributeValue(attrDef));
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
				ontolgy.addProperty(uriPropMap.get(attr.attrDef.getUri()), value);
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
			Property dcProp = uriPropMap.get(attrDef.getUri());
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
			return attrDef.getNameSpace();
		}
		
		public String getLabel() {
			return attrDef.getLocalName();
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value.trim();
		}
		
		public Property getProperty() {
			return uriPropMap.get(attrDef.getUri());
		}
	}
	

}
