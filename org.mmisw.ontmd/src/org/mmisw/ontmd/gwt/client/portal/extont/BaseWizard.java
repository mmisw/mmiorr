package org.mmisw.ontmd.gwt.client.portal.extont;

import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel;
import org.mmisw.ontmd.gwt.client.portal.wizard.WizardBase;

/**
 * Base class for the wizards in this package. 
 * @author Carlos Rueda
 */
class BaseWizard extends WizardBase {

	private TempOntologyInfo tempOntologyInfo;
	

	protected BaseWizard(PortalMainPanel portalMainPanel) {
		super(portalMainPanel);
	}


	protected void setTempOntologyInfo(TempOntologyInfo tempOntologyInfo) {
		this.tempOntologyInfo = tempOntologyInfo;
	}


	protected TempOntologyInfo getTempOntologyInfo() {
		return tempOntologyInfo;
	}

}
