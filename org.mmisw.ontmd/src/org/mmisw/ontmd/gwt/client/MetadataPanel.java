package org.mmisw.ontmd.gwt.client;

import java.util.Map;

import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.rpc.ReviewResult;
import org.mmisw.ontmd.gwt.client.vocabulary.AttrGroup;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The main metadata panel.
 * 
 * @author Carlos Rueda
 */
public class MetadataPanel extends FlexTable {

//	private MainPanel mainPanel;
	private DockPanel container = new DockPanel();
	private TabPanel tabPanel = new TabPanel();
	
	private boolean enabled = true;
	
	private TextBox originalUri_tb = new TextBox();
	private TextBox newUri_tb = new TextBox();
	

	/**
	 * Creates the metadata panel
	 * @param mainPanel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	MetadataPanel(MainPanel mainPanel, boolean editing) {
		super();
//		this.mainPanel = mainPanel;
		
		int row = 0;
		
//		setBorderWidth(1);
//		setWidth("850");
//		setSize("850", "500");
		
		container.setSize("700", "350");
		
		HorizontalPanel hp = new HorizontalPanel();
//		hp.setSpacing(4);
//		hp.setHeight("20");
		this.setWidget(row, 0, hp);
		this.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);

		originalUri_tb.setWidth("700");
		originalUri_tb.setReadOnly(true);
		if ( editing ) {
			hp.add(new Label("Original URI:"));
			originalUri_tb.setWidth("300");
		}
		hp.add(originalUri_tb);
		
		row++;
		
		if ( editing ) {
			hp.add(new Label("New URI:"));
			hp.add(newUri_tb);
			newUri_tb.setWidth("300");
			newUri_tb.setReadOnly(true);

			this.setWidget(row, 0, new HTML(
					"Fields marked * are required. " +
					"Use commas to separate values in multi-valued fields."));
			row++;
		}

//		container.setBorderWidth(1);
		this.setWidget(row, 0, container);
		this.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);

//	    tabPanel.setAnimationEnabled(true);
	    
		DockPanel dockPanel = new DockPanel();
//		dockPanel.setBorderWidth(1);

		
		dockPanel.add(tabPanel, DockPanel.NORTH);
	    container.add(dockPanel, DockPanel.CENTER);
	    
		for ( AttrGroup attrGroup: Main.baseInfo.getAttrGroups() ) {
			CellPanel groupPanel = new MetadataGroupPanel(mainPanel, attrGroup, editing);
			tabPanel.add(groupPanel, attrGroup.getName());
		}
		
	    tabPanel.selectTab(0);
	    enable(false);
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


	void resetToOriginalValues(OntologyInfo ontologyInfo, ReviewResult reviewResult, boolean confirm) {
		resetToOriginalOrNewValues(ontologyInfo, true, reviewResult, confirm);
	}
	
	void resetToNewValues(OntologyInfo ontologyInfo, ReviewResult reviewResult, boolean confirm) {
		resetToOriginalOrNewValues(ontologyInfo, false, reviewResult, confirm);
	}
	
	private void resetToOriginalOrNewValues(OntologyInfo ontologyInfo, boolean originalVals, 
			ReviewResult reviewResult, boolean confirm) 
	{
		originalUri_tb.setText("");
		newUri_tb.setText("");
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).resetToOriginalOrNewValues(ontologyInfo, originalVals, confirm);
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
