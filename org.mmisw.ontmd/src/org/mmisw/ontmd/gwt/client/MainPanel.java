package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult;
import org.mmisw.ontmd.gwt.client.rpc.UploadResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class MainPanel extends VerticalPanel {


	private CellPanel container = new VerticalPanel();
	private TabPanel tabPanel = new TabPanel();
	
	private OntologyPanel ontologyPanel = new OntologyPanel(this);

	// created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	
	private UserPanel userInfoPanel = new UserPanel(this);
//	private UploadPanel loginPanel = new UploadPanel(this);
	
	
	private PushButton reviewButton = new PushButton("Review", new ClickListener() {
		public void onClick(Widget sender) {
			review(true);
		}
	});

	private PushButton uploadButton = new PushButton("Upload", new ClickListener() {
		public void onClick(Widget sender) {
			if ( reviewResult == null || reviewResult.getError() != null ) {
				Window.alert("Please, do the review action first.");
				return;
			}
			upload(true);
		}
	});

	private PushButton exampleButton = new PushButton("Example", new ClickListener() {
		public void onClick(Widget sender) {
			exampleForAll(true);
		}
	});

	private PushButton resetAllButton = new PushButton("Reset all", new ClickListener() {
		public void onClick(Widget sender) {
			resetAllToOriginalValues(true);
		}
	});

	
	/** The requested ontology, if any. */
	private String requestedOntologyUri;
	
	private LoginResult loginResult;

	private OntologyInfo ontologyInfo;
	
	private ReviewResult reviewResult;
	
	
	
	OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}


	MainPanel(final Map<String, String> params) {
		super();
		
		requestedOntologyUri = null;
		
		if ( params.get("ontologyUri") != null ) {
			requestedOntologyUri = params.get("ontologyUri");
		}
	    else if ( false && ! GWT.isScript() ) {
	    	// NOTE: Using an ad hoc ontology uri under my hosted environment.");
	    	requestedOntologyUri = "http://localhost:8080/ont/mmi/map-cicore-cf";
	    }
		
	    if ( params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    	String userName = params.get("userName");
	    	if ( userName == null ) {
	    		userName = "?";
	    	}
	    	loginResult.setUserName(userName);
	    	
	    	container.add(prepareInterface());
	    }
	    else if ( // false && 
	    		! GWT.isScript() ) {
	    	// NOTE: Using an ad hoc session under my hosted environment.");
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId("22222222222222222");
	    	loginResult.setUserId("1002");
	    	loginResult.setUserName("carueda");
	    	
	    	container.add(prepareInterface());
	    }
	    else {
	    	container.add(userInfoPanel);
	    }
	    
	    if ( ! "n".equalsIgnoreCase(params.get("_logo")) ) {
	    	add(Main.images.mmior().createImage());
	    }
	    
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    enable(false);
	    
	    if ( requestedOntologyUri != null ) {
	    	getOntologyInfoFromRegistry(requestedOntologyUri);
	    }
	}
	
	
	void loginOk(LoginResult loginResult) {
		this.loginResult = loginResult;
		container.clear();
		container.add(prepareEditingInterface());
	}


	private Widget prepareInterface() {
		if ( requestedOntologyUri != null ) {
			return prepareViewingInterface();
		}
		else {
			return prepareEditingInterface();
		}
	}
	
	
	private Widget prepareViewingInterface() {
		// create metadata panel for vieweing:
		metadataPanel = new MetadataPanel(this, false);

	    return metadataPanel;
	}
	
	private void getOntologyInfoFromRegistry(String ontologyUri) {
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				metadataPanel.resetToOriginalValues(ontologyInfo, null, false);
			}
		};

		Main.log("getOntologyInfoFromRegistry: ontologyUri = " +ontologyUri);
		Main.ontmdService.getOntologyInfoFromRegistry(ontologyUri, callback);
	}


	
	private Widget prepareEditingInterface() {
		// create metadata panel for editing:
		metadataPanel = new MetadataPanel(this, true);

		FlexTable flexPanel = new FlexTable();
		flexPanel.setWidth("800px");
		
		int row = 0;

		CellPanel buttons = createButtons();
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 2);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		

		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 2);
		flexPanel.setWidget(row, 0, tabPanel);
	    
	    
	    tabPanel.add(ontologyPanel, "Ontology");
	    
	    tabPanel.add(metadataPanel, "Metadata");
	    
	    tabPanel.selectTab(0);
	    
	    return flexPanel;
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		String userName = loginResult.getUserName();
		if ( userName == null ) {
			userName = "?";
		}
		panel.add(new Label(userName));

		reviewButton.setTitle("Checks the metadata associated with the ontology " +
				"and prepares for its subsequent upload to the MMI Registry");
		panel.add(reviewButton);
		
		uploadButton.setTitle("Uploads the new version of the ontology");
		panel.add(uploadButton);
		
		exampleButton.setTitle("Fills in fields in all sections with example values");
		panel.add(exampleButton);
		
		resetAllButton.setTitle("Resets the fields in all sections");
		panel.add(resetAllButton);
		
//		// TODO gif is not animated, why?
//		Image progImage = Main.images.mozilla_blu().createImage();
//		panel.add(progImage);
		
		return panel;
	}

	
	
	public void logout() {
		loginResult = null;
//		loginPanel.setLoginResult(null);
	}

	
	private void review(boolean confirm) {
		String error = metadataPanel.putValues(null); // null = just check
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		doReview();
	}

	void doReview() {
		if ( ontologyInfo == null ) {
			Window.alert("Please, load an ontology first");
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			Window.alert("Please, retry the loading of the ontology");
			return;
		}
		
		if ( loginResult == null ) {
			Window.alert("Please, login");
			return;
		}
		if ( loginResult.getError() != null ) {
			Window.alert("Please, login");
			return;
		}
		
		Map<String, String> newValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(newValues);
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// Ok, put the new values in the ontologyInfo object:
		ontologyInfo.setNewValues(newValues);
		for ( String uri : newValues.keySet() ) {
			String value = newValues.get(uri);
			newValues.put(uri, value);
		}
		
		
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		Main.log("Reviewing ...");
		reenableButton(reviewButton, "Review", false);
		popup.setText("Reviewing...");
		popup.center();
		popup.show();

		AsyncCallback<ReviewResult> callback = new AsyncCallback<ReviewResult>() {
			public void onFailure(Throwable thr) {
				reenableButton(reviewButton, "Review", true);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(ReviewResult result) {
				reenableButton(reviewButton, "Review", true);
				reviewCompleted(popup, result);
			}
		};

		Main.ontmdService.review(ontologyInfo, loginResult, callback);
	}

	private void reviewCompleted(MyDialog popup , ReviewResult reviewResult) {
		String error = reviewResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();

		if ( error == null ) {
			vp.add(new Label("Ontology URI: " +reviewResult.getUri()));
			vp.add(new Label("Contents:"));
			
			metadataPanel.resetToNewValues(ontologyInfo, reviewResult, false);
			
			sb.append(reviewResult.getRdf());
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		
		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null ? "Updated ontology contents" : "Error");
		popup.center();

		Main.log("Review result: " +msg);

		this.reviewResult = reviewResult;
	}
	
	
	private void upload(boolean confirm) {
		String error = metadataPanel.putValues(null); // null = just check
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		if ( confirm && 
			! Window.confirm("This action will commit your ontology into the MMI Registry") ) {
			return;
		}
		doUpload();
	}

	void doUpload() {
		if ( ontologyInfo == null ) {
			Window.alert("Please, load an ontology first");
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			Window.alert("Please, retry the loading of the ontology");
			return;
		}
		
		if ( reviewResult == null ) {
			Window.alert("Please, do the review action first.");
			return;
		}


		if ( loginResult == null ) {
			Window.alert("Please, login");
			return;
		}
		if ( loginResult.getError() != null ) {
			Window.alert("Please, login");
			return;
		}
		
		Map<String, String> newValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(newValues);
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// Ok, put the values in the ontologyInfo object:
		ontologyInfo.setNewValues(newValues);
		for ( String uri : newValues.keySet() ) {
			String value = newValues.get(uri);
			newValues.put(uri, value);
		}
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		Main.log("Uploading ...");
		reenableButton(uploadButton, "Upload", false);
		popup.setText("Uploading...");
		popup.center();
		popup.show();


		AsyncCallback<UploadResult> callback = new AsyncCallback<UploadResult>() {
			public void onFailure(Throwable thr) {
				ontologyInfo = null;
				reenableButton(uploadButton, "Upload", true);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(UploadResult result) {
				reenableButton(uploadButton, "Upload", true);
				uploadCompleted(popup, result);
			}
		};

		Main.ontmdService.upload(reviewResult, loginResult, callback);
	}

	
	private void uploadCompleted(MyDialog popup, UploadResult result) {
		String error = result.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		
		if ( error == null ) {
			metadataPanel.resetToNewValues(ontologyInfo, reviewResult, false);

			vp.add(new Label("Ontology URI: " +result.getUri()));
			vp.add(new Label("Response from Registry back-end:"));

			sb.append(result.getInfo());
			
			// and, disable all editing fields/buttons:
			// (user will have to start from the "load" step)
			enable(false);
			ontologyInfo = null;
			reviewResult = null;
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();

		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null ? "Upload completed sucessfully" : "Error");
		popup.center();

		Main.log("Uploading result: " +msg);
	}
	
	private void enable(boolean enabled) {
		reviewButton.setEnabled(enabled);
		uploadButton.setEnabled(enabled);
		exampleButton.setEnabled(enabled);
		resetAllButton.setEnabled(enabled);
		if ( metadataPanel != null ) {
			metadataPanel.enable(enabled);
		}
	}

	private void exampleForAll(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		String error = ontologyInfo.getError();
		if ( error != null ) {
			enable(false);
			Window.alert(error);
		}
		else {
			enable(true);
			metadataPanel.example(false);
		}
	}
	
	private void resetAllToOriginalValues(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		String error = ontologyInfo.getError();
		if ( error != null ) {
			enable(false);
			Window.alert(error);
		}
		else {
			enable(true);
			metadataPanel.resetToOriginalValues(ontologyInfo, reviewResult, false);
		}
	}
	

	void setPreloadedOntologyInfo(OntologyInfo ontologyInfo, boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		String error = ontologyInfo.getError();
		if ( error != null ) {
			enable(false);
			Window.alert(error);
		}
		else {
			this.ontologyInfo = ontologyInfo;
			enable(true);
			metadataPanel.resetToOriginalValues(ontologyInfo, reviewResult, false);
		}
	}
	

	private void reenableButton(PushButton button, String text, boolean enabled) {
		button.setText(text);
		button.setEnabled(enabled);
	}

}
