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
	
	// where the previewed files are stored:
	public static final String ONTMD_PREVIEW_DIR = ONTMD_WORKSPACE_DIR+ "previews/";

	
	private Config() {};
}
