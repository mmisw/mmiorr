package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyMetadata;
import org.mmisw.ontmd.gwt.client.EditDataPanel;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.ViewDataPanel;
import org.mmisw.ontmd.gwt.client.metadata.MetadataPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTML;
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


	private DisclosurePanel mdDisclosure = new DisclosurePanel("Metadata details");
	
	// created depending on type of interface: editing or viewwing
	private MetadataPanel metadataPanel;
	
	// created for viewing data mode
	private ViewDataPanel viewDataPanel;
	
	// created for editing data mode
	private EditDataPanel editDataPanel;
	
	
	/** Ontology to be dispatched */
	private OntologyInfo ontologyInfo;
	
	
	
	public OntologyMetadata getOntologyMetadata() {
		return ontologyInfo.getOntologyMetadata();
	}


	public OntologyPanel(OntologyInfo ontologyInfo) {
		super();
		setWidth("100%");
		container.setWidth("100%");
//		container.setSize("800px", "450px");
//		container.setBorderWidth(1);
		
		this.ontologyInfo = ontologyInfo;
		
		add(container);
		
		mdDisclosure = new DisclosurePanel("Metadata details");
		mdDisclosure.setAnimationEnabled(true);
		

	    enable(false);
	    

	    if ( this.ontologyInfo != null ) {
	    	container.add(prepareViewingInterface());
	    }
	    
	    dispatchInitialRequest();
	}
	
	
	private void dispatchInitialRequest() {
		getOntologyContents();
	}
	

	
	private Widget prepareViewingInterface() {
		// create metadata panel for viewing:
		metadataPanel = new MetadataPanel(this, false);

		editDataPanel = null;
		
		// create data panel for viewing:
		viewDataPanel = new ViewDataPanel();
		
		mdDisclosure.setContent(metadataPanel);
		
		CellPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		
		panel.add(headerPanel.getWidget());
		
		panel.add(mdDisclosure);
		
		panel.add(viewDataPanel);
		
		return panel;
	}
	
	void setViewingInterface() {
		container.clear();
		container.add(prepareViewingInterface());
		getOntologyContents();
		
	}

	void setEditingInterface() {
		container.clear();
		container.add(prepareEditingInterface());
		
		ontologyLoaded(ontologyInfo);
		enable(true);
	}

	private Widget prepareEditingInterface() {
		metadataPanel = new MetadataPanel(this, true);

		viewDataPanel = null;
		editDataPanel = new EditDataPanel();
		
		mdDisclosure.setContent(metadataPanel);
		
		
		CellPanel panel = new VerticalPanel();
		panel.setSpacing(5);
		
		panel.add(headerPanel.getWidget());
		
		panel.add(mdDisclosure);
		
		panel.add(editDataPanel);
		
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

		headerPanel.updateTitle("<b>" +ontologyInfo.getDisplayLabel()+ "</b> - "+ontologyInfo.getUri()+ "<br/>");
		headerPanel.showProgressMessage("Loading contents. Please wait...");

		metadataPanel.showProgressMessage("Loading contents. Please wait...");
		Main.log("getOntologyContents: ontologyUri = " +ontologyInfo.getUri());
		Main.ontmdService.getOntologyContents(ontologyInfo, callback);
	}


	private void ontologyLoaded(OntologyInfo ontologyInfo) {
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
			
			if ( viewDataPanel != null ) {
				viewDataPanel.updateWith(ontologyInfo);
			}
			else if ( editDataPanel != null ) {
				editDataPanel.updateWith(ontologyInfo);
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
		if ( editDataPanel != null ) {
			editDataPanel.enable(enabled);
		}
	}

	
	/** Shows header information
	 */
	private static class HeaderPanel {
		
		private final Widget widget;

		private HTML titleHtml = new HTML();
		private HTML descriptionHtml = new HTML();
		HeaderPanel() {
			VerticalPanel vp = new VerticalPanel();
			vp.setWidth("100%");
			vp.setSpacing(5);
			vp.setVerticalAlignment(ALIGN_MIDDLE);
//			vp.setBorderWidth(1);
			vp.add(titleHtml);
			vp.add(descriptionHtml);
			
//			widget = new CaptionPanel("caption);
//			widget.setContentWidget(vp);
			widget = vp;
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


}
