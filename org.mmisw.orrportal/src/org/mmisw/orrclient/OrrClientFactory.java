package org.mmisw.orrclient;

import org.mmisw.orrclient.core.OrrClientImpl;
import org.mmisw.orrclient.core.OrrClientImpl2;

/**
 * Obtains the IOrrClient implementation
 */
public class OrrClientFactory {
  // interim flag to facilitate impl/testing of OrrClientImpl2
  private static final boolean _361 = false;

  public static IOrrClient init() throws Exception {
    if (_361) {
      return OrrClientImpl2.init();
    }
    else {
      return OrrClientImpl.init();
    }
  }
}
