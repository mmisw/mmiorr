package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.ResetPasswordResult;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
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
public class LoginPanel {

	private VerticalPanel widget = new VerticalPanel();
	
	private LoginListener loginListener;
	private CellPanel container = new VerticalPanel();
	
	private TextBox userName;
	private PasswordTextBox userPassword;
	
	private PushButton loginButton = new PushButton("Sign in", new ClickListener() {
		public void onClick(Widget sender) {
			login();
		}
	});

	private PushButton resetPasswordButton = new PushButton("Reset password", new ClickListener() {
		public void onClick(Widget sender) {
			resetPassword();
		}
	});
	
	private PushButton createAccountButton = new PushButton("Create account", new ClickListener() {
		public void onClick(Widget sender) {
			createAccount();
		}

	});
	
	private HTML statusLabel = new HTML("");
	
	
	public LoginPanel(LoginListener loginListener) {
		this.loginListener = loginListener;
		container.setSpacing(4);
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    widget.add(decPanel);

	    container.add(createForm());
	    
	    statusLabel.setHeight("20px");
	    DOM.setElementAttribute(resetPasswordButton.getElement(), "id", "my-button-id");
	    DOM.setElementAttribute(createAccountButton.getElement(), "id", "my-button-id");
	    
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
	
	public Widget getWidget() {
		return widget;
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		panel.setCellSpacing(5);
		
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, new HTML("<strong>Your account in the MMI Ontology Registry and Repository</strong>"));
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
		
		HorizontalPanel loginCell = new HorizontalPanel();
		loginCell.add(loginButton);
		panel.setWidget(row, 2, loginCell);
		panel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, statusLabel);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		HorizontalPanel infoResetPassword = new HorizontalPanel();
		infoResetPassword.setSpacing(3);
		infoResetPassword.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		infoResetPassword.add(new HTML("Forgot your password?"));
		
		infoResetPassword.add(resetPasswordButton);
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, infoResetPassword);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		HorizontalPanel infoCreateAccount = new HorizontalPanel();
		infoCreateAccount.setSpacing(3);
		infoCreateAccount.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		infoCreateAccount.add(new HTML("Not registered?"));
		
		infoCreateAccount.add(createAccountButton);
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, infoCreateAccount);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		userName.setFocus(true);
		return panel;
	}
	
	public void logout() {
		userPassword.setText("");
		statusMessage("");
	}
	
	String putValues(Map<String, String> values) {
		return null;
	}
	
	private void statusMessage(String msg) {
		statusLabel.setHTML("<font color=\"green\">" +msg+ "</font>");
	}

	public void getFocus() {
		userName.setFocus(true);
		userName.selectAll();
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
				_enable(true);
			}

			public void onSuccess(LoginResult loginResult) {
				if ( loginResult.getError() != null ) {
					Main.log("login error: " +loginResult.getError());
					statusError(loginResult.getError());
				}
				else {
					Main.log("login ok: " +loginResult.getUserName());
					statusMessage("OK");
					loginListener.loginOk(loginResult);
				}
				_enable(true);
			}
			
		};
		Main.log("Verifying ...");
		statusMessage("Verifying ...");
		_enable(false);
		Main.ontmdService.authenticateUser(userName, userPassword, callback);

	}

	
	private void resetPassword() {
		String username = userName.getText();
		if ( username.trim().length() == 0 ) {
			statusError("Missing username");
			userName.setFocus(true);
			return;
		}
		
		doResetPassword(username);
	}

	private void doResetPassword(String userName) {
		AsyncCallback<ResetPasswordResult> callback = new AsyncCallback<ResetPasswordResult>() {

			public void onFailure(Throwable ex) {
				String error = ex.getMessage();
				Main.log("Error resetting password: " +error);
				statusError("Error resetting password: " +error);
				_enable(true);
			}

			public void onSuccess(ResetPasswordResult result) {
				if ( result.getError() != null ) {
					Main.log("Error resetting password: " +result.getError());
					statusError(result.getError());
				}
				else {
					String email = result.getEmail();
					Main.log("Reset password OK. Email sent to: " +email);
					statusMessage("New password sent to: " +email);
				}
				_enable(true);
			}
			
		};
		Main.log("Resetting password for username: " +userName+ " ...");
		statusMessage("Resetting password...");
		_enable(false);

		Main.ontmdService.resetUserPassword(userName, callback);
	
	}


	public void setLoginResult(LoginResult loginResult) {
		boolean ok = loginResult != null && loginResult.getError() == null;
		_enable(! ok);
	}

	
	private void createAccount() {
		loginListener.loginCreateAccount();
		History.newItem("newaccount");
	}

	
	private void _enable(boolean enable) {
		loginButton.setEnabled(enable);
		resetPasswordButton.setEnabled(enable);
		createAccountButton.setEnabled(enable);
	}
}
