package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Portal header panel.
 * 
 * @author Carlos Rueda
 */
public class HeaderPanel extends FlexTable {

	private static final String HELP_LINK = "http://marinemetadata.org/orr";

	private HorizontalPanel linksPanel = new HorizontalPanel();
	
	private Hyperlink browseLink = new Hyperlink("Browse", "browse");
	
	private HTML signInButton = new HTML("<u>Sign in</u>");

	private HTML signOutButton = new HTML("<u>Sign out</u>");
	
	private HTML helpButton = new HTML(
			"<a target=\"_blank\" href=\"" +HELP_LINK+ "\">Help</a>");


	
	HeaderPanel() {
		super();
		linksPanel.setSpacing(4);
		
//		linksPanel.setBorderWidth(1);
		
		signInButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				PortalControl.getInstance().userToSignIn();
			}
		});


		signOutButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				PortalControl.getInstance().userSignedOut();
			}
		});

		
		
		FlexTable flexPanel = this;
		flexPanel.setWidth("100%");
//		flexPanel.setBorderWidth(1);
		int row = 0;
		
		flexPanel.setWidget(row, 0, Main.images.mmior().createImage());
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		flexPanel.setWidget(row, 1, linksPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP
		);

	}
	
	void updateLinks(InterfaceType type, LoginResult loginResult) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		if ( loginResult != null ) {
			widgets.add(new HTML("<b>" +loginResult.getUserName()+ "</b>"));
		}

		switch ( type ) {
		case BROWSE:
			// nothing
			break;
		case ONTOLOGY_VIEW:
			widgets.add(browseLink);
			break;
		case ONTOLOGY_EDIT:
			// nothing
			break;
		}
		
		if ( loginResult == null ) {
			widgets.add(signInButton);
		}
		else {
			widgets.add(signOutButton);
		}
		
		widgets.add(helpButton);
		
		// now assign to linksPanel
		linksPanel.clear();
		String sep = null;
		for ( Widget widget : widgets ) {
			if ( sep != null ) {
				linksPanel.add(new Label(sep));
			}
			linksPanel.add(widget);	
			sep = "|";
		}

	}
	

}
