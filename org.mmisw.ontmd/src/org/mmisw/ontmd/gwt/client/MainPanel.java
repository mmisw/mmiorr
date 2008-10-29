package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.LoginResult;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.UploadResult;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	
	private MetadataPanel metadataPanel = new MetadataPanel();
	private OntologyPanel ontologyPanel = new OntologyPanel(this);
	
	
	private UserPanel userInfoPanel = new UserPanel(this);
//	private UploadPanel loginPanel = new UploadPanel(this);
	
	
	private PushButton exampleButton;
	private PushButton resetButton;

	
	private LoginResult loginResult;
	private OntologyInfo ontologyInfo;
	
	
	
	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.mmior().createImage());
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    if ( true || // TODO temporary 
	    		params.get("sessionId") != null && params.get("userId") != null ) {
	    	loginResult = new LoginResult();
	    	loginResult.setSessionId(params.get("sessionId"));
	    	loginResult.setUserId(params.get("userId"));
	    	
	    	container.add(prepareInterface());
	    }
	    else {
	    	container.add(userInfoPanel);
	    }
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
	    
//		tabPanel.setAnimationEnabled(true);
		
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
		exampleButton = new PushButton("Example", new ClickListener() {
			public void onClick(Widget sender) {
				example(true);
			}
		});
		exampleButton.setTitle("Fills in example values in all sections");
		panel.add(exampleButton);
		
		resetButton = new PushButton("Reset all", new ClickListener() {
			public void onClick(Widget sender) {
				reset(true);
			}
		});
		resetButton.setTitle("Resets the fields in all sections");
		panel.add(resetButton);
		
		return panel;
	}

	
	
	public void logout() {
		loginResult = null;
//		loginPanel.setLoginResult(null);
	}

	
	void doUpload() {
		if ( ontologyInfo == null ) {
			Window.alert("Please, perform a conversion first");
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			Window.alert("Please, perform a successfull conversion first");
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
		
		AsyncCallback<UploadResult> callback = new AsyncCallback<UploadResult>() {
			public void onFailure(Throwable thr) {
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(UploadResult result) {
				String error = result.getError();
				
				String msg = error == null ? result.getInfo() : error;
				
				TextArea ta = new TextArea();
				ta.setSize("500", "200");
				ta.setReadOnly(true);
				ta.setText(msg);
				VerticalPanel vp = new VerticalPanel();
				vp.add(ta);
				final MyDialog popup = new MyDialog(vp);
				popup.setText(error == null ? "OK" : "Error");
				
				Main.log("Uploading result: " +msg);
				
				popup.center();
				popup.show();
			}
		};

		Main.log("Uploading ...");
		Main.ontmdService.upload(ontologyInfo, loginResult, callback);
	}

	private void example(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		metadataPanel.example(false);
	}
	private void reset(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		metadataPanel.reset(false);
//		tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
	}


	public void setOntologyInfo(OntologyInfo ontologyInfo) {
		this.ontologyInfo = null;
		reset(false);
		String error = ontologyInfo.getError();
		if ( error != null ) {
			Window.alert(error);
		}
		else {
			this.ontologyInfo = ontologyInfo;
			metadataPanel.setOntologyInfo(ontologyInfo);
		}
	}


}
