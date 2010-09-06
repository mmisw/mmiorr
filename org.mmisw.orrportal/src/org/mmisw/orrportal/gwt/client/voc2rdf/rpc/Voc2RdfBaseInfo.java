package org.mmisw.orrportal.gwt.client.voc2rdf.rpc;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Some base information.
 * 
 * @author Carlos Rueda
 */
@Deprecated
public class Voc2RdfBaseInfo implements IsSerializable {
	
	private AttrDef resourceTypeAttrDef;
	
	// map: String -> AttrDef
	private Map<String,AttrDef> attrDefMap = new HashMap<String,AttrDef>();

	
	public Voc2RdfBaseInfo() {
	}


	public void setResourceTypeAttrDef(AttrDef resourceTypeAttrDef) {
		this.resourceTypeAttrDef = resourceTypeAttrDef;
	}


	public AttrDef getResourceTypeAttrDef() {
		return resourceTypeAttrDef;
	}


	public Map<String, AttrDef> getAttrDefMap() {
		return attrDefMap;
	}


	public void setAttrDefMap(Map<String, AttrDef> attrDefMap) {
		this.attrDefMap = attrDefMap;
	}
	
	
}
