package org.mmisw.voc2rdf.gwt.client.vocabulary;

import java.io.Serializable;

/**
 * 
 * @author Carlos Rueda
 */
public class Option implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String label;
	
	public Option() {
	}

	public Option(String name) {
		this(name, name);
	}

	public Option(String name, String label) {
		super();
		this.name = name;
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public String getLabel() {
		return label;
	}
}
