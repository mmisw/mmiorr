package org.mmisw.orrportal.gwt.client.portal.md;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.HostingType;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.FieldWithChoose;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;

/**
 * 
 * @author Carlos Rueda
 */
public class MetadataSection1 extends MetadataSection {

	private static Map<String, AttrDef> uriAttrDefMap = Orr.getMetadataBaseInfo().getUriAttrDefMap();
	
	private static AttrDef _getAttrDef(String uri) {
		AttrDef attrDef = uriAttrDefMap.get(uri);
		assert attrDef != null;
		return attrDef;
	}
	
	private AttrDef resourceTypeAttrDef =     Orr.getMetadataBaseInfo().getResourceTypeAttrDef();
	private AttrDef authorityAttrDef =        _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origMaintainerCode");
	private AttrDef fullTitleAttrDef =        _getAttrDef("http://omv.ontoware.org/2005/05/ontology#name");
	private AttrDef acronymAttrDef =          _getAttrDef("http://omv.ontoware.org/2005/05/ontology#acronym");
	private AttrDef contentCreatorAttrDef =   _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/hasContentCreator");
	private AttrDef ontologyCreatorAttrDef =  _getAttrDef("http://omv.ontoware.org/2005/05/ontology#hasCreator");
	private AttrDef briefDescriptionAttrDef = _getAttrDef("http://omv.ontoware.org/2005/05/ontology#description");
	private AttrDef keywordsAttrDef =         _getAttrDef("http://omv.ontoware.org/2005/05/ontology#keywords");
	private AttrDef linkOrigVocabAttrDef =    _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocUri");
	private AttrDef linkDocAttrDef =          _getAttrDef("http://omv.ontoware.org/2005/05/ontology#documentation");
	private AttrDef contributorsAttrDef =     _getAttrDef("http://omv.ontoware.org/2005/05/ontology#hasContributor");
	private AttrDef referenceAttrDef =        _getAttrDef("http://omv.ontoware.org/2005/05/ontology#reference");
	

	
	public MetadataSection1(HostingType hostingType) {
		super();
		
		preamble = "<b>General information.</b> " +
				"These fields capture general information about this ontology, who created it, and where it came from. " +
				COMMON_INFO
		;

		List<AttrDef> list = new ArrayList<AttrDef>();

		if ( hostingType != HostingType.FULLY_HOSTED ) {
			list.add(authorityAttrDef);
		}
		list.add(resourceTypeAttrDef);
		list.add(fullTitleAttrDef);
		list.add(acronymAttrDef);
		list.add(contentCreatorAttrDef);
		list.add(ontologyCreatorAttrDef);
		list.add(briefDescriptionAttrDef);
		list.add(keywordsAttrDef);
		list.add(linkOrigVocabAttrDef);
		list.add(linkDocAttrDef);
		list.add(contributorsAttrDef);
		list.add(referenceAttrDef);

		attrDefs = list.toArray(new AttrDef[list.size()]);

		createElements();
		createForm();
	}

	protected void createElements() {
		for (AttrDef attrDef : attrDefs ) {
			if ( attrDef == null ) {
				continue;
			}
			Elem elem;
			if ( attrDef == resourceTypeAttrDef ) {
				elem = new Elem(attrDef, new FieldWithChoose(resourceTypeAttrDef, cl, "250px"));
			}
			else {
				elem = new Elem(attrDef, OrrUtil.createTextBoxBase(attrDef.getNumberOfLines(), "450px", cl));
			}
			addElem(elem);
		}
	}

}
