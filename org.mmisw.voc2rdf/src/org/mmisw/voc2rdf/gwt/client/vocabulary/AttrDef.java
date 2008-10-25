package org.mmisw.voc2rdf.gwt.client.vocabulary;

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

	private String example = "";
	

	// no-arg constructor
	public AttrDef() {
	}
	
	
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

	public AttrDef setRequired(boolean required) {
		this.required = required;
		return this;
	}


	public AttrDef setOptions(String[] options) {
		this.options = options;
		return this;
	}


	public AttrDef setInternal(boolean internal) {
		this.internal = internal;
		return this;
	}


	public String getNameSpace() {
		return nameSpace;
	}

	
	public String getExample() {
		return example;
	}


	public AttrDef setExample(String example) {
		this.example = example;
		return this;
	}


}

