package org.mmisw.ontmd.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * Handles the metadata attributes for the ontologies stored in the
 * MMI Registry.
 * 
 * 
 * @author Carlos Rueda
 */
public class MdHelper {
	private MdHelper() { }

	
	public static final String RESOURCE_TYPE_TOOLTIP =
		"The kind of resource represented by the ontology. Typically made up of a Topic or Class name, an " +
		"optional indication of a mapping ontology ('_map' if this is a term mapping ontology), and if " +
		"necessary a unique identifier (a numeric appendix or subcategory, like '_002' or '_team1'. " +
		"So while most Resource Types are simple terms like 'parameter' or 'datum', they can be more complex, " +
		"like temporal_map_ageClassifier'. The two attributes \"Resource type\" and \"Authority abbreviation\" " +
		"are used to construct the URIs for the vocabulary and terms. So Resource Type must be constrained to " +
		"alphnumerics, underscore, and (discouraged) hyphen. Underscores are recommended only between the components. " +
		"MMI converts all resource types to lower case. " +
		"For more tips on selecting Resource Type, see the " +
		"<a href=\"http://marinemetadata.org/apguides/ontprovidersguide/ontguideconstructinguris\" target=\"_blank\"" +
		">MMI Ontology Providers Guide</a>.";



	public static final String AUTHORITY_TOOLTIP = 
		"This is an MMI-controlled vocabulary of abbreviations that indicate the controlling authority of the vocabulary. " +
		"You can choose from any of the existing terms in the list, or specify a new one; MMI reserves the right to correct " +
		"your choice if that is appropriate. (For example, using a government organization as the authority should only be " +
		"made if you are in an authoritative position for that organization.) " +
		"The two attributes \"Resource type\" and \"Authority abbreviation\" are used to construct the URIs for the " +
		"vocabulary and terms. The authority abbreviation should be short but descriptive; it is possible for one " +
		"organization to have multiple authority names (e.g., mmi, mmitest, mmidev), but a profusion of authority " +
		"names is discouraged (familiarity is more important than uniqueness). Organizations that will be submitting " +
		"a lot of overlapping ontologies should consider using authority names to provide namespace separation. " +
		"For more tips on selecting Resource Type, see the " +
		"<a href=\"http://marinemetadata.org/apguides/ontprovidersguide/ontguideconstructinguris\" target=\"_blank\"" +
		">MMI Ontology Providers Guide</a>.";
	

	
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
	
	// map: propUri -> AttrDef
	private static Map<String,AttrDef> uriAttrDefMap = new HashMap<String,AttrDef>();
	

	public static Map<String, Property> getUriPropMap() {
		return uriPropMap;
	}

	public static Map<String, AttrDef> getAttrDefMap() {
		return uriAttrDefMap;
	}

	
	private static AttrDef createAttrDef(Property prop) {
		AttrDef attrDef = new AttrDef(prop.getURI(), prop.getNameSpace(), prop.getLocalName());
		uriPropMap.put(prop.getURI(), prop);
		uriAttrDefMap.put(prop.getURI(), attrDef);
		return attrDef;
	}
	
	private static AttrDef createAttrDef(Property prop, boolean required) {
		AttrDef attrDef = createAttrDef(prop);
		attrDef.setRequired(required);
		return attrDef;
	}
	
	
	private static AttrDef resourceTypeAttrDef = null;
	

	
	
	
	/**
	 * Creates the "resource type" attribute definition.
	 */
	private static AttrDef createResourceTypeAttrDef() {
		if ( resourceTypeAttrDef == null ) {
			// Note: OmvMmi.shortNameUri is now associated with Omv.acronym:
			resourceTypeAttrDef =
				createAttrDef(Omv.acronym, true) 
			    .setLabel("Resource type")
				.setTooltip(RESOURCE_TYPE_TOOLTIP)
				.setExample("parameter")
				.setAllowUserDefinedOption(true)
				.setOptionsVocabulary(Config.Prop.RESOURCE_TYPE_CLASS.getValue())
				.addRelatedAttr(
						createAttrDef(OmvMmi.shortNameUri)
						.setLabel("URI of resource type")
						.setTooltip("Ideally the resource type is selected from, and described in, a controlled " +
								"vocabulary with URIs defined. If so, enter the URI naming the term in this field. " +
								"If the term is in a controlled vocabulary but does not have its own URI, enter the " +
								"controlled vocabulary URI. Otherwise, leave this field blank.")
						.setExample("http://mmisw.org/ont/mmi/topicTheme/parameter")
				)
			;
			
			MdUtil.readResourceTypes(resourceTypeAttrDef);
			
			
		}
		
		return resourceTypeAttrDef;
	}
	
	
	
	
	public static AttrDef getResourceTypeAttrDef() {
		createResourceTypeAttrDef();
		return resourceTypeAttrDef;
	}
	
	
	
	private static AttrDef authorityAttrDef = null;
	
	
	/**
	 * Creates the "authority" attribute definition.
	 */
	private static AttrDef createAuthorityAttrDef() {
		if ( authorityAttrDef == null ) {
			authorityAttrDef = createAttrDef(OmvMmi.origMaintainerCode, true)
				.setLabel("Authority abbreviation")
				.setTooltip(AUTHORITY_TOOLTIP)
				.setExample("mmitest")
				.setAllowUserDefinedOption(true)
				.setOptionsVocabulary(Config.Prop.AUTHORITY_CLASS.getValue())
			;
			
			MdUtil.readAuthorities(authorityAttrDef);
		}
		
		return authorityAttrDef;
	}
	
	///////////////////////////////////////////////////////////////////////
	// The metadata groups
	private static AttrGroup[] attrGroups;
	
	/**
	 * @param includeVersion true to include omv:version so the user can specify it directly.
	 */
	public static void prepareGroups(boolean includeVersion) {
		
		List<AttrDef> general_attr_list = new ArrayList<AttrDef>();
		general_attr_list.add(createResourceTypeAttrDef());
		
// Not any more; OmvMmi.shortNameUri is now associated with Omv.acronym above.
//		general_attr_list.add(
//				createAttrDef(OmvMmi.shortNameUri)
//				.setLabel("URI of resource type")
//				.setTooltip("Ideally the resource type is selected from, and described in, a controlled " +
//						"vocabulary with URIs defined. If so, enter the URI naming the term in this field. " +
//						"If the term is in a controlled vocabulary but does not have its own URI, enter the " +
//						"controlled vocabulary URI. Otherwise, leave this field blank.")
//				.setExample("http://mmisw.org/ont/mmi/topicTheme/parameter")
//		);
		
		
		general_attr_list.add(
				createAttrDef(Omv.name, true)
				.setLabel("Full title")
				.setTooltip("A one-line descriptive title (title case) for the ontology.")
				.setExample("Project Athena Parameters")
		);
		
		general_attr_list.add(
				createAttrDef(Omv.hasCreator, true)
				.setLabel("Creator")
				.setTooltip("The name of the person submitting the ontology.")
				.setExample("Athena Project")
		);
		
		if ( includeVersion ) {
			general_attr_list.add(
					createAttrDef(Omv.version, true)
					.setLabel("Version")
					.setTooltip("The version to be used for the submitted ontology.")
					.setExample("20050101")
			);
		}
		
		general_attr_list.add(
				createAttrDef(Omv.description, true)
				.setLabel("Brief description")
				.setTooltip("A textual description of the ontology. Completeness is welcome. HTML characters are less than ideal.")
				.setNumberOfLines(4)
				.setExample("Parameters used in Project Athena")
		);
		
		general_attr_list.add(
				createAttrDef(Omv.keywords, false)
				.setLabel("Keywords")
				.setTooltip("Enter a list of keywords (ideally by their URI, but can also be free text) separated by commas. " +
						"These keywords should characterize the nature of the ontology, not mention every term in it.")
				.setExample("ocean, physical oceanography, environmental science, climate change")
		);
		
		general_attr_list.add(
				createAttrDef(OmvMmi.origVocUri)
				.setLabel("Link to original vocabulary")
				.setTooltip("If the original vocabulary is published on-line, put its URL here.")
				.setExample("http://marinemetadata.org/community/teams/athena/parameters.html")
		);
		
		general_attr_list.add(
				createAttrDef(Omv.documentation, false)
				.setLabel("Link to documentation")
				.setTooltip("If there is a page or site describing the vocabulary, put its URL here.")
				.setExample("http://marinemetadata.org/community/teams/athena")
		);
		
		general_attr_list.add(
				createAuthorityAttrDef()
		);
		
		general_attr_list.add(
				createAttrDef(Omv.creationDate)
				.setLabel("Creation date")
				.setInternal(true)
				.setExample(org.mmi.util.ISO8601Date.getCurrentDateBasicFormat())
		);
		
//		general_attr_list.add(
//				createAttrDef(DC.publisher)
//		);
		
		general_attr_list.add(
				createAttrDef(Omv.hasContributor)
				.setLabel("Contributor(s)")
				.setTooltip("List all the individuals and/or organizations that contributed materially " +
						"to this vocabulary/ontology. You may use comma- or semicolon-separated names " +
						"or URIs for individuals or organizations, or URIs that point to additional " +
						"information. This is a free text field.")
				.setNumberOfLines(3)
				.setExample("Jane Collaborator, Joe Ontology Manager, Fido the Mascot")
		);
		
		general_attr_list.add(
				createAttrDef(Omv.uri)
				.setInternal(true)
		);
		
		
		AttrDef[] general_attrs = general_attr_list.toArray(new AttrDef[general_attr_list.size()]);
		
		attrGroups = new AttrGroup[] {
			new AttrGroup("General",
				"These fields capture general information about this ontology, who created it, and where it came from. " +
				"The two attributes \"Resource type\" and \"Authority abbreviation\" are used to construct the URIs " +
				"for the vocabulary and terms. In our system, they can contain only letters, " +
				"numbers, underscores, and (not recommended) hyphens, and begin with a letter.",
				general_attrs
			),
			new AttrGroup("Usage/License/Permissions",
				"The Usage, License, and Permissions fields help keep track of how we obtained this vocabulary " +
				"(and know it is OK to serve to others), and on what terms other people can use it.",
				new AttrDef[] {
					
					createAttrDef(OmvMmi.origVocManager)
						.setLabel("Manager of source vocabulary")
						.setTooltip("Who actively maintains the source vocabulary (used to build the ontology) and " +
								"the changes to it. Specify an individual or very specific organization, by name or " +
								"as a URI. Include phone, mail, and URL in parentheses after the specification, " +
								"if available: First Last (831-nnn-nnnn, name@domain.com, http://domain.com/myHomePage). " +
								"If the vocabulary is not actively maintained, put \"None\" in this field.")
						.setExample("Athena Project")
					,
					
					createAttrDef(OmvMmi.contact)
						.setLabel("Contact/Responsible Party")
						.setTooltip("Who is responsible for distribution of the vocabulary, particularly to MMI. " +
								"(In other words, who MMI and the public should contact for more information, or to " +
								"negotiate changes.) This should be a specific person or authorized department. " +
								"Include phone, mail, and URL in parentheses after the name, " +
								"if available: First Last (831-nnn-nnnn, name@domain.com, http://domain.com/myHomePage). " +
								"Note also the next field shows possible roles this person plays with respect to this product.")
						.setExample("Joe Ontology Manager")
					,
					createAttrDef(OmvMmi.contactRole)
						.setLabel("Contact role")
						.setTooltip("What is the role played by the Contact/Responsible Party named above?  " +
								"Choose the most senior authority that applies of: <br/> " +
								"Content Manager: the person/organization that manages the content of the vocabulary <br/>" +
								"Ontology Producer: the person/organization that creates (and possibly serves) this information in an ontology <br/>" +
								"Organizational Manager: the person/organization manager that is responsible overall for " +
								"producing this data product (but does not necessarily produce it themselves) <br/>" +
								"IP Negotiator: the person/organization that handles intellectual property, " +
								"including this vocabulary or ontology <br/>" +
								"Other: specify if none of these fit.")
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
						.setTooltip("Until an agreement is reached on MMI's role, what role is MMI currently playing: <br/>" +
								"Content Manager: MMI actively manages this vocabulary content and is responsible (with the community) for its creation <br/>" +
								"Ontology Producer: MMI accepts vocabulary content from the community and turns it into a served ontology <br/>" +
								"Ontology Republisher: MMI accepts an ontology, caches it, and produces its own copy of the ontology " +
								"in order to provide additional services (URI generation, revision history maintenance and tracking, and so on).")
						.setExample("ontology producer")
						.addOption(new Option("--choose one--"))
						.addOption(new Option("content manager"))
						.addOption(new Option("ontology producer"))
						.addOption(new Option("ontology republisher"))
						.addOption(new Option("other"))
					,
					
					// TODO createAttrDef(OmvMmi.agreedMmiRole),
					
					createAttrDef(OmvMmi.creditRequired)
						.setLabel("Author credit required")
						.setTooltip("Specifies whether users of the ontology have to provide credit to its creator. " +
								"Please choose whatever applies ('no' is a very helpful selection), and enter the next " +
								"field if the select here is 'yes' or 'conditional'. Leave blank if you aren't sure.")
						.setExample("yes")
						.addOption(new Option("not specified"))
						.addOption(new Option("yes"))
						.addOption(new Option("no"))
						.addOption(new Option("conditional"))
					,
					
					// TODO createAttrDef(OmvMmi.creditConditions),
					
					createAttrDef(OmvMmi.creditCitation)
						.setLabel("Citation string")
						.setTooltip("Free text containing the credit language that should be included in works based on this ontology.")
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
						.setTooltip("If the original vocabulary is formally served in an easily parseable way at a URI " +
								"(e.g., using RDF, OWL, or comma- or tab-delimited text entries), please specify the URI here.")
					,
					
					createAttrDef(OmvMmi.origVocDescriptiveName)
						.setLabel("Descriptive name")
						.setTooltip("Descriptive name for the original vocabulary (typically in Dublin Core or Ontology Metadata Vocabulary entries).")
					,
					
					createAttrDef(OmvMmi.origVocVersionId)
						.setLabel("Version")
						.setTooltip("Version identification string associated with original vocabulary (typically in Dublin Core or Ontology Metadata Vocabulary entries).")
					,
					
					createAttrDef(OmvMmi.origVocKeywords)
						.setLabel("Keywords")
						.setTooltip("Keywords specified for original vocabulary (typically in Dublin Core or Ontology Metadata Vocabulary entries).")
					,
					
					createAttrDef(OmvMmi.origVocSyntaxFormat)
						.setLabel("Syntax format")
						.setTooltip("Format/syntax in which vocabulary is provided (one of: RDF, OWL, 'other XML', CSV, tab-delimited, HTML, or other).")
					,
				}
			),
		};
	}

	
	public static AttrGroup[] getAttrGroups() {
		return attrGroups;
	}
	
	public static AttrDef refreshOptions(AttrDef attrDef) {
		String attrUri = attrDef.getUri();
		if ( attrUri.equals(createResourceTypeAttrDef().getUri()) ) {
			MdUtil.readResourceTypes(resourceTypeAttrDef);
			return resourceTypeAttrDef;
		}
		else if ( attrUri.equals(createAuthorityAttrDef().getUri()) ) {
			MdUtil.readAuthorities(authorityAttrDef);
			return authorityAttrDef;
		}
		return attrDef;
	}


	
	////////////////////////////////////////
	/////// equivalences
	
	// map: mmiPropUri -> dcProperty
	private static Map<String,Property> equivalentDc = new HashMap<String,Property>();
	
	// map: mmiPropUri -> dcProperty
	private static Map<String,Property> equivalentMmi = new HashMap<String,Property>();
	
	private static void _addEquivalence(Property mmiProp, Property dcProp) {
		equivalentDc.put(mmiProp.getURI(), dcProp);
		equivalentMmi.put(dcProp.getURI(), mmiProp);
	}
	static {
		_addEquivalence(Omv.hasDomain, DC.subject);
		
		_addEquivalence(Omv.hasCreator, DC.creator);
		_addEquivalence(Omv.description, DC.description);
		_addEquivalence(Omv.creationDate, DC.date);
		_addEquivalence(Omv.hasContributor, DC.contributor);
		_addEquivalence(OmvMmi.origVocUri, DC.source);
		
		// Note: Omv.uri is internally assigned; we cannot use DC.identifier
		// NO --> _addEquivalence(Omv.uri, DC.identifier);
		
		// TODO more DC equivalences?
	}

	
	/**
	 * Gets the DC property that is equivalent to the given MMI property.
	 * @param mmiProp the MMI property
	 * @return the equivalent DC property; null if there is no such object.
	 */
	public static Property getEquivalentDcProperty(Property mmiProp) {
		return equivalentDc.get(mmiProp.getURI());
	}

	
	/**
	 * Gets the MMI property that is equivalent to the given DC property.
	 * @param dcProp the DC property
	 * @return the equivalent MMI property; null if there is no such object.
	 */
	public static Property getEquivalentMmiProperty(Property dcProp) {
		return equivalentMmi.get(dcProp.getURI());
	}

	/**
	 * Gets the DC attributes that have MMI equivalent attributes.
	 */
	public static Collection<Property> getDcPropertiesWithMmiEquivalences() {
		return equivalentDc.values();
	}

	/**
	 * Gets the prefixed name of a property according to the preferred prefixes. 
	 * @param prop
	 * @return
	 */
	public static String prefixedName(Property prop) {
		String ns = preferredPrefix.get(prop.getNameSpace());
		if ( ns == null ) {
			ns = "??";
		}
		return ns+ ":" +prop.getLocalName();
	}
	
}
