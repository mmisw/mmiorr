package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main panel.
 * 
 * @author Carlos Rueda
 */
public class MetadataPanel extends VerticalPanel {

//	private MainPanel mainPanel;
	private CellPanel container = new VerticalPanel();
	private TabPanel tabPanel = new TabPanel();
	
	private boolean enabled = true;
	
	private TextBox originalUri_tb = new TextBox();
	private TextBox newUri_tb = new TextBox();
	

	MetadataPanel(MainPanel mainPanel) {
		super();
//		this.mainPanel = mainPanel;
		
//		setBorderWidth(3);
		setWidth("850");
		setSize("850", "500");
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setSpacing(4);
		add(hp);
		hp.add(new Label("Original URI:"));
		hp.add(originalUri_tb);
		originalUri_tb.setWidth("300");
		originalUri_tb.setReadOnly(true);
		
//		hp = new HorizontalPanel();
//		hp.setSpacing(4);
//		add(hp);
		hp.add(new Label("New URI:"));
		hp.add(newUri_tb);
		newUri_tb.setWidth("300");
		newUri_tb.setReadOnly(true);
		
		add(new HTML(
				"Fields marked * are required. " +
				"Use commas to separate values in multi-valued fields."));
	    add(container);

//	    tabPanel.setAnimationEnabled(true);
	    
		FlexTable flexPanel = new FlexTable();
		int row = 0;

		CellPanel buttons = createButtons();
		flexPanel.getFlexCellFormatter().setColSpan(0, 0, 2);
		flexPanel.setWidget(row, 0, buttons);
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
	    
		
		flexPanel.setWidget(row, 0, tabPanel);
	    container.add(flexPanel); // tabPanel);
	    
		for ( AttrGroup attrGroup: Main.baseInfo.getAttrGroups() ) {
			CellPanel groupPanel = new MetadataGroupPanel(mainPanel, attrGroup);
			tabPanel.add(groupPanel, attrGroup.getName());
		}
		
	    tabPanel.selectTab(0);
	    enable(false);
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		return panel;
	}
	
	/** Puts test values */
	void putTestValues(Map<String, String> values) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).putTestValues(values);
			}
		}
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
	}
	
	void example(boolean confirm) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).example(confirm);
			}
		}
	}
	
	/** Delegates to the metadata group panels and
	 * updates the internal 'enabled' flag.
	 */
	void enable(boolean enabled) {
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).enable(enabled);
			}
		}
		this.enabled = enabled;
	}
	
	boolean isEnabled() {
		return enabled;
	}

	void reset(boolean confirm) {
		originalUri_tb.setText("");
		newUri_tb.setText("");
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).reset(confirm);
			}
		}
	}


	void setOntologyInfo(OntologyInfo ontologyInfo, ReviewResult reviewResult, boolean confirm) {
		reset(false);
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).setOntologyInfo(ontologyInfo, confirm);
			}
		}
		String origUri = ontologyInfo.getUri();
		if ( origUri != null ) {
			originalUri_tb.setText(origUri);
		}
		
		if ( reviewResult != null ) {
			String newUri = reviewResult.getUri();
			if ( origUri != null ) {
				newUri_tb.setText(newUri);
			}
		}
	}

}
