package org.mmisw.vine.gwt.client;

import java.util.List;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ResultsForm extends VerticalPanel {
	
	private int numElements;
	private int numSelected;
	
	private HTML status = new HTML("Selected: " +numSelected+ " out of " +numElements+ " element(s)");
	
	private CellPanel p2;
	private ClickListener cl = new ClickListener() {
		public void onClick(Widget sender) {
			CheckBox cb = (CheckBox) sender;
			boolean selected = cb.isChecked();
			numSelected += selected ? +1 : -1;
			updateStatus();
		}
	};
	
	ResultsForm() {
		super();
		
		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		hp.setSpacing(10);		
		
		PushButton all = new PushButton("All", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(true);
			}
		});
		hp.add(all);
		PushButton none = new PushButton("None", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(false);
			}
		});
		hp.add(none);

		hp.add(status);
		
		CellPanel p = new VerticalPanel();
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(p);
	    add(decPanel);

	    p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("450px", "150px");
		p.add(scroller);
		
	}
	
	void updateAllNone(boolean selected) {
		for ( int i = 0, c = p2.getWidgetCount(); i < c; i++ ) {
			CheckBox cb = (CheckBox) p2.getWidget(i);
			cb.setChecked(selected);
		}
		numSelected = selected ? p2.getWidgetCount() : 0;
		updateStatus();
	}
	
	void updateStatus() {
		status.setText("Selected: " +numSelected+ " out of " +numElements+ " element(s)");
	}

	public void searching() {
		p2.clear();
		p2.add(new HTML(
				"<img src=\"images/loading.gif\"> <i>Searching...</i>"
		));
	}

	public void updateEntities(List<EntityInfo> entities) {
		// TODO dispatch checkBox for the terms
		p2.clear();
		for ( EntityInfo entity : entities ) {
			String str = entity.getCode()+ ":" +entity.getLocalName();
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.setTitle(entity.getLocalName());
			p2.add(hp);
			
			CheckBox cb = new CheckBox();
			hp.add(cb);
			hp.add(new HTML(str));

			cb.addClickListener(cl);
			
		}
		numElements = entities.size();
		updateStatus();
		
		if ( numElements == 0 ) {
			p2.add(new HTML("<i>No entities found</i>"));
		}
	}

}
