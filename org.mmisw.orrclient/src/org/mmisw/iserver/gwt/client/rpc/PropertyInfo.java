package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;

/**
 * Info about a property.
 * 
 * @author Carlos Rueda
 */
public class PropertyInfo extends EntityInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	/** URI of the domain class */
	private String domainUri;
	
	/** corresponding classInfo object */
	private ClassInfo domainClassInfo;
	
	private boolean isDatatypeProperty;
	private boolean isObjectProperty;
	
	
	public PropertyInfo() {
		
	}

	/**
	 * @return the isDatatypeProperty
	 */
	public boolean isDatatypeProperty() {
		return isDatatypeProperty;
	}

	/**
	 * @param isDatatypeProperty the isDatatypeProperty to set
	 */
	public void setDatatypeProperty(boolean isDatatypeProperty) {
		this.isDatatypeProperty = isDatatypeProperty;
	}

	/**
	 * @return the isObjectProperty
	 */
	public boolean isObjectProperty() {
		return isObjectProperty;
	}

	/**
	 * @param isObjectProperty the isObjectProperty to set
	 */
	public void setObjectProperty(boolean isObjectProperty) {
		this.isObjectProperty = isObjectProperty;
	}

	public void setDomainUri(String domainUri) {
		this.domainUri = domainUri;
	}

	public String getDomainUri() {
		return domainUri;
	}

	public void setDomainClassInfo(ClassInfo domainClassInfo) {
		this.domainClassInfo = domainClassInfo;
	}

	/**
	 * @return the domainClassInfo
	 */
	public ClassInfo getDomainClassInfo() {
		return domainClassInfo;
	}

	
}
