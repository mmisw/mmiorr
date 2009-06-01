package org.mmisw.ontmd.gwt.client;

import org.mmisw.ontmd.gwt.client.metadata.MainPanel;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Captures login info and starts the upload to the registry.
 * 
 * @author Carlos Rueda
 */
public class UploadPanel extends VerticalPanel {

//	private TextBox ontologyUri;
	
	private UserPanel userInfoPanel;
	
	private CellPanel buttons = createButtons();
	
	
	UploadPanel(MainPanel mainPanel) {
		super();
		setWidth("700");
		setSpacing(4);
		
		add(new HTML("Use this panel to upload your converted vocabulary to the MMI Registry and Repository"));
		
		Widget form = createForm();
		add(form );
		this.setCellHorizontalAlignment(form, ALIGN_CENTER);
		
		userInfoPanel = new UserPanel(mainPanel);
		add(userInfoPanel.getWidget());
		this.setCellHorizontalAlignment(userInfoPanel.getWidget(), ALIGN_CENTER);
		
		add(buttons);
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		panel.setWidth("700");
//		int row = 0;
//		
//		Label lbl = new Label("Ontology URI:");
//		panel.setWidget(row, 0, lbl);
//		panel.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		
//		ontologyUri = new TextBox();
//		ontologyUri.setWidth("200");
//		panel.setWidget(row, 1, ontologyUri);
//		panel.getFlexCellFormatter().setAlignment(row, 1, 
//				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		row++;
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		
		return panel;
	}

	public void setLoginResult(LoginResult loginResult) {
		userInfoPanel.setLoginResult(loginResult);
	}
	

}
