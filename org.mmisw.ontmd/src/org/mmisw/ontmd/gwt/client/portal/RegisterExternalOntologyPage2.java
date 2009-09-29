package org.mmisw.ontmd.gwt.client.portal;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * In this page the user indicate the type of hosting.
 * 
 * TODO not implemented yet
 * 
 * @author Carlos Rueda
 */
public class RegisterExternalOntologyPage2 extends RegisterExternalOntologyPageBase {
	
	private VerticalPanel contents = new VerticalPanel();
	
	private TempOntologyInfoListener tempOntologyInfoListener;
	
	private HTML statusLoad = new HTML();
	
	
	private CheckBox preserveOriginalBaseNamespace;
	

	/**
	 * Creates the ontology panel where the initial ontology can be loaded
	 * and its original contents displayed.
	 * 
	 * @param tempOntologyInfoListener
	 * @param allowLoadOptions
	 */
	public RegisterExternalOntologyPage2(
			TempOntologyInfoListener tempOntologyInfoListener
	) {
		super(true, true);
		contents.setSize("650px", "300px");
		addContents(contents);
		
		this.tempOntologyInfoListener = tempOntologyInfoListener;

		statusLoad.setText("");

		recreate();
		
	}
	
	
	private void recreate() {
		preserveOriginalBaseNamespace = new CheckBox("Preserve original base namespace");
		preserveOriginalBaseNamespace.setChecked(false);
		
		contents.clear();
		
		FlexTable panel = new FlexTable();
		panel.setWidth("100%");
//		panel.setBorderWidth(1);
		int row = 0;
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(3);
		hp.add(statusLoad);
		
		
		panel.setWidget(row, 1, hp);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		contents.add(panel);
		contents.add(createWidget());
	}

	
	
	private Widget createWidget() {
		
		FlexTable panel = new FlexTable();
		
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, preserveOriginalBaseNamespace);
		panel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
			
		return panel;
	}

}
