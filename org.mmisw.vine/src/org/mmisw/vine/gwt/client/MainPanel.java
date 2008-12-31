package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
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
		ontSel.setBorderWidth(1);
	    layout.add(ontSel);
	    layout.add(multiPageEditor = new MultiPageEditor());

	    layout.setCellHorizontalAlignment(ontSel, ALIGN_CENTER);
	}

	
	/**
	 * Gets the entities and then notifies the event to dependent components.
	 * @param ontologyInfo
	 */
	void notifyWorkingOntologyAdded(OntologyInfo ontologyInfo) {
		
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				Main.log("getEntities: " +ontologyInfo.getUri()+ " completed.");
				
				Main.workingUris.add(ontologyInfo);
				multiPageEditor.notifyWorkingOntologyAdded(ontologyInfo);
			}
		};
		
		Main.log("getEntities: " +ontologyInfo.getUri()+ " starting");
		Main.vineService.getEntities(ontologyInfo, callback);
	}
}
