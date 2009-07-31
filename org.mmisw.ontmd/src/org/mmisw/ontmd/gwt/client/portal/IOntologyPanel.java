package org.mmisw.ontmd.gwt.client.portal;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;

/** created for refactoring purposes.  may be removed later */
public interface IOntologyPanel {

	public OntologyMetadata getOntologyMetadata();

	public void formChanged(Map<String, String> values);
}
