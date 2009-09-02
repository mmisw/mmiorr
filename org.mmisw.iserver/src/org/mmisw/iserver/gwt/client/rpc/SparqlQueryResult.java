package org.mmisw.iserver.gwt.client.rpc;


/**
 * Info about the result of performing a query
 * 
 * @author Carlos Rueda
 */
public class SparqlQueryResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	
	private String query;
	private String result;
	
	public SparqlQueryResult() {
	}

	
	public String toString() {
		return "CreateVocabularyResult{query=" +query+ ", error=" +error+ "}";
	}


	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}


	
}
