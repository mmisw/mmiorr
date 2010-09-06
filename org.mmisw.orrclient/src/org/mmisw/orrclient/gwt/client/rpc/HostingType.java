package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

/**
 * The type of hosting (to be) supported by ORR.
 * See <a href="http://marinemetadata.org/mmiorrusrman/mmiorrref/mmiorruploadexisting">this</a>
 *  
 * @author Carlos Rueda
 */
public enum HostingType implements Serializable {
	
	FULLY_HOSTED("Fully hosted ontology"), 
	RE_HOSTED("Re-hosted ontology"), 
	INDEXED("Indexed ontology"),
	;
	
	private String label;
	private HostingType(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	
}
