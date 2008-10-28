package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
import org.mmisw.voc2rdf.gwt.client.rpc.LoginResult;
import org.mmisw.voc2rdf.gwt.client.rpc.UploadResult;

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
	private VocabPanel vocabPanel = new VocabPanel(this);
	
	private ConversionPanel conversionPanel = new ConversionPanel(this);
	
	private UploadPanel uploadPanel = new UploadPanel(this);
	
	
	private PushButton exampleButton;
	private PushButton resetButton;

	
	private LoginResult loginResult;
	private ConversionResult conversionResult;
	
	
	
	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.voc2rdf().createImage());
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    
	    /////////
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
	    
	    
	    container.add(flexPanel); // tabPanel);
	    
	    tabPanel.add(vocabPanel, "Vocabulary");
	    tabPanel.add(metadataPanel, "Metadata");
	    
	    tabPanel.add(conversionPanel, "Conversion");
	    
	    tabPanel.add(uploadPanel, "MMI Registry");
	    
	    tabPanel.selectTab(0);
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

	
	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	void convertTest() {
		String namespaceRoot = "http://mmisw.org/ont"; 
		
		Map<String, String> values = new HashMap<String, String>();

		// error only possibly from the vocabPanel:
		String error;
		if ( (error = vocabPanel.putValues(values)) != null ) {
			tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
		}
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// get test values:
		metadataPanel.putTestValues(values);
		Main.log("testConvert: values = " +values);

		final TextArea textArea = new TextArea();
		textArea.setReadOnly(true);
	    textArea.setSize("600px", "350px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(textArea);
	    textArea.setText("converting ...");

		final MyDialog popup = new MyDialog(decPanel);
		popup.setText("Test Conversion: please wait ...");
		popup.center();
		popup.show();


		// do test conversion
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Main.log("convertTest: error: " +error);
				popup.setText("Test Conversion: Error");
				textArea.setText(error);
			}

			public void onSuccess(ConversionResult conversionResult) {
				String error = conversionResult.getError();
				if ( error != null ) {
					Main.log("convertTest: error: " +error);
					popup.setText("Test Conversion: Error");
					textArea.setText(error);
				}
				else {
					Main.log("convertTest: OK: " +conversionResult.getRdf());
					popup.setText("Test Conversion: OK");
					textArea.setText(conversionResult.getRdf());
				}
			}
		};

		values.put("namespaceRoot", namespaceRoot);

		Main.log("convertTest: converting ... ");
		
		Main.voc2rdfService.convert(values, callback);

	}
	
	
	/**
	 * The regular conversion using all provided information.
	 */
	void convert(String namespaceRoot) {
		Map<String, String> values = new HashMap<String, String>();
		
		String error;

		if ( (error = vocabPanel.putValues(values)) != null ) {
			tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
		}
		else if ( (error = metadataPanel.putValues(values)) != null ) {
			tabPanel.selectTab(tabPanel.getWidgetIndex(metadataPanel));
		}
		
		if ( error != null ) {
			conversionPanel.updateContents(null);
			Window.alert(error);
		}
		else {
			doConversion(namespaceRoot, values);
		}
	}

	public void doConversion(String namespaceRoot, Map<String, String> values) {
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				conversionResult = new ConversionResult();
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				conversionResult.setError(error);
				conversionPanel.updateContents(conversionResult);
			}

			public void onSuccess(ConversionResult conversionResult) {
				MainPanel.this.conversionResult = conversionResult;
				conversionPanel.updateContents(conversionResult);
			}
		};

		conversionPanel.prepareForConversion();
		tabPanel.selectTab(tabPanel.getWidgetIndex(conversionPanel));
		
		Main.log("doConversion: setting namespaceRoot = " +namespaceRoot);
		values.put("namespaceRoot", namespaceRoot);
		Main.log("Converting ...");
		Main.voc2rdfService.convert(values, callback);
	}
	
	
	void doLogin(String userName, String userPassword) {
		
		AsyncCallback<LoginResult> callback = new AsyncCallback<LoginResult>() {

			public void onFailure(Throwable ex) {
				loginResult = new LoginResult();
				loginResult.setError(ex.getMessage());
				Main.log("login error: " +loginResult);
				Window.alert("Error validating credentials: " +ex.getMessage());
			}

			public void onSuccess(LoginResult loginResult) {
				MainPanel.this.loginResult = loginResult;
				if ( loginResult.getError() != null ) {
					Main.log("login error: " +loginResult);
					Window.alert(loginResult.getError());
				}
				else {
					Main.log("login ok: " +loginResult);
				}
				uploadPanel.setLoginResult(loginResult);
			}
			
		};
		Main.log("login ...");
		Main.voc2rdfService.login(userName, userPassword, callback);

	}
	
	public void logout() {
		loginResult = null;
		uploadPanel.setLoginResult(null);
	}

	
	void doUpload() {
		if ( conversionResult == null ) {
			Window.alert("Please, perform a conversion first");
			return;
		}
		if ( conversionResult.getError() != null ) {
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
		Main.voc2rdfService.upload(conversionResult, loginResult, callback);
	}

	private void example(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		metadataPanel.example(false);
		vocabPanel.example(false);
		conversionPanel.updateContents(null);
	}
	private void reset(boolean confirm) {
		if ( confirm && ! Window.confirm("This action will replace the current values in all sections") ) {
			return;
		}
		metadataPanel.reset(false);
		vocabPanel.reset(false);
		conversionPanel.updateContents(null);
//		tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
	}


}
