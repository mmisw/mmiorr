package org.mmisw.voc2rdf.gwt.client;

import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.vocabulary.AttrGroup;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class MetadataPanel extends VerticalPanel {


	private CellPanel container = new VerticalPanel();
	private TabPanel tabPanel = new TabPanel();
//	private FormInputPanel formInputPanel = new FormInputPanel();
	

	MetadataPanel() {
		super();
		
	    add(container);

	    
	    /////////
		FlexTable flexPanel = new FlexTable();
		
		int row = 0;
		
		CellPanel buttons = createButtons();
		flexPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
	    /////////
	    
		
		flexPanel.setWidget(row, 0, tabPanel);
	    
	    
	    container.add(flexPanel); // tabPanel);
	    
	    
	    ////////
		for ( AttrGroup attrGroup: Main.baseInfo.getAttrGroups() ) {
			
			CellPanel groupPanel = new MetadataGroupPanel(attrGroup);
			
			
			tabPanel.add(groupPanel, attrGroup.getName());
		}
		

	    ///
	    
	    
	    
	    
	    
	    
	    // TODO remove
//	    tabPanel.add(formInputPanel, "General");
	    
	    
	    tabPanel.selectTab(0);
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
//		exampleButton = new PushButton("Example", new ClickListener() {
//			public void onClick(Widget sender) {
//				formInputPanel.example();
//				vocabPanel.example();
//			}
//		});
//		panel.add(exampleButton);
		
		
		return panel;
	}
	
	String putValues(Map<String, String> values) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				String m = ((MetadataGroupPanel) w).putValues(values);
				if ( m != null ) {
					return m;
				}
			}
		}
		return null;
//		return formInputPanel.putValues(values);
	}
	
	void example() {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).example();
			}
		}
//		formInputPanel.example();
	}
	
	void reset() {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).reset();
			}
		}
//		formInputPanel.reset();
	}

}
