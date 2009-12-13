package org.mmisw.ont.sparql;

/**
 * Represents the result of a query.
 * 
 * @author Carlos Rueda
 */
public class QueryResult {
	private String result;
	private String contentType;
	private boolean isEmpty;

	public void setResult(String result) {
		this.result = result;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getResult() {
		return result;
	}

	public String getContentType() {
		return contentType;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setIsEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}
	
}