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
	 * Note that upon an invalid "accept" value for a SELECT query,
	 * AG 4.4 reports :
	<pre>
	No suitable response format available. (Supported formats:
	application/json, application/x-lisp-structured-expression,
	text/integer, application/sparql-results+xml,
	application/sparql-results+json, application/processed-csv, text/csv,
	application/x-direct-upis)
	</pre>
	 * 
	 * @param form
	 * @return
	 */
	public static String mimeType(String form) {
        /*
         * TODO how to resolve for html or rdf or n3?
         */
        //String mimeType = "text/csv";
        String mimeType = "application/processed-csv";
        if (form == null || form.equals("txt") || form.equals("csv")) {
                // Ok, CSV.
        }
        else if (form.equals("json")) {
                mimeType = "application/json";
        }
        else if (form.equals("rdf") || form.equals("owl")) {
                /*
                 * TODO These would not be allowed. Let it go as this for the moment
                 * and let the response indicate the error to the user.
                 */
                mimeType = "application/rdf+xml";
        }
        else if (form.equals("n3")) {
                /*
                 * TODO These would not be allowed. Let it go as this for the moment
                 * and let the response indicate the error to the user.
                 */
                mimeType = "text/rdf+n3";
        }
        else if (form.equals("html")) {
                // TODO "text/html" is invalid response formats for AG 4.x; 
                //mimeType = "text/html";
                // keep CSV for the moment.
        }

        return mimeType;
	}


	private AgUtil() {
	}
}
