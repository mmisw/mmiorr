package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.ontmd.gwt.client.portal.PortalConsts;

/**
 * The starting page when the user indicates indexing type.
 * The action is not yet implemented.
 * 
 * @author Carlos Rueda
 */
class RegisterNewPageIndexed extends RegisterNewPageReHosted {
	
	RegisterNewPageIndexed(RegisterNewWizard wizard) {
		super(wizard, true, false, false);
		nextButton.setEnabled(false);
		finishButton.setEnabled(false);
	}
	
	void updateUri(String uri) {
		infoHtml.setHTML(
				"<font color=\"red\">(not implemented yet)</font>" +
				"<br/>" +
				"<br/>" +
				"You have chosen your ontology <b>" +uri+ "</b>" +
				"<br/>" +
				"to be <b>indexed</b> at the MMI ORR." +
				"<br/>" +
				"<br/>" +
				"With this option, the MMI ORR only analyzes your ontology so its contents are integrated into " +
				"the repository's knowledge base for search and mapping purposes. " +
				"<br/>" +
				"<br/>" +
				"Please, see this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
						">manual page</a> for details." +
				"<br/>" +
				"<br/>" +
				"Click Finish to complete the registration. "
		);
	}

}
