package org.mmisw.ont.vocabulary.util;

import java.io.Serializable;


/**
 * A group of Jena properties.
 * 
 * @author Carlos Rueda
 */
public class AttrGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private AttrDef[] attrDefs;
	
	public AttrGroup(String name, AttrDef[] attrs) {
		super();
		this.name = name;
		this.attrDefs = attrs;
	}

	public String getName() {
		return name;
	}

	public AttrDef[] getAttrDefs() {
		return attrDefs;
	}


}

