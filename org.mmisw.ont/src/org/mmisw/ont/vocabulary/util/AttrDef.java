package org.mmisw.ont.vocabulary.util;

import java.io.Serializable;


/**
 * An association of certain attributes to a given Jena property.
 * 
 * @author Carlos Rueda
 */
public class AttrDef implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String uri;
	private String nameSpace;
	private String localName;
	private boolean required;
	private String[] options;
	private boolean internal;

	
	public AttrDef(String propUri, String nameSpace, String propLocalName) {
		this.uri = propUri;
		this.nameSpace = nameSpace;
		this.localName = propLocalName;
	}
	
	
	public String getUri() {
		return uri;
	}

	public String getLocalName() {
		return localName;
	}

	public boolean isRequired() {
		return required;
	}

	public String[] getOptions() {
		return options;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}


	public void setOptions(String[] options) {
		this.options = options;
	}


	public void setInternal(boolean internal) {
		this.internal = internal;
	}


	public String getNameSpace() {
		return nameSpace;
	}

}

