package org.mmisw.ontmd.gwt.client.portal;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;

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

	private HorizontalPanel linksPanel1 = new HorizontalPanel();
	private HorizontalPanel linksPanel2 = new HorizontalPanel();
	
	private Hyperlink browseLink = new Hyperlink("Browse", PortalConsts.T_BROWSE);
	private Hyperlink searchLink = new Hyperlink("Search", PortalConsts.T_SEARCH);
	
	private Hyperlink accountLink = new Hyperlink("Create account", PortalConsts.T_USER_ACCOUNT);
	
	private Hyperlink signInLink = new Hyperlink("Sign in", PortalConsts.T_SIGN_IN);
	
	private Hyperlink signOutLink = new Hyperlink("Sign out", PortalConsts.T_SIGN_OUT);
	
	private HTML helpButton = new HTML(
			"<a target=\"_blank\" href=\"" +HELP_LINK+ "\">Help</a>");

	
	HeaderPanel() {
		super();
		linksPanel1.setSpacing(4);
		linksPanel2.setSpacing(4);
		
		FlexTable flexPanel = this;
		flexPanel.setWidth("100%");
//		flexPanel.setBorderWidth(1);
		int row = 0;
		
		flexPanel.getFlexCellFormatter().setRowSpan(row, 0, 2);
		flexPanel.setWidget(row, 0, Main.images.mmior().createImage());
		flexPanel.getFlexCellFormatter().setWidth(row, 0, "10%");
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		flexPanel.setWidget(row, 1, linksPanel1);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);

		row++;
		flexPanel.setWidget(row, 0, linksPanel2);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_BOTTOM
		);
	}
	
	void updateLinks(InterfaceType type) {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
	
		List<Widget> widgets = new ArrayList<Widget>();
		List<Widget> widgets2 = new ArrayList<Widget>();
		
		if ( loginResult != null ) {
//			widgets.add(new HTML("<b>" +loginResult.getUserName()+ "</b>"));
			accountLink.setText(loginResult.getUserName());
			widgets.add(accountLink);
		}

		switch ( type ) {
		case BROWSE:
			widgets2.add(searchLink);
			break;
		case ONTOLOGY_VIEW:
		case USER_ACCOUNT:
		case ENTITY_VIEW:
		case ENTITY_NOT_FOUND:
			widgets2.add(browseLink);
			widgets2.add(searchLink);
			break;
		case SEARCH: 
			widgets2.add(browseLink);
			break;
		case ONTOLOGY_EDIT_NEW_VERSION:
		case ONTOLOGY_EDIT_NEW:
			// nothing
			break;
		}
		
		if ( loginResult == null ) {
			widgets.add(signInLink);
			if ( type != InterfaceType.USER_ACCOUNT ) {
				accountLink.setText("Create account");
				widgets.add(accountLink);
			}
		}
		else {
			widgets.add(signOutLink);
		}
		
		widgets.add(helpButton);
		
		// now assign to linksPanels
		linksPanel1.clear();
		linksPanel2.clear();
		String sep = null;
		for ( Widget widget : widgets ) {
			if ( sep != null ) {
				linksPanel1.add(new Label(sep));
			}
			linksPanel1.add(widget);	
			sep = "|";
		}

		sep = null;
		for ( Widget widget : widgets2 ) {
			if ( sep != null ) {
				linksPanel2.add(new Label(sep));
			}
			linksPanel2.add(widget);	
			sep = "|";
		}

	}
	

}
