package org.mmisw.ontmd.gwt.client.portal;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
	
	
	private void launchVersions() {
		List<OntologyInfo> ontologyInfos = pctrl.getVersions();
		if ( ontologyInfos == null || ontologyInfos.isEmpty() ) {
			Window.alert("Info about versions not available");
			return;
		}
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		vp.setHorizontalAlignment(ALIGN_CENTER);

		final MyDialog popup = new MyDialog(vp);
		popup.setText("Available versions");
		OntologyTable ontologyTable = new OntologyTable();
		
		// this is to hide the popup when the user clicks one of the links:
		ontologyTable.addClickListenerToHyperlinks(
				new ClickListener() {
					public void onClick(Widget sender) {
						popup.hide();
					}
				}
		);
		
		ontologyTable.setOntologyInfos(ontologyInfos, pctrl.getLoginResult());
		
		vp.add(ontologyTable);

		popup.center();
		popup.show();
	}
	
	private MenuItem _createMenuItemVersions() {
		return new MenuItem("Versions", new Command() {
			public void execute() {
				launchVersions();
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
