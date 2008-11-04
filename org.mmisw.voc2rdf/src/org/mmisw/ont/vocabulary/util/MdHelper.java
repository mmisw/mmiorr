package org.mmisw.ont.vocabulary.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;
import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrDef;
import org.mmisw.voc2rdf.gwt.client.vocabulary.Option;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * Handles the metadata attributes for the ontologies stored in the
 * MMI Registry.
 * 
 * <p>
 * TODO Unify similar classes in voc2rdf and ontmd !!
 * 
 * @author Carlos Rueda
 */
public class MdHelper {
	private  MdHelper() {}

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
	

	
	private static AttrDef mainClassAttrDef =
			createAttrDef(Omv.acronym, true) 
		    .setLabel("Resource type")
			.setExample("parameter")
			//.setAllowUserDefinedOption(true) NO NEED for this in Voc2RDF -- TODO Unify!
			.addOption(new Option("Discipline", "ISO MD_Keyword: Discipline"))
			.addOption(new Option("Place", "ISO MD_Keyword: Place"))
			.addOption(new Option("Stratum", "ISO MD_Keyword: Stratum"))
			.addOption(new Option("Temporal", "ISO MD_Keyword: Temporal"))
			.addOption(new Option("Theme", "ISO MD_Keyword: Theme"))
			
			.addOption(new Option("axis", "OGC Object Type: axis"))
			.addOption(new Option("axisDirection", "OGC Object Type: axisDirection"))
			.addOption(new Option("coordinateOperation", "OGC Object Type: coordinateOperation"))
			.addOption(new Option("crs", "OGC Object Type: crs"))
			.addOption(new Option("cs", "OGC Object Type: cs"))
			.addOption(new Option("datum", "OGC Object Type: datum"))
			.addOption(new Option("dataType", "OGC Object Type: dataType"))
			.addOption(new Option("derivedCRSType", "OGC Object Type: derivedCRSType"))
			.addOption(new Option("documentType", "OGC Object Type: documentType"))
			.addOption(new Option("ellipsoid", "OGC Object Type: ellipsoid"))
			.addOption(new Option("featureType", "OGC Object Type: featureType"))
			.addOption(new Option("group", "OGC Object Type: group"))
			.addOption(new Option("meaning", "OGC Object Type: meaning"))
			.addOption(new Option("meridian", "OGC Object Type: meridian"))
			.addOption(new Option("method", "OGC Object Type: method"))
			.addOption(new Option("nil", "OGC Object Type: nil"))
			.addOption(new Option("parameter", "OGC Object Type: parameter"))
			.addOption(new Option("phenomenon", "OGC Object Type: phenomenon"))
			.addOption(new Option("pixelInCell", "OGC Object Type: pixelInCell"))
			.addOption(new Option("rangeMeaning", "OGC Object Type: rangeMeaning"))
			.addOption(new Option("referenceSystem", "OGC Object Type: referenceSystem"))
			.addOption(new Option("uom", "OGC Object Type: uom"))
			.addOption(new Option("verticalDatumType", "OGC Object Type: verticalDatumType"))
			
			.addOption(new Option("keyword", "Other: keyword"))
			.addOption(new Option("parameter", "Other: parameter"))
			.addOption(new Option("unit", "Other: unit"))
			.addOption(new Option("organization", "Other: organization"))
			.addOption(new Option("platform", "Other: platform"))
			.addOption(new Option("sensor", "Other: sensor"))
			.addOption(new Option("process", "Other: process"))
			.addOption(new Option("missingFlag", "Other: missingFlag"))
			.addOption(new Option("qualityFlag", "Other: qualityFlag"))
			.addOption(new Option("qcCategory", "Other: qcCategory"))
			.addOption(new Option("coordinateReference", "Other: coordinateReference"))
			.addOption(new Option("datum", "Other: datum"))
			.addOption(new Option("protocol", "Other: protocol"))
			.addOption(new Option("metadataStandard", "Other: metadataStandard"))
			.addOption(new Option("featureType", "Other: featureType"))
			.addOption(new Option("featureName", "Other: featureName"))
			.addOption(new Option("speciesType", "Other: speciesType"))
			.addOption(new Option("speciesName", "Other: speciesName"))
			.addOption(new Option("discipline", "Other: discipline"))
			.addOption(new Option("place", "Other: place"))
			.addOption(new Option("theme", "Other: theme"))
			.addOption(new Option("roleOfContact", "Other: roleOfContact"))
			.addOption(new Option("general", "Other: general metadata attribute"))
		;
	
//   OLD list
//			createAttrDef(Omv.acronym, true) 
//			.setLabel("Main theme")
//			.setExample("parameter")
//			.addOption(new Option("parameter", "Parameters (It will include terms like 'sea surface salinity')"))
//			.addOption(new Option("sensorType", "Sensor types (It will include terms like 'Thermometer')"))
//			.addOption(new Option("platformType", "Platform types (It will include terms like 'Mooring')"))
//			.addOption(new Option("unit", "Units  (It will include terms like 'meter')"))
//			.addOption(new Option("keyword", "Keywords  (It will include terms like 'climate', 'oceans')"))
//			.addOption(new Option("organization", "Organizations  (It will include terms like 'MBARI' or 'MMI')"))
//			.addOption(new Option("process", "Processes  (It will include terms like 'data quality control')"))
//			.addOption(new Option("missingflag", "Missing flags  (It will include terms like '-999')"))
//			.addOption(new Option("qualityflag", "Quality flags  (It will include terms like '10')"))
//			.addOption(new Option("featureType", "Feature types  (It will include terms like 'body of water')"))
//			.addOption(new Option("GeographicFeature", "Geographic features  (It will include terms like 'Monterey Bay')"))
//			;
	
	
	public static AttrDef getMainClassAttrDef() {
		return mainClassAttrDef;
	}
}
