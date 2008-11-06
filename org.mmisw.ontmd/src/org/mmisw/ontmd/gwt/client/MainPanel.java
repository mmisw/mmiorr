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
	
	// created depending on whether a given ontology from registry has been
	// passed
	private OntologyPanel ontologyPanel;

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

	
	/** URI of requested ontology from a parameter, if any. */
	private String requestedOntologyUri;
	
	/** Path of requested ontology on server, if any. */
	private String requestedOntologyOnServer;
	
	/** Edit the requested ontology? */
	private boolean editRequestedOntology;
	
	private LoginResult loginResult;
	
	// true iff login session given from parameters
	private boolean loginFromParams;

	private OntologyInfo ontologyInfo;
	
	private ReviewResult reviewResult;
	
	
	
	OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}


	MainPanel(final Map<String, String> params) {
		super();
		
		loginResult = null;
		loginFromParams = false;
		requestedOntologyUri = null;
		requestedOntologyOnServer = null;
		editRequestedOntology = false;
		
		
		///////////////////////////////////////////////////////////////////////////
		// conveniences for testing in development environment
		if ( ! GWT.isScript() ) {
			
			if ( true ) {    // true for auto-login
				loginResult = new LoginResult();
				loginResult.setSessionId("22222222222222222");
				loginResult.setUserId("1002");
				loginResult.setUserName("carueda");
				loginFromParams = true;
			}
			
			if ( false ) {
				requestedOntologyUri = "http://localhost:8080/ont/mmi/map-cicore-cf";
				editRequestedOntology = true;
			}
			if ( true ) {
				requestedOntologyOnServer = "/Users/Shared/bioportal/resources/uploads/1000/1/map-cicore-cf.owl";
				editRequestedOntology = true;
			}
	    }
		//////////////////////////////////////////////////////////////////////////////

		
		
		if ( requestedOntologyUri == null && params.get("ontologyUri") != null ) {
			requestedOntologyUri = params.get("ontologyUri");
			editRequestedOntology = "y".equalsIgnoreCase(params.get("_edit"));
		}
		
		else if ( params.get("_voc2rdf") != null ) {
			requestedOntologyOnServer = params.get("_voc2rdf");
			editRequestedOntology = "y".equalsIgnoreCase(params.get("_edit"));
		}
		
		
	    if ( loginResult == null && params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    	String userName = params.get("userName");
	    	if ( userName == null ) {
	    		userName = "?";
	    	}
	    	loginResult.setUserName(userName);
	    	loginFromParams = true;
	    }
	    
	    
	    
	    if ( ! "n".equalsIgnoreCase(params.get("_logo")) ) {
	    	add(Main.images.mmior().createImage());
	    }
	    
	    
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    enable(false);
	    

	    // Now, dispatch:
	    
	    // already logged in?
	    if ( loginResult != null ) {
	    	
	    	if ( editRequestedOntology ) {
	    		container.add(prepareEditingInterface());
	    	}
	    	else {
	    		container.add(prepareViewingInterface());
	    	}
	    	
	    }
	    
	    // not logged in but request to edit?
	    else if ( editRequestedOntology ) {
	    	container.add(userInfoPanel);
	    	return;   // loginOk will dispatch any initial request.
	    }
	    
	    // not logged in but something to visualize?
	    else if ( requestedOntologyUri != null || requestedOntologyOnServer != null ) {
	    	container.add(prepareViewingInterface());

	    }
	    // no basic initialization params at all?.
	    else {
	    	// then, start with the user panel:
	    	container.add(userInfoPanel);
	    	return;   // loginOk will dispatch any initial request.
	    }
	    
	    // we get here when the regular panels are prepared (not the user panel):
	    // so. just dispatch any initial requested ontology:
	    dispatchInitialRequest();
	}
	
	
	private void dispatchInitialRequest() {
	    if ( requestedOntologyUri != null ) {
	    	getOntologyInfoFromRegistry(requestedOntologyUri);
	    }
	    else if ( requestedOntologyOnServer != null ) {
	    	getOntologyInfoFromFileOnServer(requestedOntologyOnServer);
	    }
	}
	
	void loginOk(LoginResult loginResult) {
		this.loginResult = loginResult;
		container.clear();
		container.add(prepareEditingInterface());
		
		dispatchInitialRequest();
	}


	
	private Widget prepareViewingInterface() {
		// create metadata panel for vieweing:
		metadataPanel = new MetadataPanel(this, false);

	    return metadataPanel;
	}
	
	private void getOntologyInfoFromRegistry(String ontologyUri) {
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				if  (ontologyPanel != null ) {
					ontologyPanel.onFailure(thr);
				}
				else {
					String error = thr.getClass().getName()+ ": " +thr.getMessage();
					while ( (thr = thr.getCause()) != null ) {
						error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
					}
					Window.alert(error);
				}
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				if  (ontologyPanel != null ) {
					ontologyPanel.onSuccess(ontologyInfo);
				}
				String error = ontologyInfo.getError();
				if ( error != null ) {
					Window.alert(error);
				}
				else {
					metadataPanel.resetToOriginalValues(ontologyInfo, null, false, false);
				}
			}
		};

		if ( ! editRequestedOntology ) {
			metadataPanel.showProgressMessage("Loading metadata. Please wait...");
		}
		Main.log("getOntologyInfoFromRegistry: ontologyUri = " +ontologyUri);
		Main.ontmdService.getOntologyInfoFromRegistry(ontologyUri, callback);
	}


	private void getOntologyInfoFromFileOnServer(String full_path) {
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				if  (ontologyPanel != null ) {
					ontologyPanel.onFailure(thr);
				}
				else {
					String error = thr.getClass().getName()+ ": " +thr.getMessage();
					while ( (thr = thr.getCause()) != null ) {
						error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
					}
					Window.alert(error);
				}
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				if  (ontologyPanel != null ) {
					ontologyPanel.onSuccess(ontologyInfo);
				}
				String error = ontologyInfo.getError();
				if ( error != null ) {
					Window.alert(error);
				}
				else {
					metadataPanel.resetToOriginalValues(ontologyInfo, null, false, false);
				}
			}
		};

		if ( ! editRequestedOntology ) {
			metadataPanel.showProgressMessage("Loading metadata. Please wait...");
		}
		Main.log("getOntologyInfoFromFileOnServer: ontologyUri = " +full_path);
		Main.ontmdService.getOntologyInfoFromFileOnServer(full_path, callback);
	}


	
	private Widget prepareEditingInterface() {
		
		// create ontologyPanel
		boolean allowLoadOptions = requestedOntologyUri == null &&
		                           requestedOntologyOnServer == null
		                           ;
		
		ontologyPanel = new OntologyPanel(this, allowLoadOptions);
		
		
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
		
		// do not show username if loginFromParams
		if ( ! loginFromParams && loginResult != null ) {
			String userName = loginResult.getUserName();
			if ( userName == null ) {
				userName = "?";
			}
			panel.add(new Label(userName));
		}

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
			
			metadataPanel.resetToNewValues(ontologyInfo, reviewResult, false, false);
			
			sb.append(reviewResult.getRdf());
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		
		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null 
				? "Your ontology is ready to be uploaded" 
				: "Error");
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
		vp.setSpacing(6);
		
		if ( error == null ) {
			metadataPanel.resetToNewValues(ontologyInfo, reviewResult, false, true);

			String uri = result.getUri();

			vp.add(new HTML("<font color=\"green\">Congratulations!</font> "
					+ "Your ontology is now registered in the "
//					+ "<a href=\"" +"http://mmisw.org/or/"+ "\">"
					+ "MMI Registry and Repository."
//					+ "</a>"
			));
			
			
			vp.add(new HTML("<br/>The URI of the ontology is: "
//					+ "<a href=\"" +uri+ "\">"
					+ uri
//					+ "</a>"
			));
			
			vp.add(new HTML("<br/>Note that you may continue viewing the ontology but not editing its " +
					"contents anymore."
			));
			
			
			vp.add(new HTML("<br/>For diagnostics, this is the response from the back-end server:"));

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

		popup.getTextArea().setSize("700", "160");
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
			metadataPanel.resetToOriginalValues(ontologyInfo, reviewResult, false, false);
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
			metadataPanel.resetToOriginalValues(ontologyInfo, reviewResult, false, false);
		}
	}
	

	private void reenableButton(PushButton button, String text, boolean enabled) {
		button.setText(text);
		button.setEnabled(enabled);
	}


	public void showDetails() {
		String details = ontologyInfo.getDetails();
		String[] lines = details == null ? null : details.split("\n|\r\n|\r");
		if ( lines == null || lines.length == 0 ) {
			Window.alert("No details are available");
			return;
		}
		
		FlexTable table = new FlexTable();
		table.setStylePrimaryName("inline");
		
		table.getFlexCellFormatter().setColSpan(0, 0, 2);
		table.getFlexCellFormatter().setAlignment(0, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(0, 0, new Label("MMI attribute"));
		
		table.getFlexCellFormatter().setAlignment(0, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		table.setWidget(0, 1, new Label("Note"));
		
		
		for ( int lin = 0; lin < lines.length; lin++ ) {
			String[] vals = lines[lin].split("\\|");
			for ( int col = 0; col < vals.length; col++ ) {
				table.setWidget(lin+1, col, new Label(vals[col]));
			}
	
		}
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("400");
		vp.setSpacing(10);
		vp.add(new HTML("This table shows an initial evaluation of the loaded ontology in relation " +
				"to the required MMI attributes. " +
				"It shows any included MMI attribute as well as those that are missing but " +
				"required. Use the Metadata section to edit all attributes as necessary."));
		vp.add(table);
		final MyDialog popup = new MyDialog(vp);
		popup.setText("Diagnostics on original metadata");
		popup.center();
		popup.show();
	}

}
