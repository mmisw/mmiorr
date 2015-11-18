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

    Config cfg = ConfigFactory.parseFile(configFile).resolve();

    orrConfig = new OrrConfig(cfg);
    log.info("Loaded OrrConfig: " + orrConfig);
    return orrConfig;
  }

  public static OrrConfig instance() {
    return orrConfig;
  }


  /** URL of the MMI Ont service. */
  public final String ontServiceUrl;

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

  /** email account information used for user account management and notifications. */
  public final String emailUsername;
  public final String emailPassword;

  /** absolute file location of image shown in the upper left corner of the page. */
  public final String brandingLogo;

  /** string used in page's <title> and some other places */
  public final String brandingAppTitle;

  /** "Terms of Use" link.
   * Only has effect (with omission in the UI if undefined) if the logo is also overwritten. */
  public final String brandingTouUrl;

  /** URI of the ResourceType OWL class:
   * Instances of this class are used to populate the corresponding selection GUI component */
  public final String resourceTypeClass;

  /** URI of the Authority OWL class:
   * Instances of this class are used to populate the corresponding selection GUI component */
  public final String authorityClass;

  @Override
  public String toString() {
    return "OrrConfig{\n" +
        "  ontServiceUrl      = " + ontServiceUrl + '\n' +
        "  workspaceDir       = " + workspaceDir + '\n' +
        "  preUploadsDir      = " + preUploadsDir + '\n' +
        "  voc2rdfDir         = " + voc2rdfDir + '\n' +
        "  previewDir         = " + previewDir + '\n' +
        "  emailUsername      = " + emailUsername + '\n' +
        "  brandingLogo       = " + brandingLogo + '\n' +
        "  brandingAppTitle   = " + brandingAppTitle + '\n' +
        "  brandingTouUrl     = " + brandingTouUrl + '\n' +
        "  resourceTypeClass  = " + resourceTypeClass + '\n' +
        "  authorityClass     = " + authorityClass +
        '}';
  }

  private OrrConfig(Config cfg) {
    ontServiceUrl = cfg.getString("ont.service.url");

    workspaceDir = prepareDir(cfg.getString("workspace"));
    preUploadsDir = prepareDir(workspaceDir, "ontmd/preuploads");
    voc2rdfDir    = prepareDir(workspaceDir, "ontmd/preuploads/voc2rdf");
    previewDir    = prepareDir(workspaceDir, "ontmd/previews");

    if(cfg.hasPath("email")) {
      emailUsername = cfg.getString("email.username");
      emailPassword = cfg.getString("email.password");
    }
    else {
      emailUsername = emailPassword = null;
    }

    brandingLogo     = cfg.hasPath("branding.logo") ? cfg.getString("branding.logo") : null;
    brandingAppTitle = cfg.hasPath("branding.app.title") ? cfg.getString("branding.app.title") : null;
    brandingTouUrl   = brandingLogo == null ? "http://marinemetadata.org/orr/tou"
        : cfg.hasPath("branding.tou.url") ? cfg.getString("branding.tou.url") : null;

    resourceTypeClass = cfg.getString("resourceType.class");
    authorityClass    = cfg.getString("authority.class");
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
