package org.mmisw.ontmd.gwt.client.portal.extont;


import org.mmisw.ontmd.gwt.client.portal.PortalConsts;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The end page when the user indicates fully hosting type
 * 
 * @author Carlos Rueda
 */
class RegisterExternalOntologyPageFullyHosted extends RegisterExternalOntologyPageBase {
	
	private VerticalPanel contents = new VerticalPanel();
	
	
	RegisterExternalOntologyPageFullyHosted(RegisterExternalOntologyWizard wizard) {
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
				"You have chosen to have this ontology <b>fully hosted</b> at the MMI ORR." +
				"<br/>" +
				"<br/>" +
				"MMI ORR will assign an <i>http://mmisw.org/ont</i>-based namespace to your ontology " +
				"and associated terms sharing the same base namespace. More specifically, the xml:base " +
				"namespace associated with your ontology will be overwritten with one assigned by " +
				"MMI ORR during the registration process." +
				"<br/>" +
				"<br/>" +
				"Please, see this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
						">manual page</a> for details." +
				"<br/>" +
				"<br/>" +
				"Click Finish to proceed with providing metadata to this ontology and " +
				"then complete the registration."
		;
		panel.setWidget(row, 0, new HTML(info));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		contents.add(panel);
	}
	

}
