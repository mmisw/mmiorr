package org.mmisw.voc2rdf.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ColumnSeparator implements IsSerializable {

	private String name;
	private String label;
	
	public ColumnSeparator() {
	}

	ColumnSeparator(String name, String label) {
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
