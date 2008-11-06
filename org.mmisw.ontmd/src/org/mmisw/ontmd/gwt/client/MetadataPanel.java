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
	
	
	private boolean editing;
	
	/** Little helper to show the ontology URI in two formats: the original
	 * and the "HTML" one (to be resolved by "Ont").
	 */
	private static class NewUri extends HorizontalPanel {
		private HTML html = new HTML();
		NewUri() {
			setSpacing(5);
			setVerticalAlignment(ALIGN_MIDDLE);
			setWidth("600");
//			setBorderWidth(1);
			add(html);
		}
		void updateText(String text) {
			html.setText(text);
		}
		void setUri(String uri, boolean link) {
			String str;
			if ( link ) {
				str = "<a href=\"" +uri+ "\" target=\"_blank\" >" +uri+ "</a>";
				String htmlLink = uri+ "?form=html";
				str += " (<a href=\"" +htmlLink+ "\" target=\"_blank\" >Resolve in HTML</a>)";
			}
			else {
				str = "<font color=\"" +"gray"+ "\">" +uri+ "</font>";
			}
			html.setHTML(str);
		}
	}

	private NewUri newUri = new NewUri();


	/**
	 * Creates the metadata panel
	 * @param mainPanel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	MetadataPanel(MainPanel mainPanel, boolean editing) {
		super();
		this.editing = editing;
		
//		this.mainPanel = mainPanel;
		
		int row = 0;
		
		container.setSize("700", "350");
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		this.getFlexCellFormatter().setColSpan(row,0, 2);
		this.setWidget(row, 0, hp);
		this.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP
		);
		
		String tooltip = "The ontology base URI";
		if ( editing ) {
			tooltip = "Will show the new base URI for the generated ontology";
			Label lbl = new Label("New base URI:");
			lbl.setTitle(tooltip);
			hp.add(lbl);
		}
		newUri.setTitle(tooltip);
		hp.add(newUri);
		row++;
		
		
		if ( editing ) {
			this.setWidget(row, 0, new HTML(
					"Fields marked * are required. " +
					"Use commas to separate values in multi-valued fields."));

			row++;
		}

//		container.setBorderWidth(1);
		this.getFlexCellFormatter().setColSpan(row,0, 2);
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
	
	
	/** Basically for viewing-only mode */
	void showProgressMessage(String msg) {
		newUri.updateText(msg);
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


	void resetToOriginalValues(OntologyInfo ontologyInfo, ReviewResult reviewResult, boolean confirm, boolean link) {
		resetToOriginalOrNewValues(ontologyInfo, true, reviewResult, confirm, link);
	}
	
	void resetToNewValues(OntologyInfo ontologyInfo, ReviewResult reviewResult, boolean confirm, boolean link) {
		resetToOriginalOrNewValues(ontologyInfo, false, reviewResult, confirm, link);
	}
	
	private void resetToOriginalOrNewValues(OntologyInfo ontologyInfo, boolean originalVals, 
			ReviewResult reviewResult, boolean confirm, boolean link) 
	{
		for ( int i = 0, c = tabPanel.getWidgetCount(); i < c; i++ ) {
			Widget w = tabPanel.getWidget(i);
			if ( w instanceof MetadataGroupPanel ) {
				((MetadataGroupPanel) w).resetToOriginalOrNewValues(ontologyInfo, originalVals, confirm);
			}
		}
		
		newUri.updateText("");
		if ( reviewResult != null ) {
			String new_uri = reviewResult.getUri();
			if ( new_uri != null ) {
				newUri.setUri(new_uri, link);
			}
		}
		else if ( ! editing ) {
			newUri.setUri(ontologyInfo.getUri(), link);
		}
	}
	
}
