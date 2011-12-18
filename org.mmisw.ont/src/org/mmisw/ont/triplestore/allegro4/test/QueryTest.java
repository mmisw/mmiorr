package org.mmisw.ont.triplestore.allegro4.test;

import java.net.URLEncoder;

import org.mmisw.ont.client.util.HttpUtil;
import org.mmisw.ont.client.util.HttpUtil.HttpResponse;

public class QueryTest extends Common {

	public static void main(String[] args) throws Exception {

		_select();
	}

	private static void _select() throws Exception {
		final String query = "select ?s ?p ?o where {?s ?p ?o.} limit 20";

		System.out.println("== SELECT test ==");
		System.out.println("query: " + query);

		/*
		 * The response upon a non acceptable request says:
		 * 
		 * No suitable response format available. (Supported formats:
		 * application/json, application/x-lisp-structured-expression,
		 * text/integer, application/sparql-results+xml,
		 * application/sparql-results+json, application/processed-csv, text/csv,
		 * application/x-direct-upis)
		 */

		// the following tested to work:
		String accept = "application/json";
		// String accept = "text/csv";
		// String accept = "text/integer";
		// String accept = "application/sparql-results+xml";
		// String accept = "application/sparql-results+json";
		// String accept = "application/processed-csv";

		// the following generate: 406 Not acceptable
		// String accept = "application/rdf+xml";
		// String accept = "text/plain";
		// String accept = "text/x-nquads";
		// String accept = "application/trix";
		// String accept = "text/rdf+n3";

		System.out.println("accept: " + accept);

		String urlRequest = Common.TRIPLE_STORE_URL;

		final String encQuery = URLEncoder.encode(query, "UTF-8");
		urlRequest += "?query=" + encQuery;

		System.out.println("Making request...");
		HttpResponse httpResponse = HttpUtil.httpGet(urlRequest, accept);
		System.out.println("httpResponse = " + httpResponse);

		if (httpResponse.contentType.contains("application/json")) {
			_showJson(httpResponse.body);
		}
	}
}
