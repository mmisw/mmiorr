package org.mmisw.voc2rdf.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines an attribute to be captured. Includes convenience information
 * like tooltip and example value.
 * 
 * @author Carlos Rueda
 */
public class Attribute implements IsSerializable {

	private String name;
	private String label;
	private String tooltip;
	private boolean required;
	
	private String example;
	
	public Attribute() {
	}

	Attribute(String name, String label) {
		this(name, label, "", "");
	}

	Attribute(String name, String label, String tootip) {
		this(name, label, tootip, "");
	}

	Attribute(String name, String label, String tootip, String example) {
		super();
		this.name = name;
		this.label = label;
		this.tooltip = tootip;
		this.example = example;
		this.required = true;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public String getTooltip() {
		return tooltip;
	}
	
	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}


}
