package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.PropValue;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

/**
 * The main metadata panel.
 * 
 * @author Carlos Rueda
 */
public class DataPanel extends VerticalPanel {

	/**
	 * Creates the metadata panel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	public DataPanel(boolean editing) {
		super();
		setWidth("800");
	}
	
	public void enable(boolean enabled) {
		// TODO
	}
	
	
	/**
	 * Updates this panel with the data associated to the given ontology 
	 * @param ontologyInfoPre
	 */
	public void updateWith(OntologyInfo ontologyInfo) {
		
		this.clear();
		List<EntityInfo> entities = ontologyInfo.getEntities();
		
		Set<String> header = new HashSet<String>();
		
		for ( EntityInfo entity : entities ) {
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				header.add(pv.getPropName());
			}
		}

		FlexTable flexPanel = new FlexTable();
		flexPanel.setStylePrimaryName("DataTable");
		flexPanel.setBorderWidth(1);
		flexPanel.setCellSpacing(4);
		FlexCellFormatter cf = flexPanel.getFlexCellFormatter();
		
		int row = 0;
		
		// HEADER
		flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-header");
		String[] colNames = header.toArray(new String[header.size()]);
		for ( int i = 0; i < colNames.length; i++ ) {
			String colName = colNames[i];
			flexPanel.setWidget(row, i, new Label(colName));
			cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		}
		row++;
		
		
		// CONTENTS:
		for ( EntityInfo entity : entities ) {
			flexPanel.getRowFormatter().setStylePrimaryName(row, "DataTable-row");
			Map<String, String> vals = new HashMap<String, String>();
			List<PropValue> props = entity.getProps();
			for ( PropValue pv : props ) {
				vals.put(pv.getPropName(), pv.getValueName());
			}
			for ( int i = 0; i < colNames.length; i++ ) {
				String colName = colNames[i];
				String colValue = vals.get(colName);
				if ( colValue == null ) {
					colValue = "";
				}
				flexPanel.setWidget(row, i, new Label(colValue));
				cf.setAlignment(row, i, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
			}
			row++;
		}

		add(flexPanel);
	}

}
