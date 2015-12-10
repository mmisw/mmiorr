package org.mmisw.orrclient;

import org.mmisw.orrclient.core.OrrClientImpl;
import org.mmisw.orrclient.core.OrrClientImpl2;
import org.mmisw.orrportal.gwt.server.OrrConfig;

/**
 * Obtains the IOrrClient implementation
 */
public class OrrClientFactory {
  public static IOrrClient init() throws Exception {
    // the check with "/orr-ont" is just ad hoc during initial impl/testing of OrrClientImpl2
    if (OrrConfig.instance().ontServiceUrl.endsWith("/orr-ont")) {
      return OrrClientImpl2.init();
    }
    else {
      return OrrClientImpl.init();
    }
  }
}
