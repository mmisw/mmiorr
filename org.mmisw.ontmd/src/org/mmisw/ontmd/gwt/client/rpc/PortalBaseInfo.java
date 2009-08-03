package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Base info for the portal.
 * 
 * @author Carlos Rueda
 */
public class PortalBaseInfo implements IsSerializable {

	private String ontServiceUrl;
	private String portalServiceUrl;
	private String vineServiceUrl;
	
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
	 * @return the vineServiceUrl
	 */
	public String getVineServiceUrl() {
		return vineServiceUrl;
	}
	/**
	 * @param vineServiceUrl the vineServiceUrl to set
	 */
	public void setVineServiceUrl(String vineServiceUrl) {
		this.vineServiceUrl = vineServiceUrl;
	}
	
	
	
}
