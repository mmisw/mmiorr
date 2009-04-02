package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Carlos Rueda
 */
public class MappingsPanel extends FlexTable {
	
	MappingsPanel() {
		super();
//		setBorderWidth(1);
	    setWidth("100%");
	    
	    setStylePrimaryName("inline");
	    _addRow(new HTML("left"), new HTML("rel"), new HTML("right"), "th");
	}

	void addMapping(String leftKey, Widget sender, String rightKey) {
		Widget left = new Label(leftKey);
		Widget center = sender;
		Widget right = new Label(rightKey);

		_addRow(left, center, right, "tr");
	}

	private void _addRow(Widget left, Widget center, Widget right, String style) {
		int row = this.getRowCount();
		
//		this.getFlexCellFormatter().setColSpan(row, 0, 2);
		this.setWidget(row, 0, left);
		this.setWidget(row, 1, center);
		this.setWidget(row, 2, right);

		FlexCellFormatter cf = this.getFlexCellFormatter();
		
		this.getRowFormatter().setStyleName(row, style);
		
		if ( center instanceof Image ) {
			String width = "40";
			Image img = (Image) center;
			width = "" +img.getWidth();
			cf.setWidth(row, 1, width);
		}		
		
		cf.setWidth(row, 0, "47%");
		cf.setWidth(row, 2, "47%");
			
		cf.setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
	}
}
