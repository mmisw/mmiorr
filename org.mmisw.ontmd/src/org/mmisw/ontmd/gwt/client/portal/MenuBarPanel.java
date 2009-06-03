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
	final MenuBar mb = new MenuBar();

	
	/** Initially the menu bar is not shown */
	MenuBarPanel() {
		setWidth("100%");
		add(mb);
		
		pctrl.setMenuBarPanel(this);
	}

	private void _clear() {
		mb.clearItems();
	}

	void showMenuBar(InterfaceType type) {
		_clear();
		if ( pctrl.getLoginResult() != null ) {
			_createMenuBar(type);
		}
	}
	
	private void _createMenuBar(InterfaceType type) {
		switch ( type ) {
		case BROWSE:
			_prepareMenuBarBrowse();
			break;
		case ONTOLOGY_VIEW:
			_prepareMenuBarOntologyView();
			break;
		case ONTOLOGY_EDIT:
			_prepareMenuBarOntologyEdit();
			break;
		}
	}

	private static String _menuTitle(String title) {
		return "<u>" +title+ "</u>";
//		return title+ " <img src=\"" +GWT.getModuleBaseURL()+ "images/tridown.png\">";
	}
	private void _prepareMenuBarBrowse() {
		MenuBar new_mb = new MenuBar(true);
		new_mb.addItem(_createNewMenuItemVocabulary());
		new_mb.addItem(_createNewMenuItemMapping());
		new_mb.addItem(_createNewMenuItemUpload());
		mb.addItem(new MenuItem(_menuTitle("New"), true, new_mb));
	}

	
	MenuBar createOntologyMenuBar(OntologyInfo oi, boolean includeEdit) {
		MenuBar ont_mb = new MenuBar(true);
		
		if ( includeEdit && pctrl.checkCanEditOntology(oi) == null ) {
			ont_mb.addItem(_createMenuItemCreateNewVersion());
		}
		
		ont_mb.addItem("Download as", _createMenuBarDownloadOntologyAs(oi));
		
		if ( oi != null && oi.getPriorVersions() != null && oi.getPriorVersions().size() > 0 ) {
			ont_mb.addSeparator();
			ont_mb.addItem(_createMenuItemVersions(oi));
		}
		
		return ont_mb;
	}
	
	
	private MenuBar _prepareMenuBarOntologyView() {
		MenuBar ont_mb = createOntologyMenuBar(null, true);
		
		mb.addItem(new MenuItem(_menuTitle("Ontology"), true, ont_mb));
		
		return mb;
	}

	private void _prepareMenuBarOntologyEdit() {
		// TODO
		MenuBar ont_mb = new MenuBar(true);
		ont_mb.addItem(_createMenuItemCancelEdit());
		ont_mb.addSeparator();
		
		mb.addItem(new MenuItem(_menuTitle("Edit"), true, ont_mb));
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
	
	
	private void launchVersions(OntologyInfo oi) {
		List<OntologyInfo> ontologyInfos = oi != null ? oi.getPriorVersions() : pctrl.getVersions();
		
		if ( ontologyInfos == null || ontologyInfos.isEmpty() ) {
			Window.alert("Info about versions not available");
			return;
		}
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSpacing(4);
		vp.setHorizontalAlignment(ALIGN_CENTER);

		final MyDialog popup = new MyDialog(vp);
		popup.setText("Available versions");
		OntologyTable ontologyTable = new OntologyTable(PortalControl.getInstance().getQuickInfo());
		
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
	
	private MenuItem _createMenuItemVersions(final OntologyInfo oi) {
		return new MenuItem("Versions", new Command() {
			public void execute() {
				launchVersions(oi);
			}
		});
	}

	
	private static Command nullCmd = new Command() {
		public void execute() {
			// nothing
		}
	};
	
	private MenuBar _createMenuBarDownloadOntologyAs(OntologyInfo oi) {
		// use a nullCmd as i'm not sure addItem accepts a null command
		MenuBar mb = new MenuBar(true);
		for (PortalControl.DownloadOption dopc : PortalControl.DownloadOption.values() ) {
			String text = pctrl.getDownloadOptionHtml(dopc, oi);
			if ( text != null ) {
				mb.addItem(text, true, nullCmd);
			}
		}
		return mb;
	}
	
}
