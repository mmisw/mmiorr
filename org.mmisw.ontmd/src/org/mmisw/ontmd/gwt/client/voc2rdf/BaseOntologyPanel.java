package org.mmisw.ontmd.gwt.client.voc2rdf;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class handling viewing/editing of contents for the different type of ontologies.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseOntologyPanel {
	
	private boolean readOnly = true;
	
	protected BaseOntologyPanel(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public abstract Widget getWidget();
	

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @param readOnly the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/** Called to indicate that this panel is no longer active.
	 * In particular, the panel should stop any execution of commands.
	 */
	public abstract void cancel();
	
	

}
