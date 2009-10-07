package org.mmisw.ontmd.gwt.client.portal.md;

import java.util.Map;

import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.util.FieldWithChoose;
import org.mmisw.ontmd.gwt.client.util.Util;

/**
 * 
 * @author Carlos Rueda
 */
public class MetadataSection1 extends MetadataSection {

	private static Map<String, AttrDef> uriAttrDefMap = Main.getMetadataBaseInfo().getUriAttrDefMap();
	
	private static AttrDef _getAttrDef(String uri) {
		AttrDef attrDef = uriAttrDefMap.get(uri);
		assert attrDef != null;
		return attrDef;
	}
	
	private AttrDef resourceTypeAttrDef =     _getAttrDef("http://omv.ontoware.org/2005/05/ontology#acronym");
	private AttrDef fullTitleAttrDef =        _getAttrDef("http://omv.ontoware.org/2005/05/ontology#name");
	private AttrDef contentCreatorAttrDef =   _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/hasContentCreator");
	private AttrDef ontologyCreatorAttrDef =  _getAttrDef("http://omv.ontoware.org/2005/05/ontology#hasCreator");
	private AttrDef briefDescriptionAttrDef = _getAttrDef("http://omv.ontoware.org/2005/05/ontology#description");
	private AttrDef keywordsAttrDef =         _getAttrDef("http://omv.ontoware.org/2005/05/ontology#keywords");
	private AttrDef linkOrigVocabAttrDef =    _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocUri");
	private AttrDef linkDocAttrDef =          _getAttrDef("http://omv.ontoware.org/2005/05/ontology#documentation");
	private AttrDef contributorsAttrDef =     _getAttrDef("http://omv.ontoware.org/2005/05/ontology#hasContributor");
	private AttrDef referenceAttrDef =        _getAttrDef("http://omv.ontoware.org/2005/05/ontology#reference");
	

	
	public MetadataSection1() {
		super();
		
		preamble = "<b>General information.</b> " +
				"These fields capture general information about this ontology, who created it, and where it came from. " +
				COMMON_INFO
		;
		
		attrDefs = new AttrDef[] {
				resourceTypeAttrDef,
				fullTitleAttrDef,
				contentCreatorAttrDef,
				ontologyCreatorAttrDef,
				briefDescriptionAttrDef,
				keywordsAttrDef,
				linkOrigVocabAttrDef,
				linkDocAttrDef,
				contributorsAttrDef,
				referenceAttrDef,
		};
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
				elem = new Elem(attrDef, new FieldWithChoose(resourceTypeAttrDef, cl, "200px"));
			}
			else {
				elem = new Elem(attrDef, Util.createTextBoxBase(attrDef.getNumberOfLines(), "350px", cl));
			}
			addElem(elem);
		}
	}

}
