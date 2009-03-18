package org.mmisw.ontmd.gwt.server;

/**
 * Some configuration parameters.
 * 
 * <p>
 * TODO: handle this in a more flexible way.
 * 
 * 
 * @author Carlos Rueda
 */
public class Config {

	public static final String ONTMD_WORKSPACE_DIR = "/Users/Shared/mmiregistry/ontmd/";
	
	// where the pre-loaded files are stored:
	public static final String ONTMD_PRE_UPLOADS_DIR = ONTMD_WORKSPACE_DIR+ "preuploads/";
	
	// where voc2rdf-generated files are stored:
	public static final String ONTMD_VOC2RDF_DIR = ONTMD_PRE_UPLOADS_DIR+ "voc2rdf/";
	
	// where the previewed files are stored:
	public static final String ONTMD_PREVIEW_DIR = ONTMD_WORKSPACE_DIR+ "previews/";


	// where the resource files are stored:
	public static final String ONTMD_RESOURCES_DIR = ONTMD_WORKSPACE_DIR+ "resources/";

	
	/** URI of the OWL class: resource type */
	public static final String RESOURCE_TYPE_CLASS = "http://mmisw.org/ont/mmi/resourcetype/ResourceType";

	/** URI of the OWL class: authority  */
	public static final String AUTHORITY_CLASS = "http://mmisw.org/ont/mmi/authority/Authority";
	
	

	private Config() {};
}
