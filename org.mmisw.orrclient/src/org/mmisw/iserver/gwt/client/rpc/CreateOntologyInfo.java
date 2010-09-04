package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * Info to create a new ontology entry in the registry.
 * 
 * @author Carlos Rueda
 */
public class CreateOntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private HostingType hostingType;
	
	
	// desired authority and shortName for the to-be-registered ontology
	private String authority;
	private String shortName;
	
	// desired metadata for the to-be-registered ontology
	private Map<String,String> metadataValues;
	
	// desired data for the to-be-registered ontology
	private DataCreationInfo dataCreationInfo;
	
	/**
	 * Info about the ontology used as starting point, if any. 
	 * This can be a TempOntologyInfo or a RegisteredOntologyInfo
	 */
	private BaseOntologyInfo baseOntologyInfo;
	
	//
	// TODO REmove the following PriorOntologyInfo stuff: when the baseOntologyInfo above is
	// a RegisteredOntologyInfo, we already have the required prior info.
	//
	
	/**
	 * Necessary info for when a prior version exists and a new version
	 * is going to be created/submitted.
	 */
	public static class PriorOntologyInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		
		/** aquaportal ontology ID used, if not null, to create a new version */
		private String ontologyId;
		private String ontologyUserId;
		private String versionNumber;
		

		/**
		 * Gets the ID of the ontology base for the creation of a new version.
		 * @return the ontologyId. null for the case of a brand new ontology.
		 */
		public String getOntologyId() {
			return ontologyId;
		}
		/**
		 * Gets the ID of user associated with the ontology base for the creation of a new version.
		 * @return the ontologyUserId
		 */
		public String getOntologyUserId() {
			return ontologyUserId;
		}

		public String getVersionNumber() {
			return versionNumber;
		}

	}
	
	private PriorOntologyInfo priorOntologyInfo;
	
	/** URI of current ontology, if not null, used when creating a new version */
	private String uri;

	/**
	 * ctor.
	 */
	public CreateOntologyInfo() {
		
	}
	


	public PriorOntologyInfo getPriorOntologyInfo() {
		if ( priorOntologyInfo == null ) {
			priorOntologyInfo = new PriorOntologyInfo();
		}
		return priorOntologyInfo;
	}

	/**
	 * Sets the ID of the ontology base for the creation of a new version.
	 * Sets the ID of user associated with the ontology base for the creation of a new version.
	 * @param ontologyId the ontologyId to set
	 * @param ontologyUserId the ontologyUserId to set
	 * @param versionNumber The particular version used as a basis for the new version
	 */
	public void setPriorOntologyInfo(String ontologyId, String ontologyUserId, String versionNumber) {
		priorOntologyInfo = getPriorOntologyInfo();
		priorOntologyInfo.ontologyId = ontologyId;
		priorOntologyInfo.ontologyUserId = ontologyUserId;
		priorOntologyInfo.versionNumber = versionNumber;
	}

	
	public HostingType getHostingType() {
		return hostingType;
	}

	public void setHostingType(HostingType hostingType) {
		this.hostingType = hostingType;
	}



	/**
	 * @return the authority
	 */
	public String getAuthority() {
		return authority;
	}


	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}


	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}


	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the metadataValues
	 */
	public Map<String, String> getMetadataValues() {
		return metadataValues;
	}

	/**
	 * @param metadataValues the metadataValues to set
	 */
	public void setMetadataValues(Map<String, String> metadataValues) {
		this.metadataValues = metadataValues;
	}


	/**
	 * @return the dataCreationInfo
	 */
	public DataCreationInfo getDataCreationInfo() {
		return dataCreationInfo;
	}


	/**
	 * @param dataCreationInfo the dataCreationInfo to set
	 */
	public void setDataCreationInfo(DataCreationInfo dataCreationInfo) {
		this.dataCreationInfo = dataCreationInfo;
	}


	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}



	public BaseOntologyInfo getBaseOntologyInfo() {
		return baseOntologyInfo;
	}



	public void setBaseOntologyInfo(BaseOntologyInfo baseOntologyInfo) {
		this.baseOntologyInfo = baseOntologyInfo;
	}
	

}
