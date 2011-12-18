package org.mmisw.ont.triplestore.allegro4.test;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.ont.client.util.HttpUtil.HttpResponse;

public class UpdateTest extends Common {

	public static void main(String[] args) throws Exception {
		
		String graph = "<bb:mygraph>";

//		_doUpdate(
//				"delete { ?s ?p ?o . } " +
//				"where  { ?s ?p ?o . }"
//		);
//
//		_doUpdate("drop graph " + graph);

//		_doUpdate("clear graph " + graph);
		
//		_doUpdate("create graph " + graph);
		
//		_doUpdate(
//				"insert data {  " +
//				"<bb:taller> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#TransitiveProperty> . " +
//				" }"
//		);

//		_doUpdate("insert data { " +
//			"graph " + graph + " { " +
//				"<bb:carlos> <bb:taller> <bb:calvin> . " +
//				"<bb:calvin> <bb:taller> <bb:hobbes> . " +
//			" } " +
//			"}"
//		);
		
		_doUpdate(
				"with " + graph + " " +
				"delete { <bb:carlos> ?p ?o . } " +
				"where  { <bb:carlos> ?p ?o . }"
		);
		
	}

	
	private static void _doUpdate(String update) throws Exception {
		System.out.println("== UPDATE test ==");
		System.out.println("update: " + update);
		
		String urlRequest = Common.TRIPLE_STORE_URL;
		
		final String encUpdate = URLEncoder.encode(update, "UTF-8");
		
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("update", encUpdate);
		
		urlRequest += "?update=" + encUpdate;
		
		System.out.println("Making request...");
		HttpResponse httpResponse = HttpUtil.httpPost(urlRequest, vars);
		System.out.println("httpResponse = " + httpResponse);
	}
	
}
