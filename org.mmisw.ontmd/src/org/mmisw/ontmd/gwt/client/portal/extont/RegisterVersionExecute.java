package org.mmisw.ontmd.gwt.client.portal.extont;

import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Orr;
import org.mmisw.ontmd.gwt.client.portal.PortalControl;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Use to review and register a new version of a registered ontology.
 *  
 * @author Carlos Rueda
 */
class RegisterVersionExecute {
	
	private final String CLASS_NAME = getClass().getName();
	
	private CreateOntologyInfo createOntologyInfo;

	
	RegisterVersionExecute(CreateOntologyInfo createOntologyInfo) {
		this.createOntologyInfo = createOntologyInfo;
	}
	
	
	/**
	 * Call this to review and register a new version of a registered ontology 
	 */
	void reviewAndRegisterVersionOntology() {
		Map<String, String> newValues = createOntologyInfo.getMetadataValues();
		
		BaseOntologyInfo ontologyInfo = createOntologyInfo.getBaseOntologyInfo();
		
		assert ontologyInfo instanceof RegisteredOntologyInfo ;
		
		
		// Ok, put the new values in the ontologyInfo object:
		ontologyInfo.getOntologyMetadata().setNewValues(newValues);
		
		
		createOntologyInfo.setUri(ontologyInfo.getUri());

		Orr.log(CLASS_NAME+": reviewAndRegisterVersionOntology starting.  HostingType: " +createOntologyInfo.getHostingType());
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setSize("600", "150");
		popup.getTextArea().setText("please wait ...");
		PortalControl.getInstance().notifyActivity(true);
		popup.setText("Creating version of ontology ...");
		popup.center();
		popup.show();

		AsyncCallback<CreateOntologyResult> callback = new AsyncCallback<CreateOntologyResult>() {
			public void onFailure(Throwable thr) {
				PortalControl.getInstance().notifyActivity(false);
				Window.alert(thr.toString());
			}

			public void onSuccess(CreateOntologyResult result) {
				Orr.log(CLASS_NAME+": CreateOntologyResult obtained.");
				PortalControl.getInstance().notifyActivity(false);
				reviewCompleted(popup, result);
			}
		};

		Orr.log(CLASS_NAME+": Calling service createOntology ...");
		Orr.service.createOntology(createOntologyInfo, callback);
	}

	
	private void reviewCompleted(final MyDialog popup, final CreateOntologyResult createOntologyResult) {
		String error = createOntologyResult.getError();
		
		//  Issue 211: Remove unnecesary registration confirmation dialogs
		if ( error == null ) {
			doRegister(popup, createOntologyResult);
			return;
		}

		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		sb.append(error);
		
		String msg = sb.toString();
		
		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null 
				? "Ontology ready to be registered" 
				: "Error");
		popup.center();

		Orr.log(CLASS_NAME+": Review result: " +msg);
	}

	private void doRegister(MyDialog createPopup, CreateOntologyResult createOntologyResult) {
		
		createPopup.hide();
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		popup.getTextArea().setSize("600", "150");
		
		Orr.log(CLASS_NAME+": Registering new version of ontology ...");
		popup.setText("Registering new version of ontology ...");
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
		Orr.service.registerOntology(createOntologyResult, loginResult , callback);
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
					+ "The new version is now registered."
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
		Orr.log(CLASS_NAME+": Registration result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setCloseButtonText("Return to ontology list");
		popup.setText(error == null ? "Registration of new version completed sucessfully" : "Error");
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
