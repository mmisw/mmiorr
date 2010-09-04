package org.mmisw.iserver.gwt.client.vocabulary;

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
	private String tooltip;
	

	private boolean required;
	
	private String optionsVocabulary;
	private List<Option> options = null;
	
	private boolean internal;

	private String example = "";

	private int numberOfLines;

	private boolean allowUserDefinedOption;
	
	
	private List<AttrDef> relatedAttrs;
	

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
	
	
	public String toString() {
		return getUri();
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

	/**
	 * @returns the URI of the vocabulary the options are taken from. 
	 */
	public String getOptionsVocabulary() {
		return optionsVocabulary;
	}

	/**
	 * Sets the URI of the vocabulary the options are taken from. 
	 */
	public AttrDef setOptionsVocabulary(String optionsVocabulary) {
		if ( optionsVocabulary == null ) {
			throw new IllegalArgumentException();
		}
		this.optionsVocabulary = optionsVocabulary;
		return this;
	}

	/** @returns the list of options; empty if no options have been assigned. */
	public List<Option> getOptions() {
		if ( options == null ) {
			options = new ArrayList<Option>();
		}
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


	public AttrDef setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}


	public String getTooltip() {
		return tooltip;
	}


	public AttrDef addRelatedAttr(AttrDef relatedAttr) {
		if ( relatedAttrs == null ) {
			relatedAttrs = new ArrayList<AttrDef>();
		}
		relatedAttrs.add(relatedAttr);
		return this;
	}
	
	public List<AttrDef> getRelatedAttrs() {
		return relatedAttrs;
	}

}

