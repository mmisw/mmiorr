package org.mmisw.orrportal.gwt.server;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.ServiceConfigurationError;

/**
 * ORR runtime configuration.
 */
public class OrrConfig {

  public static OrrConfig init() {
    Config baseAppConfig = ConfigFactory.load();
    String configFilename = baseAppConfig.getString("configFile");
    if (configFilename == null) {
      throw new ServiceConfigurationError("Could not retrieve configuration parameter: configFile");
    }
    log.info("Loading configuration from " + configFilename);
    File configFile = new File(configFilename);
    if (!configFile.canRead()) {
      throw new ServiceConfigurationError("Could not read configuration file: " + configFile);
    }

    Config cfg = ConfigFactory.parseFile(configFile);

    orrConfig = new OrrConfig(cfg);
    log.info("Loaded OrrConfig: " + orrConfig);
    return orrConfig;
  }

  public static OrrConfig instance() {
    return orrConfig;
  }


  /** Main workspace parent directory */
  public final File workspaceDir;

  /** where the "pre-loaded" files are stored: (a pre-loaded file is one that the user uploads
   * when starting the process of submitting an ontology to the repository) */
  public final File preUploadsDir;

  /** where voc2rdf-generated files are stored: (the converted RDF file is stored here) */
  public final File voc2rdfDir;

  /** where the reviewed files are stored: (once a review operation is completed, the resulting
   * file is stored here) */
  public final File previewDir;

  public final File resourceDir;

  public final String agraphSparql;

  public final String emailUsername;
  public final String emailPassword;

  @Override
  public String toString() {
    return "OrrConfig{\n" +
        "  workspaceDir =" + workspaceDir + '\n' +
        "  preUploadsDir=" + preUploadsDir + '\n' +
        "  voc2rdfDir   =" + voc2rdfDir + '\n' +
        "  previewDir   =" + previewDir + '\n' +
        "  resourceDir  =" + resourceDir + '\n' +
        "  agraphSparql =" + agraphSparql + '\n' +
        "  emailUsername=" + emailUsername +
        '}';
  }

  private OrrConfig(Config cfg) {
    workspaceDir = prepareDir(cfg.getString("workspace"));

    preUploadsDir = prepareDir(workspaceDir, "ontmd/preuploads");
    voc2rdfDir    = prepareDir(workspaceDir, "ontmd/preuploads/voc2rdf");
    previewDir    = prepareDir(workspaceDir, "ontmd/previews");
    resourceDir   = prepareDir(workspaceDir, "ontmd/resources");

    if(cfg.hasPath("email")) {
      emailUsername = cfg.getString("email.username");
      emailPassword = cfg.getString("email.password");
    }
    else {
      emailUsername = emailPassword = null;
    }

    agraphSparql = cfg.hasPath("agraph.sparql") ? cfg.getString("agraph.sparql") : null;
  }

  private static File prepareDir(String dirName) {
    return prepareDir(new File(dirName));
  }

  private static File prepareDir(File parentDir, String subdirName) {
    return prepareDir(new File(parentDir, subdirName));
  }

  private static File prepareDir(File dirFile) {
    if (dirFile.exists()) {
      if (!dirFile.isDirectory()) {
        throw new RuntimeException(dirFile + " already exists but is not a directory");
      }
    }
    else {
      if (!dirFile.mkdirs()) {
        throw new RuntimeException("could not create directory: " + dirFile);
      }
    }

    return dirFile;
  }

  private static OrrConfig orrConfig;

  private static final Log log = LogFactory.getLog(OrrConfig.class);
}
