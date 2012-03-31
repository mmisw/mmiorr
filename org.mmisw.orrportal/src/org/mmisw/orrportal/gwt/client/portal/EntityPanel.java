package org.mmisw.orrportal.gwt.client.portal;

import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.EntityInfo;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * The main panel for an entity (aka term).
 * 
 * @author Carlos Rueda
 */
public class EntityPanel extends VerticalPanel {

	private final VerticalPanel container = new VerticalPanel();
	private ScrollPanel scroller;
	
	/**
	 * 
	 */
	public EntityPanel() {
		super();
		
		setSpacing(10);
		
		add(container);
	}
	
	public void setWidth(String width) {
		if ( scroller != null ) {
			scroller.setWidth(width);
		}
		else {
			super.setWidth(width);
		}
	}

	public void setSize(String width, String height) {
		if ( scroller != null ) {
			scroller.setSize(width, height);
		}
		else {
			super.setSize(width, height);
		}
	}

	/** Updates the contents */
	public void update(EntityInfo entityInfo) {
		
		container.add(_getPropertiesTable(entityInfo));
	}
	
	
	private FlexTable _getPropertiesTable(EntityInfo entity) {
		
		FlexTable flexPanel = new FlexTable();
		flexPanel.setStylePrimaryName("OntologyTable");
		flexPanel.setBorderWidth(1);
		flexPanel.setCellSpacing(4);
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		
		int row = 0;
		
//		String uri = entity.getUri();
//		cf.setColSpan(row, 0, 2);
//		flexPanel.setWidget(row, 0, new Label(uri));
//		cf.setStyleName(row, 0, "OntologyTable-header");
//		cf.setAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
//		row++;
		
		
		flexPanel.setWidget(row, 0, new Label("Predicate"));
		cf.setStyleName(row, 0, "OntologyTable-header");
		cf.setAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
		
		flexPanel.setWidget(row, 1, new Label("Object"));
		cf.setStyleName(row, 1, "OntologyTable-header");
		cf.setAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		row++;
		
		List<PropValue> props = entity.getProps();
		for ( PropValue pv : props ) {
			
            cf.setStyleName(row, 0, "OntologyTable-row");
            cf.setStyleName(row, 1, "OntologyTable-row");

            String htmlStr;
			
			// column 0
			String propName = pv.getPropName();
			String propUri = pv.getPropUri();
			if ( propName == null ) {
				propName = "?";
			}
			if ( propUri != null ) {
				Hyperlink link = new Hyperlink(propUri, propUri);
//				Hyperlink link = new Hyperlink(propName, propUri);
				if ( propUri != null ) {
					link.setTitle(propUri);
				}
				flexPanel.setWidget(row, 0, link);
			}
			else {
				htmlStr = propName;
				HTML html = new HTML("<b>" +htmlStr+ "</b>:");
				if ( propUri != null ) {
					html.setTitle(propUri);
				}
				flexPanel.setWidget(row, 0, html);
			}
			cf.setAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP);

			
			// column 1
			String valueName = pv.getValueName();
			String valueUri = pv.getValueUri();
			if ( valueName == null ) {
				valueName = "?";
			}
			if ( valueUri != null ) {
				Hyperlink link = new Hyperlink(valueUri, valueUri);
//				Hyperlink link = new Hyperlink(valueName, valueUri);
				if ( valueUri != null ) {
					link.setTitle(valueUri);
				}
				flexPanel.setWidget(row, 1, link);
			}
			else {
				htmlStr = valueName;
				HTML html = new HTML(htmlStr);
				if ( valueUri != null ) {
					html.setTitle(valueUri);
				}
				flexPanel.setWidget(row, 1, html);
			}
			cf.setAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);

			
			row++;
		}

		return flexPanel;
	}
}
