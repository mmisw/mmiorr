package org.mmisw.iserver.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.JenaUtil2;
import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.iserver.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;

/**
 * Helper to extract metadata from an ontology model.
 * @author Carlos Rueda
 */
class MetadataExtractor {
	private MetadataExtractor() {}
	
	
	private static final Log log = LogFactory.getLog(MetadataExtractor.class);
	
	/**
	 * Does the preparation by reading the model from the given URI.
	 * @param uriModel URI of the model to be loaded
	 * @param ontologyInfoPre  The object to be completed
	 * @return
	 */
	static String prepareOntologyMetadata(
			MetadataBaseInfo metadataBaseInfo,
			OntModel model, BaseOntologyInfo ontologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("prepareOntologyMetadata: ontologyUri=" +ontologyInfo.getUri());
		}
		
		OntologyMetadata ontologyMetadata = ontologyInfo.getOntologyMetadata();

		Resource ontRes = JenaUtil.getFirstIndividual(model, OWL.Ontology);
		
		StringBuilder moreDetails = new StringBuilder();
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		Map<String,String> originalValues = new HashMap<String, String>();
		
		if ( ontRes != null ) {
			//
			// Get values from the existing ontology resource
			//
			for ( AttrGroup attrGroup : metadataBaseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					
					// get value of MMI property:
					Property mmiProp = uriPropMap.get(attrDef.getUri());
					String prefixedMmi = MdHelper.prefixedName(mmiProp);
					String value = JenaUtil.getValue(ontRes, mmiProp);
					
					// DC equivalent, which is obtained if necessary
					Property dcProp = null;
					
					if (value == null) {
						// try a DC equivalent to use:
						dcProp = MdHelper.getEquivalentDcProperty(mmiProp);
						if ( dcProp != null ) {
							value = JenaUtil.getValue(ontRes, dcProp);
						}
					}
					
					if ( value != null ) {
						
						// get value:
						if ( log.isDebugEnabled() ) {
							log.debug("Assigning: " +attrDef.getUri()+ " = " + value);
						}
						originalValues.put(attrDef.getUri(), value);
						
						// Special case: Omv.acronym/OmvMmi.shortNameUri  
						if ( Omv.acronym.getURI().equals(attrDef.getUri()) ) {
							// add also the value of OmvMmi.shortNameUri:
							String shortNameValue = JenaUtil.getValue(ontRes, OmvMmi.shortNameUri);
							if ( log.isDebugEnabled() ) {
								log.debug("Also assigning " +OmvMmi.shortNameUri.getURI()+ " = " +shortNameValue);
							}
							originalValues.put(OmvMmi.shortNameUri.getURI(), shortNameValue);
						}
						
						

						// add detail:
						if ( dcProp != null ) {
							String prefixedDc = MdHelper.prefixedName(dcProp);
							_addDetail(moreDetails, prefixedMmi, "not present", "Will use " +prefixedDc);
						}
						else {
							_addDetail(moreDetails, prefixedMmi, "present", " ");
						}
					}
					else {
						if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
							if ( dcProp != null ) {
								String prefixedDc = MdHelper.prefixedName(dcProp);
								_addDetail(moreDetails, prefixedMmi, "not present", "and " +prefixedDc+ " not present either");
							}	
							else {
								_addDetail(moreDetails, prefixedMmi, "not present", " not equivalent DC");
							}
						}
					}
				}
			}
		}
		else {
			//
			// No ontology resource. Check required attributes to report in the details:
			//
			for ( AttrGroup attrGroup : metadataBaseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
						Property mmiProp = uriPropMap.get(attrDef.getUri());
						String prefixedMmi = MdHelper.prefixedName(mmiProp);
						_addDetail(moreDetails, prefixedMmi, "not present", "required");
					}
				}
			}
		}
		
		// add the new details if any:
		if ( moreDetails.length() > 0 ) {
			String details = ontologyMetadata.getDetails();
			if ( details == null ) {
				ontologyMetadata.setDetails(moreDetails.toString());
			}
			else {
				ontologyMetadata.setDetails(details + "\n" +moreDetails.toString());
			}
		}
		
		ontologyMetadata.setOriginalValues(originalValues);
		
		// associate the original base URI:
		String uri = model.getNsPrefixURI("");
		if ( uri != null ) {
			String base_ = JenaUtil2.removeTrailingFragment(uri);
			ontologyInfo.setUri(base_);
		}

		// OK:
		return null;
	}
	

	private static void _addDetail(StringBuilder details, String a1, String a2, String a3) {
		String str = a1 + "|" + a2 + "|" + a3; 
		log.info(str);
		details.append(str + "\n");
	}
	

}
