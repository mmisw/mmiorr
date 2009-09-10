package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashMap;
import java.util.Map;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.DataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.iserver.gwt.client.rpc.OtherOntologyData;
import org.mmisw.iserver.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.ontmd.gwt.client.DataPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.metadata.MetadataPanel;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;
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
	
	
	private static final boolean USE_ONTOLOGY_URI_PANEL = false; 
	private OntologyUriPanel ontologyUriPanel = USE_ONTOLOGY_URI_PANEL? new OntologyUriPanel() : null;
	

	// re-created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	// Handles the data either editing or viewing
	private DataPanel dataPanel;
	
	// created for editing data mode
//	private EditDataPanel editDataPanel;
	
	
	/** Ontology to be dispatched */
	private RegisteredOntologyInfo ontologyInfo;
	
	/** dispatch with explicit version? */
	private final boolean versionExplicit;
	
	
	public MetadataPanel getMetadataPanel() { 
		return metadataPanel; 
	}

	// IOntologyPanel operation
	public OntologyMetadata getOntologyMetadata() {
		return ontologyInfo.getOntologyMetadata();
	}

	// IOntologyPanel operation
	public void formChanged(Map<String, String> values) {
		if ( USE_ONTOLOGY_URI_PANEL ) {
			// TODO use params or config for these values
			String authority = values.get("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origMaintainerCode");
			String shortName = values.get("http://omv.ontoware.org/2005/05/ontology#acronym");
			ontologyUriPanel.update(authority, shortName);
		}
	}
	

	/**
	 * Prepares the overall interface for the given ontology.
	 * 
	 * @param ontologyInfo
	 * @param readOly the initial mode
	 */
	public OntologyPanel(RegisteredOntologyInfo ontologyInfo, boolean readOnly, boolean versionExplicit) {
		super();
		setWidth("100%");
		container.setWidth("100%");
		
		this.ontologyInfo = ontologyInfo;
		this.versionExplicit = versionExplicit;
		
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
	    
	    if ( this.ontologyInfo.getUri() != null ) {
	    	getOntologyContents(readOnly);
	    }
	    // if Uri is null, then this is a new ontology being created in the interface.
	}
	
	
	/**
	 * @return the versionExplicit
	 */
	public boolean isVersionExplicit() {
		return versionExplicit;
	}

	/**
	 * @return the ontologyInfo
	 */
	public RegisteredOntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}
	
	
	private void createNewBase() {
		
		ontologyInfo.setDisplayLabel("(creating new ontology)");
		ontologyInfo.setUri("");
		headerPanel.resetElements(false, true);
		headerPanel.updateTitle("<b>" +ontologyInfo.getDisplayLabel()+ "</b> - "+ontologyInfo.getUri()+ "<br/>");
		metadataPanel = new MetadataPanel(this, true);
		mdDisclosure.setContent(metadataPanel);
	}

	
	/**
	 * Prepares the panel for creation of an ontology using the voc2rdf style.
	 */
	void createNewVocabulary() {
		createNewBase();
		
		// create (empty) data for the ontologyInfo
		VocabularyOntologyData vocababularyOntologyData = new VocabularyOntologyData();
		BaseOntologyData baseOntologyData = null;
		vocababularyOntologyData.setBaseOntologyData(baseOntologyData);
		vocababularyOntologyData.setClasses(null);
		ontologyInfo.setOntologyData(vocababularyOntologyData);

		// create dataPanel
		dataPanel = new DataPanel(false);
		dataPanel.updateWith(ontologyInfo, false);
		dataDisclosure.setContent(dataPanel);
		
		enable(true);
	}

	/**
	 * Prepares the panel for creation of an ontology using the vine style.
	 */
	void createNewMappingOntology() {
		createNewBase();
		
		// create (empty) data for the ontologyInfo
		MappingOntologyData mappingOntologyData = new MappingOntologyData();
		BaseOntologyData baseOntologyData = null;
		mappingOntologyData.setBaseOntologyData(baseOntologyData);
		mappingOntologyData.setMappings(null);
		ontologyInfo.setOntologyData(mappingOntologyData);

		// create dataPanel
		dataPanel = new DataPanel(false);
		dataPanel.updateWith(ontologyInfo, false);
		dataDisclosure.setContent(dataPanel);
		
		enable(true);
	}


	/**
	 * Prepares the panel for creation of an ontology from a local file to
	 * be uploaded.
	 */
	void createNewFromFile() {
		createNewBase();
		
		// create (empty) data for the ontologyInfo
		OtherOntologyData otherOntologyData = new OtherOntologyData();
		BaseOntologyData baseOntologyData = null;
		otherOntologyData.setBaseOntologyData(baseOntologyData);
		ontologyInfo.setOntologyData(otherOntologyData);

		// create dataPanel
		dataPanel = new DataPanel(false);
		
		dataPanel.updateWith(ontologyInfo, false);
		
		dataDisclosure.setContent(dataPanel);
		
		enable(true);
	}


	
	


	void updateInterface(InterfaceType interfaceType) {
		
		assert interfaceType != InterfaceType.ONTOLOGY_EDIT_NEW ;
		
		boolean readOnly = interfaceType == InterfaceType.BROWSE || interfaceType == InterfaceType.ONTOLOGY_VIEW;
		
		headerPanel.resetElements(readOnly, interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW);
		metadataPanel = new MetadataPanel(this, !readOnly);
		mdDisclosure.setContent(metadataPanel);

		boolean link = true;
		metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
	
		Main.log("OntologyPanel updateInterface, readOnly=" +readOnly);
		
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
		
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				Main.log("RET getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
				String error = ontologyInfo.getError();
				if ( error != null ) {
					Main.log("RET getOntologyContents: error = " +error);
				}
				
				ontologyContentsRetrieved(ontologyInfo, readOnly);
			}
		};

		headerPanel.updateTitle("<b>" +ontologyInfo.getDisplayLabel()+ "</b>" +
				" - "+ontologyInfo.getUri()+ 
				"  (version "+ontologyInfo.getVersionNumber()+ ")" + 
				"<br/>"
		);
		headerPanel.showProgressMessage("Loading contents. Please wait...");

		metadataPanel.showProgressMessage("Loading contents. Please wait...");
		Main.log("getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
		Main.ontmdService.getOntologyContents(ontologyInfo, null, callback);
	}


	private void ontologyContentsRetrieved(RegisteredOntologyInfo ontologyInfo, boolean readOnly) {
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
			
			if ( USE_ONTOLOGY_URI_PANEL ) {
			if ( ! readOnly ) {
				ontologyUriPanel.update();
			}
			}
			
			resetElements(readOnly, false);
		}
		
		void resetElements(boolean readOnly, boolean newOntology) {
			widget.clear();
			widget.add(titleHtml);
			widget.add(descriptionHtml);
			
			if ( USE_ONTOLOGY_URI_PANEL ) {
			if ( ! readOnly && newOntology ) {
				widget.add(ontologyUriPanel);
			}
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


	void reviewAndRegister() {
		if ( ontologyInfo == null ) {
			Window.alert("Please, load an ontology first");
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			Window.alert("Please, retry the loading of the ontology");
			return;
		}
		
		// check metadata values:
		Map<String, String> newValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(newValues, true);
		if ( error != null ) {
			mdDisclosure.setOpen(true);
			Window.alert(error);
			return;
		}
		
		boolean isNewVersion = ontologyInfo != null && ontologyInfo.getOntologyId() != null;
		
		// check data values
		error = dataPanel.checkData(isNewVersion);
		if ( error != null ) {
			dataDisclosure.setOpen(true);
			Window.alert(error);
			return;
		}
		
		DataCreationInfo dataCreationInfo = dataPanel.getCreateOntologyInfo();
		

		//
		// refactoring
		// TODO clean-up the replication of info in the ontologyInfo and createOntologyInfo.
		//
		
		// Ok, put the new values in the ontologyInfo object:
		ontologyInfo.getOntologyMetadata().setNewValues(newValues);
//		for ( String uri : newValues.keySet() ) {        // TODO remove unnecesary code (surely remanents of prior code changes)
//			String value = newValues.get(uri);
//			newValues.put(uri, value);
//		}
		
		// prepare info for creation of the ontology:
		CreateOntologyInfo createOntologyInfo = new CreateOntologyInfo();
		
		// transfer info about prior ontology, if any, for eventual creation of new version:
		createOntologyInfo.setPriorOntologyInfo(
				ontologyInfo.getOntologyId(), 
				ontologyInfo.getOntologyUserId(), 
				ontologyInfo.getVersionNumber()
		);
		
		createOntologyInfo.setUri(ontologyInfo.getUri());
		createOntologyInfo.setAuthority(ontologyInfo.getAuthority());
		createOntologyInfo.setShortName(ontologyInfo.getShortName());
		
		createOntologyInfo.setMetadataValues(newValues);
		

		String authority = createOntologyInfo.getAuthority();
		String shortName = createOntologyInfo.getShortName();
		
		if ( authority == null || authority.trim().length() == 0 ) {
			// TODO improve this code:
			authority = newValues.get("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/origMaintainerCode");
			if ( authority == null || authority.trim().length() == 0 ) {
				authority = "mmitest";  // TODO
			}
			createOntologyInfo.setAuthority(authority);
		}
		
		if ( shortName == null || shortName.trim().length() == 0 ) {
			// TODO improve this code:
			shortName = newValues.get("http://omv.ontoware.org/2005/05/ontology#acronym");
			if ( shortName == null || shortName.trim().length() == 0 ) {
				shortName = "shorttest";  // TODO
			}
			createOntologyInfo.setShortName(shortName);
		}
		
		createOntologyInfo.setDataCreationInfo(dataCreationInfo);
		
//		if ( dataCreationInfo instanceof VocabularyDataCreationInfo
//		||   dataCreationInfo instanceof OtherDataCreationInfo
//		) {
//			// OK: continue
//		}
//		else {
//			Window.alert("sorry, not implemented yet");
//			return;
//		}
		
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setSize("600", "150");
		popup.getTextArea().setText("please wait ...");
		PortalControl.getInstance().notifyActivity(true);
		popup.setText("Creating ontology ...");
		popup.center();
		popup.show();

		AsyncCallback<CreateOntologyResult> callback = new AsyncCallback<CreateOntologyResult>() {
			public void onFailure(Throwable thr) {
				PortalControl.getInstance().notifyActivity(false);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(CreateOntologyResult result) {
				Main.log("CreateOntologyResult obtained.");
				PortalControl.getInstance().notifyActivity(false);
				reviewCompleted(popup, result);
			}
		};

		Main.log("Calling ontmdService.createOntology ...");
		Main.ontmdService.createOntology(createOntologyInfo, callback);
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
			PushButton registerButton = new PushButton("Register", new ClickListener() {
				public void onClick(Widget sender) {
					register(popup, true, createOntologyResult);
				}
			});
			registerButton.setTitle("Registers the new version of the ontology");

			popup.getButtonsPanel().insert(registerButton, 0);
			
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
	
	private void register(MyDialog popup, boolean confirm, CreateOntologyResult createOntologyResult) {
		if ( confirm && 
			! Window.confirm("This action will commit your ontology into the MMI Registry") ) {
			return;
		}
		doRegister(popup, createOntologyResult);
	}

	private void doRegister(MyDialog createPopup, CreateOntologyResult createOntologyResult) {
		
		createPopup.hide();
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		popup.getTextArea().setSize("600", "150");
		
		Main.log("Registering ontology ...");
		popup.setText("Registering ontology ...");
		popup.center();
		popup.show();


		AsyncCallback<RegisterOntologyResult> callback = new AsyncCallback<RegisterOntologyResult>() {
			public void onFailure(Throwable thr) {
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(RegisterOntologyResult result) {
				registrationCompleted(popup, result);
			}
		};

		LoginResult loginResult = PortalControl.getInstance().getLoginResult();
		Main.ontmdService.registerOntology(createOntologyResult, loginResult , callback);
	}

	private void registrationCompleted(MyDialog registrationPopup, final RegisterOntologyResult uploadOntologyResult) {
		
		registrationPopup.hide();
		
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
		Main.log("Registration result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setText(error == null ? "Registration completed sucessfully" : "Error");
		popup.addTextArea(null).setText(msg);
		popup.getTextArea().setSize("600", "150");
		
		popup.getDockPanel().add(vp, DockPanel.NORTH);
		popup.center();
		
		popup.addPopupListener(new PopupListener() {
			public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
				PortalControl.getInstance().completedRegisterOntologyResult(uploadOntologyResult);
			}
		});
		popup.show();
	}


}
