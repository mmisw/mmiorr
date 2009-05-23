package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.PropValue;
import org.mmisw.ontmd.gwt.client.portal.IOntologyPanel;
import org.mmisw.ontmd.gwt.client.rpc.DataResult;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfoPre;
import org.mmisw.ontmd.gwt.client.voc2rdf.TermTable;
import org.mmisw.ontmd.gwt.client.voc2rdf.TermTableCreator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
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
	 * @param mainPanel
	 * @param editing true for the editing interface; false for the vieweing interface.
	 */
	public DataPanel(IOntologyPanel mainPanel, boolean editing) {
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
	public void updateWith(OntologyInfoPre ontologyInfoPre) {
		
		if ( false ) {
			updateWith2(ontologyInfoPre);
		}
		else {
			updateWith(ontologyInfoPre.getUri());
		}
	}
	
	
	private void updateWith(final String ontologyUri) {
		this.clear();
		
		AsyncCallback<List<EntityInfo>> callback = new AsyncCallback<List<EntityInfo>>() {
			public void onFailure(Throwable thr) {
				add(new Label(thr.getMessage()));
			}

			public void onSuccess(List<EntityInfo> entities) {
				_doUpdate(ontologyUri, entities);
			}
		};

		Main.log("DataPanel.updateWith: getEntities" +ontologyUri);
		Main.ontmdService.getEntities(ontologyUri, callback);

	}

	
	private void _doUpdate(String ontologyUri, List<EntityInfo> entities) {
		
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
	
	private void updateWith2(OntologyInfoPre ontologyInfoPre) {
		this.clear();
		
		AsyncCallback<DataResult> callback = new AsyncCallback<DataResult>() {
			public void onFailure(Throwable thr) {
				add(new Label(thr.getMessage()));
			}

			public void onSuccess(DataResult dataResult) {
				_doUpdate2(dataResult);
			}
		};

		Main.log("DataPanel.updateWith: " +ontologyInfoPre);
		Main.ontmdService.getData(ontologyInfoPre, callback);
	}
	
	private void _doUpdate2(DataResult dataResult) {
		String error = dataResult.getError();
		if ( error != null ) {
			add(new Label(error));
			return;
		}
		
		String contents = dataResult.getCsv();
		
		StringBuffer errorMsg = new StringBuffer();
		TermTable termTable = TermTableCreator.createTermTable(',', contents, true, errorMsg);
		
		if ( errorMsg.length() > 0 ) {
			add(new HTML("<font color=\"red\">" +errorMsg+ "</font>"));
			return;
		}
		
		// OK:
		add(termTable);
//		ScrollPanel tableScroll = new ScrollPanel();
//		tableScroll.setWidget(termTable);
//		termTable.setScrollPanel(tableScroll);
		
	}

}
