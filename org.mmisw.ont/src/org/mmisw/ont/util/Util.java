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
import org.mmisw.ont.db.Db;
import org.mmisw.ont.mmiuri.MmiUri;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class Util {
	
	private static final Log log = LogFactory.getLog(Util.class);

	/** @returns true iff the given param is defined in the request
	 * AND either no value is associated OR none of the values is equal to "n".
	 */
	public static boolean yes(HttpServletRequest request, String param) {
		List<String> values = getParamValues(request, param);
		return values != null && ! values.contains("n");
	}

	/** @returns the list of values associated to the given param.
	 * null if the param is not included in the request.
	 */
	public static List<String> getParamValues(HttpServletRequest request, String param) {
		Map<String, String[]> params = getParams(request);
		String[] vals = params.get(param);
		if ( null == vals ) {
			return null;
		}
		List<String> list = Arrays.asList(params.get(param));
		return list;
	}


	/** @returns The last value associated with the given parameter. If not value is 
	 * explicitly associated, it returns the given default value.
	 */
	public static String getParam(HttpServletRequest request, String param, String defaultValue) {
		Map<String, String[]> params = getParams(request);
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
	public static  void showReq(HttpServlet httpServlet, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Show req</title>");
        out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<pre>");
        
        out.println("httpServlet.getServletContext().getContextPath()  = " +httpServlet.getServletContext().getContextPath() );
        
        out.println("request.getRequestURL()         = " + request.getRequestURL()  );
        out.println("request.getRequestURI()         = " + request.getRequestURI()  );
        out.println("request.getQueryString()        = " + request.getQueryString()  );
        
        out.println("request.getParameterMap()       = " + request.getParameterMap()  );
		Map<String, String[]> params = Util.getParams(request);
		for (Entry<String, String[]> pair : params.entrySet() ) {
			out.println("    " +pair.getKey()+ " => " + Arrays.asList(pair.getValue()));	
		}
        out.println("request.getContextPath()        = " + request.getContextPath() ); 
        out.println("request.getMethod()             = " + request.getMethod()  ); 
        out.println("request.getPathInfo()           = " + request.getPathInfo()  ); 
        out.println("request.getPathTranslated()     = " + request.getPathTranslated()  ); 
        out.println("request.getRemoteUser()         = " + request.getRemoteUser()  );
        out.println("request.getRequestedSessionId() = " + request.getRequestedSessionId()  );
        out.println("request.getServletPath()        = " + request.getServletPath()  );
        out.println("request.getAttributeNames()     = " + request.getAttributeNames()  ); 
        out.println("request.getCharacterEncoding()  = " + request.getCharacterEncoding()  );
        out.println("request.getContentLength()      = " + request.getContentLength()  );
        out.println("request.getContentType()        = " + request.getContentType()  );
        out.println("request.getProtocol()           = " + request.getProtocol()  );
        out.println("request.getRemoteAddr()         = " + request.getRemoteAddr()  );
        out.println("request.getRemoteHost()         = " + request.getRemoteHost()  ); 
        out.println("request.getScheme()             = " + request.getScheme()  );
        out.println("request.getServerName()         = " + request.getServerName()  );
        out.println("request.getServerPort()         = " + request.getServerPort()  );
        out.println("request.isSecure()              = " + request.isSecure()  ); 
        
        out.println("request. headers             = ");
        Enumeration<?> hnames = request.getHeaderNames();
        while ( hnames.hasMoreElements() ) {
        	Object hname = hnames.nextElement();
        	out.print("        " +hname+ " : ");
        	Enumeration<?> hvals = request.getHeaders(hname.toString());
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
	public static void doDbQuery(HttpServletRequest request, HttpServletResponse response, Db db) throws ServletException, IOException {
		Connection _con = null;
		Statement _stmt = null;
		try {
			_con = db.getConnection();
			_stmt = _con.createStatement();
			String table = Util.getParam(request, "table", "v_ncbo_ontology");
			int limit = Integer.parseInt(Util.getParam(request, "limit", "500"));

			String query = "select * from " +table+ "  limit " +limit;
			
			ResultSet rs = _stmt.executeQuery(query);
			
			response.setContentType("text/html");
	        PrintWriter out = response.getWriter();
	        out.println("<html>");
	        out.println("<head>");
	        out.println("<link rel=stylesheet href=\"" +request.getContextPath()+ "/main.css\" type=\"text/css\">");
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
