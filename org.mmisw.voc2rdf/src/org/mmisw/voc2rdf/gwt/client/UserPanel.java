package org.mmisw.voc2rdf.gwt.client;

import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Captures login info and starts the upload to the registry.
 * 
 * @author Carlos Rueda
 */
public class UserPanel extends VerticalPanel {

	private MainPanel mainPanel;
	private CellPanel container = new VerticalPanel();
	
	private TextBox userName;
	private PasswordTextBox userPassword;
	
	private PushButton loginButton;
	private PushButton logoutButton;
	private PushButton uploadButton;
	
	
	UserPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		container.setSpacing(4);
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    container.add(createForm());
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		panel.setCellSpacing(5);
		
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, new HTML("<strong>Your account in the MMI Registry</strong>"));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		panel.setWidget(row, 0, new Label("Username:"));
		userName = new TextBox();
		userName.setWidth("200");
		panel.setWidget(row, 1, userName);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		panel.setWidget(row, 0, new Label("Password:"));
		userPassword = new PasswordTextBox();
		userPassword.setWidth("200");
		panel.setWidget(row, 1, userPassword);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		loginButton = new PushButton("Log in", new ClickListener() {
			public void onClick(Widget sender) {
				login();
			}
		});
		panel.add(loginButton);

		logoutButton = new PushButton("Log out", new ClickListener() {
			public void onClick(Widget sender) {
				logout();
			}
		});
		panel.add(logoutButton);
		logoutButton.setEnabled(false);

		uploadButton = new PushButton("Upload", new ClickListener() {
			public void onClick(Widget sender) {
				upload();
			}
		});
		panel.add(uploadButton);
		uploadButton.setEnabled(false);

		return panel;
	}
	
	String putValues(Map<String, String> values) {
		return null;
	}

	private void login() {
		String username = userName.getText();
		String password = userPassword.getText();
		if ( username.trim().length() == 0 || password.trim().length() == 0 ) {
			Window.alert("Please provide your account information");
			return;
		}
		mainPanel.doLogin(username, password);
	}
	
	private void upload() {
		mainPanel.doUpload();
	}
	
	private void logout() {
		mainPanel.logout();
	}


	public void setLoginResult(LoginResult loginResult) {
		boolean ok = loginResult != null && loginResult.getError() == null;
		loginButton.setEnabled(! ok);
		logoutButton.setEnabled(ok);
		uploadButton.setEnabled(ok);
	}

}
