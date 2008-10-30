package org.mmisw.ontmd.gwt.client;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Form elements for the contents of the vocabulary.
 * 
 * @author Carlos Rueda
 */
public class OntologyPanel extends VerticalPanel {

	private static final String UPLOAD_ACTION = 
		GWT.isScript() ? "/ontmd/upload" : "/upload";
	
	private RadioButton rb0 = new RadioButton("grp", "Local file:");
	private RadioButton rb1 = new RadioButton("grp", "Registry file:");

	private FormPanel formPanel = new FormPanel();
	private FileUpload upload;
	private TextBox statusField = new TextBox();
	
	private final TextArea textArea = new TextArea();
	
	private PushButton loadButton;
	
	private String registryOntologyUri;
	private PushButton selectButton;
	
	protected MainPanel mainPanel;

	
	OntologyPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
		recreate();
	}
	
	
	void recreate() {
		upload = new FileUpload();
		upload.setTitle("The path to the ontology in your local system");
		upload.setWidth("300");
		upload.setName("ontologyFile");

		statusField.setText("");
		textArea.setText("");
		clear();
		add(new HTML("Please specify your ontology file."));
		add(createWidget());
	}

	
	
	private Widget createWidget() {

		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, prepareUploadPanel());
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		
		CellPanel buttons = createButtons();
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		statusField.setWidth("600");
		statusField.setReadOnly(true);
		panel.setWidget(row, 1, statusField);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		CellPanel resultPanel = new VerticalPanel();
		textArea.setReadOnly(true);
	    textArea.setSize("700px", "300px");
	    
	    panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, resultPanel);
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(textArea);
	    resultPanel.add(decPanel);
	    row++;


		return panel;
	}
	
	
	FormPanel prepareUploadPanel() {
		formPanel.setAction(UPLOAD_ACTION);
		
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_POST);

		
		FlexTable panel = new FlexTable();
		formPanel.setWidget(panel);
		
		int row = 0;

		selectButton = new PushButton("Select registered ontology", new ClickListener() {
			public void onClick(Widget sender) {
				Window.alert("Sorry, not yet implemented");
				registryOntologyUri = null;  // TODO
			}
		});
		selectButton.setTitle("Selects an ontology from the MMI Registry");

		final HorizontalPanel uploadContainer = new HorizontalPanel();
		uploadContainer.add(upload);
		rb0.setChecked(true);
//		upload.setEnabled(true);   // --> this method is not available
		selectButton.setEnabled(false);
		ClickListener clickListener = new ClickListener() {
			private TextBox chooseLabel;
			public void onClick(Widget sender) {
				statusField.setText("");
//				upload.setEnabled(rb0.isChecked());  // --> this method is not available 
				uploadContainer.clear();
				if ( rb0.isChecked() ) {
					uploadContainer.add(upload);
				}
				else {
					if ( chooseLabel == null ) {
						chooseLabel = new TextBox();
						chooseLabel.setText("");
						chooseLabel.setEnabled(false);
					}
					uploadContainer.add(chooseLabel);
				}
				selectButton.setEnabled(rb1.isChecked());
			}
		};
		rb0.addClickListener(clickListener);
		rb1.addClickListener(clickListener);


		panel.setWidget(row, 0, rb0);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.setWidget(row, 1, uploadContainer);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		panel.setWidget(row, 2, new Label("To submit a new ontology"));
		row++;


		
		panel.setWidget(row, 0, rb1);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		panel.setWidget(row, 1, selectButton);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		panel.setWidget(row, 2, new Label("To prepare a new version of an existing ontology"));
		row++;


		
		formPanel.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
				statusField.setText("Loading ...");
				Main.log("onSubmit.");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				statusField.setText("Examining ontology ...");
				String results = event.getResults();
				Main.log("onSubmitComplete: " +results);
				if ( results != null ) {
					textArea.setText(results);
					getOntologyInfo(results);
				}
			}
			
		});

		return formPanel;
	}
	
	
	private void getOntologyInfo(String uploadResults) {
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				statusField.setText("Error loading");
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				statusField.setText("Ontology loaded. Original base URI: " +ontologyInfo.getUri());
				mainPanel.setOntologyInfo(ontologyInfo, false);
				String rdf = ontologyInfo.getRdf();
				if ( rdf != null ) {
					textArea.setText(rdf);
				}
			}
		};

		Main.log("getOntologyInfo: uploadResults = " +uploadResults);
		Main.ontmdService.getOntologyInfo(uploadResults, callback);

	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		loadButton = new PushButton("Load ontology", new ClickListener() {
			public void onClick(Widget sender) {
				String filename = upload.getFilename();
				if ( rb0.isChecked() ) {
					if ( filename != null && filename.length() > 0 ) {
						formPanel.submit();
					}
					else {
						statusField.setText("Please, select the local ontology file");
					}
				}
				else {
					loadRegistryOntology();
				}
			}
		});
		loadButton.setTitle("Upload");
		panel.add(loadButton);
		
		return panel;
	}
	

	private void loadRegistryOntology() {
		if ( registryOntologyUri == null ) {
			statusField.setText("Please, select the registry ontology");
			return;
		}
		// TODO: load selected remote ontology
		// ...
	}

}
