package org.mmisw.ont.util;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Utilities to generate responses taking the content type and encoding into
 * consideration.
 * 
 * @author Carlos Rueda
 */
public class ServletUtil {

	// a flag while I test the new implementation.
	// setting this to false should result in the exact previous behavior before
	// the existence of this helper class.
	private static final boolean USE_NEW_IMPL = true;

	
	private static final String UTF8_CHARSET = "UTF-8";
	
	private static final String CT_APPLICATION_RDF_XML_UTF8 = "application/rdf+xml; charset=UTF-8";
	private static final String CT_APPLICATION_RDF_XML = "application/rdf+xml";

	private static final String CT_TEXT_PLAIN_UTF8 = "text/plain; charset=UTF-8";
	private static final String CT_TEXT_PLAIN = "text/plain";
	

	
	
	/**
	 * Sets the "application/rdf+xml" content type into the {@link HttpServletResponse},
	 * perhaps adding the charset depending on the implementation (changes under testing).
	 * 
	 * @param response
	 *            response instance
	 * @throws IOException
	 */
	public static void setContentTypeRdfXml(
			HttpServletResponse response
	) throws IOException {
		
		if ( USE_NEW_IMPL ) {
			response.setContentType(CT_APPLICATION_RDF_XML_UTF8);
		}
		else {
			response.setContentType(CT_APPLICATION_RDF_XML);
		}
	}
	
	
	/**
	 * Sets the "text/plain" content type into the {@link HttpServletResponse},
	 * perhaps adding the charset depending on the implementation (changes under testing).
	 * 
	 * @param response
	 *            response instance
	 * @throws IOException
	 */
	public static void setContentTypeTextPlain(
			HttpServletResponse response
	) throws IOException {
		
		if ( USE_NEW_IMPL ) {
			response.setContentType(CT_TEXT_PLAIN_UTF8);
		}
		else {
			response.setContentType(CT_TEXT_PLAIN);
		}
	}
	
	
	/**
	 * Writes an RDF/XML content into the {@link HttpServletResponse}.
	 * 
	 * @param response
	 *            response instance
	 * @param responseContent
	 *            a string containing the response content
	 * @throws IOException
	 */
	public static void writeResponseRdfXml(
			HttpServletResponse response, 
			String responseContent
	) throws IOException {
		
		if ( USE_NEW_IMPL ) {
			// new version
			String contentType = CT_APPLICATION_RDF_XML_UTF8;
			ServletUtil.writeResponse(response, responseContent, contentType);
		}
		else {
			// previous version:
			response.setContentType(CT_APPLICATION_RDF_XML);
			ServletOutputStream os = response.getOutputStream();
			IOUtils.write(responseContent, os);
			os.close();
		}
	
	}
	
	
	
	/**
	 * Write the response content into the {@link HttpServletResponse}.
	 * 
	 * @param response
	 *            response instance
	 * @param responseContent
	 *            a string containing the response content
	 * @param contentType
	 *            the content type
	 * @throws IOException
	 *             if reading, writing, or closing the response's output stream
	 *             fails
	 */
	public static void writeResponse(
			HttpServletResponse response, 
			String responseContent,
			String contentType
	) throws IOException {

		byte[] responseBytes = responseContent.getBytes(UTF8_CHARSET);
		
		response.setContentLength(responseBytes.length);
		response.setContentType(contentType);
		response.setStatus(HttpServletResponse.SC_OK);
		response.getOutputStream().write(responseBytes);
	}

	// non instanceable class.
	private ServletUtil() {
	}
}
