package org.mmisw.vine.gwt.client;

import org.mmisw.vine.gwt.client.rpc.OntologyInfo;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class OntologySelection extends VerticalPanel {
	
	private  MainPanel mainPanel;
	private CellPanel vp = new VerticalPanel();
	
	OntologySelection(MainPanel mainPanel) {
		super();
		this.mainPanel = mainPanel;
		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		
		hp.add(new HTML("Working ontologies:"));
		
		PushButton addButton = new PushButton("Add...");
		hp.add(addButton);
		
		add(vp);
		
		
		char id = 'A';
		for ( OntologyInfo s : Main.workingUris ) {
			vp.add(new HTML("<b>" +id+ "</b>: " + s.getUri()));
			id++;
		}
		
		addButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				addVocabulary();
			}

		});
	}
	
	private void addVocabulary() {
		final ListBox lb = createListBox();
		lb.setVisibleItemCount(Math.min(lb.getItemCount(), 20));
		
		final MyDialog popup = new MyDialog(lb);
		lb.addChangeListener(new ChangeListener () {
			public void onChange(Widget sender) {
				String sindex = lb.getValue(lb.getSelectedIndex());
				int index = Integer.parseInt(sindex);
				
				OntologyInfo ontologyInfo = Main.allUris.get(index);
				
				char id = (char) ((int) 'A' + Main.workingUris.size());
				vp.add(new HTML("<b>" +id+ "</b>: " 
						+ ontologyInfo.getUri()
						+ " -- "
						+ ontologyInfo.getDisplayLabel()
				));
				
				mainPanel.notifyWorkingOntologyAdded(ontologyInfo);
				
				popup.hide();
			}
		});
		popup.setText("Select vocabulary");
		popup.center();
		popup.show();
	}
	
	private static ListBox createListBox() {
		final ListBox lb = new ListBox();
		for ( int index = 0, count = Main.allUris.size(); index < count; index++ ) {
			OntologyInfo ontologyInfo = Main.allUris.get(index);
			if ( Main.workingUris.contains(ontologyInfo) ) {
				continue;
			}
			
			String lab = ontologyInfo.getDisplayLabel();
			String uri = ontologyInfo.getUri();
			lb.addItem(uri+ " : " +lab, String.valueOf(index));
		}
		return lb;
	}


}
