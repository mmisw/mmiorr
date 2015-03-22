package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

public abstract class OntologyData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private BaseOntologyData baseOntologyData;
	
	
	public OntologyData() {
	}


	/**
	 * @return the baseOntologyData
	 */
	public BaseOntologyData getBaseOntologyData() {
		return baseOntologyData;
	}


	/**
	 * @param baseOntologyData the baseOntologyData to set
	 */
	public void setBaseOntologyData(BaseOntologyData baseOntologyData) {
		this.baseOntologyData = baseOntologyData;
	}
	
	

}
