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
	
}
