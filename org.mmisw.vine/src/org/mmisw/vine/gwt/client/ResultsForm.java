package org.mmisw.vine.gwt.client;

import java.util.List;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ResultsForm extends VerticalPanel {
	
	private CellPanel p2;
	
	ResultsForm() {
		super();
		
		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		hp.setSpacing(10);
		CheckBox cba = new CheckBox("All");
		hp.add(cba);
		hp.add(new HTML("Found: ?   Selected: ?"));
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("400px", "100px");
		p.add(scroller);
		
	}

	public void updateTerms(List<String> terms) {
		// TODO Auto-generated method stub
		p2.clear();
		for ( String term : terms ) {
			CheckBox cb = new CheckBox("" +term);
			p2.add(cb);			
		}
	}

}
