package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;

/**
 * Spec of a SPARQL query.
 * 
 * @author Carlos Rueda
 */
public class SparqlQueryInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String query;
	
	private String format;
	
	
	public SparqlQueryInfo() {
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}

	
}
