package org.mmisw.ontmd.gwt.client.portal.admin;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.InternalOntologyResult;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Administrative interface.
 * 
 * @author Carlos Rueda
 */
public class AdminPanel extends VerticalPanel {

	private PushButton prepareUsersButton = new PushButton("Prepare users ontology", new ClickListener() {
		public void onClick(Widget sender) {
			_prepareUsers();
		}
	});
	
	private PushButton createGroupsButton = new PushButton("Create groups ontology", new ClickListener() {
		public void onClick(Widget sender) {
			_createGroups();
		}
	});
	
	
	private HTML statusHtml = new HTML();
	private TextArea infoTextArea = new TextArea();
	
	
	/**
	 */
	public AdminPanel() {
		infoTextArea.setReadOnly(true);
		infoTextArea.setSize("650px", "350px");
		super.setSpacing(5);
		
		prepareUsersButton.setTitle("Creates/updates the users instantiation ontology");
		createGroupsButton.setTitle("Creates the groups instantiation ontology");
		
		add(prepareUsersButton);
		add(createGroupsButton);
		
		add(statusHtml);
		add(infoTextArea);
	}


	private void _prepareUsers() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			statusHtml.setHTML("<font color=\"red\">Only an admin can run this.</font>");
			return;
		}
		Main.log("_prepareUsers called.");
		
		AsyncCallback<InternalOntologyResult> callback = new AsyncCallback<InternalOntologyResult>() {

			public void onFailure(Throwable caught) {
				String error = caught.getMessage();
				statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
				Main.log("Error preparing users: " +error);
			}

			public void onSuccess(InternalOntologyResult result) {
				Main.log("onSuccess: " +result);
				String error = result.getError();
				if ( error != null ) {
					statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
					Main.log("Error preparing users: " +error);
					return;
				}
				
				statusHtml.setHTML("<font color=\"blue\">" +"OK"+ "</font>. uri: " +result.getUri());
				String info = result.getInfo();
				infoTextArea.setText(info);
			}
			
		};
		
		statusHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
			"<i><font color=\"blue\">Preparing users ontology ...</font></i>");
		
		Main.ontmdService.prepareUsersOntology(loginResult, callback);
	}

	private void _createGroups() {
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			statusHtml.setHTML("<font color=\"red\">Only an admin can run this.</font>");
			return;
		}
		Main.log("_createGroups called.");
		
		AsyncCallback<InternalOntologyResult> callback = new AsyncCallback<InternalOntologyResult>() {

			public void onFailure(Throwable caught) {
				String error = caught.getMessage();
				statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
				Main.log("Error creating groups ontology: " +error);
			}

			public void onSuccess(InternalOntologyResult result) {
				Main.log("onSuccess: " +result);
				String error = result.getError();
				if ( error != null ) {
					statusHtml.setHTML("<font color=\"red\">" +error+ "</font>");
					Main.log("Error creating groups ontology: " +error);
					return;
				}
				
				statusHtml.setHTML("<font color=\"blue\">" +"OK"+ "</font>. uri: " +result.getUri());
				String info = result.getInfo();
				infoTextArea.setText(info);
			}
			
		};
		
		statusHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
			"<i><font color=\"blue\">Creating groups ontology ...</font></i>");
		
		Main.ontmdService.createGroupsOntology(loginResult, callback);
	}
	
}
