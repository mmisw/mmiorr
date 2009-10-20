package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

class RegisterNewExecute {
	
	private CreateOntologyInfo createOntologyInfo;

	
	RegisterNewExecute(CreateOntologyInfo createOntologyInfo) {
		this.createOntologyInfo = createOntologyInfo;
	}
	
	
	/**
	 * Call this to review and register an new ontology (not a new version of an existing registered ontology).
	 */
	void reviewAndRegisterNewOntology() {
		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		
		BaseOntologyInfo ontologyInfo = createOntologyInfo.getBaseOntologyInfo();
		
		assert ! ( ontologyInfo instanceof RegisteredOntologyInfo ) ;
		
		
		// Ok, put the new values in the ontologyInfo object:
		ontologyInfo.getOntologyMetadata().setNewValues(newValues);
		
		
		createOntologyInfo.setUri(ontologyInfo.getUri());

		Main.log("reviewAndRegisterNewOntology starting.  HostingType: " +createOntologyInfo.getHostingType());
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setSize("600", "150");
		popup.getTextArea().setText("please wait ...");
		PortalControl.getInstance().notifyActivity(true);
		popup.setText("Creating ontology ...");
		popup.center();
		popup.show();

		AsyncCallback<CreateOntologyResult> callback = new AsyncCallback<CreateOntologyResult>() {
			public void onFailure(Throwable thr) {
				PortalControl.getInstance().notifyActivity(false);
				Window.alert(thr.toString());
			}

			public void onSuccess(CreateOntologyResult result) {
				Main.log("CreateOntologyResult obtained.");
				PortalControl.getInstance().notifyActivity(false);
				reviewCompleted(popup, result);
			}
		};

		Main.log("Calling ontmdService.createOntology ...");
		Main.ontmdService.createOntology(createOntologyInfo, callback);
	}

	
	private void reviewCompleted(final MyDialog popup, final CreateOntologyResult createOntologyResult) {
		String error = createOntologyResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);

		if ( error == null ) {
			vp.add(new Label("Ontology URI: " +createOntologyResult.getUri()));
			
			vp.add(new Label("You can now register your ontology or close this " +
					"dialog to continue editing the contents."));
			
			// prepare uploadButton
			PushButton registerButton = new PushButton("Register", new ClickListener() {
				public void onClick(Widget sender) {
					register(popup, true, createOntologyResult);
				}
			});
			registerButton.setTitle("Registers the new version of the ontology");

			popup.getButtonsPanel().insert(registerButton, 0);
			
//			vp.add(new Label("Contents:"));
			
//			metadataPanel.resetToNewValues(ontologyInfoPre, createOntologyResult, false, false);
			
//			sb.append(createOntologyResult.getRdf());
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		
		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null 
				? "Ontology ready to be registered" 
				: "Error");
		popup.center();

		Main.log("Review result: " +msg);

	}

	private void register(MyDialog popup, boolean confirm, CreateOntologyResult createOntologyResult) {
		if ( confirm && 
			! Window.confirm("This action will commit your ontology into the MMI Registry") ) {
			return;
		}
		doRegister(popup, createOntologyResult);
	}

	private void doRegister(MyDialog createPopup, CreateOntologyResult createOntologyResult) {
		
		createPopup.hide();
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		popup.getTextArea().setSize("600", "150");
		
		Main.log("Registering ontology ...");
		popup.setText("Registering ontology ...");
		popup.center();
		popup.show();


		AsyncCallback<RegisterOntologyResult> callback = new AsyncCallback<RegisterOntologyResult>() {
			public void onFailure(Throwable thr) {
				Window.alert(thr.toString());
			}

			public void onSuccess(RegisterOntologyResult result) {
				registrationCompleted(popup, result);
			}
		};

		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		Main.ontmdService.registerOntology(createOntologyResult, loginResult , callback);
	}

	private void registrationCompleted(MyDialog registrationPopup, final RegisterOntologyResult uploadOntologyResult) {
		
		registrationPopup.hide();
		
		String error = uploadOntologyResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(6);
		
		if ( error == null ) {

			String uri = uploadOntologyResult.getUri();

			vp.add(new HTML("<font color=\"green\">Congratulations!</font> "
					+ "Your ontology is now registered."
			));
			
			
			vp.add(new HTML("<br/>The URI of the ontology is: "
//					+ "<a href=\"" +uri+ "\">"
					+ uri
//					+ "</a>"
			));
			
			
			vp.add(new HTML("<br/>For diagnostics, this is the response from the back-end server:"));

			sb.append(uploadOntologyResult.getInfo());
			
			// and, disable all editing fields/buttons:
			// (user will have to start from the "load" step)
//			enable(false);
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		Main.log("Registration result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setText(error == null ? "Registration completed sucessfully" : "Error");
		popup.addTextArea(null).setText(msg);
		popup.getTextArea().setSize("600", "150");
		
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.center();
		
		popup.addPopupListener(new PopupListener() {
			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				PortalControl.getInstance().completedRegisterOntologyResult(uploadOntologyResult);
			}
		});
		popup.show();
	}



}
