package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.DataPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.metadata.MetadataPanel;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfoPre;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel for a given ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyPanel extends VerticalPanel implements IOntologyPanel {

	private CellPanel container = new VerticalPanel();

	// created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	// created ONLY in viewing mode
	private DataPanel dataPanel;
	
	
	/** URI of requested ontology from a parameter, if any. */
	private String requestedOntologyUri;
	
	
	private OntologyInfoPre ontologyInfoPre;
	
	
	public OntologyInfoPre getOntologyInfo() {
		return ontologyInfoPre;
	}


	public OntologyPanel(String requestedOntologyUri) {
		super();
		
		this.requestedOntologyUri = requestedOntologyUri;
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    enable(false);
	    

	    if ( requestedOntologyUri != null ) {
	    	container.add(prepareViewingInterface());
	    }
	    
	    dispatchInitialRequest();
	}
	
	
	private void dispatchInitialRequest() {
	    if ( requestedOntologyUri != null ) {
	    	getOntologyInfoFromRegistry(requestedOntologyUri);
	    }
	}
	

	
	private Widget prepareViewingInterface() {
		// create metadata panel for vieweing:
		metadataPanel = new MetadataPanel(this, false);

		dataPanel = new DataPanel(this, false);
		
		CellPanel panel = new VerticalPanel();
		panel.add(metadataPanel);
		panel.add(dataPanel);
		
		return panel;
//	    return metadataPanel;
	}
	
	private void getOntologyInfoFromRegistry(String ontologyUri) {
		AsyncCallback<OntologyInfoPre> callback = new AsyncCallback<OntologyInfoPre>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfoPre ontologyInfoPre) {
				String error = ontologyInfoPre.getError();
				if ( error != null ) {
					Window.alert(error);
				}
				else {
					boolean link = true;
					metadataPanel.resetToOriginalValues(ontologyInfoPre, null, false, link);
					
					if ( dataPanel != null ) {
						dataPanel.updateWith(ontologyInfoPre);
					}
				}
			}
		};

		metadataPanel.showProgressMessage("Loading metadata. Please wait...");
		Main.log("getOntologyInfoFromRegistry: ontologyUri = " +ontologyUri);
		Main.ontmdService.getOntologyInfoFromRegistry(ontologyUri, callback);
	}


	
	private void enable(boolean enabled) {
		if ( metadataPanel != null ) {
			metadataPanel.enable(enabled);
		}
		if ( dataPanel != null ) {
			dataPanel.enable(enabled);
		}
	}

}
