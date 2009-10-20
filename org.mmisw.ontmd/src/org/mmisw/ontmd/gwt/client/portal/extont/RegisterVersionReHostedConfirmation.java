package org.mmisw.ontmd.gwt.client.portal.extont;


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
class RegisterVersionReHostedConfirmation extends BasePage {
	private static final String ONT_URI = "XXXX";
	
	private static final String INFO_TEMPLATE = 
		"<br/>" +
		"You have chosen to have this ontology <b>re-hosted</b> at the MMI ORR." +
		"<br/>" +
		"<br/>" +
		"Ontology URI: <b>" +ONT_URI+ "</b> " +
		"<br/>" +
		"<br/>" +
		"Click Finish to complete the registration at the MMI ORR." +
		"<br/>" +
		"Click Back to change any information."
		;


	private VerticalPanel contents = new VerticalPanel();
	
	
	RegisterVersionReHostedConfirmation(RegisterVersionWizard wizard) {
		super(wizard, true, false, true);
		contents.setSize("650px", "200px");
		addContents(contents);
		
		finishButton.setEnabled(true);
		recreate();
	}
	
	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		String ontologyUri = getWizard().getOntologyUri();
		assert ontologyUri != null;
		
		panel.setWidget(row, 0, new HTML(INFO_TEMPLATE.replaceFirst(ONT_URI, ontologyUri)));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		contents.add(panel);
	}

	@Override
	public RegisterVersionWizard getWizard() {
		return (RegisterVersionWizard) wizard;
	}

}
