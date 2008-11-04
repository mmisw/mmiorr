package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	
	private VocabPanel vocabPanel = new VocabPanel(this);
	private ConversionPanel conversionPanel = new ConversionPanel(this);
	
	private HTML statusLabel = new HTML();
	
	private PushButton convertButton = new PushButton("Convert to RDF", new ClickListener() {
		public void onClick(Widget sender) {
			convert2Rdf();
		}
	});

	
	
	MainPanel(final Map<String, String> params) {
		super();
		
		add(Main.images.voc2rdf().createImage());
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    
	    /////////
		
		
		FlexTable flexPanel = new FlexTable();
		flexPanel.setWidth("800px");
		int row = 0;
		
		convertButton.setTitle("Converts the current vocabulary contents into RDF format.");
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(4);
		
		HTML info = new HTML(
				"Voc2RDF transforms a vocabulary in text format into a format that greatly " +
				"facilitates the exploitation of your vocabulary by Semantic Web tools, thus " +
				"enabling semantic interoperability. " +
				"This format is called the <a href=\"http://www.w3.org/TR/REC-rdf-syntax/\"" +
				" target=\"_blank\" >Resource Description Framework.</a> " 	
		);
		
//		hp.add(info);	
		hp.add(statusLabel);
		
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, info);
		flexPanel.getCellFormatter().setAlignment(row, 0, ALIGN_LEFT, ALIGN_MIDDLE);
		row++;

		hp.add(convertButton);

		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, hp);
		flexPanel.getCellFormatter().setAlignment(row, 0, ALIGN_RIGHT, ALIGN_MIDDLE);
		row++;
		
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, tabPanel);
		tabPanel.add(vocabPanel, "Vocabulary");
		tabPanel.add(conversionPanel, "Conversion");
		tabPanel.selectTab(0);
	    
	    container.add(flexPanel); // tabPanel);
	}
	
	
	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	void convert2Rdf() {
		Map<String, String> values = new HashMap<String, String>();

		// error only possibly from the vocabPanel:
		String error;
		if ( (error = vocabPanel.putValues(values)) != null ) {
			statusLabel.setHTML("<font color=\"red\">" + error+ "</font>");
//			Window.alert(error);
			return;
		}
		
		statusLabel.setHTML("<font color=\"blue\">" + "Converting ..." + "</font>");
		conversionPanel.showProgressMessage("Converting. Please wait ...");
		conversionPanel.updateForm(null);
		
		Main.log("testConvert: values = " +values);

		// do test conversion
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Main.log("convertTest: error: " +error);
				conversionPanel.setText(error);
				statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
			}

			public void onSuccess(ConversionResult conversionResult) {
				String error = conversionResult.getError();
				if ( error != null ) {
					Main.log("convertTest: error: " +error);
					statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
					conversionPanel.setText(error);
				}
				else {
					Main.log("convertTest: OK: " +conversionResult.getRdf());
					statusLabel.setHTML("<font color=\"green\">" + "Conversion complete" + "</font>");
					tabPanel.selectTab(tabPanel.getWidgetIndex(conversionPanel));
					conversionPanel.setText(conversionResult.getRdf());
					conversionPanel.updateForm(conversionResult.getPathOnServer());
				}
			}
		};

		Main.log("convertTest: converting ... ");
		
		Main.voc2rdfService.convert(values, callback);

	}


}
