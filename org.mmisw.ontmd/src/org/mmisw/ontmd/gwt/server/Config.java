package org.mmisw.ontmd.gwt.server;

/**
 * Some configuration parameters.
 * 
 * <p>
 * TODO Note: this class is replicated -- Unify!
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

	
	private Config() {};
}
