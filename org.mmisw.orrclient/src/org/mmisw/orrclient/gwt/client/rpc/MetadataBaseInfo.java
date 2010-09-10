package org.mmisw.orrclient.gwt.client.rpc;

import java.util.Map;

import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;
import org.mmisw.orrclient.gwt.client.vocabulary.AttrGroup;


/**
 * Provides the main elements used to create the attributes to
 * be captured.
 * 
 * @author Carlos Rueda
 */
@SuppressWarnings("serial")
public class MetadataBaseInfo extends Errorable {
	
	private AttrGroup[] attrGroups = {};
	
	private String resourceTypeUri;
	private String authorityAbbreviationUri;
	private AttrDef authorityAttrDef;
	
	private AttrDef resourceTypeAttrDef;
	
	private Map<String,AttrDef> uriAttrDefMap;
	
	
	public MetadataBaseInfo() {
	}
	
	/**
	 * Gets the metadata groups.
	 * @return the metadata groups.
	 */
	public AttrGroup[] getAttrGroups() {
		return attrGroups;
	}
	
	public void setAttrGroups(AttrGroup[] attrGroups) {
		this.attrGroups = attrGroups;
	}
	
	
	/**
	 *  NOTE: this attribute has a special handling in the GUI
	 */
	public String getResourceTypeUri() {
		return resourceTypeUri;
	}
	public void setResourceTypeUri(String resourceTypeUri) {
		this.resourceTypeUri = resourceTypeUri;
	}
	
	/**
	 *  NOTE: this attribute has a special handling in the GUI
	 * @return the authorityAbbreviationUri
	 */
	public String getAuthorityAbbreviationUri() {
		return authorityAbbreviationUri;
	}
	public void setAuthorityAbbreviationUri(String authorityAbbreviationUri) {
		this.authorityAbbreviationUri = authorityAbbreviationUri;
	}

	public void setAuthorityAttrDef(AttrDef authorityAttrDef) {
		this.authorityAttrDef = authorityAttrDef;
	}

	public AttrDef getAuthorityAttrDef() {
		return authorityAttrDef;
	}

	public AttrDef getResourceTypeAttrDef() {
		return resourceTypeAttrDef;
	}

	public void setResourceTypeAttrDef(AttrDef resourceTypeAttrDef) {
		this.resourceTypeAttrDef = resourceTypeAttrDef;
	}

	public Map<String, AttrDef> getUriAttrDefMap() {
		return uriAttrDefMap;
	}

	public void setUriAttrDefMap(Map<String, AttrDef> uriAttrDefMap) {
		this.uriAttrDefMap = uriAttrDefMap;
	}

	
}
