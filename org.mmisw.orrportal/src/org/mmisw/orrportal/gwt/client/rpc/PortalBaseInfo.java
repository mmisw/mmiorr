package org.mmisw.orrportal.gwt.client.rpc;

import java.io.Serializable;

/**
 * Base info for the portal.
 * 
 * @author Carlos Rueda
 */
public class PortalBaseInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String appServerUrl;
	private String ontServiceUrl;
	private String ontbrowserServiceUrl;
	private String gaUaNumber;
	
	
	/**
	 * @return the appServerUrl
	 */
	public String getAppServerUrl() {
		return appServerUrl;
	}
	/**
	 * @param appServerUrl the appServerUrl to set
	 */
	public void setAppServerUrl(String appServerUrl) {
		this.appServerUrl = appServerUrl;
	}
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

	/**
	 * @return the ontbrowserServiceUrl
	 */
	public String getOntbrowserServiceUrl() {
		return ontbrowserServiceUrl;
	}
	/**
	 * @param ontbrowserServiceUrl the ontbrowserServiceUrl to set
	 */
	public void setOntbrowserServiceUrl(String ontbrowserServiceUrl) {
		this.ontbrowserServiceUrl = ontbrowserServiceUrl;
	}

	/**
	 * Sets the google analytics UA number
	 * @param gaUaNumber
	 */
	public void setGaUaNumber(String gaUaNumber) {
		this.gaUaNumber = gaUaNumber;
	}
	
	/**
	 * Gets the google analytics UA number
	 * @return the google analytics UA number
	 */
	public String getGaUaNumber() {
		return gaUaNumber;
	}
	
}
