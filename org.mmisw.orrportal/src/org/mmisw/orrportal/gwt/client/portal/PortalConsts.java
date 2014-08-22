package org.mmisw.orrportal.gwt.client.portal;

/**
 * Some constants.
 * @author Carlos Rueda
 */
public class PortalConsts {
	private PortalConsts() {}
	
	
	public static final String T_ADMIN = "admin";
	
	public static final String T_BROWSE = "b";
	
	public static final String T_SEARCH_ONTS = "so";
	public static final String T_SEARCH_TERMS = "st";
	
	public static final String T_REGISTERED_BY_USER = "bu";
	public static final String T_REGISTERED_BY_AUTHORITY = "ba";
	
	public static final String T_USER_ACCOUNT = "ua";
	
	public static final String T_SIGN_IN = "login";
	public static final String T_SIGN_OUT = "logout";
	
	public static final String T_VOC2RDF = "voc2rdf";
	
	public static final String T_VINE = "vine";
	
	public static final String T_REGISTER_EXTERNAL = "rx";
	
	// TODO instead of hard-coded, make this a configurable parameter for deployment 
	public static final String REG_TYPE_HELP_PAGE = "http://marinemetadata.org/mmiorrusrman/registerexisting";
	
	
	/** Maximum ontology size (in #statements) for the data contents to be displayed automatically right after
	 * the metadata loading in the OntologyPanel. See Issue #308:immediately show vocabulary contents.
     * From 1000 to 8000, see issue #325
	 * */
	public static final long MAX_ONTOLOGY_SIZE_SHOW_DATA = 8000;

}
