package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
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
	
	private ConversionPanel resultPanel = new ConversionPanel(this);
	
	private UploadPanel uploadPanel = new UploadPanel(this);
	
	
	private PushButton exampleButton;
	private PushButton resetButton;

	
	
	private ConversionResult conversionResult;
	
	
	
	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.voc2rdf().createImage());
		
		container.setSize("740px", "400px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    
	    /////////
		FlexTable flexPanel = new FlexTable();
		flexPanel.setWidth("740px");
		
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
	    
	    
	    container.add(flexPanel); // tabPanel);
	    
	    tabPanel.add(vocabPanel, "Vocabulary");
	    tabPanel.add(metadataPanel, "Metadata");
	    
	    tabPanel.add(resultPanel, "Conversion");
	    
	    tabPanel.add(uploadPanel, "MMI Registry");
	    
	    tabPanel.selectTab(0);
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		exampleButton = new PushButton("Example", new ClickListener() {
			public void onClick(Widget sender) {
				metadataPanel.example();
				vocabPanel.example();
			}
		});
		panel.add(exampleButton);
		
		resetButton = new PushButton("Reset all", new ClickListener() {
			public void onClick(Widget sender) {
				metadataPanel.reset();
				vocabPanel.reset();
				resultPanel.updateContents(null);
				tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
			}
		});
		panel.add(resetButton);
		
		return panel;
	}

	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	// TODO convertTest
	void convertTest() {
		
		// TODO for now, run the general conversion.
		convert();
	}
	
	void convert() {
		Map<String, String> values = new HashMap<String, String>();
		
		String error;

		if ( (error = vocabPanel.putValues(values)) != null ) {
			tabPanel.selectTab(tabPanel.getWidgetIndex(vocabPanel));
		}
		else if ( (error = metadataPanel.putValues(values)) != null ) {
			tabPanel.selectTab(tabPanel.getWidgetIndex(metadataPanel));
		}
		
		if ( error != null ) {
			resultPanel.updateContents(null);
			Window.alert(error);
		}
		else {
			doConversion(values);
		}
	}

	public void doConversion(final Map<String, String> values) {
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				conversionResult = new ConversionResult();
				conversionResult.setError(thr.getClass().getName()+ ": " +thr.getMessage());
				resultPanel.updateContents(conversionResult);
				tabPanel.selectTab(tabPanel.getWidgetIndex(resultPanel));
			}

			public void onSuccess(ConversionResult conversionResult) {
				MainPanel.this.conversionResult = conversionResult;
				resultPanel.updateContents(conversionResult);
				tabPanel.selectTab(tabPanel.getWidgetIndex(resultPanel));
			}
		};

		Main.log("Converting ...");
		Main.voc2rdfService.convert(values, callback);
	}
	
	
	private void upload(ConversionResult conversionResult) {
		this.conversionResult = conversionResult;
		UploadPanel uploadPanel = new UploadPanel(this);
		Main.log("uploadPanel created");
		MyDialog popup = new MyDialog(uploadPanel);
		popup.setText("User account");
		popup.center();
		popup.show();
	}

	
	void doUpload(Map<String, String> values) {
		if ( conversionResult == null ) {
			Main.log("doUpload: conversionResult is null.");
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
				ta.setSize("400px", "100px");
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
		Main.voc2rdfService.upload(conversionResult, values, callback);
	}

}
