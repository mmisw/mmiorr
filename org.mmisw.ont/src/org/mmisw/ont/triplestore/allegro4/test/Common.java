package org.mmisw.ont.triplestore.allegro4.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Common {
	public static final String TRIPLE_STORE_URL = "http://mmi2.shore.mbari.org:10035/repositories/mmiorr";
	
	public static void _showJson(String str) throws Exception {
		JSONTokener jsonParser = new JSONTokener(str);
		JSONObject jsonObj = new JSONObject(jsonParser);
		System.out.println("JSON:");
		System.out.println(jsonObj);
		JSONArray names = jsonObj.getJSONArray("names");
		JSONArray values = jsonObj.getJSONArray("values");
		System.out.println(" names: " + names);
		System.out.println(" values: " + values);

	}


}
