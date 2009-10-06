package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.ontmd.gwt.client.portal.wizard.WizardPageBase;

/**
 * Base class for the wizard-like pages to register an external ontology.
 * 
 * @author Carlos Rueda
 */
abstract class RegisterExternalOntologyPageBase extends WizardPageBase {
	
	// false: do not retrieve RDF contents from server.
	protected static final boolean INCLUDE_RDF = false;
	
	
	protected RegisterExternalOntologyPageBase(RegisterExternalOntologyWizard wizard,
			boolean includeBack, boolean includeNext
	) {
		this(wizard, includeBack, includeNext, false);
	}

	protected RegisterExternalOntologyPageBase(RegisterExternalOntologyWizard wizard,
			boolean includeBack, boolean includeNext, boolean includeFinish
	) {
		super(wizard, includeBack, includeNext, includeFinish);
	}
	
	@Override
	protected RegisterExternalOntologyWizard getWizard() {
		return (RegisterExternalOntologyWizard) wizard;
	}
}
