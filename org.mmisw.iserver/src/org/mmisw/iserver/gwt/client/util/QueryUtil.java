package org.mmisw.iserver.gwt.client.util;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult.ParsedResult;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Provides client-side utilities related with queries.
 * 
 * @author Carlos Rueda
 */
public class QueryUtil {
	
	/**
	 * Parses a query result.
	 * 
	 * @param query
	 * @param sqResult
	 */
	public static void parseResult(SparqlQueryInfo query, SparqlQueryResult sqResult) {
		ParsedResult parsedResult = new ParsedResult();
		sqResult.setParsedResult(parsedResult);
		
		if ( ! query.getFormat().equalsIgnoreCase("json") ) {
			String error = "parse result only available for 'json' output format. " +
						"Given format was: " +query.getFormat();
			parsedResult.setError(error);
			return;
		}
		
		JSONValue jsonValue = JSONParser.parse(sqResult.getResult());
	
		JSONArray vars = jsonValue.isObject().get("head").isObject().get("vars").isArray();
		for ( int i = 0, cnt = vars.size(); i < cnt; i++ ) {
			String key = valueOfNoQuotes(vars.get(i));
			parsedResult.keys.add(key);
		}
		
		JSONArray bindings = jsonValue.isObject().get("results").isObject().get("bindings").isArray();
		
		for ( int i = 0, cnt = bindings.size(); i < cnt; i++ ) {
			JSONObject tuple = bindings.get(i).isObject();
			Map<String,String> record = new HashMap<String,String>();
			for ( String key : parsedResult.keys ) {
				JSONValue val = tuple.get(key);
				// the following check because there may be OPTIONAL variables with no value
				String value = val == null ? "" : valueOfNoQuotes(val.isObject().get("value"));
				record.put(key, value);
			}
			parsedResult.values.add(record);
		}
	}
	
	private static String valueOfNoQuotes(Object obj) {
		return String.valueOf(obj).replaceAll("^\"+|\"+$", "");
	}
	

}
