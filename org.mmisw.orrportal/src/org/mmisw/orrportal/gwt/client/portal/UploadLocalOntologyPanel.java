package org.mmisw.orrportal.gwt.client.portal;

import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel to "upload" a local ontology file into the working space.
 * 
 * @author Carlos Rueda
 */
public class UploadLocalOntologyPanel extends VerticalPanel {
	
	private static final String UPLOAD_ACTION = GWT.getModuleBaseURL() + "upload";

	// false: do not retrieve RDF contents from server.
	private static final boolean INCLUDE_RDF = false;
	
	
	private TempOntologyInfoListener tempOntologyInfoListener;
	
	// note: the 2 radiobuttons were for i) local file and ii) remote (URI) file, as in an previous
	// version of this utility; I'm only keeping the local file option but didn't clean up everything
	// yet (the remote option may be re-incorporated).
	//
	private RadioButton rb0;
	private RadioButton rb1;

	private FormPanel formPanel = new FormPanel();
	private FileUpload upload;
	private HTML statusLoad = new HTML();
	
//	private TextBox statusField2 = new TextBox();
	
	private final TextArea textArea = INCLUDE_RDF ? new TextArea() : null;
	
	private PushButton loadButton;
	
	private String details;
	private PushButton detailsButton;
	
	private String registryOntologyUri;
	private PushButton selectButton;
	
	private CheckBox preserveOriginalBaseNamespace;
	

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * 
	 * @param tempOntologyInfoListener
	 * @param allowLoadOptions
	 */
	public UploadLocalOntologyPanel(
			TempOntologyInfoListener tempOntologyInfoListener,
			boolean allowLoadOptions
	) {
		this.tempOntologyInfoListener = tempOntologyInfoListener;
		setWidth("650");

		createDetailsButton();

		statusLoad.setText("");
//		statusField2.setText("");

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
			
			preserveOriginalBaseNamespace = new CheckBox("Preserve original base namespace");
			preserveOriginalBaseNamespace.setChecked(false);
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
		
//		if ( allowLoadOptions ) {
//			hp.add(loadButton);
//		}
		
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
			
			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, preserveOriginalBaseNamespace);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
			
		}
		
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add(loadButton);
		buttons.add(detailsButton);
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
//		statusField2.setWidth("400");
//		statusField2.setReadOnly(true);
//		panel.setWidget(row, 0, statusField2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

//		panel.setWidget(row, 1, detailsButton);
//		panel.getFlexCellFormatter().setAlignment(row, 1, 
//				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
		row++;

		
		if ( INCLUDE_RDF ) {
			CellPanel resultPanel = new VerticalPanel();
			textArea.setReadOnly(true);
			textArea.setSize("400px", "100px");

			panel.getFlexCellFormatter().setColSpan(row, 0, 2);
			panel.setWidget(row, 0, resultPanel);

			DecoratorPanel decPanel = new DecoratorPanel();
			decPanel.setWidget(textArea);
			resultPanel.add(decPanel);
			row++;
		}

		return panel;
	}
	
	
	FormPanel prepareUploadPanel() {
		formPanel.setAction(UPLOAD_ACTION);
		
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		formPanel.setMethod(FormPanel.METHOD_POST);

		
		FlexTable panel = new FlexTable();
		formPanel.setWidget(panel);
		
		
		
		rb0 = new RadioButton("grp", "Local file:");

		
		final HorizontalPanel uploadContainer = new HorizontalPanel();
		uploadContainer.add(upload);
		rb0.setChecked(true);
//		upload.setEnabled(true);   // --> this method is not available
		ClickListener clickListener = new ClickListener() {
			private TextBox chooseLabel;
			public void onClick(Widget sender) {
				statusLoad.setText("");
//				statusField2.setText("");
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
				
				if ( selectButton != null ) {
					selectButton.setEnabled(rb1.isChecked());
				}
			}
		};
		rb0.addClickListener(clickListener);
		
		if ( rb1 != null ) {
			rb1.addClickListener(clickListener);
		}

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


		
		formPanel.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
				statusLoad.setHTML("<font color=\"blue\">Loading ...</font>");
//				statusField2.setText("");
				Orr.log("onSubmit.");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				statusLoad.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
					"<i><font color=\"blue\">Please wait, examining ontology ...</font></i>");
				String results = event.getResults();
				Orr.log("onSubmitComplete: " +results);
				if ( results != null ) {
					getTempOntologyInfo(results);
				}
				else {
					statusLoad.setHTML("<font color=\"red\">Unexpected null response from server." +
							"Please try again later.</font>");
					enable(true);
				}
			}
			
		});

		return formPanel;
	}
	
	
	private void getTempOntologyInfo(String uploadResults) {
		AsyncCallback<TempOntologyInfo> callback = new AsyncCallback<TempOntologyInfo>() {
			public void onFailure(Throwable thr) {
				enable(true);
				Orr.log("calling getTempOntologyInfo ... failure! ");
				UploadLocalOntologyPanel.this.onFailure(thr);
			}

			public void onSuccess(TempOntologyInfo tempOntologyInfo) {
				enable(true);
				Orr.log("calling getTempOntologyInfo ... success!");
				UploadLocalOntologyPanel.this.onSuccess(tempOntologyInfo);
			}
		};

		Orr.log("calling getTempOntologyInfo ... ");
		String fileType = null; // TODO handle fileType
		Orr.service.getTempOntologyInfo(fileType, uploadResults, true, INCLUDE_RDF, callback);

	}
	
	
	
	void onFailure(Throwable thr) {
		statusLoad.setHTML("<font color=\"red\">Error</font>");
		String error = thr.getClass().getName()+ ": " +thr.getMessage();
		while ( (thr = thr.getCause()) != null ) {
			error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
		}
//		statusField2.setText(error);
		Window.alert(error);
	}

	void onSuccess(TempOntologyInfo tempOntologyInfo) {
		ontologyInfoObtained(tempOntologyInfo);
	}

	
	private void createLoadButton() {
		loadButton = new PushButton("Load ontology", new ClickListener() {
			public void onClick(Widget sender) {
				String filename = upload.getFilename();
				if ( rb0.isChecked() ) {
					if ( filename != null && filename.length() > 0 ) {
						enable(false); 
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
	
	private void enable(boolean enabled) {
		loadButton.setEnabled(enabled);
		detailsButton.setEnabled(enabled);
	}
	
	
	private void showDetails(String details) {
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

	
	private void createDetailsButton() {
		detailsButton = new PushButton("Details", new ClickListener() {
			public void onClick(Widget sender) {
				showDetails(details);
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

	
	private void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		String error = tempOntologyInfo.getError();
		if ( error != null ) {
			statusLoad.setHTML("<font color=\"red\">Error</font>");
			Window.alert("Error reading file. Make sure it is an RDF file.\n" +
					"Server reports:\n\n" +error);
			return;
		}
		statusLoad.setHTML("<font color=\"green\">Ontology loaded into editor</font>");
//		statusField2.setText("Original base URI: " +tempOntologyInfo.getUri());
		
		if ( INCLUDE_RDF ) {
			String rdf = tempOntologyInfo.getRdf();
			if ( rdf != null ) {
				textArea.setText(rdf);
			}
		}
		
		details = tempOntologyInfo.getDetails();
		detailsButton.setEnabled(true);
		
		tempOntologyInfo.setPreserveOriginalBaseNamespace(preserveOriginalBaseNamespace.isChecked());
		
		if ( tempOntologyInfoListener != null ) {
			tempOntologyInfoListener.tempOntologyInfoObtained(tempOntologyInfo);
		}
	}

}
