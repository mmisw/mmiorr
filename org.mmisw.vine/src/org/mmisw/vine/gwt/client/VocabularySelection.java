package org.mmisw.vine.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VocabularySelection extends VerticalPanel {
	
	VocabularySelection(int searchIndex) {
		super();
		
		CellPanel hp = new HorizontalPanel();
		add(hp);
		
		final PushButton all = new PushButton("All");
		final PushButton none = new PushButton("None");
		final List<CheckBox> cbs = new ArrayList<CheckBox>();
		hp.add(new HTML("Search the following ontologies:"));
		hp.add(all);
		all.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				for (CheckBox cb: cbs) {
					cb.setChecked(true);
				}
			}
		});
		hp.add(none);
		none.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				for (CheckBox cb: cbs) {
					cb.setChecked(false);
				}
			}
		});
		
		CellPanel vp = new VerticalPanel();
		add(vp);
		
		
		for ( String s : Main.workingUris ) {
			CheckBox cb = new CheckBox(s);
			cbs.add(cb);
			vp.add(cb);
		}
		if ( searchIndex >= 0 ) {
			cbs.get(searchIndex).setEnabled(true);
		}
	}

}
