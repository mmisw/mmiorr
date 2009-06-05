package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class handling viewing/editing of contents for the different type of ontologies.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseOntologyContentsPanel {
	
	private boolean readOnly = true;
	
	protected BaseOntologyContentsPanel(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public abstract Widget getWidget();
	
	public abstract DataCreationInfo getCreateOntologyInfo();

	
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
	
	
	/**
	 * Checks the data.
	 * 
	 * @return null if OK.  Otherwise an error message that can be displayed to the user.
	 */
	public abstract String checkData();

}
