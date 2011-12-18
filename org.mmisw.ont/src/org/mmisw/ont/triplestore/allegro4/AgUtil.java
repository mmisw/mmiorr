package org.mmisw.ont.triplestore.allegro4;


/**
 * ad hoc utilities.
 * 
 * @author Carlos Rueda
 */
public class AgUtil {

	/**
	 * Gets mime type corresponding to the given format.
	 * 
	 * @param form
	 * @return
	 */
	public static String mimeType(String form) {
		/*
		 * FIXME
		 */
		String mimeType = "text/plain";
		if (form == null || form.equals("txt")) {
			return mimeType;
		}
		else if (form.equals("json")) {
			mimeType = "application/json";
		}
		else if (form.equals("rdf") || form.equals("owl")) {
			mimeType = "application/rdf+xml";
		}
		else if (form.equals("n3")) {
			mimeType = "text/rdf+n3";
		}
		/*
		 * TODO others plus adjustments. for example, how to resolve for html?
		 */
		else if (form.equals("html")) {
			mimeType = "text/html";
		}
		return mimeType;
	}


	private AgUtil() {
	}
}
