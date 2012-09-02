package org.mmisw.orrportal.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.ExternalOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.GetAllOntologiesResult;
import org.mmisw.orrclient.gwt.client.rpc.MappingOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;
import org.mmisw.orrportal.gwt.client.Orr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main editor panel. This contains the ontology selection panel and
 * the multi page editor.
 * 
 * @author Carlos Rueda
 */
public class VineEditorPanel extends VerticalPanel {
	
	private MappingOntologyData ontologyData;
	private boolean readOnly;
	private VerticalPanel layout;
	
	private OntologySelection ontSel;
	private MapperPage mapperPage;
	private MappingsPanel mappingsPanel;
	
	private List<Mapping> savedMappings;
	
	
	public VineEditorPanel(MappingOntologyData ontologyData, boolean readOnly) {
		super();
		this.ontologyData = ontologyData;
		this.readOnly = readOnly;
		
		layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    add(decPanel);

	    savedMappings = null;
	    _setUp();
	}
	
	private void _setUp() {
		Orr.log("VineEditorPanel._setUp: readOnly=" +readOnly);
		layout.clear();
		
		final Set<String> namespaces = ontologyData.getNamespaces();
		
		VineMain.setWorkingUrisWithGivenNamespaces(namespaces);
		
		List<Mapping> mappings = ontologyData.getMappings();

		if ( ! readOnly || VineMain.getWorkingUris().size() > 0 ) {
			ontSel = new OntologySelection(this);
			layout.add(ontSel);
			layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
		}
		
		if ( ! readOnly ) {
			_saveMappings(mappings);
		}
		
		List<RelationInfo> relInfos = ontologyData.getRelationInfos();
		
		mappingsPanel = new MappingsPanel(relInfos, readOnly);
		mappingsPanel.setMappings(mappings);
		
		
		if ( ! readOnly ) {
			mapperPage = new MapperPage(relInfos, mappingsPanel);
			layout.add(mapperPage);
		}

	    layout.add(mappingsPanel);

		if ( ! readOnly ) {
			_prepareForEditing();
		}
	}

	private void _prepareForEditing() {
		Orr.log("_prepareForEditing called.");
		
		final MyDialog pePopup = new MyDialog(null) {
			// to prevent Enter from closing the dialog
			public boolean onKeyUpPreview(char key, int modifiers) {
				return true;
			}
		};
		pePopup.addTextArea(null).setSize("700", "150");
		pePopup.setText("Preparing for editing. Please wait ...");
		
		// TODO: handle activity? For now, do not disable any buttons
//		PortalControl.getInstance().notifyActivity(true);

		pePopup.center();
		pePopup.show();
		
	    DeferredCommand.addCommand(new Command() {
	    	public void execute() {
	    		_refreshListAllOntologies(pePopup);
	    	}
	    });
	}
	
	/**
	 * First operation in _prepareForEditing.
	 * Fixes 296: Direct load of mapping ontology does not load mapped ontologies when editing.
	 * Also fixes the associated bug of not seeing any entries in the OntologySelection panel.
	 */
	private void _refreshListAllOntologies(final MyDialog pePopup) {
		AsyncCallback<GetAllOntologiesResult> callback = new AsyncCallback<GetAllOntologiesResult>() {

			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(GetAllOntologiesResult result) {
				if ( result.getError() == null ) {
					List<RegisteredOntologyInfo> ontologyInfos = result.getOntologyList();
					Orr.log("ORR: Got list of registered ontologies: " +ontologyInfos.size());
					pePopup.appendToTextArea("done\n");
					_refreshedListAllOntologies(pePopup, ontologyInfos);
				}
				else {
					Orr.log("Error getting list of ontologies: " +result.getError());
					Window.alert("Error getting list of ontologies. Please try again later." 
							+ "\n\n" +result.getError()
					);
				}
			}
			
		};
		Orr.log("ORR: Getting list of registered ontologies ...");
		pePopup.appendToTextArea("Refreshing list of registered ontologies ... ");
		Orr.service.getAllOntologies(true, callback);
	}
	
	private void _refreshedListAllOntologies(final MyDialog pePopup, List<RegisteredOntologyInfo> ontologyInfos) {
		// update the list of all ontologies
		VineMain.setAllUris(ontologyInfos);
		
	    // and load data of the working ontologies (to properly enable the search)
		pePopup.appendToTextArea("Loading working ontologies:\n");
		_loadDataOfWorkingOntologiesForMapping(pePopup, 0);
	}

	/** saves in memory the given mappings (especially for its metadata), which are used
	 *  later by {@link #_restoreMappings()} in case the editing is canceled.
	 */
	private void _saveMappings(List<Mapping> mappings) {
		if ( mappings == null || mappings.size() == 0 ) {
			Orr.log("VineEditorPanel._saveMappings: unnecessary, no mappings given.");
			return;
		}
		Orr.log("VineEditorPanel._saveMappings: " +mappings.size());
		savedMappings = new ArrayList<Mapping>();
		for ( Mapping mm : mappings ) {
			Mapping savedMapping = new Mapping(mm.getLeft(), mm.getRelation(), mm.getRight());
			Map<String, String> md = mm.getMetadata();
			if ( md != null && md.size() > 0 ) {
				Map<String, String> savedMd = new LinkedHashMap<String, String>();
				for ( Entry<String, String> e : md.entrySet() ) {
					savedMd.put(e.getKey(), e.getValue());
				}
				savedMapping.setMetadata(savedMd);
			}
			savedMappings.add(savedMapping);
		}

	}

	/** restored the saved mappings to the MappingOntologyData object */
	private void _restoreMappings() {
		if ( savedMappings != null ) {
			Orr.log("VineEditorPanel._restoreMappings: restoring saved mappings");
			ontologyData.setMappings(savedMappings);
			savedMappings = null;
		}
		else {
			Orr.log("VineEditorPanel._restoreMappings: NO saved mappings");			
		}
	}
	
	/**
	 * Gets the mappings currently inserted in the mapping table.
	 */
	public List<Mapping> getMappings() {
		return mappingsPanel.getMappings();
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		_setUp();
	}

	/** 
	 * Cancels changes done to the contents, if any.
	 */
	public void cancel() {
		Orr.log("VineEditorPanel.cancel");
		_restoreMappings();
		/* Note:
		 * _restoreMappings used to call _setUp(), but this is not needed and in 
		 * fact it was unnecesarily re-loading the mapped ontologies because readOnly
		 * is usually still false here although we are canceling. But then there
		 * will be a call to setReadOnly(true) when updating the interface.
		 */
	}


	/**
	 * Gets the entities and then notifies the event to dependent components.
	 * @param ontologySelection 
	 * @param ontologyInfo
	 */
	void notifyWorkingOntologyAdded(final OntologySelection ontologySelection, RegisteredOntologyInfo ontologyInfo, final MyDialog popup) {
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(10);
		
		// TODO: why preloaded animated images don't animate? ...
		// (see http://groups.google.com/group/Google-Web-Toolkit-Contributors/browse_thread/thread/c6bc51da338262af)
//		hp.add(Main.images.loading().createImage());
		hp.add(new HTML(
			// ... workaround: insert it with img tag -- which does work, but that's not the idea
			"<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\">" +
			" Loading " +ontologyInfo.getUri()+ 
			" : <i>" +ontologyInfo.getDisplayLabel()+ "</i>" +
			"<br/>Please wait..."
		));
		popup.setWidget(hp);
		popup.setText("Loading vocabulary...");
		
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
				popup.hide();
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				popup.setWidget(new HTML("Load complete"));
				
				Orr.log("getOntologyContents: " +ontologyInfo.getUri()+ " completed.");
				
				if ( ontologyInfo.getOntologyData() == null ) {
					Orr.log("getOntologyContents: unexpected: data not retrieved");
				}
				
				VineMain.ontologySucessfullyLoaded(ontologyInfo);
				
				VineMain.addWorkingUri(ontologyInfo.getUri());
				ontologySelection.refreshListWorkingUris();
				
				if ( mapperPage != null ) {
					mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
				}
				
				popup.hide();
			}
		};
		
		Orr.log("getOntologyContents: " +ontologyInfo.getUri()+ " starting");
		Orr.service.getOntologyContents(ontologyInfo, null, callback);
	}

	
	/**
	 * Gets the entities and then notifies the event to dependent components.
	 * @param ontologySelection 
	 * @param ontologyInfo
	 */
	void notifyWorkingExternalOntologyAdded(final OntologySelection ontologySelection, BaseOntologyInfo ontologyInfo) {
		VineMain.ontologySucessfullyLoaded(ontologyInfo);
		VineMain.addWorkingUri(ontologyInfo.getUri());
		ontologySelection.refreshListWorkingUris();
		if ( mapperPage != null ) {
			mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
		}
	}
	
	private void _loadExternalMappedOntologies(final MyDialog pePopup) {
    	/*
    	 * if there are any namespaces with no OntologyInfo, then
    	 * try loading them as external ontologies
    	 */
		final Set<String> namespaces = ontologyData.getNamespaces();
    	final List<String> urisToLoadAsExternal = new ArrayList<String>();
    	for ( String namespace : namespaces ) {
    		if ( null == VineMain.getOntologyInfo(namespace)) {
    			urisToLoadAsExternal.add(namespace);
    		}
    	}
    	Orr.log("_loadExternalMappedOntologies: " + urisToLoadAsExternal);
    	if (urisToLoadAsExternal.size() > 0) {
    		pePopup.appendToTextArea("Loading external ontologies:\n");
    	}
    	_loadExternalOntologiesForMapping(pePopup, urisToLoadAsExternal);
	}
	
	/**
	 * Loads the data for the working ontologies that do not have data yet.
	 * This is a recursive routine used to traverse the list of working
	 * ontologies with a RPC call for each entry needing the retrieval of data.
	 * 
	 * At the end, it call _loadExternalMappedOntologies().
	 * 
	 * @param currentIdx the current index to examine.
	 */
	private void _loadDataOfWorkingOntologiesForMapping(final MyDialog pePopup, final int currentIdx) {
		List<String> uris = VineMain.getWorkingUris();
		if ( uris.size() == 0 || currentIdx >= uris.size() ) {
			// Done.
			// load any external mapped ontologies
			_loadExternalMappedOntologies(pePopup);
			return;
		}
	
		final String log_prefix = "_loadDataOfWorkingOntologiesForMapping(" +currentIdx+ "): ";
		
		String uri = uris.get(currentIdx);
		BaseOntologyInfo ontologyInfo = VineMain.getOntologyInfo(uri);
		
		if ( ontologyInfo == null ) {
			// Not a registered ontology; continue to next entry:
			Orr.log(log_prefix +"VineMain.getOntologyInfo returned null: " +uri);
			_loadDataOfWorkingOntologiesForMapping(pePopup, currentIdx + 1);
			return;
		}

		if ( ontologyInfo.getError() != null ) {
			// continue to next entry:
			_loadDataOfWorkingOntologiesForMapping(pePopup, currentIdx + 1);
			return;
		}

		OntologyData ontologyData = ontologyInfo.getOntologyData();
		if ( ontologyData != null ) {
			// this entry already has data; continue to next entry:
			Orr.log(log_prefix +"already with OntologyData: " +uri);
			_loadDataOfWorkingOntologiesForMapping(pePopup, currentIdx + 1);
			return;
		}
		
		// this entry needs data.
		
		if (! (ontologyInfo instanceof RegisteredOntologyInfo)) {
			Orr.log(log_prefix +"Ontology from VineMain is not a RegisteredOntologyInfo: " +uri);
			_loadDataOfWorkingOntologiesForMapping(pePopup, currentIdx + 1);
			return;
		}
		
		RegisteredOntologyInfo registeredOntologyInfo = (RegisteredOntologyInfo) ontologyInfo;
		
		AsyncCallback<RegisteredOntologyInfo> callback = new AsyncCallback<RegisteredOntologyInfo>() {

			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Orr.log(log_prefix + " ERROR: " +error);
				Window.alert(error);
			}

			public void onSuccess(RegisteredOntologyInfo ontologyInfo) {
				Orr.log(log_prefix +ontologyInfo.getUri()+ " completed.");
				pePopup.appendToTextArea("done\n");
				
				if ( ontologyInfo.getOntologyData() == null ) {
					Orr.log("  UNEXPECTED: data not retrieved");
				}
				
				VineMain.ontologySucessfullyLoaded(ontologyInfo);
				VineMain.addWorkingUri(ontologyInfo.getUri());
				
				if ( mapperPage != null ) {
					mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
				}
				
				// continue with next entry:
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						_loadDataOfWorkingOntologiesForMapping(pePopup, currentIdx + 1);
					}
				});

			}
			
		};
		String ontologyUri = ontologyInfo.getUri();
		pePopup.appendToTextArea("   Loading : " + ontologyUri + " ... ");
		Orr.log(log_prefix +ontologyUri+ " starting");
		Orr.service.getOntologyContents(registeredOntologyInfo, null, callback);
	}

	/**
	 * Adds an external ontology to the working group.
	 */
	void addExternalOntology(final String ontologyUri, final MyDialog popup) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(10);
		
		hp.add(new HTML(
			"<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\">" +
			" Loading " +ontologyUri+ 
			"<br/>Please wait..."
		));
		popup.setWidget(hp);
		popup.setText("Loading ontology...");
		
		AsyncCallback<ExternalOntologyInfo> callback = new AsyncCallback<ExternalOntologyInfo>() {
			public void onFailure(Throwable thr) {
				popup.hide();
				Orr.log("calling getExternalOntologyInfo ... failure! ");
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(ExternalOntologyInfo ontologyInfo) {
				String error = ontologyInfo.getError();
				if (error != null) {
					Orr.log("calling getExternalOntologyInfo ... error=" + error);
					popup.setWidget(new Label(error));
					Window.alert("Error while trying to load '" +ontologyUri+ "':\n\n" + error);
				}
				else {
					Orr.log("calling getExternalOntologyInfo ... success");
					popup.setWidget(new HTML("Load complete"));
					notifyWorkingExternalOntologyAdded(ontSel, ontologyInfo);
				}
				popup.hide();
			}
		};

		Orr.log("calling getExternalOntologyInfo: " + ontologyUri);
		Orr.service.getExternalOntologyInfo(ontologyUri, callback);
	}

	/**
	 * Recursive routine to load the given external ontologies.
	 */
	private void _loadExternalOntologiesForMapping(final MyDialog pePopup, final List<String> urisToLoadAsExternal) {
		if (urisToLoadAsExternal.isEmpty()) {
			// Done.
			pePopup.appendToTextArea("\nReady.");
			// refresh the OntologySelection:
			if ( ontSel != null ) {
				ontSel.refreshListWorkingUris();
			}
			pePopup.hide();
			return;
		}
		
		String ontologyUri = urisToLoadAsExternal.remove(0);
		AsyncCallback<ExternalOntologyInfo> callback = new AsyncCallback<ExternalOntologyInfo>() {
			public void onFailure(Throwable thr) {
				Orr.log("calling getExternalOntologyInfo ... failure! ");
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(ExternalOntologyInfo ontologyInfo) {
				Orr.log("calling getExternalOntologyInfo ... success");
				pePopup.appendToTextArea("done\n");
				notifyWorkingExternalOntologyAdded(ontSel, ontologyInfo);
				_loadExternalOntologiesForMapping(pePopup, urisToLoadAsExternal);
			}
		};
		Orr.log("calling getExternalOntologyInfo: " + ontologyUri);
		pePopup.appendToTextArea("   Loading : " + ontologyUri + " ... ");
		Orr.service.getExternalOntologyInfo(ontologyUri, callback);
	}

	
}
