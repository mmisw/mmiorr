package org.mmisw.ont.util;

import java.io.IOException;
import java.io.PrintWriter;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mmisw.ont.Db;

/**
 * Misc utilities.
 * @author Carlos Rueda
 */
public class Util {

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
		for ( String key: params.keySet() ) {
			out.println("    " +key+ " => " + Arrays.asList(params.get(key))  );	
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
		try {
			_con = db.getConnection();
			Statement _stmt = _con.createStatement();
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
			if ( _con != null ) {
				try {
					_con.close();
				}
				catch (SQLException e) {
					throw new ServletException(e);
				}
			}
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

	private Util() {}
}
