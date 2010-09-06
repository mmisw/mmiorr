package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

/**
 * Spec of a SPARQL query.
 * 
 * @author Carlos Rueda
 */
public class SparqlQueryInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String endPoint;
	
	private String query;
	
	private String format;
	
	private String[] acceptEntries;
	
	private boolean parseResult;
	
	
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


	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}


	public String getEndPoint() {
		return endPoint;
	}


	public void setAcceptEntries(String... acceptEntries) {
		this.acceptEntries = acceptEntries;
	}

	/** never null */
	public String[] getAcceptEntries() {
		if ( acceptEntries == null ) {
			acceptEntries = new String[0];
		}
		return acceptEntries;
	}


	public void setParseResult(boolean parseResult) {
		this.parseResult = parseResult;
	}


	public boolean isParseResult() {
		return parseResult;
	}

	
}
