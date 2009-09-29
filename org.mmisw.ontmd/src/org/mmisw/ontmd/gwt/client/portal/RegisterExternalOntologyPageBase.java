package org.mmisw.ontmd.gwt.client.portal;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for the wizard-like pages to register an external ontology.
 * 
 * TODO complete implementation
 * 
 * @author Carlos Rueda
 */
abstract class RegisterExternalOntologyPageBase  {
	
	// false: do not retrieve RDF contents from server.
	protected static final boolean INCLUDE_RDF = false;
	
	private final DockPanel dockPanel = new DockPanel();
	
	private HorizontalPanel buttonsPanel = new HorizontalPanel();
	
	protected PushButton backButton;
	
	protected PushButton nextButton;
	

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * 
	 * @param tempOntologyInfoListener
	 * @param allowLoadOptions
	 */
	protected RegisterExternalOntologyPageBase(boolean includeBack, boolean includeNext) {
		dockPanel.setWidth("650px");
		buttonsPanel.setBorderWidth(1);

		if ( includeBack ) {
			backButton = new PushButton("< Back", new ClickListener() {
				public void onClick(Widget sender) {
					// TODO
				}
			});
		}
		
		if ( includeNext ) {
			nextButton = new PushButton("Next >", new ClickListener() {
				public void onClick(Widget sender) {
				}
			});
		}
		
		_prepareButtons();
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setWidth("650px");
		hp.add(buttonsPanel);
		hp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		dockPanel.add(hp, DockPanel.SOUTH);
	}
	
	Widget getWidget() {
		return dockPanel;
	}
	
	/**
	 * A subclass calls this to define the contents of the page.
	 * @param contents
	 */
	protected void addContents(Widget contents) {
		dockPanel.add(contents, DockPanel.CENTER);
	}
	
	private void _prepareButtons() {
		if ( backButton != null ) {
			buttonsPanel.add(backButton);
//			backButton.setTitle("TODO");
		}
		
		if ( nextButton != null ) {
			buttonsPanel.add(nextButton);
//			nextButton.setTitle("TODO");
		}
		
	}
	
	protected void enable(boolean enabled) {
		if ( backButton != null ) {
			backButton.setEnabled(enabled);
		}
		if ( nextButton != null ) {
			nextButton.setEnabled(enabled);
		}
	}

}
