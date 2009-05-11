package org.mmisw.ontmd.gwt.client;

import org.mmisw.ontmd.gwt.client.rpc.DataResult;
import org.mmisw.ontmd.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.voc2rdf.TermTable;
import org.mmisw.ontmd.gwt.client.voc2rdf.TermTableCreator;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

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
	DataPanel(MainPanel mainPanel, boolean editing) {
		super();
		setWidth("800");
	}
	
	void enable(boolean enabled) {
		// TODO
	}
	
	
	/**
	 * Updates this panel with the data associated to the given ontology 
	 * @param ontologyInfo
	 */
	void updateWith(OntologyInfo ontologyInfo) {
		
		this.clear();
		
		AsyncCallback<DataResult> callback = new AsyncCallback<DataResult>() {
			public void onFailure(Throwable thr) {
				add(new Label(thr.getMessage()));
			}

			public void onSuccess(DataResult dataResult) {
				_doUpdate(dataResult);
			}
		};

		Main.log("DataPanel.updateWith: " +ontologyInfo);
		Main.ontmdService.getData(ontologyInfo, callback);

	}
	
	private void _doUpdate(DataResult dataResult) {
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
