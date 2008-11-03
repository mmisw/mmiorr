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
	private HTML statusLoad = new HTML();
	private TextBox statusField2 = new TextBox();
	
	private final TextArea textArea = new TextArea();
	
	private PushButton loadButton;
	private PushButton detailsButton;
	
	private String registryOntologyUri;
	private PushButton selectButton;
	
	protected MainPanel mainPanel;

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * @param mainPanel
	 * @param allowLoadOptions  true to include buttons to load an ontology;
	 */
	OntologyPanel(MainPanel mainPanel, boolean allowLoadOptions) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
		createDetailsButton();
		statusLoad.setText("");
		statusField2.setText("");
		textArea.setText("");

		if ( allowLoadOptions ) {
			createLoadButton();
		}
		recreate(allowLoadOptions);
	}
	
	
	private void recreate(boolean allowLoadOptions) {
		if ( allowLoadOptions ) {
			upload = new FileUpload();
			upload.setTitle("The path to the ontology in your local system");
			upload.setWidth("300");
			upload.setName("ontologyFile");
		}
		
		clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
//		panel.setBorderWidth(1);
		int row = 0;
		
		String info;
		if ( allowLoadOptions ) {
			info = "Please specify your ontology file.";
		}
		else {
			info = "This panel shows the contents of the loaded ontology.";
		}
		panel.setWidget(row, 0, new HTML(info));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(3);
		hp.add(statusLoad);
		
		if ( allowLoadOptions ) {
			hp.add(loadButton);
		}
		
		panel.setWidget(row, 1, hp);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		add(panel);
		add(createWidget(allowLoadOptions));
	}

	
	
	private Widget createWidget(boolean allowLoadOptions) {
		
		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		if ( allowLoadOptions ) {
			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, prepareUploadPanel());
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		
		statusField2.setWidth("600");
		statusField2.setReadOnly(true);
		panel.setWidget(row, 0, statusField2);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		
		panel.setWidget(row, 1, detailsButton);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
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
				statusLoad.setText("");
				statusField2.setText("");
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

		int row = 0;

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
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(selectButton);
		panel.setWidget(row, 1, hp);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		panel.setWidget(row, 2, new Label("To prepare a new version of an existing ontology"));

		row++;
		
		formPanel.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
				statusLoad.setHTML("<font color=\"blue\">Loading ...</font>");
				statusField2.setText("");
				Main.log("onSubmit.");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				statusLoad.setHTML("<font color=\"blue\">Examining ontology ...</font>");
				String results = event.getResults();
				Main.log("onSubmitComplete: " +results);
				if ( results != null ) {
					textArea.setText(results);
					getOntologyInfoFromPreLoaded(results);
				}
			}
			
		});

		return formPanel;
	}
	
	
	private void getOntologyInfoFromPreLoaded(String uploadResults) {
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				OntologyPanel.this.onFailure(thr);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				OntologyPanel.this.onSuccess(ontologyInfo);
			}
		};

		Main.log("getOntologyInfo: uploadResults = " +uploadResults);
		Main.ontmdService.getOntologyInfoFromPreLoaded(uploadResults, callback);

	}
	
	
	
	void onFailure(Throwable thr) {
		statusLoad.setHTML("<font color=\"red\">Error</font>");
		String error = thr.getClass().getName()+ ": " +thr.getMessage();
		while ( (thr = thr.getCause()) != null ) {
			error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
		}
		statusField2.setText(error);
		Window.alert(error);
	}

	void onSuccess(OntologyInfo ontologyInfo) {
		ontologyInfoObtained(ontologyInfo);
	}

	
	private void createLoadButton() {
		loadButton = new PushButton("Load ontology", new ClickListener() {
			public void onClick(Widget sender) {
				String filename = upload.getFilename();
				if ( rb0.isChecked() ) {
					if ( filename != null && filename.length() > 0 ) {
						formPanel.submit();
					}
					else {
						statusLoad.setHTML("<font color=\"red\">No file selected</font>");
					}
				}
				else {
					loadRegistryOntology();
				}
			}
		});
		loadButton.setTitle("Uploads the specified file");
		
	}
	
	private void createDetailsButton() {
		detailsButton = new PushButton("Details", new ClickListener() {
			public void onClick(Widget sender) {
				mainPanel.showDetails();
			}
		});
		detailsButton.setEnabled(false);
		detailsButton.setTitle("Shows some details about the metadata " +
				"values captured from the original ontology file");
	}
	
	
	private void loadRegistryOntology() {
		if ( registryOntologyUri == null ) {
			statusLoad.setHTML("<font color=\"red\">No file selected</font>");
			return;
		}
		// TODO: load selected remote ontology
		// ...
	}

	
	private void ontologyInfoObtained(OntologyInfo ontologyInfo) {
		String error = ontologyInfo.getError();
		if ( error != null ) {
			statusLoad.setHTML("<font color=\"red\">Error</font>");
			textArea.setText("Error reading file. Make sure it is an RDF file.\n" +
					"Server reports:\n\n" +error);
			Window.alert(error);
			return;
		}
		statusLoad.setHTML("<font color=\"green\">Ontology loaded</font>");
		statusField2.setText("Original base URI: " +ontologyInfo.getUri());
		mainPanel.setPreloadedOntologyInfo(ontologyInfo, false);
		String rdf = ontologyInfo.getRdf();
		if ( rdf != null ) {
			textArea.setText(rdf);
		}
		detailsButton.setEnabled(true);
	}

}
