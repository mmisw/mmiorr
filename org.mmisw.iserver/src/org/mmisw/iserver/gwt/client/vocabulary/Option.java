package org.mmisw.iserver.gwt.client.vocabulary;

import java.io.Serializable;

/**
 * 
 * @author Carlos Rueda
 */
public class Option implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String label;
	private String uri;
	
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
}
