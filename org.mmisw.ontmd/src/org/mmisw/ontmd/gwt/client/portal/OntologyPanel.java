package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.CreateVocabularyInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.iserver.gwt.client.rpc.UploadOntologyResult;
import org.mmisw.ontmd.gwt.client.DataPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.metadata.MetadataPanel;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel for a given ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyPanel extends VerticalPanel implements IOntologyPanel {

	private CellPanel container = new VerticalPanel();

	
	private HeaderPanel headerPanel;


	private final DisclosurePanel mdDisclosure = new DisclosurePanel("Metadata details");
	private final DisclosurePanel dataDisclosure = new DisclosurePanel("Contents");
	
	// re-created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	// Handles the data either editing or viewing
	private DataPanel dataPanel;
	
	// created for editing data mode
//	private EditDataPanel editDataPanel;
	
	
	private PushButton reviewButton = null;

	
	/** Ontology to be dispatched */
	private OntologyInfo ontologyInfo;
	
	
	
	public OntologyMetadata getOntologyMetadata() {
		return ontologyInfo.getOntologyMetadata();
	}


	/**
	 * Prepares the overall interface for the given ontology.
	 * 
	 * @param ontologyInfo
	 * @param readOly the initial mode
	 */
	public OntologyPanel(OntologyInfo ontologyInfo, boolean readOnly) {
		super();
		setWidth("100%");
		container.setWidth("100%");
		
		this.ontologyInfo = ontologyInfo;
		
		add(container);
		
		mdDisclosure.setAnimationEnabled(true);
		dataDisclosure.setAnimationEnabled(true);
		
		
		metadataPanel = new MetadataPanel(this, !readOnly);
		mdDisclosure.setContent(metadataPanel);

		dataPanel = new DataPanel(readOnly);
		dataDisclosure.setContent(dataPanel);
		
		
		CellPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		
		headerPanel = new HeaderPanel(readOnly);
		
		panel.add(headerPanel.getWidget());
		panel.add(mdDisclosure);
		panel.add(dataDisclosure);
		
		dataDisclosure.setOpen(true);
		
		enable(!readOnly);
		
	    container.add(panel);
	    
	    getOntologyContents(readOnly);
	}
	
	
	/**
	 * @return the ontologyInfo
	 */
	public OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}


	void updateInterface(boolean readOnly) {
		
		headerPanel.resetElements(readOnly);
		metadataPanel = new MetadataPanel(this, !readOnly);
		mdDisclosure.setContent(metadataPanel);

		boolean link = true;
		metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
	
		if ( readOnly ) {
			// coming from edit mode to view only mode-- reload data
			dataPanel.updateWith(ontologyInfo, readOnly);
		}
		else {
			// coming from view to edit--just update elements for editing (do not reload contents)
			dataPanel.setReadOnly(readOnly);
		}
		
		enable(!readOnly);
	}


	private void getOntologyContents(final boolean readOnly) {
		
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				Main.log("RET getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
				String error = ontologyInfo.getError();
				if ( error != null ) {
					Window.alert(error);
				}
				else {
					ontologyLoaded(ontologyInfo, readOnly);
				}
			}
		};

		headerPanel.updateTitle("<b>" +ontologyInfo.getDisplayLabel()+ "</b> - "+ontologyInfo.getUri()+ "<br/>");
		headerPanel.showProgressMessage("Loading contents. Please wait...");

		metadataPanel.showProgressMessage("Loading contents. Please wait...");
		Main.log("getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
		Main.ontmdService.getOntologyContents(ontologyInfo, callback);
	}


	private void ontologyLoaded(OntologyInfo ontologyInfo, boolean readOnly) {
		this.ontologyInfo = ontologyInfo;
		String error = ontologyInfo.getError();
		if ( error != null ) {
			headerPanel.updateDescription("<font color=\"red\">" +error+ "</font>");
		}
		else {
			// TODO put more info in the description
			headerPanel.updateDescription("");
			
			boolean link = true;
			metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
			
			if ( dataPanel != null ) {
				dataPanel.updateWith(ontologyInfo, readOnly);
			}
//			else if ( editDataPanel != null ) {
//				editDataPanel.updateWith(ontologyInfo);
//			}
		}
	}
	
	private void enable(boolean enabled) {
		if ( metadataPanel != null ) {
			metadataPanel.enable(enabled);
		}
		if ( dataPanel != null ) {
			dataPanel.enable(enabled);
		}
//		if ( editDataPanel != null ) {
//			editDataPanel.enable(enabled);
//		}
	}

	
	/** Shows header information
	 */
	private class HeaderPanel {
		
		private final HorizontalPanel widget = new HorizontalPanel();
		

		private HTML titleHtml = new HTML();
		private HTML descriptionHtml = new HTML();
		HeaderPanel(boolean readOnly) {
			widget.setWidth("100%");
			widget.setSpacing(5);
			widget.setVerticalAlignment(ALIGN_MIDDLE);
//			widget.setBorderWidth(1);
			resetElements(readOnly);
		}
		
		void resetElements(boolean readOnly) {
			widget.clear();
			widget.add(titleHtml);
			widget.add(descriptionHtml);
			if ( ! readOnly ) {
				if ( reviewButton == null ) {
					reviewButton = new PushButton("Review and Upload", new ClickListener() {
						public void onClick(Widget sender) {
							review(true);
						}
					});
					reviewButton.setTitle("Checks the contents " +
						"and prepares the ontology for subsequent upload to the MMI Registry");

				}
				
				widget.add(reviewButton);
				
			}
		}
		
		Widget getWidget() {
			return widget;
		}
		
		void updateTitle(String text) {
			titleHtml.setHTML(text);
		}
		
		void updateDescription(String text) {
			descriptionHtml.setHTML(text);
		}
		
		void showProgressMessage(String msg) {
			descriptionHtml.setHTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> <i>" +msg+ "</i>");
		}
	}


	public void cancel() {
		metadataPanel.cancel();
		dataPanel.cancel();
	}


	private void reenableButton(PushButton button, String text, boolean enabled) {
		if ( text != null ) {
			button.setText(text);
		}
		button.setEnabled(enabled);
	}


	
	private void review(boolean confirm) {
		String error = metadataPanel.putValues(null); // null = just check
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		doReview();
	}

	void doReview() {
		if ( ontologyInfo == null ) {
			Window.alert("Please, load an ontology first");
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			Window.alert("Please, retry the loading of the ontology");
			return;
		}
		
		Map<String, String> newValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(newValues);
		if ( error != null ) {
			Window.alert(error);
			return;
		}
		
		// Ok, put the new values in the ontologyInfo object:
		ontologyInfo.getOntologyMetadata().setNewValues(newValues);
		for ( String uri : newValues.keySet() ) {
			String value = newValues.get(uri);
			newValues.put(uri, value);
		}
		
		CreateOntologyInfo createOntologyInfo = dataPanel.getCreateOntologyInfo();
		
		if ( createOntologyInfo instanceof CreateVocabularyInfo ) {
			// OK: continue
		}
		else {
			Window.alert("sorry, not implemented yet");
			return;
		}
		
		createOntologyInfo.setAuthority(ontologyInfo.getAuthority());
		createOntologyInfo.setShortName(ontologyInfo.getShortName());

		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setSize("600", "150");
		popup.getTextArea().setText("please wait ...");
		Main.log("Creating ontology ...");
		reenableButton(reviewButton, null, false);
		popup.setText("Creating ontology ...");
		popup.center();
		popup.show();

		AsyncCallback<CreateOntologyResult> callback = new AsyncCallback<CreateOntologyResult>() {
			public void onFailure(Throwable thr) {
				reenableButton(reviewButton, null, true);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(CreateOntologyResult result) {
				reenableButton(reviewButton, null, true);
				reviewCompleted(popup, result);
			}
		};

		createOntologyInfo.setMetadataValues(newValues);
		
		
		
		if ( createOntologyInfo instanceof CreateVocabularyInfo ) {
			CreateVocabularyInfo cvi = (CreateVocabularyInfo) createOntologyInfo;
			Main.ontmdService.createVocabulary(ontologyInfo, cvi, callback);
		}
		else {
			assert false;  // see check above
		}
	}

	
	private void reviewCompleted(final MyDialog popup, final CreateOntologyResult createOntologyResult) {
		String error = createOntologyResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);

		if ( error == null ) {
			vp.add(new Label("Ontology URI: " +createOntologyResult.getUri()));
			
			vp.add(new Label("You can now register your ontology or close this " +
					"dialog to continue editing the contents."));
			
			// prepare uploadButton
			PushButton uploadButton = new PushButton("Upload", new ClickListener() {
				public void onClick(Widget sender) {
					upload(popup, true, createOntologyResult);
				}
			});
			uploadButton.setTitle("Uploads the new version of the ontology");

			popup.getButtonsPanel().insert(uploadButton, 0);
			
//			vp.add(new Label("Contents:"));
			
//			metadataPanel.resetToNewValues(ontologyInfoPre, createOntologyResult, false, false);
			
//			sb.append(createOntologyResult.getRdf());
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		
		
		popup.getTextArea().setText(msg);
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.setText(error == null 
				? "Ontology ready to be registered" 
				: "Error");
		popup.center();

		Main.log("Review result: " +msg);

	}
	
	private void upload(MyDialog popup, boolean confirm, CreateOntologyResult createOntologyResult) {
		if ( confirm && 
			! Window.confirm("This action will commit your ontology into the MMI Registry") ) {
			return;
		}
		doUpload(popup, createOntologyResult);
	}

	private void doUpload(MyDialog createPopup, CreateOntologyResult createOntologyResult) {
		
		createPopup.hide();
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		popup.getTextArea().setSize("600", "150");
		
		Main.log("Registering ontology ...");
		popup.setText("Registering ontology ...");
		popup.center();
		popup.show();


		AsyncCallback<UploadOntologyResult> callback = new AsyncCallback<UploadOntologyResult>() {
			public void onFailure(Throwable thr) {
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(UploadOntologyResult result) {
				uploadCompleted(popup, result);
			}
		};

		Main.ontmdService.uploadOntology(createOntologyResult, PortalControl.getInstance().getLoginResult(), callback);
	}

	private void uploadCompleted(MyDialog uploadPopup, final UploadOntologyResult uploadOntologyResult) {
		
		uploadPopup.hide();
		
		String error = uploadOntologyResult.getError();
		
		StringBuffer sb = new StringBuffer();
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(6);
		
		if ( error == null ) {

			String uri = uploadOntologyResult.getUri();

			vp.add(new HTML("<font color=\"green\">Congratulations!</font> "
					+ "Your ontology is now registered."
			));
			
			
			vp.add(new HTML("<br/>The URI of the ontology is: "
//					+ "<a href=\"" +uri+ "\">"
					+ uri
//					+ "</a>"
			));
			
			
			vp.add(new HTML("<br/>For diagnostics, this is the response from the back-end server:"));

			sb.append(uploadOntologyResult.getInfo());
			
			// and, disable all editing fields/buttons:
			// (user will have to start from the "load" step)
			enable(false);
		}
		else {
			sb.append(error);
		}
		
		String msg = sb.toString();
		Main.log("Uploading result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setText(error == null ? "Upload completed sucessfully" : "Error");
		popup.addTextArea(null).setText(msg);
		popup.getTextArea().setSize("600", "150");
		
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.center();
		
		popup.addPopupListener(new PopupListener() {
			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				PortalControl.getInstance().completedUploadOntologyResult(uploadOntologyResult);
			}
		});
		popup.show();
	}


}
