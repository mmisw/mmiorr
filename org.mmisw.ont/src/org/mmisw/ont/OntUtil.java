package org.mmisw.ont;


/**
 * Some basic operations.
 * 
 * @author Carlos Rueda
 */
public class OntUtil {
	private OntUtil() {}
	
	/**
	 * Determines whether the given URI corresponds to an ontology or term
	 * served or that can be served by the "ont" service.  
	 * It uses the "ont.service.url" property value provided
	 * by the configuration object.
	 * 
	 * <p>
	 * A similar method exists in the "orrclient" module.
	 * 
	 * @param uri The URI to check.
	 * @return if the given URI has the Ont service URL as a prefix (ignoring case).
	 * @throws IllegalStateException if the configuration object returns a null value for the 
	 *         {@link OntConfig.Prop.ONT_SERVICE_URL} parameter.
	 */
	public static boolean isOntResolvableUri(String uri) {
		 String ontServiceUrl = OntConfig.Prop.ONT_SERVICE_URL.getValue();
		 if ( ontServiceUrl == null ) {
			 throw new IllegalStateException("OntConfig.Prop.ONT_SERVICE_URL.getValue() returned null");
		 }
		 return uri.toLowerCase().startsWith(ontServiceUrl.toLowerCase());
	}
}
