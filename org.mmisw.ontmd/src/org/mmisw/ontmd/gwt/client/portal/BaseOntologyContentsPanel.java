package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;

import com.google.gwt.user.client.ui.Widget;

/**
 * Base class handling viewing/editing of contents for the different type of ontologies.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseOntologyContentsPanel {
	
	private boolean active = true;
	
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

	/** Called to indicate that this panel should stop any command at the next available chance.
	 * See {@link #cancelRequested()}.
	 */
	public void cancel() {
		active = false;
	}

	/**
	 * A subclass should call this method at approrpiate places, particularly during the execution of
	 * incremental commands and stop activity if this returns true.
	 */
	public boolean cancelRequested() {
		return ! active;
	}

	/**
	 * Checks the data.
	 * 
	 * @return null if OK.  Otherwise an error message that can be displayed to the user.
	 */
	public abstract String checkData();

}
