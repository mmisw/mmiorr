package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.ontmd.gwt.client.ViewDataPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.metadata.MetadataPanel;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfoPre;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel for a given ontology.
 * 
 * @author Carlos Rueda
 */
public class OntologyPanel extends VerticalPanel implements IOntologyPanel {

	private CellPanel container = new VerticalPanel();

	
	private HeaderPanel headerPanel = new HeaderPanel();


	// created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	// created ONLY in viewing mode
	private ViewDataPanel viewDataPanel;
	
	
	/** Ontology to be dispatched */
	private OntologyInfo ontologyInfo;
	
	
	private OntologyInfoPre ontologyInfoPre;
	
	
	public OntologyMetadata getOntologyMetadata() {
		return ontologyInfoPre.getOntologyMetadata();
	}


	public OntologyPanel(OntologyInfo ontologyInfo) {
		super();
		
		this.ontologyInfo = ontologyInfo;
		
//		container.setSize("800px", "450px");
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    enable(false);
	    

	    if ( ontologyInfo != null ) {
	    	container.add(prepareViewingInterface());
	    }
	    
	    dispatchInitialRequest();
	}
	
	
	private void dispatchInitialRequest() {
		getOntologyContents();
	}
	

	
	private Widget prepareViewingInterface() {
		// create metadata panel for vieweing:
		metadataPanel = new MetadataPanel(this, false);

		viewDataPanel = new ViewDataPanel();
		
		DisclosurePanel disclosure = new DisclosurePanel("Metadata details");
		disclosure.setAnimationEnabled(true);
		
		disclosure.setContent(metadataPanel);
		
		CellPanel panel = new VerticalPanel();
		
		panel.add(headerPanel);
		
//		panel.add(metadataPanel);
		panel.add(disclosure);
		
		panel.add(viewDataPanel);
		
		return panel;
	}
	
	private void getOntologyContents() {
		
		AsyncCallback<OntologyInfo> callback = new AsyncCallback<OntologyInfo>() {
			public void onFailure(Throwable thr) {
				String error = thr.getClass().getName()+ ": " +thr.getMessage();
				while ( (thr = thr.getCause()) != null ) {
					error += "\ncaused by: " +thr.getClass().getName()+ ": " +thr.getMessage();
				}
				Window.alert(error);
			}

			public void onSuccess(OntologyInfo ontologyInfo) {
				String error = ontologyInfo.getError();
				if ( error != null ) {
					Window.alert(error);
				}
				else {
					ontologyLoaded(ontologyInfo);
				}
			}
		};

		headerPanel.showProgressMessage("Loading " +ontologyInfo.getUri()+ ". Please wait...");
		metadataPanel.showProgressMessage("Loading contents. Please wait...");
		Main.log("getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
		Main.ontmdService.getOntologyContents(ontologyInfo, callback);
	}


	private void ontologyLoaded(OntologyInfo ontologyInfo) {
		String error = ontologyInfo.getError();
		if ( error != null ) {
			headerPanel.updateHtml(error);
		}
		else {
			headerPanel.updateHtml(
					  "Ontology URI: " +ontologyInfo.getUri()+ "<br/>"
					+ "Title: " +ontologyInfo.getDisplayLabel()+ "<br/>"
			);
			
			boolean link = true;
			metadataPanel.resetToOriginalValues(ontologyInfo, null, false, link);
			
			if ( viewDataPanel != null ) {
				viewDataPanel.updateWith(ontologyInfo);
			}
		}
	}
	
	private void enable(boolean enabled) {
		if ( metadataPanel != null ) {
			metadataPanel.enable(enabled);
		}
		if ( viewDataPanel != null ) {
			viewDataPanel.enable(enabled);
		}
	}

	
	/** Shows header information
	 */
	private static class HeaderPanel extends HorizontalPanel {
		private HTML html = new HTML();
		HeaderPanel() {
			setSpacing(5);
			setVerticalAlignment(ALIGN_MIDDLE);
			setWidth("600");
//			setBorderWidth(1);
			add(html);
		}
		
		void updateHtml(String text) {
			html.setHTML(text);
		}
		
		void showProgressMessage(String msg) {
			html.setHTML("<img src=\"../images/loading.gif\"> <i>" +msg+ "</i>");
		}
	}


}
