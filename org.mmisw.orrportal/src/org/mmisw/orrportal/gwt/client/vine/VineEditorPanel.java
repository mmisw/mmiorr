package org.mmisw.orrportal.gwt.client.vine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;
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
		Orr.log("VineEditorPanel: _setUp");
		layout.clear();
		
		Set<String> namespaces = ontologyData.getNamespaces();
		
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

	    // prepare command to load data of working ontologies
	    // (in particular, this will enable the search)
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				_loadDataOfWorkingOntologiesForMapping(0);
			}
		});

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
			_setUp();
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
	
	
	/**
	 * Loads the data for the working ontologies that do not have data yet.
	 * This is a recursive routine used to traverse the list of working
	 * ontologies with a RPC call for each entry needing the retrieval of data.
	 * 
	 * @param currentIdx the current index to examine.
	 */
	private void _loadDataOfWorkingOntologiesForMapping(final int currentIdx) {
		List<String> uris = VineMain.getWorkingUris();
		if ( uris.size() == 0 || currentIdx >= uris.size() ) {
			// we re done.
			if ( currentIdx > 0 ) {
				// if we did something, refresh the OntologySelection:
				if ( ontSel != null ) {
					ontSel.refreshListWorkingUris();
				}
			}
			return;
		}
	
		final String log_prefix = "_loadDataOfWorkingOntologiesForMapping(" +currentIdx+ "): ";
		
		String uri = uris.get(currentIdx);
		BaseOntologyInfo ontologyInfo = VineMain.getOntologyInfo(uri);
		
		if ( ontologyInfo == null ) {
			// Not a registered ontology; continue to next entry:
			_loadDataOfWorkingOntologiesForMapping(currentIdx + 1);
			return;
		}

		if ( ontologyInfo.getError() != null ) {
			// continue to next entry:
			_loadDataOfWorkingOntologiesForMapping(currentIdx + 1);
			return;
		}

		OntologyData ontologyData = ontologyInfo.getOntologyData();
		if ( ontologyData != null ) {
			// this entry already has data; continue to next entry:
			_loadDataOfWorkingOntologiesForMapping(currentIdx + 1);
			return;
		}
		
		// this entry needs data.
		
		if (! (ontologyInfo instanceof RegisteredOntologyInfo)) {
			Orr.log("Ontology from VineMain is not a RegisteredOntologyInfo");
			_loadDataOfWorkingOntologiesForMapping(currentIdx + 1);
			return;
		}
		
		RegisteredOntologyInfo registeredOntologyInfo = (RegisteredOntologyInfo) ontologyInfo;
		
		Orr.log(log_prefix +ontologyInfo.getUri()+ " starting");
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
						_loadDataOfWorkingOntologiesForMapping(currentIdx + 1);
					}
				});

			}
			
		};
		Orr.service.getOntologyContents(registeredOntologyInfo, null, callback);
	}

}
