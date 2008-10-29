package org.mmisw.ontmd.gwt.client;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;

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
		"/upload";
//		"http://localhost:8080/ont/?_upload";
	
	
	private FormPanel formPanel = new FormPanel();
	private FileUpload upload = new FileUpload();
	private TextBox uploadStatus = new TextBox();
	
	private final TextArea textArea = new TextArea();
	
	private PushButton loadButton;
	
	protected MainPanel mainPanel;

	
	OntologyPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		setWidth("850");
		
		add(new HTML("Please specify your ontology file"));
		add(createForm());
	}

	private Widget createForm() {

		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, prepareUploadPanel());
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		
		uploadStatus.setWidth("700");
		uploadStatus.setReadOnly(true);
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, uploadStatus);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		CellPanel resultPanel = new VerticalPanel();
		textArea.setReadOnly(true);
	    textArea.setSize("800px", "100px");
	    
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

		VerticalPanel panel = new VerticalPanel();
		formPanel.setWidget(panel);

		upload.setTitle("The path to the ontology in your local system");
		upload.setWidth("400");
		upload.setName("ontologyFile");
		panel.add(upload);

		
		formPanel.addFormHandler(new FormHandler() {

			public void onSubmit(FormSubmitEvent event) {
				uploadStatus.setText("Loading ...");
				Main.log("onSubmit.");
			}

			public void onSubmitComplete(FormSubmitCompleteEvent event) {
				uploadStatus.setText("submit complete");
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
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				mainPanel.setOntologyInfo(ontologyInfo);
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
				if ( filename != null && filename.length() > 0 ) {
					formPanel.submit();
				}
				else {
					uploadStatus.setText("Please, specify the file");
				}
			}
		});
		loadButton.setTitle("Upload");
		panel.add(loadButton);
		
		return panel;
	}
	

}
