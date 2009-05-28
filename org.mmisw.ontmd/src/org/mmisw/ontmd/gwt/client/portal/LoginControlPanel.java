package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.rpc.LoginResult;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Carlos Rueda
 */
public class LoginControlPanel extends HorizontalPanel {
	
	private final PortalMainPanel portalMainPanel;
	
	private HTML signInButton = new HTML("<u>Sign in</u>");

	private HTML signOutButton = new HTML("<u>Sign out</u>");
	
	private HTML helpButton = new HTML(
			"<a target=\"_blank\" href=\"http://marinemetadata.org/or\">Help</a>");

	
	/**
	 * 
	 * @param portalMainPanel
	 */
	LoginControlPanel(PortalMainPanel portalMainPanel) {
		this.portalMainPanel = portalMainPanel;
		
		setSpacing(4);
		
		update(null);
		
		signInButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				_signIn();
			}
		});


		signOutButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				_signOut();
			}
		});
		
	}
	
	void update(LoginResult loginResult) {
		clear();
		if ( loginResult == null ) {
			add(signInButton);
		}
		else {
			add(new Label(loginResult.getUserName()));
			add(signOutButton);
		}
		add(helpButton);
	}

	private void _signOut() {
		portalMainPanel.userSignedOut();
	}

	private void _signIn() {
		portalMainPanel.userToSignIn();
	}

}
