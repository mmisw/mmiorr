package org.mmisw.orrportal.gwt.server;

import com.typesafe.config.Config;

/**
 * ORR runtime configuration.
 */
public class OrrConfig {

  public static OrrConfig load(Config cfg) {
    orrConfig = new OrrConfig(cfg);
    return orrConfig;
  }

  public static OrrConfig instance() {
    return orrConfig;
  }


  public final String workspace;

  public final boolean useAgraph;
  public final String agraphSparql;

  @Override
  public String toString() {
    return "OrrConfig{" +
        "workspace='" + workspace + '\'' +
        ", agraphSparql='" + agraphSparql + '\'' +
        '}';
  }

  private OrrConfig(Config cfg) {
    workspace = cfg.getString("workspace");

    useAgraph = cfg.hasPath("agraph");
    agraphSparql = useAgraph ? cfg.getString("agraph.sparql") : null;
  }

  private static OrrConfig orrConfig;
}
