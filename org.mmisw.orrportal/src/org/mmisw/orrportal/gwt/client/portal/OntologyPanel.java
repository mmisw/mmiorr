package org.mmisw.orrportal.gwt.client.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.DataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.MappingOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyMetadata;
import org.mmisw.orrclient.gwt.client.rpc.RegisterOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrportal.gwt.client.DataPanel;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.metadata.MetadataPanel;
import org.mmisw.orrportal.gwt.client.portal.PortalMainPanel.InterfaceType;
import org.mmisw.orrportal.gwt.client.util.MyDialog;
import org.mmisw.orrportal.gwt.client.vine.VineMain;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
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
	
	private static final String CLASS_NAME = OntologyPanel.class.getName();
	
	private static void log(String msg) {
		Orr.log(CLASS_NAME+": " +msg);
	}

    private static final String progressMsg(String msg) {
        return "<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"><i>" + msg + ". Please wait...</i>";
    }

	private final HTML DATA_PROGRESS_HTML = new HTML(progressMsg("Retrieving ontology data"));

	private CellPanel container = new VerticalPanel();

	
	private HeaderPanel headerPanel;


	private final DisclosurePanel mdDisclosure = new DisclosurePanel("Metadata details");
	private final DisclosurePanel dataDisclosure = new DisclosurePanel("Contents");
	
	
	private static final boolean USE_ONTOLOGY_URI_PANEL = false; 
	private OntologyUriPanel ontologyUriPanel = USE_ONTOLOGY_URI_PANEL? new OntologyUriPanel() : null;
	
	// true for view interface; false for editing interface
	private boolean readOnly;

	// re-created depending on type of interface: editing or viewing
	private MetadataPanel metadataPanel;
	
	// Handles the data either editing or viewing
	private DataPanel dataPanel;
	
	// created for editing data mode
//	private EditDataPanel editDataPanel;
	
	
	/** Ontology to be dispatched */
	private BaseOntologyInfo ontologyInfo;
	
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
	 * @param readOnly the initial mode
	 */
	public OntologyPanel(BaseOntologyInfo ontologyInfo, boolean readOnly, boolean versionExplicit) {
		super();
		this.readOnly = readOnly;
		setWidth("100%");
		container.setWidth("100%");
		
		this.ontologyInfo = ontologyInfo;
		this.versionExplicit = versionExplicit;
		
		add(container);
		
		_prepareMdDisclosure();
		_prepareDataDisclosure();
		
		metadataPanel = new MetadataPanel(this, !readOnly);
		mdDisclosure.setContent(metadataPanel);

		dataPanel = new DataPanel(readOnly);
		
		CellPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		
		headerPanel = new HeaderPanel();
		
		panel.add(headerPanel.getWidget());
		panel.add(mdDisclosure);
		panel.add(dataDisclosure);
		
		enable(!readOnly);
		
	    container.add(panel);
	    
	    if ( this.ontologyInfo instanceof RegisteredOntologyInfo && this.ontologyInfo.getUri() != null ) {
	    	_getOntologyMetadata();
	    }
	    // if Uri is null, then this is a new ontology being created in the interface.
	}
	
	private void _prepareMdDisclosure() {
		mdDisclosure.getHeader().setStyleName("ont-DisclosurePanel-header");
		mdDisclosure.setAnimationEnabled(true);
		mdDisclosure.getHeader().setTitle("This section shows the metadata associated to the ontology.");
	}
	
	private void _prepareDataDisclosure() {
		log("_prepareDataDisclosure: ontologyUri = '" +ontologyInfo.getUri()+ 
				"' class=" +ontologyInfo.getClass().getName());
		dataDisclosure.getHeader().setStyleName("ont-DisclosurePanel-header");
		dataDisclosure.getHeader().setTitle("This section shows either the full contents or a synopsis of the ontology.");
		dataDisclosure.setContent(DATA_PROGRESS_HTML);
        /*
         * Ontology contents are retrieved when this disclosure is open, either by the
         * user or programmatically (see  _ontologyMetadataRetrieved).
         */
		dataDisclosure.addEventHandler(new DisclosureHandler() {
			public void onOpen(DisclosureEvent event) {
				if ( dataDisclosure.getContent() == DATA_PROGRESS_HTML ) {
                    log("dataDisclosure onOpen: calling _getOntologyContents(null)");
					_getOntologyContents(null);
				}
			}
			
			public void onClose(DisclosureEvent event) {
				// ok; nothing to do.
			}
		});
		
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
	public BaseOntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}
	
	
	private void createNewBase() {
		readOnly = false;
		ontologyInfo.setDisplayLabel("(creating new ontology)");
		ontologyInfo.setUri("");
		headerPanel.resetElements(true);
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
		vocababularyOntologyData.setBaseOntologyData(null);
		vocababularyOntologyData.setClasses(null);
		ontologyInfo.setOntologyData(vocababularyOntologyData);

		// create dataPanel
		dataPanel = new DataPanel(false);
		dataPanel.updateWith(null, ontologyInfo, false);
		dataDisclosure.setContent(dataPanel);
		
		enable(true);
	}

	/**
	 * Prepares the panel for creation of a new ontology using the vine style.
	 */
	void createNewMappingOntology() {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				_getDefaultVineRelationInfosAndCreateNewMappingOntology();
			}
		});
	}
	
	private void _getDefaultVineRelationInfosAndCreateNewMappingOntology() {
		VineMain.getDefaultVineRelationInfos(new AsyncCallback<List<RelationInfo>>() {
			public void onFailure(Throwable caught) {
				// ignored here -- should have been dispatched by VineMain.
			}

			public void onSuccess(List<RelationInfo> relInfos) {
				// now, do actually launch the creation of the new mapping ontology
				_doCreateNewMappingOntology(relInfos);
			}
		});
	}

	/**
	 * Prepares the panel for creation of an ontology using the vine style
	 * and the given RelationInfos.
	 */
	private void _doCreateNewMappingOntology(List<RelationInfo> relInfos) {
		createNewBase();
		
		// create data with only the given RelationInfos for the ontologyInfo
		MappingOntologyData mappingOntologyData = new MappingOntologyData();
		mappingOntologyData.setRelationInfos(relInfos);
		mappingOntologyData.setBaseOntologyData(null);
		mappingOntologyData.setMappings(null);
		ontologyInfo.setOntologyData(mappingOntologyData);

		// create dataPanel
		dataPanel = new DataPanel(false);
		dataPanel.updateWith(null, ontologyInfo, false);
		dataDisclosure.setContent(dataPanel);
		
		enable(true);
	}
	
	
	
	

	void updateInterface(InterfaceType interfaceType) {
		
		assert interfaceType != InterfaceType.ONTOLOGY_EDIT_NEW ;
		
		readOnly = interfaceType == InterfaceType.BROWSE || interfaceType == InterfaceType.ONTOLOGY_VIEW;
		
		headerPanel.resetElements(interfaceType == InterfaceType.ONTOLOGY_EDIT_NEW);
		metadataPanel = new MetadataPanel(this, !readOnly);
		mdDisclosure.setContent(metadataPanel);

		boolean link = true;
		metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
	
		log("OntologyPanel updateInterface, readOnly=" +readOnly);
		
		if ( readOnly ) {
			// coming from edit mode to view only mode-- reload data
			dataPanel.updateWith(null, ontologyInfo, readOnly);
		}
		else {
			// coming from view to edit--just update elements for editing (do not reload contents)
			dataPanel.setReadOnly(readOnly);
		}
		
		enable(!readOnly);
	}

	/**
	 * 267: Lazy loading of ontology contents.
	 * First retrieve only the metadata.
	 */
	private void _getOntologyMetadata() {
		
		assert ontologyInfo instanceof RegisteredOntologyInfo ;
		RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
		
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				log("RET getOntologyMetadata: ontologyUri = '" +ontologyInfo.getUri()+ "'");
				String error = ontologyInfo.getError();
				if ( error != null ) {
					log("RET getOntologyMetadata: error = " +error);
				}
				
				_ontologyMetadataRetrieved(ontologyInfo);
			}
		};

		String title = "<b>" +roi.getDisplayLabel()+ "</b> - " +roi.getUri()
					+ "  (version "+roi.getVersionNumber()+ ")" + "<br/>";

		headerPanel.updateTitle(title);
		String progressMsg = "Retrieving ontology metadata. Please wait...";
		headerPanel.showProgressMessage(progressMsg);

		metadataPanel.showProgressMessage(progressMsg);
		log("getOntologyMetadata: ontologyUri = '" +ontologyInfo.getUri()+ "'");
		Orr.service.getOntologyMetadata(roi, null, callback);
	}

	private void _ontologyMetadataRetrieved(final BaseOntologyInfo ontologyInfo) {
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
			mdDisclosure.setOpen(true);
			
			/*
			 * 308: immediately show vocabulary contents
			 * If the ontology is not "too" big, just call dataDisclosure.setOpen(true)
			 * to trigger _getOntologyContents.
			 * (Note: a previous version *also* called getOntologyContents here resulting in a misbehavior:
			 * new terms were not captured for saving of new version of ontology.)
			 */
			long ontSize = ontologyInfo.getSize();
			log("_ontologyMetadataRetrieved: ontologyInfo.getSize=" + ontSize);
			if ( ontSize <= PortalConsts.MAX_ONTOLOGY_SIZE_SHOW_DATA
			     && dataDisclosure.getContent() == DATA_PROGRESS_HTML
			) {
                log("_ontologyMetadataRetrieved: calling dataDisclosure.setOpen(true) " +
                        "to trigger _getOntologyContents");
				dataDisclosure.setOpen(true);
			}
		}
	}
	
	
	
	
	private void _getOntologyContents(final Command postCmd) {
		
		assert ontologyInfo instanceof RegisteredOntologyInfo ;
		RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
		
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				log("RET getOntologyContents: ontologyUri = '" +ontologyInfo.getUri()+ "'");
				String error = ontologyInfo.getError();
				if ( error != null ) {
					log("RET getOntologyContents: error = " +error);
				}
				
				ontologyContentsRetrieved(ontologyInfo, postCmd);
			}
		};

		String title = "<b>" +roi.getDisplayLabel()+ "</b> - " +roi.getUri()
					+ "  (version "+roi.getVersionNumber()+ ")" + "<br/>";
		;
		headerPanel.updateTitle(title);
		log("getOntologyContents: ontologyUri = '" +ontologyInfo.getUri()+ "'");
		Orr.service.getOntologyContents(roi, null, callback);
	}


	private void ontologyContentsRetrieved(BaseOntologyInfo ontologyInfo, final Command postCmd) {
		this.ontologyInfo = ontologyInfo;
		String error = ontologyInfo.getError();
		if ( error != null ) {
			String errorMsg = "<font color=\"red\">" +error+ "</font>";
			headerPanel.updateDescription(errorMsg);
			HTML errorHtml = new HTML(errorMsg);
			dataDisclosure.clear();
			dataDisclosure.setContent(errorHtml);
			dataDisclosure.setOpen(true);
		}
		else {
			// TODO put more info in the description
			headerPanel.updateDescription("");
			
			boolean link = true;
			metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
			
			dataDisclosure.clear();
			
			if ( dataPanel != null ) {
				log("ontologyContentsRetrieved: updating dataPanel");
				dataPanel.updateWith(null, ontologyInfo, readOnly);
				dataDisclosure.setContent(dataPanel);
				dataDisclosure.setOpen(true);
			}
			else {
				log("ontologyContentsRetrieved: no dataPanel to update");
			}
//			else if ( editDataPanel != null ) {
//				editDataPanel.updateWith(ontologyInfo);
//			}
			
			if (postCmd != null) {
				// A direct deferred command (ie. using DeferredCommand.add(postCmd))
				// doesn't work, why?.  The effect is that postCmd won't see the 
				// updated dataPanel! With a Timer all works fine.
				new Timer() {
					public void run() {
						log("ontologyContentsRetrieved: executing post-command");
						postCmd.execute();
					}
				}.schedule(500);
			}
		}
	}
	
	private void enable(boolean enabled) {
		readOnly = !enabled;
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
		
		
		HeaderPanel() {
			widget.setWidth("100%");
			widget.setSpacing(5);
			widget.setVerticalAlignment(ALIGN_MIDDLE);
//			widget.setBorderWidth(1);
			
			if ( USE_ONTOLOGY_URI_PANEL ) {
				if ( ! readOnly ) {
					ontologyUriPanel.update();
				}
			}
			
			resetElements(false);
		}
		
		void resetElements(boolean newOntology) {
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

	/**
	 * Cancels changes done to the metadata and data contents, if any.
	 */
	public void cancel() {
		log("OntologyPanel.cancel");
		metadataPanel.cancel();
		dataPanel.cancel();
	}

	
	void reviewAndRegister() {
		if ( dataDisclosure.getContent() == DATA_PROGRESS_HTML ) {
			/*
			 * 297: error: "No data creation info provided! (please report this bug)"
			 * The issue was that the contents were not yet loaded at this point.
			 * Just load contents first, and then do the actual review-and-register 
			 * in a post-command:
			 */
			log("reviewAndRegister: loading contents first ...");
			final MyDialog popup = new MyDialog(null);
			popup.addTextArea(null).setSize("600", "150");
			popup.getTextArea().setText("please wait ...");
			PortalControl.getInstance().notifyActivity(true);
			popup.center();
			popup.setText("Loading ontology contents ...");
			popup.show();
			
			_getOntologyContents(new Command() {
				public void execute() {
					log("reviewAndRegister: calling _doReviewAndRegister");
					_doReviewAndRegister(popup);
				}
			});
		}
		else {
			_doReviewAndRegister(null);
		}
	}

	private void _doReviewAndRegister(final MyDialog aPopup) {
		log("_doReviewAndRegister called.");
		if ( ontologyInfo == null ) {
			String alert = "Please, load an ontology first";
			log("_doReviewAndRegister: " + alert);
			Window.alert(alert);
			return;
		}
		if ( ontologyInfo.getError() != null ) {
			String alert = "Please, retry the loading of the ontology";
			log("_doReviewAndRegister: " + alert);
			Window.alert(alert);
			return;
		}
		
		// check metadata values:
		Map<String, String> newValues = new HashMap<String, String>();
		String error = metadataPanel.putValues(newValues, true);
		if ( error != null ) {
			log("_doReviewAndRegister: " + error);
			mdDisclosure.setOpen(true);
			Window.alert(error);
			return;
		}
		
		boolean isNewVersion = ontologyInfo != null
			&& ontologyInfo instanceof RegisteredOntologyInfo
			&& ((RegisteredOntologyInfo) ontologyInfo).getOntologyId() != null;
		
		// check data values
		error = dataPanel.checkData(isNewVersion);
		if ( error != null ) {
			log("_doReviewAndRegister: " + error);
			dataDisclosure.setOpen(true);
			Window.alert(error);
			return;
		}
		
		DataCreationInfo dataCreationInfo = dataPanel.getCreateOntologyInfo();
		
		log("_doReviewAndRegister: dataCreationInfo=" + dataCreationInfo);
		

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
		if ( ontologyInfo instanceof RegisteredOntologyInfo ) {
			RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
			createOntologyInfo.setPriorOntologyInfo(
					roi.getOntologyId(), 
					roi.getOntologyUserId(), 
					roi.getVersionNumber()
			);
		}
		
		createOntologyInfo.setUri(ontologyInfo.getUri());
		
		if ( ontologyInfo instanceof RegisteredOntologyInfo ) {
			RegisteredOntologyInfo roi = (RegisteredOntologyInfo) ontologyInfo;
			createOntologyInfo.setAuthority(roi.getAuthority());
			createOntologyInfo.setShortName(roi.getShortName());
		}
		
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
		
		
		final MyDialog popup = aPopup == null ? new MyDialog(null): aPopup;
		if (aPopup == null) {
			popup.addTextArea(null).setSize("600", "150");
			popup.getTextArea().setText("please wait ...");
			PortalControl.getInstance().notifyActivity(true);
			popup.center();
			popup.show();
		}
		popup.setText("Creating ontology ...");

		AsyncCallback<CreateOntologyResult> callback = new AsyncCallback<CreateOntologyResult>() {
			public void onFailure(Throwable thr) {
				PortalControl.getInstance().notifyActivity(false);
				container.clear();				
				container.add(new HTML(thr.toString()));
			}

			public void onSuccess(CreateOntologyResult result) {
				log("CreateOntologyResult obtained." + result.getCreateOntologyInfo().getHostingType());
				PortalControl.getInstance().notifyActivity(false);
				reviewCompleted(popup, result);
			}
		};

		log("Calling service createOntology, hostingType=" +createOntologyInfo.getHostingType());
		Orr.service.createOntology(createOntologyInfo, callback);
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
					doRegister(popup, createOntologyResult);
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

		log("Review result: " +msg);

	}
	
	private void doRegister(MyDialog createPopup, CreateOntologyResult createOntologyResult) {
		
		createPopup.hide();
		
		final MyDialog popup = new MyDialog(null);
		popup.addTextArea(null).setText("please wait ...");
		popup.getTextArea().setSize("600", "150");
		
		log("Registering ontology ...");
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
		Orr.service.registerOntology(createOntologyResult, loginResult , callback);
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
		log("Registration result: " +msg);

		final MyDialog popup = new MyDialog(null);
		popup.setCloseButtonText("Return to ontology list");
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
