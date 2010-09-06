package org.mmisw.orrclient.gwt.client.rpc;


/**
 * Data Info for the creation of an ontology using a pre-loaded ontology
 * in the working space via a TempOntologyInfo.
 * @author Carlos Rueda
 */
public class OtherDataCreationInfo extends DataCreationInfo {
	private static final long serialVersionUID = 1L;
	
	private TempOntologyInfo tempOntologyInfo;

	/**
	 * @return the tempOntologyInfo
	 */
	public TempOntologyInfo getTempOntologyInfo() {
		return tempOntologyInfo;
	}

	/**
	 * @param tempOntologyInfo the tempOntologyInfo to set
	 */
	public void setTempOntologyInfo(TempOntologyInfo tempOntologyInfo) {
		this.tempOntologyInfo = tempOntologyInfo;
	}
	


}
