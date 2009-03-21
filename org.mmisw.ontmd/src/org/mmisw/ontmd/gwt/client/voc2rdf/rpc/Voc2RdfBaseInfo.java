package org.mmisw.ontmd.gwt.client.voc2rdf.rpc;

import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Some base information.
 * 
 * @author Carlos Rueda
 */
public class Voc2RdfBaseInfo implements IsSerializable {
	
	private AttrDef resourceTypeAttrDef;
	
	
	public Voc2RdfBaseInfo() {
	}


	public void setResourceTypeAttrDef(AttrDef resourceTypeAttrDef) {
		this.resourceTypeAttrDef = resourceTypeAttrDef;
	}


	public AttrDef getResourceTypeAttrDef() {
		return resourceTypeAttrDef;
	}
	
	
}
