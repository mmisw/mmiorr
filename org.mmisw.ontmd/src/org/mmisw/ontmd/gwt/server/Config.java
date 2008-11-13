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

	
	// csv file with list of resource types:
	public static final String RESOURCE_TYPES_CSV_FILE = ONTMD_RESOURCES_DIR+ "resourcetypes.csv";

	// csv file with list of authorities:
	public static final String AUTHORITIES_CSV_FILE = ONTMD_RESOURCES_DIR+ "authorities.csv";

	
	private Config() {};
}
