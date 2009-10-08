package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.ontmd.gwt.client.portal.PortalConsts;

/**
 * The starting page when the user indicates indexing type
 * 
 * @author Carlos Rueda
 */
class RegisterExternalOntologyPageIndexed extends RegisterExternalOntologyPageReHosted {
	
	RegisterExternalOntologyPageIndexed(RegisterExternalOntologyWizard wizard) {
		super(wizard);
	}
	
	void updateUri(String uri) {
		infoHtml.setHTML(
				"<br/>" +
				"You have chosen your ontology <b>" +uri+ "</b>" +
				"<br/>" +
				"to be just <b>indexed</b> at the MMI ORR." +
				"<br/>" +
				"<br/>" +
				"With this option, the MMI ORR only analyzes your ontology so its contents are integrated into " +
				"the repository's knowledge base for search and mapping purposes. " +
				"Your ontology will not appear in regular listings provided by the MMI ORR Portal." +
				"<br/>" +
				"<br/>" +
				"<br/>" +
				"Please, see this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
						">manual page</a> for details." +
				"<br/>" +
				"<br/>" +
				"Click Finish to complete the registration. " +
				"<font color=\"red\">(not implemented yet)</font>"
		);
	}

}
