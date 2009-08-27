package org.mmisw.ontmd.gwt.client.vine;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.core.client.GWT;
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
	
	
	public VineEditorPanel(MappingOntologyData ontologyData, boolean readOnly) {
		super();
		this.ontologyData = ontologyData;
		this.readOnly = readOnly;
		
		layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    add(decPanel);

	    _setUp();
	}
	
	private void _setUp() {
		layout.clear();
		
		List<Mapping> mappings = ontologyData.getMappings();

		if ( ! readOnly || (mappings != null && mappings.size() > 0) ) {
			ontSel = new OntologySelection(this);
			layout.add(ontSel);
			layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
		}
		
		mappingsPanel = new MappingsPanel(readOnly);
		mappingsPanel.setMappings(mappings);
		
		
		if ( ! readOnly ) {
			mapperPage = new MapperPage(mappingsPanel);
			layout.add(mapperPage);
		}

	    layout.add(mappingsPanel);

	}

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
				Main.log("getEntities: " +ontologyInfo.getUri()+ " completed.");
				
				VineMain.addWorkingUri(ontologyInfo);
				ontologySelection.ontologySucessfullyLoaded(ontologyInfo);
				
				if ( mapperPage != null ) {
					mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
				}
				
				popup.hide();
			}
		};
		
		Main.log("getEntities: " +ontologyInfo.getUri()+ " starting");
		Main.ontmdService.getOntologyContents(ontologyInfo, callback);
	}

}
