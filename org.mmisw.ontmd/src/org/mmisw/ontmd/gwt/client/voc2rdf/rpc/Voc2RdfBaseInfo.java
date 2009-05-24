package org.mmisw.ontmd.gwt.client.voc2rdf.rpc;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Some base information.
 * 
 * @author Carlos Rueda
 */
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
