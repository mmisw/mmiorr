package org.mmisw.ontmd.gwt.client.rpc;

import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Provides the main elements used to create the attributes to
 * be captured.
 * 
 * @author Carlos Rueda
 */
public class BaseInfo implements IsSerializable {
	
	private AttrGroup[] attrGroups = {};
	
	private String shortNameUri;
	
	public BaseInfo() {
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
	 * (not very elegant by time contrains force quick solutions!)
	 */
	public String getShortNameUri() {
		return shortNameUri;
	}
	public void setShortNameUri(String shortNameUri) {
		this.shortNameUri = shortNameUri;
	}
	

}
