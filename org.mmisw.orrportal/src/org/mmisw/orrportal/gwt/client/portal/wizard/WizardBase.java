package org.mmisw.orrportal.gwt.client.portal.wizard;

import org.mmisw.orrportal.gwt.client.portal.PortalMainPanel;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sequence of wizard pages to do some task.
 * 
 * <p>
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
public class WizardBase {
	
	protected final PortalMainPanel portalMainPanel;
	
	
	protected VerticalPanel contents = new VerticalPanel();
	
	protected HTML statusLoad = new HTML();
	
	/**
	 * @param portalMainPanel 
	 */
	protected WizardBase(PortalMainPanel portalMainPanel) {
		this.portalMainPanel = portalMainPanel;
		contents.setSize("650px", "300px");
		statusLoad.setText("");
	}
	
	
	public Widget getWidget() {
		return contents;
	}

	protected void pageNext(WizardPageBase currentPage) {
		
	}
	
	protected void pageBack(WizardPageBase currentPage) {
	}


	protected void finish(WizardPageBase currentPage) {
	}

}
