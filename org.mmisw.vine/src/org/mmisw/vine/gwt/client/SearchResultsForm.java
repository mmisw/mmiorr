package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.vine.gwt.client.rpc.EntityInfo;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel where the search results are displayed.
 * 
 * @author Carlos Rueda
 */
public class SearchResultsForm extends VerticalPanel {
	
	private ResourceViewer resourceViewer;
	private int numElements;
	private int numSelected;
	
	private HTML status = new HTML("Selected: " +numSelected+ " out of " +numElements+ " element(s)");
	
	private List<CheckBox> cbs;
	
	private CellPanel p2;
	private ClickListener cl = new ClickListener() {
		public void onClick(Widget sender) {
			CheckBox cb = (CheckBox) sender;
			boolean selected = cb.isChecked();
			numSelected += selected ? +1 : -1;
			updateStatus();
		}
	};

	private FocusListener fl = new FocusListener() {

		public void onFocus(Widget sender) {
			TextBox comp = (TextBox) sender;
			resourceViewer.update(comp.getText());  // TODO	
		}

		public void onLostFocus(Widget sender) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * @param resourceViewer 
	 * 
	 */
	SearchResultsForm(ResourceViewer resourceViewer) {
		super();
		this.resourceViewer = resourceViewer;
		
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

	    cbs = new ArrayList<CheckBox>();
	    
	    p2 = new VerticalPanel();
		ScrollPanel scroller = new ScrollPanel(p2);
	    scroller.setSize("450px", "150px");
		p.add(scroller);
		
	}
	
	void updateAllNone(boolean selected) {
		for ( CheckBox cb : cbs  ) {
			cb.setChecked(selected);
		}
		numSelected = selected ? cbs.size() : 0;
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
		cbs.clear();
		
//		final FlexTable flexTable = new FlexTable();
//		FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
//		cellFormatter.s
		
		for ( EntityInfo entity : entities ) {
			String str = entity.getCode()+ ": <b>" +entity.getLocalName()+ "</b>";
			
			HorizontalPanel hp = new HorizontalPanel();
			p2.add(hp);
			hp.setTitle(entity.getLocalName());
			
			CheckBox cb = new CheckBox(str, true);
			cb.setFocus(true);
			cbs.add(cb);
			hp.add(cb);
			
			TextBox tb = new TextBox();
			tb.setText(str);
			tb.setReadOnly(true);
			tb.addFocusListener(fl);
			hp.add(tb);
//			cb.addFocusListener(fl);

//			HTML html = new HTML(str);
//			hp.add(html);

			cb.addClickListener(cl);
			
		}
		numElements = entities.size();
		updateStatus();
		
		if ( numElements == 0 ) {
			p2.add(new HTML("<i>No entities found</i>"));
		}
	}

}
