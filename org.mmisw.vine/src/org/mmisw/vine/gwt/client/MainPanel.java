package org.mmisw.vine.gwt.client;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * The main panel. This contains the ontology selection panel and
 * the multi page editor.
 * 
 * @author Carlos Rueda
 */
public class MainPanel extends VerticalPanel {
	
	private MapperPage mapperPage;
	private MappingsPanel mappingsPanel;
	
	MainPanel() {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    add(decPanel);

		OntologySelection ontSel = new OntologySelection(this);
		mappingsPanel = new MappingsPanel();
	    mapperPage = new MapperPage(mappingsPanel);


	    layout.add(ontSel);
	    layout.add(mapperPage);

	    layout.add(mappingsPanel);

	    layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
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
			"<img src=\"images/loading.gif\">" +
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
				
				Main.addWorkingUri(ontologyInfo);
				ontologySelection.ontologySucessfullyLoaded(ontologyInfo);
				mapperPage.notifyWorkingOntologyAdded(ontologyInfo);
				
				popup.hide();
			}
		};
		
		Main.log("getEntities: " +ontologyInfo.getUri()+ " starting");
		Main.vineService.getEntities(ontologyInfo, callback);
	}
}
