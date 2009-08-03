package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.util.TLabel;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handles the information about the resulting URI for the ontology.
 */
class OntologyUriPanel extends HorizontalPanel {

	private static String defaultNameSpace;
	private TLabel tlabel;
	
	private String namespaceRoot;
	private boolean userGiven = false;
	
	private String authority;
	private String shortName;
	
	private HTML uriHtml = new HTML();
	
	private PushButton userUriButton = new PushButton("Set", new ClickListener() {
		public void onClick(Widget sender) {
			promptUserUri(getAbsoluteLeft(), getAbsoluteTop());
		}
	});
	
	/**
	 * 
	 */
	OntologyUriPanel() {
		super();
		setStylePrimaryName("TermTable-OddRow");
		setSpacing(3);
		setVerticalAlignment(ALIGN_MIDDLE);

		userUriButton.setTitle("Allows you to set the URI");
		DOM.setElementAttribute(userUriButton.getElement(), "id", "my-button-id");
		
		if ( defaultNameSpace == null ) {
			defaultNameSpace = Portal.portalBaseInfo.getOntServiceUrl();
		}

		tlabel = new TLabel("", 
				"This is the URI that will be given to the resulting ontology.<br/> " +
				"<br/>" +
				"By default, this URI is automatically composed from the authority and class name fields and according to " +
				"<a target=\"_blank\" href=\"http://marinemetadata.org/apguides/ontprovidersguide/ontguideconstructinguris\"" +
				">MMI recommendations</a>.<br/> " +
				"<br/>" +
				defaultNameSpace+ " will be given as the server and root, and the values<br/> " +
				"entered in the authority and class name fields will be used to complete the authority and shortName components. <br/>" +
				"<br/>" +
				"Use the \"Set\" button if you prefer different server/root values."
		);
		
		HorizontalPanel hp = this;
		hp.add(tlabel);
		hp.add(uriHtml);
		hp.add(userUriButton);
	}

	/** displays a popup to prompt the user for the URI or revert to default assignment 
	 * @param left 
	 * @param top 
	*/
	void promptUserUri(int left, int top) {
		final TextBoxBase textBox = new TextBox();
		final MyDialog popup = new MyDialog(textBox) {
			public boolean onKeyUpPreview(char key, int modifiers) {
				// avoid ENTER from closing the popup without proper reaction
				if ( key == KeyboardListener.KEY_ESCAPE ) {
					hide();      // only ESCAPE keystroke closes the popup
					return false;
				}
				
				if ( key == KeyboardListener.KEY_ENTER ) {
					String str = textBox.getText().trim();
					_processAccept(str, this);
				}
			    return true;
			  }
		};
		popup.setText("Specify the namespace root for the ontology URI");
		
		textBox.setWidth("300");
		if ( namespaceRoot != null ) {
			textBox.setText(namespaceRoot);
		}

		popup.getButtonsPanel().insert(
				new PushButton("OK", new ClickListener() {
					public void onClick(Widget sender) {
						String str = textBox.getText().trim();
						_processAccept(str, popup);
					}
				})
				, 0
		);

		if ( userGiven ) {
			popup.getButtonsPanel().insert(
					new PushButton("Revert to default", new ClickListener() {
						public void onClick(Widget sender) {
							userGiven = false;
							namespaceRoot = defaultNameSpace;
							update();
							popup.hide();
						}
					})
					, 0
			);
		}
		
		popup.setPopupPosition(left, top + 20);
		new Timer() { @Override
			public void run() {
				textBox.setFocus(true);
			}
		}.schedule(180);
		
		popup.show();

	}
	
	private void _processAccept(String str, MyDialog popup) {
		if ( str.length() > 0 ) {
			namespaceRoot = str;
			userGiven = true;
			
			if ( ! namespaceRoot.endsWith("/") ) {
				namespaceRoot += "/";
			}
			
			update();
			popup.hide();	
		}
	}

	
	void update() {
		update(this.authority, this.shortName);
	}

	void updateAuthority(String authority) {
		update(authority, this.shortName);
	}

	void updateShortName(String shortName) {
		update(this.authority, shortName);
	}

	void update(String authority, String shortName) {
		authority = authority == null ? null : authority.toLowerCase();
		shortName = shortName == null ? null : shortName.toLowerCase();
		
		this.authority = authority;
		this.shortName = shortName;

		String uri;
		
		//
		// replace any colon (:) in the pieces that go to the ontology URI
		// with underscores (_):
		//
		if ( authority == null || authority.length() == 0 ) {
			authority = "<font color=\"red\">auth</font>";
		}
		else {
			authority = authority.replace(':', '_');
		}

		// TODO handle className vs. specific shortName field
		if ( shortName == null || shortName.length() == 0 ) {
			shortName = "<font color=\"red\">shortName</font>";
		}
		else {
			shortName = shortName.replace(':', '_');
		}

		uri = namespaceRoot + authority+ "/" +shortName+ "/";
		
		uriHtml.setHTML(
				"<code>" +
				uri +
				"</code>"
		);
	}
}
