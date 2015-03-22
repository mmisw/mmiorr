package org.mmisw.orrclient.gwt.client.rpc;

/**
 * Type of object returned by Utf8Util.readFileWithConversionToUtf8
 * 
 * @author Carlos Rueda
 */
public class ReadFileResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	private String contents;
	private String logInfo = "";
	
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getContents() {
		return contents;
	}
	public void setLogInfo(String logInfo) {
		this.logInfo = logInfo;
	}
	public void addLogInfo(String logInfo) {
		this.logInfo += logInfo;
	}
	public String getLogInfo() {
		return logInfo;
	}

}
