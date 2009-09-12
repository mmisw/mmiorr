package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.UserInfoResult;
import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.user.client.Timer;
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
 * Panel for showing/creating/updating a user account.
 * 
 * @author Carlos Rueda
 */
public class UserAccountPanel extends VerticalPanel {
	private final HorizontalPanel widget = new HorizontalPanel();
	
	private final CellPanel container = new VerticalPanel();
	
	
	private static class Entry {
		String label;
		TextBox tb;
		Entry(String label, TextBox tb) {
			super();
			this.label = label;
			this.tb = tb;
		}
		
	}
	private final Map<String,Entry> tbs = new LinkedHashMap<String, Entry>();
	
	
	
	private final PushButton createUpdateButton = new PushButton("Create/Update", new ClickListener() {
		public void onClick(Widget sender) {
			createUpdate();
		}
	});

	
	private final HTML statusLabel = new HTML("");
	
	
	private void _addTb(String name, String label) {
		TextBox tb = new TextBox();
		tb.setWidth("200px");
		tbs.put(name, new Entry(label, tb));
	}
	private void _addPwTb(String name, String label) {
		TextBox tb = new PasswordTextBox();
		tb.setWidth("200px");
		tbs.put(name, new Entry(label, tb));
	}
	
	public UserAccountPanel() {
		
		_addTb("username", "Username");
		_addTb("firstname", "First name");
		_addTb("lastname", "Last name");
		_addTb("email", "E-mail");
		_addTb("phone", "Phone");
		_addPwTb("password", "Password");
		_addPwTb("password2", "Verify password");
		
		
		widget.setWidth("600px");
		widget.setHorizontalAlignment(ALIGN_CENTER);
		
		container.setSpacing(4);
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    widget.add(decPanel);

	    container.add(createForm());
	    
	    statusLabel.setHeight("20px");
	    
	    KeyboardListener kl = new KeyboardListenerAdapter() {
	    	@Override
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
	    		statusLabel.setText("");
	    		if ( keyCode == KeyboardListener.KEY_ENTER ) {
	    			_cancelKey();
	    			justCheck();
	    		}
			}
	    };
	    
	    for ( Entry entry : tbs.values() ) {
	    	entry.tb.addKeyboardListener(kl);
	    }
	}
	
	public Widget getWidget() {
		return widget;
	}
	
	public void dispatch() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		String userLoggedIn = (loginResult != null && loginResult.getError() == null) ?
				loginResult.getUserName() : null
		;


		String nameToFocus;
		if ( userLoggedIn != null ) {
			tbs.get("username").tb.setReadOnly(true);
			nameToFocus = "firstname";
			dispatchUpdate(userLoggedIn);
		}
		else {
			nameToFocus = "username";
			dispatchCreate();
		}
		final TextBox tb2Focus = tbs.get(nameToFocus).tb;
		
		// use a timer to make the userPanel focused (there must be a better way)
		new Timer() {
			public void run() {
				tb2Focus.setFocus(true);
				tb2Focus.selectAll();
			}
		}.schedule(700);

	}
	
	private void dispatchUpdate(String username) {
		
		AsyncCallback<UserInfoResult> callback = new AsyncCallback<UserInfoResult>() {

			public void onFailure(Throwable ex) {
				String error = ex.getMessage();
				Main.log("Error getting user info: " +error);
				statusError("Error getting user info: " +error);
				_enable(true);
			}

			public void onSuccess(UserInfoResult result) {
				if ( result.getError() != null ) {
					Main.log("Error getting user info: " +result.getError());
					statusError(result.getError());
				}
				else {
					Main.log("user info ok" );

					Map<String, String> props = result.getProps();
					for ( String name : props.keySet() ) {
						Entry entry = tbs.get(name);
						if ( entry != null ) {
							entry.tb.setText(props.get(name));
						}
				    }

					statusMessage("");
				}
				_enable(true);
			}
			
		};
		
	    _enable(false);
		Main.ontmdService.getUserInfo(username, callback );	
	}
	
	
	private void dispatchCreate() {
		_enable(true);
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

		
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		String userLoggedIn = (loginResult != null && loginResult.getError() == null) ?
				loginResult.getUserName() : null;
		
		HTML tipForPassword = null;
		
		for ( String name : tbs.keySet() ) {
			Entry entry = tbs.get(name); 
			TextBox tb = entry.tb;
			
			if ( (tb instanceof PasswordTextBox) && userLoggedIn != null && tipForPassword == null ) {
				tipForPassword = new HTML("<font color=\"gray\"><i>Fill in the following if you want to change your password:</i></font>");
				panel.getFlexCellFormatter().setColSpan(row, 0, 3);
				panel.setWidget(row, 0, tipForPassword);
				panel.getFlexCellFormatter().setAlignment(row, 0, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
				);
				row++;
			}
			
			panel.setWidget(row, 0, new Label(entry.label+ ":"));

			panel.setWidget(row, 1, tb);
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			
			row++;
			
		}

		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, statusLabel);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		createUpdateButton.setText(userLoggedIn == null ? "Create" : "Update");
		HorizontalPanel loginCell = new HorizontalPanel();
		loginCell.add(createUpdateButton);
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, loginCell);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		tbs.get("username").tb.setFocus(true);
		return panel;
	}
	
	
	private void statusMessage(String msg) {
		statusLabel.setHTML("<font color=\"green\">" +msg+ "</font>");
	}

	public void getFocus() {
		tbs.get("username").tb.setFocus(true);
		tbs.get("username").tb.selectAll();
	}
	
	private void statusError(String error) {
		statusLabel.setHTML("<font color=\"red\">" +error+ "</font>");
	}

	
	private Map<String,String> checkFields(String userName) {
		Map<String,String> values = new HashMap<String,String>();
		for (  String name : tbs.keySet() ) {
			Entry entry = tbs.get(name);
			TextBox tb = entry.tb;
			String value = tb.getText().trim();
			values.put(name, value);
			
			if ( tb instanceof PasswordTextBox ) {
				continue;  // passwords checked below
			}
			
			if ( value.length() == 0 ) {
				statusError("Missing value for field: " +entry.label);
				tb.setFocus(true);
				tb.selectAll();
				return null;
			}
			else if ( name.equals("email") ) {
				// basic check:  something@something:
				String[] toks = value.split("@");
				if ( toks.length != 2 ) {
					statusError("Malformed email address. Expected name and domain");
					tb.setFocus(true);
					tb.selectAll();
					return null;
				}
			}
		}
		
		boolean checkPassword = true;
		
		if ( userName != null ) {
			// Update. do not check passwords if both fields are empty:
			if ( values.get("password").length() == 0 && values.get("password2").length() == 0 ) {
				checkPassword = false;
			}
		}

		if ( checkPassword ) {
			// now, check passwords:
			if ( values.get("password").length() == 0 ) {
				statusError("Missing password");
				tbs.get("password").tb.setFocus(true);
				return null;
			}
			else if ( ! values.get("password").equals(values.get("password2")) ) {
				statusError("Password mismatch");
				tbs.get("password").tb.setFocus(true);
				return null;
			}
		}
		
		return values;
	}
	
	
	private void justCheck() {
		
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		String userName = null;
			
		if ( loginResult != null && loginResult.getError() == null ) {
			userName = loginResult.getUserName();
		}

		checkFields(userName);
	}
	
	private void createUpdate() {
		
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		String userName = null;
		String userId = null;
		String sessionId = null;
			
		if ( loginResult != null && loginResult.getError() == null ) {
			userName = loginResult.getUserName();
			userId = loginResult.getUserId();
			sessionId = loginResult.getSessionId();
		}

		Map<String, String> values = checkFields(userName);
		
		if ( values != null ) {
			doCreateUpdate(userId, sessionId, values);
		}
	}
	
	
	private void doCreateUpdate(final String userId, String sessionId, Map<String,String> values) {
		
		AsyncCallback<CreateUpdateUserAccountResult> callback = new AsyncCallback<CreateUpdateUserAccountResult>() {

			public void onFailure(Throwable ex) {
				String error = ex.getMessage();
				Main.log("login error: " +error);
				statusError("Error creating/updating account: " +error);
				_enable(true);
			}

			public void onSuccess(CreateUpdateUserAccountResult result) {
				if ( result.getError() != null ) {
					Main.log("login error: " +result.getError());
					statusError(result.getError());
				}
				else {
					boolean created = userId == null;
					String msg = "Account " + (created ? "created" : "updated");
					Main.log(msg);
					statusMessage(msg);
					LoginResult loginResult = result.getLoginResult();
					PortalControl.getInstance().userAccountCreatedOrUpdated(created, loginResult);
				}
				_enable(true);
			}
		};
		
		if (userId != null ) {
			values.put("id", userId);
			values.put("sessionid", sessionId);
			Main.log("Updating user account. UserId: " +userId);
			statusMessage("Updating user account. values=" +values);
		}
		else {
			if ( sessionId == null || sessionId.trim().length() == 0 ) {
				sessionId = "33333333333333";
			}
			values.put("sessionid", sessionId);
			Main.log("Creating user account. values=" +values);
			statusMessage("Creating user account ...");
		}
		_enable(false);

		Main.ontmdService.createUpdateUserAccount(values, callback);
	}

	
	private void _enable(boolean enable) {
		createUpdateButton.setEnabled(enable);
	}
	
	
	private void _cancelKey() {
	    for ( Entry entry : tbs.values() ) {
	    	entry.tb.cancelKey();
	    }
	}


}
