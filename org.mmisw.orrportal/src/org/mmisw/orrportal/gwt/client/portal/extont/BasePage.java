package org.mmisw.orrportal.gwt.client.portal.extont;


import org.mmisw.orrportal.gwt.client.portal.wizard.WizardPageBase;

/**
 * Base class for wizard pages in this package.
 * 
 * @author Carlos Rueda
 */
abstract class BasePage extends WizardPageBase {
	
	// false: do not retrieve RDF contents from server.
	protected static final boolean INCLUDE_RDF = false;
	
	
	protected BasePage(BaseWizard wizard,
			boolean includeBack, boolean includeNext
	) {
		this(wizard, includeBack, includeNext, false);
	}

	protected BasePage(BaseWizard wizard,
			boolean includeBack, boolean includeNext, boolean includeFinish
	) {
		super(wizard, includeBack, includeNext, includeFinish);
	}
	
	@Override
	public BaseWizard getWizard() {
		return (BaseWizard) wizard;
	}
}
