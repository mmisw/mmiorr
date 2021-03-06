package org.mmisw.orrportal.gwt.client.portal.md;

import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.Option;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.FieldWithChoose;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;

/**
 * 
 * @author Carlos Rueda
 */
public class MetadataSection3 extends MetadataSection {

	private static Map<String, AttrDef> uriAttrDefMap = Orr.getMetadataBaseInfo().getUriAttrDefMap();
	
	private static AttrDef _getAttrDef(String uri) {
		AttrDef attrDef = uriAttrDefMap.get(uri);
		assert attrDef != null;
		return attrDef;
	}
	
	private AttrDef origVocDocUriAttrDef =          _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocDocumentationUri");
	private AttrDef origVocDescriptiveNameAttrDef = _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocDescriptiveName");
	private AttrDef origVocVersionIdAttrDef =       _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocVersionId");
	private AttrDef origVocKeywordsAttrDef =        _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocKeywords");
	private AttrDef origVocSyntaxFormatAttrDef =    _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocSyntaxFormat");
	

	
	public MetadataSection3() {
		super();
		
		preamble = "<b>Original source.</b> " +
				"The fields in this section capture specific metadata that was documented in the original vocabulary, so you can see how it was originally documented. " +
				COMMON_INFO
		;
		
		attrDefs = new AttrDef[] {
				origVocDocUriAttrDef,
				origVocDescriptiveNameAttrDef,
				origVocVersionIdAttrDef,
				origVocKeywordsAttrDef,
				origVocSyntaxFormatAttrDef,
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
			
			final List<Option> options = attrDef.getOptions();
			
			final String optionsVocabulary = attrDef.getOptionsVocabulary();
			
			// Bofore, the following section was conditioned by: 
//			if ( options.size() > 0 ) {
			// and now by:
			if ( optionsVocabulary != null || options.size() > 0 ) {
				
				boolean allowUserOption = attrDef.isAllowUserDefinedOption();
				if ( allowUserOption ) {
					elem = new Elem(attrDef, new FieldWithChoose(attrDef, cl, "250px"));
				}
				else {
					elem = new Elem(attrDef, OrrUtil.createListBox(options, cl));
				}
			}
			else {
				elem = new Elem(attrDef, OrrUtil.createTextBoxBase(attrDef.getNumberOfLines(), "450px", cl));
			}
			addElem(elem);
		}
	}

}
