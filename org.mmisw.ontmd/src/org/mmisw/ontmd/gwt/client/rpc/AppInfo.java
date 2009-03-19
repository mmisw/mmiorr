package org.mmisw.ontmd.gwt.client.rpc;

import java.io.Serializable;

/**
 * Application info.
 * 
 * @author Carlos Rueda
 */
public class AppInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String appName;
	
	private String version;
	
	
	public AppInfo() {
	}
	
	public AppInfo(String appName) {
		this.appName = appName;
	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String toString() {
		return getAppName()+ (getVersion() == null ? "" : " " +getVersion());
	}

}
