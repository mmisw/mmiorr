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
public class MetadataSection2 extends MetadataSection {

	private static Map<String, AttrDef> uriAttrDefMap = Orr.getMetadataBaseInfo().getUriAttrDefMap();
	
	private static AttrDef _getAttrDef(String uri) {
		AttrDef attrDef = uriAttrDefMap.get(uri);
		assert attrDef != null;
		return attrDef;
	}
	
	private AttrDef origVocManagerAttrDef =   _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origVocManager");
	private AttrDef contactAttrDef =          _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/contact");
	private AttrDef contactRoleAttrDef =      _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/contactRole");
	private AttrDef temporaryMmiRoleAttrDef = _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/temporaryMmiRole");
	private AttrDef creditRequiredAttrDef =   _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/creditRequired");
	private AttrDef creditCitationAttrDef =   _getAttrDef("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/creditCitation");
	

	
	public MetadataSection2() {
		super();
		
		preamble = "<b>Usage/License/Permissions.</b> " +
				"The Usage, License, and Permissions fields help keep track of how we obtained this vocabulary (and know it is OK to serve to others), and on what terms other people can use it. " +
				COMMON_INFO
		;
		
		attrDefs = new AttrDef[] {
				origVocManagerAttrDef,
				contactAttrDef,
				contactRoleAttrDef,
				temporaryMmiRoleAttrDef,
				creditRequiredAttrDef,
				creditCitationAttrDef,
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
			
			if ( options.size() > 0 ) {
				
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
