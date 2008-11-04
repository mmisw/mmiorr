package org.mmisw.voc2rdf.gwt.client.rpc;

import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Some base information.
 * 
 * @author Carlos Rueda
 */
public class BaseInfo implements IsSerializable {
	
	private AttrDef mainClass;
	
	
	public BaseInfo() {
	}


	public void setMainClassAttrDef(AttrDef mainClass) {
		this.mainClass = mainClass;
	}


	public AttrDef getMainClassAttrDef() {
		return mainClass;
	}
	
	
}
