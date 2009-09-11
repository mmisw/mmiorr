package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateUpdateUserAccountResult;
import org.mmisw.ontmd.gwt.client.Main;

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
	private final VerticalPanel widget = new VerticalPanel();
	
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
		_addTb("email", "E-mail");
		_addTb("firstname", "First name");
		_addTb("lastname", "Last name");
		_addTb("phone", "Phone");
		_addPwTb("password", "Password");
		_addPwTb("password2", "Verify password");
		
		
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
	    			createUpdate();
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

		
		for ( String name : tbs.keySet() ) {
			Entry entry = tbs.get(name); 
			TextBox tb = entry.tb;
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

		
		HorizontalPanel loginCell = new HorizontalPanel();
		loginCell.add(createUpdateButton);
		panel.setWidget(row, 0, loginCell);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, statusLabel);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
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
		tbs.get("username").tb.setFocus(true);
		tbs.get("username").tb.selectAll();
	}

	private void createUpdate() {
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
				return;
			}
		}

		// now, check passwordsL
		if ( values.get("password").length() == 0 ) {
			statusError("Missing password");
			tbs.get("password").tb.setFocus(true);
			return;
		}
		else if ( ! values.get("password").equals(values.get("password2")) ) {
			statusError("Password mismatch");
			tbs.get("password").tb.setFocus(true);
			return;
		}
		
		doCreateUpdate(values);
	}
	
	void doCreateUpdate(Map<String,String> values) {
		
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
					Main.log("account ok" );
					statusMessage("OK");
//					loginListener.loginOk(loginResult);
				}
				_enable(true);
			}
			
		};
		Main.log("Creating/updating user account ...");
		statusMessage("Creating/updating user account ...");
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
