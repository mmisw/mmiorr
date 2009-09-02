package org.mmisw.ontmd.gwt.client.portal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.portal.PortalControl.ExternalViewersInfo;
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
 * Controls for the portal application.
 * 
 * @author Carlos Rueda
 */
public class ControlsPanel extends HorizontalPanel {

	private PortalControl pctrl = PortalControl.getInstance();
	
	private final HorizontalPanel controls = new HorizontalPanel();
	private final Set<PushButton> buttons =  new HashSet<PushButton>();

	
	/** Initially the menu bar is not shown */
	ControlsPanel() {
		setWidth("100%");
		
		pctrl.setMenuBarPanel(this);
		
		this.setStylePrimaryName("ToolBar");

		controls.setStylePrimaryName("ControlsBar");
		controls.setVerticalAlignment(ALIGN_MIDDLE);
		controls.setBorderWidth(1);
		this.add(controls);
	}

	private void _clear() {
		controls.clear();
		buttons.clear();
	}

	void showMenuBar(InterfaceType type) {
		_clear();
		_createButtons(type);
	}
	
	private void _createButtons(InterfaceType type) {
		switch ( type ) {
		case BROWSE:
			_prepareBrowseButtons();
			break;
		case SEARCH:
			_prepareSearchTermsButtons();
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

	private void _prepareSearchTermsButtons() {
		
		// TODO do we need buttons here?
		
	}
	
	private void _prepareBrowseButtons() {
		PushButton button;
		
		button = new PushButton("Refresh", new ClickListener() {
			public void onClick(Widget sender) {
				pctrl.refreshListAllOntologies();
			}
		});
		button.setTitle("Reloads the list of registered ontologies");
		controls.add(button);
		buttons.add(button);


		// TODO re-enable "search" when completed
//		button = new PushButton("Search", new ClickListener() {
//			public void onClick(Widget sender) {
//				pctrl.searchTerms();
//			}
//		});
//		button.setTitle("Searches terms");
//		controls.add(button);
//		buttons.add(button);

		
		if ( pctrl.getLoginResult() != null ) {
			button = new PushButton("Create vocabulary", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewVocabulary();
				}
			});
			button.setTitle("Allows to create an ontology from a tabular arrangement of terms (voc2rdf tool)");
			controls.add(button);
			buttons.add(button);

			button = new PushButton("Create mapping", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewMappingOntology();
				}
			});
			button.setTitle("Allows to create a mapping ontology (vine tool)");
			controls.add(button);
			buttons.add(button);
			
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
		boolean includeVersion = pctrl.getOntologyPanel().isVersionExplicit();
		
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
			String text = pctrl.getDownloadOptionHtml(dopc, oi, includeVersion);
			if ( text != null ) {
				viewAsPanel.add(new HTML(text));
			}
		}
		controls.add(viewAsPanel);
		
		ExternalViewersInfo xvi = pctrl.getExternalViewersInfo(oi, includeVersion);
		if ( xvi != null ) {
			controls.add(xvi.hp);
		}
		
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
	

	
	
	MenuBar createOntologyMenuBar(RegisteredOntologyInfo oi, boolean includeEdit, boolean includeVersion,
			boolean includeVersionsMenu
	) {
		MenuBar ont_mb = new MenuBar(true);
		
		if ( includeEdit && pctrl.checkCanEditOntology(oi) == null ) {
			ont_mb.addItem(_createMenuItemCreateNewVersion());
		}
		
		ont_mb.addItem("View as", _createMenuBarDownloadOntologyAs(oi, includeVersion));
		
		if ( oi == null ) {
			oi = pctrl.getOntologyInfo();
		}
		
		if ( includeVersionsMenu && oi != null && oi.getPriorVersions() != null && oi.getPriorVersions().size() > 0 ) {
			ont_mb.addSeparator();
			ont_mb.addItem(_createMenuItemVersions(oi));
		}
		
		ExternalViewersInfo xvi = pctrl.getExternalViewersInfo(oi, includeVersion);
		if ( xvi != null ) {
			ont_mb.addSeparator();
			MenuItem mi = new MenuItem(xvi.hrefHtml.getHTML(), true, nullCmd);
			mi.setTitle(xvi.tooltip);
			ont_mb.addItem(mi);
		}

		
		return ont_mb;
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
		popup.setText("Available versions for " +oi.getUnversionedUri());
		OntologyTable ontologyTable = new OntologyTable(PortalControl.getInstance().getQuickInfo(), true);
		ontologyTable.setIncludeVersionInLinks(true);
		ontologyTable.setSortColumn("version", false); // (version, false) = most recent version first. 
		
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
	
	private MenuBar _createMenuBarDownloadOntologyAs(RegisteredOntologyInfo oi, boolean includeVersion) {
		// use a nullCmd as i'm not sure addItem accepts a null command
		MenuBar mb = new MenuBar(true);
		for (PortalControl.DownloadOption dopc : PortalControl.DownloadOption.values() ) {
			String text = pctrl.getDownloadOptionHtml(dopc, oi, includeVersion);
			if ( text != null ) {
				mb.addItem(text, true, nullCmd);
			}
		}
		return mb;
	}

}
