package org.mmisw.orrportal.gwt.client.portal.extont;

import org.mmisw.orrportal.gwt.client.portal.PortalConsts;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * In this page the user indicates whether a new file will be uploaded for the
 * new version.
 * 
 * TODO not implemented yet
 * 
 * @author Carlos Rueda
 */
class RegisterVersionPage0 extends BasePage {
	
	private VerticalPanel contents = new VerticalPanel();
	
	private CheckBox uploadFileRb = new CheckBox("I will upload ontology file for the new version");
	

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * 
	 * @param allowLoadOptions
	 */
	RegisterVersionPage0(RegisterVersionWizard wizard) {
		super(wizard, false, true);
		
		uploadFileRb.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				getWizard().setUploadFileIndicated(uploadFileRb.isChecked());
			}
		});
		contents.setSize("650px", "200px");
		addContents(contents);
		recreate();
	}
	
	
	private void recreate() {
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
		int row = 0;
		
		String info = "<br/>Will you upload a file with the contents for the new " +
				"version, or just edit the metadata for the current registered ontology?"; 
		
		panel.setWidget(row, 0, new HTML(info));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		contents.add(panel);
		contents.add(createWidget());
	}

	
	private Widget createWidget() {
		FlexTable panel = new FlexTable();
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, uploadFileRb);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		String info2 = 
			"<br/>" +
			"<br/>" +
			"See this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
					">manual page</a> for details."
		;
		panel.setWidget(row, 0, new HTML(info2));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		
		return panel;
	}

	@Override
	public RegisterVersionWizard getWizard() {
		return (RegisterVersionWizard) wizard;
	}

}
