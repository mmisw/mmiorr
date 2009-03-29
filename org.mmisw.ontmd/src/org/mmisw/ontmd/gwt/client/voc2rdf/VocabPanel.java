package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.FieldWithChoose;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.TLabel;
import org.mmisw.ontmd.gwt.client.Util;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class VocabPanel extends VerticalPanel {

	private static final String NAMESPACE_ROOT = "http://mmisw.org/ont";

	private AttrDef fullTitleAttrDef;
	private AttrDef creatorAttrDef;
	private AttrDef descriptionAttrDef;
	private AttrDef authorityAttrDef;

	private CellPanel contentsContainer = new VerticalPanel();
	
	// User-specified URI for the ontology
//	private TextBoxBase ontologyUriTb;
	
	private TextBoxBase fullTitleTb;
	private TextBoxBase creatorTb;
	private TextBoxBase descriptionTb;
	private FieldWithChoose authorityField;
	
	
	private ClassPanel classPanel;
	
	
	private HTML statusLabel = new HTML();
	private PushButton convertButton;


	private PushButton exampleButton;
	private PushButton resetButton;
	
	protected Voc2RdfMainPanel mainPanel;

	
	VocabPanel(Voc2RdfMainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
		fullTitleAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("fullTitle");
		creatorAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("creator");
		descriptionAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("description");
		authorityAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("authority");

		add(createForm());
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
		contentsContainer.setBorderWidth(1);
		
		
		fullTitleTb = Util.createTextBoxBase(1, "500", new ChangeListener() {
			public void onChange(Widget sender) {
				statusLabel.setText("");
			}
		});
		
		creatorTb = Util.createTextBoxBase(1, "500", new ChangeListener() {
			public void onChange(Widget sender) {
				statusLabel.setText("");
			}
		});
		
		descriptionTb = Util.createTextBoxBase(4, "500", new ChangeListener() {
			public void onChange(Widget sender) {
				statusLabel.setText("");
			}
		});
		
		ChangeListener cl = new ChangeListener () {
			public void onChange(Widget sender) {
				statusLabel.setText("");
			}
		};
		authorityField = new FieldWithChoose(authorityAttrDef, cl);

		
		
		
//		ontologyUriTb = Util.createTextBoxBase(1, "500", 
//				new ChangeListener() {
//					public void onChange(Widget sender) {
//						  statusLabel.setText("");
//					}
//				}
//		);


//		ascii_ta.setSize("800", "200");
//		table.setSize("800", "200");
//		
//		ascii_ta.addKeyboardListener(new KeyboardListenerAdapter(){
//			  public void onKeyUp(Widget sender, char keyCode, int modifiers) {
//				  statusLabel.setText("");
//			  }
//		});
		
		FlexTable flexPanel = new FlexTable();
//		flexPanel.setBorderWidth(1);
		flexPanel.setWidth("850");
		int row = 0;
				
		
		CellPanel buttons = createConvertButton();
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		
		/////////////////////
		// full title
		flexPanel.setWidget(row, 0, new TLabel("Full title:", true, "<b>Full title</b>:<br/>" +fullTitleAttrDef.getTooltip()));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, fullTitleTb);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		row++;


		CellPanel exampleButtonPanel = createExampleButton();
//		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 2, exampleButtonPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		/////////////////////
		// creator
		flexPanel.setWidget(row, 0, new TLabel("Creator:", true, "<b>Creator</b>:<br/>" +creatorAttrDef.getTooltip()));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, creatorTb);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		row++;

		CellPanel resetButtonPanel = createResetButton();
//		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 2, resetButtonPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		/////////////////////
		// briefDescription
		flexPanel.setWidget(row, 0, new TLabel("Description:", true, "<b>Description</b>:<br/>" +descriptionAttrDef.getTooltip()));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, descriptionTb);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;


		/////////////////////
		// authority
		flexPanel.setWidget(row, 0, new TLabel("Authority:", true, "<b>Authority</b>:<br/>" +authorityAttrDef.getTooltip()));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, authorityField);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;


		
		/////////////////////
		// ontologUri
//		
//		flexPanel.setWidget(row, 0, new TLabel("Ontology URI:", true, "<b>Ontology URI</b>:<br/>" +ONTOLOGY_URI_TOOTIP));
//		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//
//		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
//		flexPanel.setWidget(row, 1, ontologyUriTb);
//		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
//				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		row++;




		classPanel = new ClassPanel(mainPanel);
		TabPanel tabPanel = new TabPanel();
		tabPanel.add(classPanel, "Vocabulary");
		tabPanel.selectTab(0);
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);

		flexPanel.setWidget(row, 0, tabPanel);
		row++;

		
		return flexPanel;
	}
	
	private CellPanel createExampleButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		exampleButton = new PushButton("Example", new ClickListener() {
			public void onClick(Widget sender) {
				example(true);
			}
		});
		exampleButton.setTitle("Fills in example values for demonstration purposes");
		panel.add(exampleButton);
		
		return panel;
	}
	
	private CellPanel createResetButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		resetButton = new PushButton("Reset", new ClickListener() {
			public void onClick(Widget sender) {
				reset(true);
			}
		});
		resetButton.setTitle("Resets all fields");
		panel.add(resetButton);
		
		return panel;
	}
	
	private CellPanel createConvertButton() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		panel.add(statusLabel);
		convertButton = new PushButton("Convert to RDF", new ClickListener() {
			public void onClick(Widget sender) {
				convert2Rdf();
			}
		});
		convertButton.setTitle("Converts the current vocabulary contents into RDF format.");
		panel.add(convertButton);
		
		return panel;
	}
	

	String putValues(Map<String, String> values) {

		String fullTitle = fullTitleTb.getText().trim();
		if ( fullTitle.length() == 0 ) {
			return "Please, specify a title for the vocabulary";
		}
		values.put("fullTitle", fullTitle);
		
		String creator = creatorTb.getText().trim();
		if ( creator.length() == 0 ) {
			return "Please, specify the creator of the vocabulary";
		}
		values.put("creator", creator);
		
		String briefDescription = descriptionTb.getText().trim();
		if ( briefDescription.length() == 0 ) {
			return "Please, specify the brief description of the vocabulary";
		}
		values.put("briefDescription", briefDescription);
		
		String authority = authorityField.getValue();
		if ( authority.length() == 0 ) {
			return "Please, specify the authority abbreviation";
		}
		values.put("authority", authority);
		
//		String ontologyUri = ontologyUriTb.getText().trim();
//		if ( ontologyUri.length() == 0 ) {
//			return "Please, select a URI for the ontology to be generated";
//		}
//		values.put("ontologyUri", ontologyUri);
		
		// NOTE 3/21/09: namespaceRoot will be ignored because ontologyUri takes precedence
		// See Converter.
		values.put("namespaceRoot", NAMESPACE_ROOT);


		
		String err = classPanel.putValues(values);
		if ( err != null ) {
			return err;
		}
		
		
		return null;
	}

	
	void reset(boolean confirm) {
		if ( confirm
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		statusLabel.setText("");
		
		fullTitleTb.setText("");
		creatorTb.setText("");
		descriptionTb.setText("");
		authorityField.setValue("");
		classPanel.reset(false);
	}

	void example(boolean confirm) {
		if ( confirm 
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		
		statusLabel.setText("");
		
		fullTitleTb.setText(fullTitleAttrDef.getExample());
		creatorTb.setText(creatorAttrDef.getExample());
		descriptionTb.setText(descriptionAttrDef.getExample());
		authorityField.setValue(authorityAttrDef.getExample());
		
		classPanel.example(false);
	}
	
	
	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	void convert2Rdf() {
		Map<String, String> values = new HashMap<String, String>();

		// error only possibly from the vocabPanel:
		String error;
		if ( (error = putValues(values)) != null ) {
			statusLabel.setHTML("<font color=\"red\">" + error+ "</font>");
			mainPanel.conversionError(error);
			return;
		}
		
		statusLabel.setHTML("<font color=\"blue\">" + "Converting ..." + "</font>");
		enable(false);
		mainPanel.converting();
		
		Main.log("convert2Rdf: values = " +values);

		// do test conversion
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Main.log("convertTest: error: " +error);
				mainPanel.conversionError(error);
				statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
				enable(true);
			}

			public void onSuccess(ConversionResult conversionResult) {
				String error = conversionResult.getError();
				if ( error != null ) {
					Main.log("convertTest: error: " +error);
//					statusLabel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
					statusLabel.setHTML("<font color=\"red\">" +error+ "</font>");
					mainPanel.conversionError(error);
				}
				else {
					Main.log("convertTest: OK: " +conversionResult.getRdf());
					statusLabel.setHTML("<font color=\"green\">" + "Conversion complete" + "</font>");
					mainPanel.conversionOk(conversionResult);
					statusLabel.setText("");
				}
				enable(true);
			}
		};

		Main.log("convertTest: converting ... ");
		
//		Voc2Rdf.voc2rdfService.convert(values, callback);
		Main.ontmdService.convert2Rdf(values, callback);

	}

	private void enable(boolean enabled) {
		convertButton.setEnabled(enabled);
		exampleButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		classPanel.enable(enabled);
	}

}
