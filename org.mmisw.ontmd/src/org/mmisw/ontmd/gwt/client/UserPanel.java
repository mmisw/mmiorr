package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.metadata.MainPanel;
import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
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
	private HTML statusLabel = new HTML("");
	
	
	public UserPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		container.setSpacing(4);
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    container.add(createForm());
	    
	    KeyboardListener kl = new KeyboardListenerAdapter() {
	    	@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
	    		statusLabel.setText("");
	    		if ( keyCode == KeyboardListener.KEY_ENTER ) {
	    			userName.cancelKey();
	    			userPassword.cancelKey();
	    			login();
	    		}
			}
	    };
	    userName.addKeyboardListener(kl);
	    userPassword.addKeyboardListener(kl);
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
		
		HTML info = new HTML();
		info.setHTML("Not registered? Forgot your password? " +
				"Please click <a href=\"/or/login\" target=\"_blank\">here</a>"
		);
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, info);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		userName.setFocus(true);
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		panel.add(statusLabel);
		
		loginButton = new PushButton("Log in", new ClickListener() {
			public void onClick(Widget sender) {
				login();
			}
		});
		panel.add(loginButton);

//		logoutButton = new PushButton("Log out", new ClickListener() {
//			public void onClick(Widget sender) {
//				logout();
//			}
//		});
//		panel.add(logoutButton);
//		logoutButton.setEnabled(false);
//
//		uploadButton = new PushButton("Upload", new ClickListener() {
//			public void onClick(Widget sender) {
//				upload();
//			}
//		});
//		panel.add(uploadButton);
//		uploadButton.setEnabled(false);

		return panel;
	}
	
	String putValues(Map<String, String> values) {
		return null;
	}
	
	private void statusMessage(String msg) {
		statusLabel.setHTML("<font color=\"green\">" +msg+ "</font>");
	}

	private void statusError(String error) {
		statusLabel.setHTML("<font color=\"red\">" +error+ "</font>");
		userName.setFocus(true);
		userName.selectAll();
	}

	private void login() {
		String username = userName.getText();
		String password = userPassword.getText();
		if ( username.trim().length() == 0 ) {
			statusError("Missing username");
			userName.setFocus(true);
			return;
		}
		else if ( password.trim().length() == 0 ) {
			statusError("Missing password");
			userPassword.setFocus(true);
			return;
		}
		
		doLogin(username, password);
	}
	
	void doLogin(String userName, String userPassword) {
		
		AsyncCallback<LoginResult> callback = new AsyncCallback<LoginResult>() {

			public void onFailure(Throwable ex) {
				String error = ex.getMessage();
				Main.log("login error: " +error);
				statusError("Error validating credentials: " +error);
				loginButton.setEnabled(true);
			}

			public void onSuccess(LoginResult loginResult) {
				if ( loginResult.getError() != null ) {
					Main.log("login error: " +loginResult);
					statusError(loginResult.getError());
				}
				else {
					Main.log("login ok: " +loginResult);
					statusMessage("OK");
					mainPanel.loginOk(loginResult);
				}
				loginButton.setEnabled(true);
//				loginPanel.setLoginResult(loginResult);
			}
			
		};
		Main.log("login ...");
		statusMessage("Verifying...");
		loginButton.setEnabled(false);
		Main.ontmdService.login(userName, userPassword, callback);

	}

	
//	private void upload() {
//		mainPanel.doUpload();
//	}
//	
//	private void logout() {
//		mainPanel.logout();
//	}


	public void setLoginResult(LoginResult loginResult) {
		boolean ok = loginResult != null && loginResult.getError() == null;
		loginButton.setEnabled(! ok);
		logoutButton.setEnabled(ok);
		uploadButton.setEnabled(ok);
	}

}
