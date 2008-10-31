package org.mmisw.ont.vocabulary.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

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
	
	// Examples: preferredPrefix(Omv.NS) == "omv";
	private static Map<String,String> preferredPrefix = new HashMap<String,String>();
	
	static {
		preferredPrefix.put(DC.NS, "dc");
		preferredPrefix.put(Omv.NS, "omv");
		preferredPrefix.put(OmvMmi.NS, "omvmmi");
		preferredPrefix = Collections.unmodifiableMap(preferredPrefix);
	}
	
	/** 
	 * Returns a map of preferred prefixes, eg., <code>preferredPrefix(Omv.NS) == "omv"</code>.
	 * This is an unmodifiable map. 
	 */
	public static Map<String,String> getPreferredPrefixMap() {
		return preferredPrefix;
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
	
	///////////////////////////////////////////////////////////////////////
	// The metadata groups
	private static AttrGroup[] attrGroups;
	
	public static void prepareGroups() {
		attrGroups = new AttrGroup[] {
			new AttrGroup("General",
				"These fields capture general information about this ontology, who created it, and where it came from.",
				new AttrDef[] {
					
					createAttrDef(Omv.acronym, true) 
						.setLabel("Ontology theme")
						.setExample("parameter")
						.addOption(new Option("--choose one--"))
						.addOption(new Option("parameter", "Parameters (It will include terms like 'sea surface salinity')"))
						.addOption(new Option("sensorType", "Sensor types (It will include terms like 'Thermometer')"))
						.addOption(new Option("platformType", "Platform types (It will include terms like 'Mooring')"))
						.addOption(new Option("unit", "Units  (It will include terms like 'meter')"))
						.addOption(new Option("keyword", "Keywords  (It will include terms like 'climate', 'oceans')"))
						.addOption(new Option("organization", "Organizations  (It will include terms like 'MBARI' or 'MMI')"))
						.addOption(new Option("process", "Processes  (It will include terms like 'data quality control')"))
						.addOption(new Option("missingflag", "Missing flags  (It will include terms like '-999')"))
						.addOption(new Option("qualityflag", "Quality flags  (It will include terms like '10')"))
						.addOption(new Option("featureType", "Feature types  (It will include terms like 'body of water')"))
						.addOption(new Option("GeographicFeature", "Geographic features  (It will include terms like 'Monterey Bay')"))
					, 
		
					createAttrDef(OmvMmi.shortNameUri)
						.setLabel("URI of ontology theme")
						.setExample("http://mmisw.org/ont/mmi/topicTheme/parameter")
					,
					
	
					
	
					createAttrDef(Omv.name, true)
						.setLabel("Full title")
						.setExample("Project Athena Parameters")
					,
					createAttrDef(Omv.hasCreator, true)
						.setLabel("Creator")
						.setExample("Athena Project")
					,
					createAttrDef(Omv.description, true)
						.setLabel("Brief description")
						.setNumberOfLines(4)
						.setExample("Parameters used in Project Athena")
					,
					
					createAttrDef(Omv.keywords, false)
						.setLabel("Keywords")
						.setExample("ocean, physical oceanography, environmental science, climate change")
					,
				
					createAttrDef(OmvMmi.origVocUri)
						.setLabel("Link to original vocabulary")
						.setExample("http://marinemetadata.org/community/teams/athena/parameters.html")
					,
	
					createAttrDef(Omv.documentation, false)
						.setExample("http://marinemetadata.org/community/teams/athena")
						.setLabel("Link to documentation")
					,
			
					createAttrDef(OmvMmi.origMaintainerCode, true)
						.setLabel("Authority abbreviation")
						.setExample("mmi")
						.setAllowUserDefinedOption(true)
						.addOption(new Option("cencoos", "cencoos: Central California Ocean Observing System"))
						.addOption(new Option("gcoos", "gcoos: Gulf of Mexico Ocean Observing System"))
						.addOption(new Option("mmi", "mmi: Marine Metadata Interoperability"))
						.addOption(new Option("oi2008demo", "oi2008demo: Oceans Innovations 2008 Demonstration"))
						.addOption(new Option("q2o", "q2o: QARTOD-to-OGC Project"))
	//					.addOption(new Option("argo", "argo: Argo Project"))
	//					.addOption(new Option("gcmd", "gcmd: Global Change Master Directory"))
	//					.addOption(new Option("cf", "cf: Climate and Forecast Conventions Standards Names"))
//						.addOption(new Option("*", "--other, please specify"))
					, 
					
	//				createAttrDef(Omv.hasDomain),
					
					createAttrDef(Omv.creationDate)
						.setLabel("Creation date")
						.setInternal(true)
						.setExample(org.mmi.util.ISO8601Date.getCurrentDateBasicFormat())
					,
					
					
					//createAttrDef(DC.publisher),
					
					createAttrDef(Omv.hasContributor)
						.setLabel("Contributor(s)")
						.setNumberOfLines(3)
						.setExample("Jane Collaborator, Joe Ontology Manager, Fido the Mascot")
					,
					
					createAttrDef(Omv.uri)
						.setInternal(true)
					,
					
					
					// TODO createAttrDef(Omv.hasPriorVersion),
					
					
					
	//				createAttrDef(DC.coverage),
	//				createAttrDef(DC.format),
	//				createAttrDef(DC.language),
	//				createAttrDef(DC.relation),
	//				createAttrDef(DC.rights),
	//				createAttrDef(DC.type),
	
				}
			),
			new AttrGroup("Usage/License/Permissions",
				"The Usage, License, and Permissions fields help keep track of how we obtained this vocabulary " +
				"(and know it is OK to serve to others), and on what terms other people can use it.",
				new AttrDef[] {
					
					createAttrDef(OmvMmi.origVocManager)
						.setLabel("Manager of source vocabulary")
						.setExample("Athena Project")
					,
					
					createAttrDef(OmvMmi.contact)
						.setLabel("Contact/Responsible Party")
						.setExample("Joe Ontology Manager")
					,
					createAttrDef(OmvMmi.contactRole)
						.setLabel("Contact role")
						.setExample("content manager")
						.addOption(new Option("--choose one--"))
						.addOption(new Option("content manager"))
						.addOption(new Option("ontology producer"))
						.addOption(new Option("organizational manager"))
						.addOption(new Option("IP negotiator"))
						.addOption(new Option("other"))
					,
					
					// TODO createAttrDef(OmvMmi.accessStatus),
					
					// TODO createAttrDef(OmvMmi.accessStatusDate),
	
					// TODO createAttrDef(OmvMmi.licenseCode),
					// TODO createAttrDef(OmvMmi.licenseReference),
					// TODO createAttrDef(OmvMmi.licenseAsOfDate),
					
					createAttrDef(OmvMmi.temporaryMmiRole)
						.setLabel("Temporary MMI role")
						.setExample("ontology producer")
						.addOption(new Option("--choose one--"))
						.addOption(new Option("content manager"))
						.addOption(new Option("ontology producer"))
						.addOption(new Option("ontology republisher"))
						.addOption(new Option("other"))
					,
					
					// TODO createAttrDef(OmvMmi.agreedMmiRole),
					
					createAttrDef(OmvMmi.creditRequired)
						.setExample("yes")
						.setLabel("Author credit required")
						.addOption(new Option("not specified"))
						.addOption(new Option("yes"))
						.addOption(new Option("no"))
						.addOption(new Option("conditional"))
					,
					
					// TODO createAttrDef(OmvMmi.creditConditions),
					
					createAttrDef(OmvMmi.creditCitation)
						.setLabel("Citation string")
						.setNumberOfLines(3)
						.setExample("Ontology provided by Athena Project")
					,
				}
			),
			new AttrGroup("Original source",
				"The fields in this section capture specific metadata that was documented in the " +
				"original vocabulary, so you can see how it was originally documented. " +
				"Typically, this metadata is much less extensive than we maintain.",
				new AttrDef[] {
					
					createAttrDef(OmvMmi.origVocDocumentationUri)
						.setLabel("URI of original vocabulary")
					,
					
					createAttrDef(OmvMmi.origVocDescriptiveName)
						.setLabel("Descriptive name")
					,
					
					createAttrDef(OmvMmi.origVocVersionId)
						.setLabel("Version")
					,
					
					createAttrDef(OmvMmi.origVocKeywords)
						.setLabel("Keywords")
					,
					
					createAttrDef(OmvMmi.origVocSyntaxFormat)
						.setLabel("Syntax format")
					,
				}
			),
		};
	}

	
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
