package org.mmisw.iserver.gwt.client.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Info about the result of performing a query
 * 
 * @author Carlos Rueda
 */
public class SparqlQueryResult extends BaseResult {
	private static final long serialVersionUID = 1L;
	
	
	public static class ParsedResult extends BaseResult {
		private static final long serialVersionUID = 1L;
		
		public List<String> keys = new ArrayList<String>();
		public List<Map<String,String>> values = new  ArrayList<Map<String,String>>();
	}

	private String query;
	private String result;
	
	private ParsedResult parsedResult;
	
	
	public SparqlQueryResult() {
	}

	
	public String toString() {
		return "SparqlQueryResult{query=" +query+ ", error=" +error+ "}";
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


	public void setParsedResult(ParsedResult parsedResult) {
		this.parsedResult = parsedResult;
	}


	public ParsedResult getParsedResult() {
		return parsedResult;
	}


	
}
