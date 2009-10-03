package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.ontmd.gwt.client.portal.PortalConsts;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The end page when the user indicates indexing type
 * 
 * @author Carlos Rueda
 */
class RegisterExternalOntologyPageIndexed extends RegisterExternalOntologyPageBase {
	
	private VerticalPanel contents = new VerticalPanel();
	
	
	RegisterExternalOntologyPageIndexed(RegisterExternalOntologyWizard wizard) {
		super(wizard, true, false, true);
		contents.setSize("650px", "200px");
		addContents(contents);
		
		nextButton.setEnabled(false);
		recreate();
	}
	
	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		String info = 
				"<br/>" +
				"You have chosen to have this ontology just <b>indexed</b> at the MMI ORR." +
				"<br/>" +
				"<br/>" +
				"With this option, the MMI ORR only analyzes your ontology so its contents are integrated into " +
				"the repository's knowledge base for search and mapping purposes. " +
				"Your ontology will not appear in regular listings provided by the MMI ORR Portal." +
				"<br/>" +
				"<br/>" +
				"<br/>" +
				"<br/>" +
				"Please, see this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
						">manual page</a> for details." +
				"<br/>" +
				"<br/>" +
				"Click Finish to complete the registration. " +
				"<font color=\"red\">(not implemented yet)</font>"
		;
		panel.setWidget(row, 0, new HTML(info));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contents.add(panel);
	}
	

}
