package org.mmisw.orrclient;

import org.mmisw.orrclient.core.OrrClientImpl;

/**
 * Obtains the IOrrClient implementation
 */
public class OrrClientFactory {
  public static IOrrClient init(OrrClientConfiguration config) throws Exception {
    return OrrClientImpl.init(config);
  }

  public static IOrrClient getOrrClient() {
    return OrrClientImpl.getInstance();
  }
}
