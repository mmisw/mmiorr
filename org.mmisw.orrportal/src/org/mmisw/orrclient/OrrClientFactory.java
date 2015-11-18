package org.mmisw.orrclient;

import org.mmisw.orrclient.core.OrrClientImpl;

/**
 * Obtains the IOrrClient implementation
 */
public class OrrClientFactory {
  public static IOrrClient init() throws Exception {
    return OrrClientImpl.init();
  }

  public static IOrrClient getOrrClient() {
    return OrrClientImpl.getInstance();
  }
}
