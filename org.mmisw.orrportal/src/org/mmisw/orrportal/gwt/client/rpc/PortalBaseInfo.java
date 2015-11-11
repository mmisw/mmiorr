package org.mmisw.orrportal.gwt.client.rpc;

import java.io.Serializable;

/**
 * Base info for the portal.
 */
public class PortalBaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String ontServiceUrl;

	public PortalBaseInfo() {}

	/**
	 * @return the ontServiceUrl
	 */
	public String getOntServiceUrl() {
		return ontServiceUrl;
	}
  /**
   * @param ontServiceUrl the ontServiceUrl to set
   */
  public void setOntServiceUrl(String ontServiceUrl) {
    this.ontServiceUrl = ontServiceUrl;
  }
}
