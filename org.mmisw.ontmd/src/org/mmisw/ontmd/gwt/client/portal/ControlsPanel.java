package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.portal.PortalMainPanel.InterfaceType;
import org.mmisw.ontmd.gwt.client.util.MyDialog;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Menu bar for the portal application.
 * 
 * @author Carlos Rueda
 */
public class ControlsPanel extends HorizontalPanel {

	private PortalControl pctrl = PortalControl.getInstance();
	
	// actually use a MenuBar?
	private static final boolean useMenuBar = false;
	
	private final MenuBar mb = useMenuBar ? new MenuBar() : null;
	
	private final HorizontalPanel controls = useMenuBar ? null : new HorizontalPanel();
	private final Set<PushButton> buttons =  useMenuBar ? null : new HashSet<PushButton>();

	
	/** Initially the menu bar is not shown */
	ControlsPanel() {
		setWidth("100%");
		
		if ( useMenuBar ) { 
			add(mb);
		}
		
		pctrl.setMenuBarPanel(this);
		
		if ( ! useMenuBar ) {
			this.setStylePrimaryName("ToolBar");
			
			controls.setVerticalAlignment(ALIGN_MIDDLE);
			this.add(controls);
		}
	}

	private void _clear() {
		if ( useMenuBar ) {
			mb.clearItems();
		}
		else {
			controls.clear();
			buttons.clear();
		}
	}

	void showMenuBar(InterfaceType type) {
		_clear();
		if ( useMenuBar ) {
			_createMenuBar(type);
		}
		else {
			_createButtons(type);
		}
	}
	
	private void _createButtons(InterfaceType type) {
		switch ( type ) {
		case BROWSE:
			_prepareBrowseButtons();
			break;
		case ONTOLOGY_VIEW:
			_prepareOntologyViewButtons();
			break;
		case ONTOLOGY_EDIT_NEW_VERSION:
		case ONTOLOGY_EDIT_NEW:
			_prepareOntologyEditButtons();
			break;
		}
	}

	private void _prepareBrowseButtons() {
		if ( pctrl.getLoginResult() != null ) {
			
			PushButton button;
			
			button = new PushButton("Create vocabulary", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewVocabulary();
				}
			});
			button.setTitle("Allows to create an ontology from a tabular arrangement of terms (voc2rdf tool)");
			controls.add(button);
			buttons.add(button);

if ( false ) {  //TODO not implemented
			button = new PushButton("Create mapping", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.launchCreateMapping();
				}
			});
			button.setTitle("Allows to create a mapping ontology (vine tool)");
			controls.add(button);
			buttons.add(button);
}
			
			button = new PushButton("Upload", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewFromFile();
				}
			});
			button.setTitle("Allows to prepare and register a local ontology file");
			controls.add(button);
			buttons.add(button);
		}
	}

	private void _prepareOntologyViewButtons() {
		
		PushButton button;
		
		RegisteredOntologyInfo oi = pctrl.getOntologyInfo();
		
		if ( pctrl.checkCanEditOntology(oi) == null ) {
			button = new PushButton("Edit new version", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.editNewVersion();
				}
			});
			controls.add(button);
			buttons.add(button);
		}
		
		if ( oi == null ) {
			oi = pctrl.getOntologyInfo();
		}
		
		if ( oi != null && oi.getPriorVersions() != null && oi.getPriorVersions().size() > 0 ) {
			final RegisteredOntologyInfo roi = oi;
			button = new PushButton("Versions", new ClickListener() {
				public void onClick(Widget sender) {
					launchVersions(roi);
				}
			});
			controls.add(button);
			buttons.add(button);
		}
		
		// "view as" options:
		HorizontalPanel viewAsPanel = new HorizontalPanel();
		viewAsPanel.setSpacing(4);
		viewAsPanel.add(new HTML("View as: "));
		for (PortalControl.DownloadOption dopc : PortalControl.DownloadOption.values() ) {
			String text = pctrl.getDownloadOptionHtml(dopc, oi);
			if ( text != null ) {
				viewAsPanel.add(new HTML(text));
			}
		}
		controls.add(viewAsPanel);
	}

	private void _prepareOntologyEditButtons() {
		if ( pctrl.getLoginResult() != null ) {
			PushButton button;
			
			button = new PushButton("Review and Register", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.reviewAndRegister();
				}
			});
			button.setTitle("Checks the contents and prepares the ontology for subsequent registration");
			controls.add(button);
			buttons.add(button);
			
			button = new PushButton("Cancel", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.cancelEdit();
				}
			});
			controls.add(button);
			buttons.add(button);
		}
	}

	
	public void notifyActivity(boolean b) {
	
		if ( buttons != null ) {
			for ( PushButton button : buttons ) {
				button.setEnabled(!b);
			}
		}
		
	}
	

	
	
	private void _createMenuBar(InterfaceType type) {
		switch ( type ) {
		case BROWSE:
			_prepareBrowseMenuBar();
			break;
		case ONTOLOGY_VIEW:
			_prepareOntologyViewMenuBar();
			break;
		case ONTOLOGY_EDIT_NEW_VERSION:
		case ONTOLOGY_EDIT_NEW:
			_prepareOntologyEditMenuBar();
			break;
		}
	}

	private static String _menuTitle(String title) {
		return "<u>" +title+ "</u>";
//		return title+ " <img src=\"" +GWT.getModuleBaseURL()+ "images/tridown.png\">";
	}
	private void _prepareBrowseMenuBar() {
		if ( pctrl.getLoginResult() != null ) {
			MenuBar new_mb = new MenuBar(true);
			new_mb.addItem(_createNewMenuItemVocabulary());
			
			if ( false ) {  //TODO not implemented
				new_mb.addItem(_createNewMenuItemMapping());
			}
			
			new_mb.addItem(_createNewMenuItemUpload());
			mb.addItem(new MenuItem(_menuTitle("New"), true, new_mb));
		}
	}

	
	MenuBar createOntologyMenuBar(RegisteredOntologyInfo oi, boolean includeEdit) {
		MenuBar ont_mb = new MenuBar(true);
		
		if ( includeEdit && pctrl.checkCanEditOntology(oi) == null ) {
			ont_mb.addItem(_createMenuItemCreateNewVersion());
		}
		
		ont_mb.addItem("Download as", _createMenuBarDownloadOntologyAs(oi));
		
		if ( oi == null ) {
			oi = pctrl.getOntologyInfo();
		}
		
		if ( oi != null && oi.getPriorVersions() != null && oi.getPriorVersions().size() > 0 ) {
			ont_mb.addSeparator();
			ont_mb.addItem(_createMenuItemVersions(oi));
		}
		
		return ont_mb;
	}
	
	
	private MenuBar _prepareOntologyViewMenuBar() {
		MenuBar ont_mb = createOntologyMenuBar(null, true);
		
		mb.addItem(new MenuItem(_menuTitle("Ontology"), true, ont_mb));
		
		return mb;
	}

	private void _prepareOntologyEditMenuBar() {
		if ( pctrl.getLoginResult() != null ) {
			// TODO
			MenuBar ont_mb = new MenuBar(true);
			ont_mb.addItem(_createMenuItemCancelEdit());
			ont_mb.addSeparator();

			mb.addItem(new MenuItem(_menuTitle("Edit"), true, ont_mb));
		}
	}

	private MenuItem _createMenuItemCancelEdit() {
		return new MenuItem("Cancel", new Command() {
			public void execute() {
				pctrl.cancelEdit();
			}
		});
	}
	
	
	private MenuItem _createNewMenuItemUpload() {
		return new MenuItem("Upload local file", new Command() {
			public void execute() {
				pctrl.createNewFromFile();
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
				pctrl.createNewVocabulary();
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
	
	
	private void launchVersions(RegisteredOntologyInfo oi) {
		List<RegisteredOntologyInfo> ontologyInfos = oi != null ? oi.getPriorVersions() : pctrl.getVersions();
		
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
	
	private MenuItem _createMenuItemVersions(final RegisteredOntologyInfo oi) {
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
	
	private MenuBar _createMenuBarDownloadOntologyAs(RegisteredOntologyInfo oi) {
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
