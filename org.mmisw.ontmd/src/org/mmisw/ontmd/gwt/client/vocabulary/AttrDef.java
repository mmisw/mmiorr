package org.mmisw.ontmd.gwt.client.vocabulary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


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
	
	private String label;
	

	private boolean required;
	private List<Option> options = null;
	private boolean internal;

	private String example = "";

	private int numberOfLines;

	private boolean allowUserDefinedOption;
	

	public int getNumberOfLines() {
		return numberOfLines;
	}


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

	public String getLabel() {
		return label;
	}


	public AttrDef setLabel(String label) {
		this.label = label;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	/** @returns the list of options; null if no options */
	public List<Option> getOptions() {
		return options;
	}

	public boolean isInternal() {
		return internal;
	}

	public AttrDef setRequired(boolean required) {
		this.required = required;
		return this;
	}


	public AttrDef addOption(Option option) {
		if ( options == null ) {
			options = new ArrayList<Option>();
		}
		options.add(option);
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


	public AttrDef setNumberOfLines(int numberOfLines) {
		this.numberOfLines = numberOfLines;
		return this;
	}


	public AttrDef setAllowUserDefinedOption(boolean allowUserDefinedOption) {
		this.allowUserDefinedOption = allowUserDefinedOption;
		return this;
	}


	public boolean isAllowUserDefinedOption() {
		return allowUserDefinedOption;
	}


}

