package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.util.FieldWithChoose;
import org.mmisw.ontmd.gwt.client.util.MyDialog;
import org.mmisw.ontmd.gwt.client.util.TLabel;
import org.mmisw.ontmd.gwt.client.util.Util;
import org.mmisw.ontmd.gwt.client.voc2rdf.rpc.ConversionResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
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
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
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
	
	private TextBoxBase fullTitleTb;
	private TextBoxBase creatorTb;
	private TextBoxBase descriptionTb;
	private FieldWithChoose authorityField;
	
	
	private OntologyUriPanel ontologyUriPanel = new OntologyUriPanel();
	
	private ClassPanel classPanel;
	
	static class StatusPanel {
		private HorizontalPanel hp = new HorizontalPanel();
		private HTML waitingHtml = new HTML();
		private HTML statusLabel = new HTML();

		StatusPanel() {
			hp.setSpacing(4);
			hp.setVerticalAlignment(ALIGN_MIDDLE);
			hp.add(waitingHtml);
			hp.add(statusLabel);
		}
		void setWaiting(boolean waiting) {
			waitingHtml.setHTML(waiting ? "<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\">" : "");
		}
		void setText(String text) {
			statusLabel.setText(text);
		}

		void setHTML(String html) {
			statusLabel.setHTML(html);
		}

		Widget getWidget() {
			return hp;
		}
	}
	
	StatusPanel statusPanel = new StatusPanel();
	private PushButton convertButton;


	private PushButton exampleButton;
	private PushButton resetButton;
	
	protected Voc2RdfMainPanel mainPanel;

	
	VocabPanel(Voc2RdfMainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("1000");
		
		fullTitleAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("fullTitle");
		creatorAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("creator");
		descriptionAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("description");
		authorityAttrDef = Voc2Rdf.baseInfo.getAttrDefMap().get("authority");

		add(createForm());
		
		ontologyUriPanel.update();

		ChangeListener cl = new ChangeListener() {
			public void onChange(Widget sender) {
				ontologyUriPanel.update();
			}
		};
		
		classPanel.getFieldWithChoose().addChangeListener(cl);
		authorityField.addChangeListener(cl);
	}

	/**
	 * Creates the main form
	 */
	private Widget createForm() {
		contentsContainer.setBorderWidth(1);
		
		
		fullTitleTb = Util.createTextBoxBase(1, "700", new ChangeListener() {
			public void onChange(Widget sender) {
				statusPanel.setText("");
			}
		});
		
		creatorTb = Util.createTextBoxBase(1, "700", new ChangeListener() {
			public void onChange(Widget sender) {
				statusPanel.setText("");
			}
		});
		
		descriptionTb = Util.createTextBoxBase(4, "700", new ChangeListener() {
			public void onChange(Widget sender) {
				statusPanel.setText("");
			}
		});
		
		ChangeListener cl = new ChangeListener () {
			public void onChange(Widget sender) {
				statusPanel.setText("");
			}
		};
		authorityField = new FieldWithChoose(authorityAttrDef, cl);

		
		
		
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
		flexPanel.setWidth("1000");
		int row = 0;
				
		
		// Note: preamble and ontologyUriPanel were originally in a single row, but I reverted to
		// separate rows to avoid mis-behaviors in layout as the ontology URI is updated
		
		HTML preamble = new HTML(
				"Please fill in the following information to create your vocabulary in RDF.<br/>" +
				"Fields marked <font color=red>*</font> are required."
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, preamble);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);
		flexPanel.setWidget(row, 0, ontologyUriPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		

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
		flexPanel.setWidget(row, 0, new TLabel(authorityAttrDef.getLabel(), true, "<b>Authority</b>:<br/>" +authorityAttrDef.getTooltip()));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		flexPanel.getFlexCellFormatter().setColSpan(row, 1, 2);
		flexPanel.setWidget(row, 1, authorityField);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;


		classPanel = new ClassPanel(this);
		TabPanel tabPanel = new TabPanel();
		tabPanel.add(classPanel, "Vocabulary");
		tabPanel.selectTab(0);
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 4);

		flexPanel.setWidget(row, 0, tabPanel);
		row++;

		
		return flexPanel;
	}
	
	/**
	 * Handles the information about the resulting URI for the ontology.
	 */
	private class OntologyUriPanel extends HorizontalPanel {

		// TODO get this from some parameter
		private static final String serverAndRoot = "http://mmisw.org/ont/";
		
		private TLabel tlabel = new TLabel("", 
				"This is the URI that will be given to the resulting ontology.<br/> " +
				"<br/>" +
				"By default, this URI is automatically composed from the authority and class name fields and according to " +
				"<a target=\"_blank\" href=\"http://marinemetadata.org/apguides/ontprovidersguide/ontguideconstructinguris\"" +
				">MMI recommendations</a>.<br/> " +
				"Further validation will be applied if the ontology is summitted to the MMI Registry and Repository. <br/> " +
				"<br/>" +
				serverAndRoot+ " will be given as the server and root, and the values<br/> " +
				"entered in the authority and class name fields will be used to complete the authority and shortName components. <br/>" +
				"<br/>" +
				"Use the \"Set\" button if you prefer a different URI than the one given by default."
		);
		
		private boolean userGiven = false;
		private String userUri;
		
		private HTML uriHtml = new HTML();
		
		private PushButton userUriButton = new PushButton("Set", new ClickListener() {
			public void onClick(Widget sender) {
				promptUserUri(getAbsoluteLeft(), getAbsoluteTop());
			}
		});
		
		/**
		 * 
		 */
		OntologyUriPanel() {
			super();
			setStylePrimaryName("TermTable-OddRow");
			setSpacing(3);
			setVerticalAlignment(ALIGN_MIDDLE);

			userUriButton.setTitle("Allows you to set the URI");
			DOM.setElementAttribute(userUriButton.getElement(), "id", "my-button-id");

			HorizontalPanel hp = this;
			hp.add(tlabel);
			hp.add(uriHtml);
			hp.add(userUriButton);
		}

		/** displays a popup to prompt the user for the URI or revert to default assignment 
		 * @param left 
		 * @param top 
		*/
		void promptUserUri(int left, int top) {
			final TextBoxBase textBox = new TextBox();
			final MyDialog popup = new MyDialog(textBox) {
				public boolean onKeyUpPreview(char key, int modifiers) {
					// avoid ENTER from closing the popup without proper reaction
					if ( key == KeyboardListener.KEY_ESCAPE ) {
						hide();      // only ESCAPE keystroke closes the popup
						return false;
					}
					
					if ( key == KeyboardListener.KEY_ENTER ) {
						String str = textBox.getText().trim();
						_processAccept(str, this);
					}
				    return true;
				  }
			};
			popup.setText("Specify the URI for the ontology");
			
			textBox.setWidth("300");
			if ( userUri != null ) {
				textBox.setText(userUri);
			}

			popup.getButtonsPanel().insert(
					new PushButton("OK", new ClickListener() {
						public void onClick(Widget sender) {
							String str = textBox.getText().trim();
							_processAccept(str, popup);
						}
					})
					, 0
			);

			if ( userGiven ) {
				popup.getButtonsPanel().insert(
						new PushButton("Revert to default", new ClickListener() {
							public void onClick(Widget sender) {
								userGiven = false;
								update();
								popup.hide();
							}
						})
						, 0
				);
			}
			
			popup.setPopupPosition(left, top + 20);
			new Timer() { @Override
				public void run() {
					textBox.setFocus(true);
				}
			}.schedule(180);
			
			popup.show();

		}
		
		private void _processAccept(String str, MyDialog popup) {
			if ( str.length() > 0 ) {
				userUri = str;
				userGiven = true;
				update();
				popup.hide();	
			}
		}
		
		void update() {
			String uri;
			
			if ( userGiven ) {
				// use the user-given value as it is:
				uri = userUri;
			}
			else {
				//
				// replace any colon (:) in the pieces that go to the ontology URI
				// with underscores (_):
				//
				String authority = authorityField.getValue().trim();
				if ( authority.length() == 0 ) {
					authority = "<font color=\"red\">auth</font>";
				}
				else {
					authority = authority.replace(':', '_');
				}
	
				// TODO handle className vs. specific shortName field
				String shortName = classPanel.getClassName();
				if ( shortName.length() == 0 ) {
					shortName = "<font color=\"red\">shortName</font>";
				}
				else {
					shortName = shortName.replace(':', '_');
				}
				
				uri = serverAndRoot + authority+ "/" +shortName+ "/";
			}
			
			uriHtml.setHTML(
					"<code>" +
					uri +
					"</code>"
			);
		}
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
		panel.add(statusPanel.getWidget());
		convertButton = new PushButton("Convert to RDF", new ClickListener() {
			public void onClick(Widget sender) {
				convert2Rdf();
			}
		});
		convertButton.setTitle("Converts the current vocabulary contents into RDF format.");
		panel.add(convertButton);
		
		return panel;
	}
	

	CheckError putValues(Map<String, String> values) {

		String fullTitle = fullTitleTb.getText().trim();
		if ( fullTitle.length() == 0 ) {
			return new CheckError("Please, specify a title for the vocabulary");
		}
		values.put("fullTitle", fullTitle);
		
		String creator = creatorTb.getText().trim();
		if ( creator.length() == 0 ) {
			return new CheckError("Please, specify the creator of the vocabulary");
		}
		values.put("creator", creator);
		
		String briefDescription = descriptionTb.getText().trim();
		if ( briefDescription.length() == 0 ) {
			return new CheckError("Please, specify the brief description of the vocabulary");
		}
		values.put("briefDescription", briefDescription);
		
		String authority = authorityField.getValue();
		if ( authority.length() == 0 ) {
			return new CheckError("Please, specify the authority abbreviation");
		}
		values.put("authority", authority);
		
		if ( ontologyUriPanel.userGiven ) {
			String ontologyUri = ontologyUriPanel.uriHtml.getText();
			values.put("ontologyUri", ontologyUri);
		}
		
		// NOTE 3/21/09: namespaceRoot will be ignored because ontologyUri takes precedence
		// See Converter.
		values.put("namespaceRoot", NAMESPACE_ROOT);


		
		CheckError err = classPanel.putValues(values);
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
		statusPanel.setText("");
		
		fullTitleTb.setText("");
		creatorTb.setText("");
		descriptionTb.setText("");
		authorityField.setValue("");
		classPanel.reset();
		
		ontologyUriPanel.update();
	}

	void example(boolean confirm) {
		if ( confirm 
		&&  ! Window.confirm("This action will replace the current values") ) {
			return;
		}
		
		statusPanel.setText("");
		
		fullTitleTb.setText(fullTitleAttrDef.getExample());
		creatorTb.setText(creatorAttrDef.getExample());
		descriptionTb.setText(descriptionAttrDef.getExample());
		authorityField.setValue(authorityAttrDef.getExample());
		
		classPanel.example();
		ontologyUriPanel.update();
	}
	
	static class CheckError {
		String msg;
		CheckError(String msg) {
			super();
			this.msg = msg;
		}
		public String toString() {
			return msg;
		}
	}
	
	/**
	 * Runs the "test conversion" on the vocabulary contents and with
	 * ad hoc metadata atttributes.
	 */
	void convert2Rdf() {
		Map<String, String> values = new HashMap<String, String>();

		statusPanel.setWaiting(true);
		statusPanel.setHTML(" <font color=\"blue\">" + "Checking ..." + "</font>");
		enable(false);

		// error only possibly from the vocabPanel:
		CheckError error;
		if ( (error = putValues(values)) != null ) {
			statusPanel.setWaiting(false);
			statusPanel.setHTML("<font color=\"red\">" + error+ "</font>");
			mainPanel.conversionError(error.msg);
			enable(true);
			return;
		}
		
		statusPanel.setHTML("<font color=\"blue\">" + "Converting ..." + "</font>");
		mainPanel.converting();
		
//		Main.log("convert2Rdf: values = " +values);

		// do test conversion
		AsyncCallback<ConversionResult> callback = new AsyncCallback<ConversionResult>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Main.log("convertTest: error: " +error);
				mainPanel.conversionError(error);
				statusPanel.setWaiting(false);
				statusPanel.setHTML("<font color=\"red\">" +"Error"+ "</font>");
				enable(true);
			}

			public void onSuccess(ConversionResult conversionResult) {
				String error = conversionResult.getError();
				if ( error != null ) {
					Main.log("convertTest: error: " +error);
					statusPanel.setWaiting(false);
					statusPanel.setHTML("<font color=\"red\">" +error+ "</font>");
					mainPanel.conversionError(error);
				}
				else {
					Main.log("convert2Rdf: OK");
					mainPanel.conversionOk(conversionResult);
				}
				enable(true);
			}
		};

		Main.log("convertTest: converting ... ");
		
		Main.ontmdService.convert2Rdf(values, callback);

	}

	void enable(boolean enabled) {
		authorityField.enable(enabled);
		convertButton.setEnabled(enabled);
		exampleButton.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		classPanel.enable(enabled);
	}

}
