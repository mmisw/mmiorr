package org.mmisw.orrclient.gwt.client.rpc;

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
	private String build;
	
	
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
	
	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String toString() {
		return getAppName()+ " " +getVersion()+ " (" +getBuild()+ ")";
	}

}
