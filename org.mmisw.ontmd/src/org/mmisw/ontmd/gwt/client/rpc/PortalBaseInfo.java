package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Base info for the portal.
 * 
 * @author Carlos Rueda
 */
public class PortalBaseInfo implements IsSerializable {

	private String appServerUrl;
	private String ontServiceUrl;
	private String portalServiceUrl;
	private String ontbrowserServiceUrl;
	
	
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
	 * @return the portalServiceUrl
	 */
	public String getPortalServiceUrl() {
		return portalServiceUrl;
	}
	/**
	 * @param portalServiceUrl the portalServiceUrl to set
	 */
	public void setPortalServiceUrl(String portalServiceUrl) {
		this.portalServiceUrl = portalServiceUrl;
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
	
	
	
}
