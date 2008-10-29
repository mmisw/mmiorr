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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
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
	
	private MetadataPanel metadataPanel = new MetadataPanel(this);
	private OntologyPanel ontologyPanel = new OntologyPanel(this);
	
	
	private UserPanel userInfoPanel = new UserPanel(this);
//	private UploadPanel loginPanel = new UploadPanel(this);
	
	
	private PushButton reviewButton = new PushButton("Review", new ClickListener() {
		public void onClick(Widget sender) {
			review(true);
		}
	});

	private PushButton uploadButton = new PushButton("Upload", new ClickListener() {
		public void onClick(Widget sender) {
			upload(true);
		}
	});

	private PushButton resetAllButton = new PushButton("Reset all", new ClickListener() {
		public void onClick(Widget sender) {
			setOntologyInfo(ontologyInfo, true);
		}
	});

	
	private LoginResult loginResult;
	private OntologyInfo ontologyInfo;
	
	private ReviewResult reviewResult;
	
	private boolean enabled;
	
	
	
	OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}


	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.mmior().createImage());
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    if ( params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    	
	    	container.add(prepareInterface());
	    }
	    else if ( ! GWT.isScript() ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId("22222222222222222");
	    	loginResult.setUserId("1002");
	    	
	    	container.add(prepareInterface());
	    }
	    else {
	    	container.add(userInfoPanel);
	    }
	    
	    enable(false);
	}
	
	
	void loginOk(LoginResult loginResult) {
		this.loginResult = loginResult;
		container.clear();
		container.add(prepareInterface());
	}


	
	private FlexTable prepareInterface() {
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setWidth("800px");
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		flexPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
	    /////////
	    
		flexPanel.setWidget(row, 0, tabPanel);
	    
	    
	    tabPanel.add(ontologyPanel, "Ontology");
	    
	    tabPanel.add(metadataPanel, "Metadata");
	    
//	    tabPanel.add(conversionPanel, "Conversion");
	    
	    tabPanel.selectTab(0);
	    
	    return flexPanel;
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		reviewButton.setTitle("Updates the contents of the ontology for review");
		panel.add(reviewButton);
		
		uploadButton.setTitle("Uploads the new version of the ontology");
		panel.add(uploadButton);
		
		resetAllButton.setTitle("Resets the fields in all sections");
		panel.add(resetAllButton);
		
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
		
		Map<String, String> tmpValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(tmpValues);
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// Ok, put the values in the ontologyInfo object:
		Map<String, String> values = ontologyInfo.getValues();
		for ( String uri : tmpValues.keySet() ) {
			String value = tmpValues.get(uri);
			values.put(uri, value);
		}
		
		AsyncCallback<ReviewResult> callback = new AsyncCallback<ReviewResult>() {
			public void onFailure(Throwable thr) {
				reenableButton(reviewButton, "Review", true);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(ReviewResult result) {
				reenableButton(reviewButton, "Review", true);
				reviewCompleted(result);
			}
		};

		Main.log("Reviewing ...");
		reenableButton(reviewButton, "Reviewing...", false);
		Main.ontmdService.review(ontologyInfo, loginResult, callback);
	}

	private void reviewCompleted(ReviewResult reviewResult) {
		String error = reviewResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();

		if ( error == null ) {
			vp.add(new Label("Ontology URI: " +reviewResult.getUri()));
			vp.add(new Label("Contents:"));
			
			metadataPanel.setOntologyInfo(ontologyInfo, false);
			
			sb.append(reviewResult.getRdf());
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		TextArea ta = new TextArea();
		ta.setSize("700", "320");
		ta.setReadOnly(true);
		ta.setText(msg );
		vp.add(ta);
		final MyDialog popup = new MyDialog(vp);
		popup.setText(error == null ? "Updated ontology" : "Error");
		
		Main.log("Review result: " +msg);
		
		popup.center();
		popup.show();

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
		
		if ( loginResult == null ) {
			Window.alert("Please, login");
			return;
		}
		if ( loginResult.getError() != null ) {
			Window.alert("Please, login");
			return;
		}
		
		Map<String, String> tmpValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(tmpValues);
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// Ok, put the values in the ontologyInfo object:
		Map<String, String> values = ontologyInfo.getValues();
		for ( String uri : tmpValues.keySet() ) {
			String value = tmpValues.get(uri);
			values.put(uri, value);
		}
		
		AsyncCallback<UploadResult> callback = new AsyncCallback<UploadResult>() {
			public void onFailure(Throwable thr) {
				reenableButton(uploadButton, "Upload", true);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(UploadResult result) {
				uploadCompleted(result);
			}
		};

		Main.log("Uploading ...");
		reenableButton(uploadButton, "Uploading...", false);
		Main.ontmdService.upload(reviewResult, loginResult, callback);
	}

	
	private void uploadCompleted(UploadResult result) {
		String error = result.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		
		if ( error == null ) {
			metadataPanel.setOntologyInfo(ontologyInfo, false);

			vp.add(new Label("Ontology URI: " +result.getUri()));
			vp.add(new Label("Response form Registry back-end:"));

			sb.append(result.getInfo());
			
			// and, disable all editing fields/buttons:
			// (user will have to start from the "load" step)
			enable(false);
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		TextArea ta = new TextArea();
		ta.setSize("600", "220");
		ta.setReadOnly(true);
		ta.setText(msg );
		vp.add(ta);
		final MyDialog popup = new MyDialog(vp);
		popup.setText(error == null ? "Upload completed sucessfully" : "Error");
		
		Main.log("Uploading result: " +msg);
		
		popup.center();
		popup.show();
	}
	
	private void enable(boolean enabled) {
		reviewButton.setEnabled(enabled);
		uploadButton.setEnabled(enabled);
		resetAllButton.setEnabled(enabled);
		metadataPanel.enable(enabled);
		
		this.enabled = enabled;
	}

	void setOntologyInfo(OntologyInfo ontologyInfo, boolean confirm) {
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
			metadataPanel.setOntologyInfo(ontologyInfo, false);
		}
	}

	private void reenableButton(PushButton button, String text, boolean enabled) {
		button.setText(text);
		button.setEnabled(enabled);
	}

}
