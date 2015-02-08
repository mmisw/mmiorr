package org.mmisw.ont.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntRequest;
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class Util {

	private static final Log log = LogFactory.getLog(Util.class);

    /** @return true iff the given param is defined in the params
     * AND none of the values is equal to "n".
     */
    public static boolean yes(Map<String, String[]> params, String param) {
        String[] vals = params.get(param);
        if ( null == vals ) {
            return false;
        }
        for(String val: vals) {
            if ("n".equals(val)) {
                return false;
            }
        }
        return true;
    }

	/** @returns The last value associated with the given parameter. If not value is
	 * explicitly associated, it returns the given default value.
	 */
	public static String getParam(Map<String, String[]> params, String param, String defaultValue) {
		String[] array = params.get(param);
		if ( array == null || array.length == 0 ) {
			return defaultValue;
		}
		// return last value in the array:
		return array[array.length -1];
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String[]> getParams(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		return params;
	}

	/**
	 * @returns the list of values associated with a header. Never null.
	 */
	public static List<String> getHeader(HttpServletRequest request, String hname) {
		List<String> values = new ArrayList<String>();

    	Enumeration<?> hvals = request.getHeaders(hname.toString());
        while ( hvals.hasMoreElements() ) {
        	String hval = String.valueOf(hvals.nextElement());
        	values.add(hval);
        }

		return values;
	}

	/**
	 * Developer option.
	 * @param httpServlet
	 */
	public static  void showReq(HttpServlet httpServlet, OntRequest req) throws ServletException, IOException {

        req.response.setContentType("text/html");
        PrintWriter out = req.response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Show req</title>");
        out.println("<link rel=stylesheet href=\"" +req.request.getContextPath()+ "/main.css\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<pre>");

        out.println("httpServlet.getServletContext().getContextPath()  = " +httpServlet.getServletContext().getContextPath() );

        out.println("request.getRequestURL()         = " + req.request.getRequestURL()  );
        out.println("request.getRequestURI()         = " + req.request.getRequestURI()  );
        out.println("request.getQueryString()        = " + req.request.getQueryString()  );

        out.println("request.getParameterMap()       = " + req.request.getParameterMap()  );
		for (Entry<String, String[]> pair : req.params.entrySet() ) {
			out.println("    " +pair.getKey()+ " => " + Arrays.asList(pair.getValue()));
		}
        out.println("request.getContextPath()        = " + req.request.getContextPath() );
        out.println("request.getMethod()             = " + req.request.getMethod()  );
        out.println("request.getPathInfo()           = " + req.request.getPathInfo()  );
        out.println("request.getPathTranslated()     = " + req.request.getPathTranslated()  );
        out.println("request.getRemoteUser()         = " + req.request.getRemoteUser()  );
        out.println("request.getRequestedSessionId() = " + req.request.getRequestedSessionId()  );
        out.println("request.getServletPath()        = " + req.request.getServletPath()  );
        out.println("request.getAttributeNames()     = " + req.request.getAttributeNames()  );
        out.println("request.getCharacterEncoding()  = " + req.request.getCharacterEncoding()  );
        out.println("request.getContentLength()      = " + req.request.getContentLength()  );
        out.println("request.getContentType()        = " + req.request.getContentType()  );
        out.println("request.getProtocol()           = " + req.request.getProtocol()  );
        out.println("request.getRemoteAddr()         = " + req.request.getRemoteAddr()  );
        out.println("request.getRemoteHost()         = " + req.request.getRemoteHost()  );
        out.println("request.getScheme()             = " + req.request.getScheme()  );
        out.println("request.getServerName()         = " + req.request.getServerName()  );
        out.println("request.getServerPort()         = " + req.request.getServerPort()  );
        out.println("request.isSecure()              = " + req.request.isSecure()  );

        out.println("request. headers             = ");
        Enumeration<?> hnames = req.request.getHeaderNames();
        while ( hnames.hasMoreElements() ) {
        	Object hname = hnames.nextElement();
        	out.print("        " +hname+ " : ");
        	Enumeration<?> hvals = req.request.getHeaders(hname.toString());
        	String sep = "";
            while ( hvals.hasMoreElements() ) {
            	Object hval = hvals.nextElement();
				out.println(hval + sep);
				sep = "  ;  ";
            }
            out.println();
        }

        out.println("</pre>");
        out.println("</body>");
        out.println("</html>");
	}

	/**
	 * Developer option.
	 */
	public static void doDbQuery(HttpServletRequest request, Map<String, String[]> params, HttpServletResponse response, Db db) throws ServletException, IOException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();
			String table = Util.getParam(params, "table", "v_ncbo_ontology");
			int limit = Integer.parseInt(Util.getParam(params, "limit", "500"));

			String query = "select * from " +table+ "  limit " +limit;

			ResultSet rs = _stmt.executeQuery(query);

			response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<head>");
	        out.println("<link rel=\"stylesheet\" href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\" />");
	        out.println("<title>" +query+ "</title>");
	        out.println("</head>");
	        out.println("<body>");
	        out.println("<code>" +query+ "</code>");
	        out.println("<table class=\"inline\">");


	        ResultSetMetaData md = rs.getMetaData();
	        int cols = md.getColumnCount();
	        out.println("<tr>");
	        for (int i = 0; i < cols; i++) {
	        	out.println("<th>");
	        	out.println(md.getColumnLabel(i+1));
	            out.println("</th>");
	        }
	        out.println("</tr>");

	        while ( rs.next() ) {
	        	out.println("<tr>");
	        	for (int i = 0; i < cols; i++) {
		        	out.println("<td>");
		        	out.println(rs.getObject(i+1));
		            out.println("</td>");
	        	}
	        	out.println("</tr>");
	        }

			out.println("</table></body></html>");

		}
		catch (SQLException e) {
			throw new ServletException(e);
		}
		finally {
			db.closeStatementAndConnection(_stmt, _con);
		}
	}

	/** "cleans" the string so it can be embedded in html content */
	public static String toHtml(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
	}

	/** "cleans" the string so it can be embedded in a html comment */
	public static String toHtmlComment(String s) {
		return s.replaceAll("--", "\\\\-\\\\-");
	}

	/**
	 * Does the string correspond to a CSV contentType?
	 */
	public static boolean contentTypeIsCsv(String ct) {
		return ct != null && (ct.contains("application/processed-csv") || ct.contains("text/csv"));
	}

	/**
	 * Converts CSV contents into an HTML table.
	 * @param csv The CSV contents
	 * @return the HTML contents.
	 */
	public static String csv2html(String csv) {
		StringBuilder html = new StringBuilder();

		html.append("<table class=\"inline2\">\n");

		String thtd = "th";
		String[] lines = csv.split("\n|\r\n");
		for (String line : lines) {
			if (line.startsWith("\"") && line.endsWith("\"")) {
				line = line.substring(1, line.length() -1);
			}

            /* the following conditional split is because AllegroGraph mixes non-quoted
             * with quoted fields for the Content-Type: application/processed-csv, for
             * example:
             *   http http://mmisw.org:10035/repositories/mmiorr\?infer\=false\&query\=SELECT+DISTINCT+%3Fproperty+%3Fvalue+WHERE+%7B+%3Chttp%3A%2F%2Fmmisw.org%2Font%2Fmmitest%2Fparameter%2FParameter%3E+%3Fproperty+%3Fvalue+.+%7D+ORDER+BY+%3Fproperty "Accept: application/processed-csv"
             *   HTTP/1.1 200 OK
             *   Cache-control: max-age=0, must-revalidate
             *   Connection: keep-alive
             *   Content-Encoding: gzip
             *   Content-Type: application/processed-csv; charset=UTF-8
             *   Date: Wed, 18 Jun 2014 03:08:54 GMT
             *   Etag: 2c3000000000000000
             *   Server: AllegroServe/1.3.19
             *   Transfer-Encoding: chunked
             *
             *   property,value
             *   "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>","<http://www.w3.org/2002/07/owl#Class>"
             *   "<http://www.w3.org/2000/01/rdf-schema#label>","parameter"
             */
			String[] cols = line.indexOf('"') >= 0 ? line.split("\",\"") : line.split(",");

			html.append("<tr>");

			for (String col : cols) {
				while (col.startsWith("\"") && col.endsWith("\"")) {
					col = col.substring(1, col.length() -1);
				}
				if (col.startsWith("<") && col.endsWith(">")) {
					col = col.substring(1, col.length() -1);
				}

				String link = Util.getLink(col);
				if ( link != null ) {
					col = String.format("<a target=\"_blank\" href=\"%s\">%s</a>", link, Util.toHtml(col));
				}
				else {
					col = Util.toHtml(col);
				}
				html.append(String.format("\n\t<%s>%s</%s>", thtd, col, thtd));
			}
			html.append("\n</tr>\n");
			thtd = "td";
		}

		html.append("</table>\n");
		return html.toString();
	}

	public static String elapsedTime(long start) {
		long total = System.currentTimeMillis() - start;
		long min = total/60000;
		long msec = total%60000;
		double sec = msec/1000.0;
		String report;
		if (min > 0) {
			report = min + ":" + sec + " min:sec";
		} else {
			report = sec + " sec";
		}
		return report;
	}


	/**
	 * helper method to retrieve the contents of a resource in the classpath .
	 */
	public static String getResource(String resourceName) {
		InputStream infRulesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
		if ( infRulesStream == null ) {
			log.error(resourceName+ ": resource not found -- check classpath");
			return null;
		}
		StringWriter output = new StringWriter();
		try {
			IOUtils.copy(infRulesStream, output);
			return output.toString();
		}
		catch (IOException e) {
			log.error(resourceName+ ": cannot read resource", e);
			return null;
		}
		finally {
			IOUtils.closeQuietly(infRulesStream);
		}
	}



	/**
	 * Returns a string that can be used as a link.
	 * If it is an MmiUri, a ".html" is appended;
	 * otherwise, if it is a valid URL, it is returned as it is;
	 * otherwise, null is returned.
	 *
	 * @param value a potential URL
	 * @return the string that can be used as a link as stated above; null if value is not a URL.
	 */
	public static String getLink(String value) {
		// try mmiUri:
		try {
			MmiUri mmiUri = new MmiUri(value);
			return mmiUri.getTermUri() + ".html";
		}
		catch (URISyntaxException e1) {
			// ignore. Try URL below.
		}

		// try regular URL:
		try {
			URL url = new URL(value);
			return url.toString();
		}
		catch (MalformedURLException e) {
			// ignore.
		}

		return null;
	}

	/**
	 * Gets the output format corresponding to the given Accept object.
	 * @param accept
	 * @return
	 */
	public static String getOutFormatByContentNegotiation(Accept accept) {
		// accept empty? --> OWL
		if ( accept.isEmpty() ) {
			return "owl";
		}

		// Dominating Accept: application/rdf+xml dominates? --> OWL
		else if ( accept.getDominating().equalsIgnoreCase("application/rdf+xml") ) {
			return "owl";
		}

		// Dominating Accept contains "xml"? --> OWL
		else if ( accept.getDominating().indexOf("xml") >= 0 ) {
			return "owl";
		}

		// Dominating Accept: text/html dominates? --> HTML
		else if ( accept.getDominating().equalsIgnoreCase("text/html") ) {
			return "html";
		}

		// default:
		else {
			return "owl";
		}
	}


	private Util() {}
}
