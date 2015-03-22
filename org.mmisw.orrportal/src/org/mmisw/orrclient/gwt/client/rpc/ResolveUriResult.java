package org.mmisw.orrclient.gwt.client.rpc;

/**
 * Info about the result of a resolveUri operation.
 * 
 * Upon success, this result is either a {@link RegisteredOntologyInfo} or an {@link EntityInfo}.
 * Otherwise, if the requested URI is a URL, then {@lin #isUrl()} will return true (this
 * faciliates dispatching not-found URIs via external means (like just opening the link directly
 * in a browser--see PortalMainPanel in portal module). 
 * 
 * @author Carlos Rueda
 */
public class ResolveUriResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	/** the requested URI */
	private String uri;
	
	private RegisteredOntologyInfo registeredOntologyInfo;
	private EntityInfo entityInfo;
	
	private boolean isUrl;
	
	// no-arg ctor for serialization
	public ResolveUriResult() {
	}
	
	public ResolveUriResult(String uri) {
		this.uri = uri;
	}
	
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public RegisteredOntologyInfo getRegisteredOntologyInfo() {
		return registeredOntologyInfo;
	}
	public void setRegisteredOntologyInfo(RegisteredOntologyInfo registeredOntologyInfo) {
		this.registeredOntologyInfo = registeredOntologyInfo;
	}
	public EntityInfo getEntityInfo() {
		return entityInfo;
	}
	public void setEntityInfo(EntityInfo entityInfo) {
		this.entityInfo = entityInfo;
	}
	
	public boolean isUrl() {
		return isUrl;
	}
	public void setIsUrl(boolean isUrl) {
		this.isUrl = isUrl;
	}

}
