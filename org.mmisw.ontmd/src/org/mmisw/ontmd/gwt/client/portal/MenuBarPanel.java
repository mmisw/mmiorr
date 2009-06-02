package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * Menu bar for the portal application.
 * 
 * @author Carlos Rueda
 */
public class MenuBarPanel extends HorizontalPanel {

	PortalControl pctrl = PortalControl.getInstance();
	
	/** Initially the menu bar is not shown */
	MenuBarPanel() {
		setWidth("100%");
	}

	private void _clear() {
		clear();
	}

	void showMenuBar(InterfaceType type) {
		_clear();
		if ( pctrl.getLoginResult() != null ) {
			_createMenuBar(type);
		}
	}
	
	private void _createMenuBar(InterfaceType type) {
		MenuBar mb = null;
		switch ( type ) {
		case BROWSE:
			mb = _createMenuBarBrowse();
			break;
		case ONTOLOGY_VIEW:
			mb = _createMenuBarOntologyView();
			break;
		case ONTOLOGY_EDIT:
			mb = _createMenuBarOntologyEdit();
			break;
		}
		
		if ( mb != null ) {
			add(mb);
		}
	}

	private static String _menuTitle(String title) {
		return "<u>" +title+ "</u>";
//		return title+ " <img src=\"" +GWT.getModuleBaseURL()+ "images/tridown.png\">";
	}
	private MenuBar _createMenuBarBrowse() {
		MenuBar mb = new MenuBar();

		MenuBar new_mb = new MenuBar(true);
		new_mb.addItem(_createNewMenuItemVocabulary());
		new_mb.addItem(_createNewMenuItemMapping());
		new_mb.addItem(_createNewMenuItemUpload());
		mb.addItem(new MenuItem(_menuTitle("New"), true, new_mb));
		
		return mb;
	}

	private MenuBar _createMenuBarOntologyView() {

		MenuBar ont_mb = new MenuBar(true);
		ont_mb.addItem(_createMenuItemCreateNewVersion());
		ont_mb.addItem("Download as", _createMenuBarDownloadOntologyAs());
		ont_mb.addSeparator();
		ont_mb.addItem(_createMenuItemVersions());
		
		MenuBar mb = new MenuBar();
		mb.addItem(new MenuItem(_menuTitle("Ontology"), true, ont_mb));
		return mb;
	}

	private MenuBar _createMenuBarOntologyEdit() {
		// TODO
		MenuBar ont_mb = new MenuBar(true);
		ont_mb.addItem(_createMenuItemCancelEdit());
		ont_mb.addSeparator();
		
		MenuBar mb = new MenuBar();
		mb.addItem(new MenuItem(_menuTitle("Edit"), true, ont_mb));
		return mb;
	}

	private MenuItem _createMenuItemCancelEdit() {
		return new MenuItem("Cancel", new Command() {
			public void execute() {
				pctrl.cancelEdit();
			}
		});
	}
	
	
	private MenuItem _createNewMenuItemUpload() {
		return new MenuItem("Upload", new Command() {
			public void execute() {
				pctrl.launchCreateUpload();
			}
		});
	}

	private MenuItem _createNewMenuItemMapping() {
		return new MenuItem("Mapping", new Command() {
			public void execute() {
				pctrl.launchCreateMapping();
			}
		});
	}

	private MenuItem _createNewMenuItemVocabulary() {
		return new MenuItem("Vocabulary", new Command() {
			public void execute() {
				pctrl.launchCreateVocabulary();
			}
		});
	}


	private MenuItem _createMenuItemCreateNewVersion() {
		return new MenuItem("Edit new version", new Command() {
			public void execute() {
				pctrl.editNewVersion();
			}
		});
	}
	
	private MenuItem _createMenuItemVersions() {
		return new MenuItem("Versions", new Command() {
			public void execute() {
				pctrl.showVersions();
			}
		});
	}
	
	private static Command nullCmd = new Command() {
		public void execute() {
			// nothing
		}
	};
	
	private MenuBar _createMenuBarDownloadOntologyAs() {
		// use a nullCmd as i'm not sure addItem accepts a null command
		MenuBar mb = new MenuBar(true);
		for (PortalControl.DownloadOption dopc : PortalControl.DownloadOption.values() ) {
			String text = pctrl.getDownloadOptionHtml(dopc);
			if ( text != null ) {
				mb.addItem(text, true, nullCmd);
			}
		}
		return mb;
	}
}
