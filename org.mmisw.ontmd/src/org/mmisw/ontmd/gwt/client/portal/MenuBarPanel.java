package org.mmisw.ontmd.gwt.client.portal;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Carlos Rueda
 */
public class MenuBarPanel extends VerticalPanel {
	
	private MenuBar mb = null;

	
	/** Initially the menu bar is not shown */
	MenuBarPanel() {
		
	}
	
	void showMenuBar(boolean show) {
		clear();
		if ( show ) {
			if ( mb == null ) {
				createMenuBar();
			}
			add(mb);
		}
	}

	
	private void createMenuBar() {
		mb = new MenuBar();

		// New
		MenuBar new_mb = new MenuBar(true);
		new_mb.addItem(new MenuItem("Vocabulary (Voc2RDF)", new Command() {
			public void execute() {
				// TODO Auto-generated method stub
			}
		}));
		new_mb.addItem(new MenuItem("Mapping (Vine)", new Command() {
			public void execute() {
				// TODO Auto-generated method stub
			}
		}));
		mb.addItem(new MenuItem("New", new_mb));

		mb.addItem(new MenuItem("Upload", new Command() {
			public void execute() {
				// TODO Auto-generated method stub
			}
		}));

	}
}
