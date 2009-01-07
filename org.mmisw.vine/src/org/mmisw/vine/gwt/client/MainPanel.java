package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPanel extends VerticalPanel {
	
	private MultiPageEditor multiPageEditor;
	
	MainPanel() {
		super();
		
		VerticalPanel layout = new VerticalPanel();
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(layout);
	    
	    add(decPanel);

		OntologySelection ontSel = new OntologySelection(this);
//		ontSel.setBorderWidth(1);
	    layout.add(ontSel);
	    multiPageEditor = new MultiPageEditor();
	    layout.add(multiPageEditor.getWidget());

	    layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
	}

	
	/**
	 * Gets the entities and then notifies the event to dependent components.
	 * @param ontologySelection 
	 * @param ontologyInfo
	 */
	void notifyWorkingOntologyAdded(final OntologySelection ontologySelection, OntologyInfo ontologyInfo, final MyDialog popup) {
		
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
		
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
				popup.hide();
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				popup.setWidget(new HTML("Load complete"));
				Main.log("getEntities: " +ontologyInfo.getUri()+ " completed.");
				
				Main.addWorkingUri(ontologyInfo);
				ontologySelection.ontologySucessfullyLoaded(ontologyInfo);
				multiPageEditor.notifyWorkingOntologyAdded(ontologyInfo);
				
				popup.hide();
			}
		};
		
		Main.log("getEntities: " +ontologyInfo.getUri()+ " starting");
		Main.vineService.getEntities(ontologyInfo, callback);
	}
}
