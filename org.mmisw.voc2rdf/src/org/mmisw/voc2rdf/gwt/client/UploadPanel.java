package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Captures login info and starts the upload to the registry.
 * 
 * @author Carlos Rueda
 */
public class UploadPanel extends VerticalPanel {

	private UserPanel userInfoPanel;
	
	private CellPanel buttons = createButtons();
	
	private Map<String, Widget> widgets = new HashMap<String, Widget>();
	
	
	
	UploadPanel(MainPanel mainPanel) {
		super();
		
		Widget form = createForm();
		add(form );
		this.setCellHorizontalAlignment(form, ALIGN_CENTER);
		
		userInfoPanel = new UserPanel(mainPanel);
		add(userInfoPanel);
		this.setCellHorizontalAlignment(userInfoPanel, ALIGN_CENTER);
		
		add(buttons);
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		panel.setWidth("700");
		
		int row = 0;
		
		String[] attrNames =  { "namespaceRoot", "ontologyUri" };
		String[] attrLabels = { "Namespace root:", "Ontology URI:" };
		
		for ( int i = 0; i < attrNames.length; i++ ) {
			String attrName = attrNames[i];
			String attrLabel = attrLabels[i];
			
			Widget widget;
			final TextBox tb = new TextBox();
			tb.setName(attrName );
			tb.setWidth("200");
			
			widget = tb;
				
			widgets.put(attrName, widget);
				
			Label lbl = new Label(attrLabel);
			panel.setWidget(row, 0, lbl);
			panel.setWidget(row, 1, widget);
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		
//		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
//		panel.setWidget(row, 0, buttons);
//		panel.getFlexCellFormatter().setAlignment(row, 0, 
//				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
//		);
//		row++;
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		
		return panel;
	}
	

}
