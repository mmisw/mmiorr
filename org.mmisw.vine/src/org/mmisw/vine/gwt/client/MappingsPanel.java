package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.Mapping;
import org.mmisw.vine.gwt.client.rpc.RelationInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel containing the mappings.
 * 
 * @author Carlos Rueda
 */
public class MappingsPanel extends FlexTable {
	
	private List<Mapping> mappings = new ArrayList<Mapping>();
	
	/**
	 * 
	 */
	MappingsPanel() {
		super();
//		setBorderWidth(1);
	    setWidth("100%");
	    
	    setStylePrimaryName("MappingsTable");
	    _setHeader(0);
	    _addNoMappingsRow(1);
	}

	/**
	 * Adds a new mapping to the table.
	 * @param leftKey
	 * @param sender
	 * @param rightKey
	 */
	public void addMapping(String leftKey, RelationInfo relInfo, String rightKey) {
		int row = mappings.size();
		if ( row == 0 ) {
			this.clear();
			_setHeader(0);
		}

		String imgUri = GWT.getModuleBaseURL()+ "images/" +relInfo.getIconUri();
		Image img = new Image(imgUri);
		img.setTitle(relInfo.getDescription());
		
		
		Mapping mapping = new Mapping(leftKey, relInfo, rightKey);
		mappings.add(mapping);
		
		Widget left = new Label(leftKey);
		Widget center = img;
		Widget right = new Label(rightKey);

		_addRow(left, center, right, "MappingsTable-row");
	}
	
	private void _setHeader(int row) {
		this.getRowFormatter().setStyleName(row, "MappingsTable-header");
		
		SelectAllNonePanel selAllNonePanel = new SelectAllNonePanel() {
			@Override
			void updateAllNone(boolean selected) {
				// TODO Auto-generated method stub
			}
		};

		HTML title = new HTML("<b>Mappings</b>");
		
		this.setWidget(row, 0, selAllNonePanel);
		
		FlexCellFormatter cf = this.getFlexCellFormatter();
		cf.setColSpan(row, 1, 3);
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setWidth(row, 1, "100%");
		this.setWidget(row, 1, title);
		row++;
		
	}
	
	private void _addNoMappingsRow(int row) {
		this.getRowFormatter().setStyleName(row, "MappingsTable-row");
		FlexCellFormatter cf = this.getFlexCellFormatter();
//		_setAlignments(row);
		cf.setColSpan(row, 1, 3);
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
//		cf.setWidth(row, 1, "100%");
		HTML noYet = new HTML("<font color=\"gray\">(<i>No mappings</i>)</font>");
		this.setWidget(row, 0, noYet);
//		this.setText(row, 1, ".");
//		this.setText(row, 2, ".");
//		this.setText(row, 3, ".");
	}

	private void _addRow(Widget left, Widget center, Widget right, String style) {
		int row = this.getRowCount();
		FlexCellFormatter cf = this.getFlexCellFormatter();
		this.getRowFormatter().setStyleName(row, style);
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new CheckBox());
		hp.add(Main.images.metadata().createImage());
//		hp.add(Main.images.delete().createImage());
		
		this.setWidget(row, 0, hp);
		
		this.setWidget(row, 1, left);
		this.setWidget(row, 2, center);
		this.setWidget(row, 3, right);

		if ( center instanceof Image ) {
			String width = "30";
			Image img = (Image) center;
			width = "" +img.getWidth();
			cf.setWidth(row, 2, width);
		}		
		_setAlignments(row);
	}
	
	private void _setAlignments(int row) {
		FlexCellFormatter cf = this.getFlexCellFormatter();
		cf.setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setWidth(row, 1, "50%");
		cf.setWidth(row, 3, "50%");
			
		cf.setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 2, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		cf.setAlignment(row, 3, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
	}
}
