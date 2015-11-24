package org.mmisw.orrportal.gwt.client.portal.extont;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.portal.PortalConsts;
import org.mmisw.orrportal.gwt.client.portal.PortalControl;
import org.mmisw.orrportal.gwt.client.util.MyDialog;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This page is for allowing the user to "upload" the ontology into the working
 * space. (Note: do not confuse "upload" with "register".)
 *
 * <p>
 * Based on UploadLocalOntologyPanel, which will be eventually removed.
 *
 * @author Carlos Rueda
 */
class RegisterNewPage1 extends BasePage {

	private static final String UPLOAD_ACTION = GWT.getModuleBaseURL() + "upload";

	private VerticalPanel contents = new VerticalPanel();

	// note: the 2 radiobuttons were for i) local file and ii) remote (URI) file, as in an previous
	// version of this utility; I'm only keeping the local file option but didn't clean up everything
	// yet (the remote option may be re-incorporated).
	//
	private RadioButton rb0;
	private RadioButton rb1;

	private FormPanel formPanel = new FormPanel();
	private FileUpload upload;

	private final TextArea textArea = INCLUDE_RDF ? new TextArea() : null;

	private PushButton loadButton;

	private String details;
	private PushButton detailsButton;

	private String registryOntologyUri;
	private PushButton selectButton;


	private final FileTypePanel fileTypePanel = new FileTypePanel();


	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 *
	 * @param tempOntologyInfoListener
	 * @param allowLoadOptions
	 */
	RegisterNewPage1(RegisterNewWizard wizard) {
		super(wizard, false, true);
		nextButton.setEnabled(false);
		contents.setSize("650px", "200px");
		addContents(contents);

		createDetailsButton();

		createLoadButton();
		recreate();
	}


	private void recreate() {
		upload = new FileUpload();
		upload.setTitle("The path to the ontology in your local system");
		upload.setWidth("300");
		upload.setName("ontologyFile");

		contents.clear();

		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
//		panel.setBorderWidth(1);
		int row = 0;

		String info =
			"<br/>" +
			"First step is to indicate the ontology you want to register. " +
			"Several file formats are supported. Select the format that corresponds to your file. " +
			"Then, you can indicate the " +
			"desired type of hosting. You may need to provide additional information depending on the " +
			"type of hosting. " +
			"See this <a target=\"_blank\" href=\"" +PortalConsts.REG_TYPE_HELP_PAGE+ "\"" +
			">manual page</a> for details." +
			"<br/>" +
			"<br/>" +
			"Please, select your ontology file and format:"
		;
		panel.setWidget(row, 0, new HTML(info));
		panel.getFlexCellFormatter().setAlignment(row, 0,
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		contents.add(panel);
		contents.add(createWidget());
	}



	private Widget createWidget() {

		FlexTable panel = new FlexTable();
		int row = 0;

		panel.getFlexCellFormatter().setColSpan(row, 0, 3);
		panel.setWidget(row, 0, prepareUploadPanel());
		panel.getFlexCellFormatter().setAlignment(row, 1,
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		int col = 0;

		panel.setWidget(row, col, fileTypePanel.getWidget());
		panel.getFlexCellFormatter().setAlignment(row, col,
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;


		HorizontalPanel buttons = new HorizontalPanel();
		if ( loadButton != null ) {
			buttons.add(loadButton);
		}

		// include the "details" button only if an administrator is logged in
		// OR this is running in my dev environment (for testing)
		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		if ( (loginResult != null && loginResult.isAdministrator())
		||   !GWT.isScript()
		) {
			buttons.add(detailsButton);
		}

		panel.getFlexCellFormatter().setColSpan(row, col, 2);
		panel.setWidget(row, col, buttons);
		panel.getFlexCellFormatter().setAlignment(row, col,
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		row++;


		if ( INCLUDE_RDF ) {
			CellPanel resultPanel = new VerticalPanel();
			textArea.setReadOnly(true);
			textArea.setSize("400px", "100px");

			panel.getFlexCellFormatter().setColSpan(row, 0, 3);
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
				statusHtml.setText("");
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
				statusHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
					"<i><font color=\"blue\">Please wait, loading ontology ...</font></i>");
				Orr.log("onSubmit.");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				statusHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
					"<i><font color=\"blue\">Please wait, examining ontology ...</font></i>");
				String results = event.getResults();
				Orr.log("onSubmitComplete: " +results);
				if ( results != null ) {
					getTempOntologyInfo(results);
				}
				else {
					statusHtml.setHTML("<font color=\"red\">Unexpected null response from server." +
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
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				statusHtml.setHTML("<font color=\"red\">Error</font>"
//						+"<pre>" +error+ "</pre>"
				);
				Window.alert(error);
			}

			public void onSuccess(TempOntologyInfo tempOntologyInfo) {
				enable(true);
				Orr.log("calling getTempOntologyInfo ... success");
				ontologyInfoObtained(tempOntologyInfo);
			}
		};

		nextButton.setEnabled(false);
		Orr.log("calling getTempOntologyInfo ... ");
		String fileType = fileTypePanel.getSelectedType();
		Orr.service.getTempOntologyInfo(fileType, uploadResults, true, INCLUDE_RDF, callback);

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
						statusHtml.setHTML("<font color=\"red\">No file selected</font>");
					}
				}
				else {
					loadRegistryOntology();
				}
			}
		});
		loadButton.setTitle("Uploads the specified file");

	}

	protected void enable(boolean enabled) {
		super.enable(enabled);
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
			statusHtml.setHTML("<font color=\"red\">No file selected</font>");
			return;
		}
		// TODO: load selected remote ontology
		// ...
	}


	private void ontologyInfoObtained(TempOntologyInfo tempOntologyInfo) {
		String error = tempOntologyInfo.getError();
		if ( error != null ) {
			nextButton.setEnabled(false);
			final String baseError = "Make sure the contents are in the proper format and encoding";
			statusHtml.setHTML("<font color=\"red\">Error: " +baseError + ".</font>");
			Orr.log(error);
			Window.alert("Error reading file.\n" +
					baseError + ".\n" +
					"\n" +
					"Server reports:\n\n" +error);
			return;
		}

		getWizard().ontologyInfoObtained(tempOntologyInfo);

		String namespace = tempOntologyInfo.getUri();
		nextButton.setEnabled(true);
		statusHtml.setHTML(
				"<font color=\"green\">Ontology loaded in work space.</font>" +
				"<br/>" +
				"Ontology URI: <b>" +(namespace != null ? namespace : "undefined") + "</b>" +
				"<br/>" +
				"Click Next to continue."
		);

		if ( INCLUDE_RDF ) {
			String rdf = tempOntologyInfo.getRdf();
			if ( rdf != null ) {
				textArea.setText(rdf);
			}
		}

		details = tempOntologyInfo.getDetails();
		detailsButton.setEnabled(true);
	}

	@Override
	public RegisterNewWizard getWizard() {
		return (RegisterNewWizard) wizard;
	}


	/**
	 * Panel that allows to select the type of the file to be loaded.
	 */
	static class FileTypePanel  {

		/** these are the serialization languages that Jena supports for reading, plus
		    the types supported by the orrclient module.
		    TODO: obtain this from orrclient itself when we have time.
		*/
		private static String[] FILE_TYPES = {
			"RDF/XML",
			"OWL/XML",
			"N3",
			"N-TRIPLE",
			"TURTLE",
			"voc2skos"
		};
		private static RadioButton[] bts = new RadioButton[FILE_TYPES.length];

		FileTypePanel() {
			panel.add(new Label("File type: "));
			VerticalPanel vp = new VerticalPanel();
			panel.add(vp);
			for ( int i = 0; i < FILE_TYPES.length; i++ ) {
				vp.add( bts[i] = new RadioButton("filetype", FILE_TYPES[i]) );
			}
			bts[0].setChecked(true);
		}

		Widget getWidget() { return panel; }

		String getSelectedType() {
			for ( int i = 0; i < FILE_TYPES.length; i++ ) {
				if ( bts[i].isChecked() ) {
					return FILE_TYPES[i];
				}
			}
			// getting here should not happen.
			return FILE_TYPES[0];
		}

		private CellPanel panel = new HorizontalPanel();

	}

}
