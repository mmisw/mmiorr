package org.mmisw.orrportal.gwt.client.portal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.portal.PortalControl.ExternalViewersInfo;
import org.mmisw.orrportal.gwt.client.portal.PortalMainPanel.InterfaceType;
import org.mmisw.orrportal.gwt.client.util.MyDialog;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;
import org.mmisw.orrportal.gwt.client.util.table.IOntologyTable;
import org.mmisw.orrportal.gwt.client.util.table.OntologyTableCreator;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
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

	private final SearchOntologiesPanel searchOntologiesPanel = new SearchOntologiesPanel();


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
		_addTitle();
		_createButtons(type);
	}

	private void _addTitle() {
		String title = pctrl.getTitle();
		if ( title != null ) {
			controls.add(new HTML("<b>&nbsp;" +title+ "&nbsp;</b>"));
		}
	}

	private void _createButtons(InterfaceType type) {
		controls.setBorderWidth(1);
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
			_prepareOntologyEditButtons(true);
			break;

		case UPLOAD_ONTOLOGY:
		case UPLOAD_NEW_VERSION:
			_prepareOntologyEditButtons(false);
			break;

		case ENTITY_VIEW:
			_prepareEntityViewButtons();
			break;
		case ENTITY_NOT_FOUND:

			break;

		case ADMIN:
			_prepareAdminButtons();
			break;
		}
	}

	private void _prepareAdminButtons() {
		controls.setBorderWidth(0);
		HTML html = new HTML("<blockquote><h2>Admin interface</h2></blockquote>");
		controls.add(html);
	}

	private void _prepareSearchTermsButtons() {
		controls.setBorderWidth(0);
		String sparqlPage = GWT.getModuleBaseURL() + "sparql/";
		HTML sparqlButton = new HTML("<blockquote><a target=\"_blank\" href=\"" +sparqlPage+
				"\">Advanced search with SPARQL</a></blockquote>");
		controls.add(sparqlButton);
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

		if ( pctrl.getLoginResult() == null ) {
			controls.add(_putHFillers("30px", searchOntologiesPanel, null));
		}
		else {
			controls.add(_putHFillers("30px", searchOntologiesPanel, "100px"));
			button = new PushButton("Create vocabulary", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewVocabulary();
				}
			});
			button.setTitle("Allows to create an ontology from a tabular arrangement of terms " +
					"using the integrated Voc2RDF tool");
			controls.add(button);
			buttons.add(button);

			button = new PushButton("Create mapping", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.createNewMappingOntology();
				}
			});
			button.setTitle("Allows to create a mapping ontology using the integrated VINE tool");
			controls.add(button);
			buttons.add(button);

			// Enhancement #202: "Upload button (in new portal) is ambiguous"
			//	Name of the button is now "Upload ontology"
			button = new PushButton("Upload ontology", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.startRegisterExternal();
				}
			});
			button.setTitle("Allows to prepare and register an external ontology file");
			controls.add(button);
			buttons.add(button);
		}
	}


	private HorizontalPanel _putHFillers(String wleft, Widget widget, String wright) {
		HorizontalPanel hp = new HorizontalPanel();
		if ( wleft != null ) {
			hp.add(_createHFiller(wleft));
		}
		hp.add(widget);
		if ( wright != null ) {
			hp.add(_createHFiller(wright));
		}
		return hp;
	}

	private Widget _createHFiller(String width) {
		HTML html = new HTML();
		html.setWidth(width);
		return html;
	}

	private void _prepareEntityViewButtons() {
		// nothing
	}

	private void _prepareOntologyViewButtons() {

		if ( ! (pctrl.getOntologyInfo() instanceof RegisteredOntologyInfo) ) {
			return;
		}

		RegisteredOntologyInfo oi = (RegisteredOntologyInfo) pctrl.getOntologyInfo();

		boolean includeVersion = pctrl.getOntologyPanel().isVersionExplicit();


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


		PushButton button;

		if ( pctrl.checkCanEditOntology(oi) == null ) {
			button = new PushButton("Edit new version", new ClickListener() {
				public void onClick(Widget sender) {
					pctrl.editNewVersion();
				}
			});
			controls.add(button);
			buttons.add(button);
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

		if ( oi != null && PortalControl.getInstance().getLoginResult() != null ) {
			final LoginResult loginResult = PortalControl.getInstance().getLoginResult();
			if ( loginResult != null && loginResult.isAdministrator() ) {
				final RegisteredOntologyInfo roi = oi;
				button = new PushButton("Unregister", new ClickListener() {
					public void onClick(Widget sender) {
						unregisterOntology(loginResult, roi);
					}
				});
				controls.add(button);
				buttons.add(button);

                boolean isTesting = OrrUtil.isTestingOntology(roi);
                controls.add(new HTML("&nbsp;&nbsp;"));
				button = new PushButton(isTesting ? "Remove 'testing' mark" : "Mark as 'testing'",
                new ClickListener() {
					public void onClick(Widget sender) {
                        markTestingOntology(loginResult, roi);
					}
				});
				controls.add(button);
				buttons.add(button);
			}
		}

	}

	private void _prepareOntologyEditButtons(boolean includeReviewAndRegister) {
		PushButton button;
		if ( pctrl.getLoginResult() != null ) {
			if ( includeReviewAndRegister ) {
				button = new PushButton("Review and Register", new ClickListener() {
					public void onClick(Widget sender) {
						pctrl.reviewAndRegister();
					}
				});
				button.setTitle("Checks the contents and prepares the ontology for subsequent registration");
				controls.add(button);
				buttons.add(button);
			}
		}

		button = new PushButton("Cancel", new ClickListener() {
			public void onClick(Widget sender) {
				pctrl.cancelEdit();
			}
		});
		controls.add(button);
		buttons.add(button);
	}


	public void notifyActivity(boolean b) {

		if ( buttons != null ) {
			for ( PushButton button : buttons ) {
				button.setEnabled(!b);
			}
		}

	}




	public MenuBar createOntologyMenuBar(RegisteredOntologyInfo oi, boolean includeEdit, boolean includeVersion,
			boolean includeVersionsMenu
	) {
		MenuBar ont_mb = new MenuBar(true);
		ont_mb.setAutoOpen(true);

		if ( includeEdit && pctrl.checkCanEditOntology(oi) == null ) {
			ont_mb.addItem(_createMenuItemCreateNewVersion());
		}

		ont_mb.addItem("View as", _createMenuBarDownloadOntologyAs(oi, includeVersion));

		if ( oi == null && (pctrl.getOntologyInfo() instanceof RegisteredOntologyInfo) ) {
			oi = (RegisteredOntologyInfo) pctrl.getOntologyInfo();
		}

		if ( oi != null ) {
			if ( includeVersionsMenu && oi.getPriorVersions() != null && oi.getPriorVersions().size() > 0 ) {
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
		IOntologyTable ontologyTable = OntologyTableCreator.create(PortalControl.getInstance().getQuickInfo(), true);
		ontologyTable.setIncludeVersionInLinks(true);

		final boolean sortDown = true; // (version, true) = most recent version first.
		ontologyTable.setSortColumn("version", sortDown);

		// this is to hide the popup when the user clicks one of the links:
		ontologyTable.addClickListenerToHyperlinks(
				new ClickListener() {
					public void onClick(Widget sender) {
						popup.hide();
					}
				}
		);

		vp.add(ontologyTable.getWidget());

		ontologyTable.setOntologyInfos(ontologyInfos, pctrl.getLoginResult());

		// close the popup if history changes:
		History.addHistoryListener(new HistoryListener() {
			public void onHistoryChanged(String historyToken) {
				popup.setAnimationEnabled(false);
				popup.hide();
			}
		});

//		popup.center();
//		popup.show();
		popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
			public void setPosition(int offsetWidth, int offsetHeight) {
				int left = (Window.getClientWidth() - offsetWidth) / 3;
				int top = (Window.getClientHeight() - offsetHeight) / 3;
				popup.setPopupPosition(left, top);
			}
		});

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


	private void unregisterOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		pctrl.unregisterOntology(loginResult, oi);
	}

	private void markTestingOntology(LoginResult loginResult, RegisteredOntologyInfo oi) {
		pctrl.markTestingOntology(loginResult, oi);
	}

	void dispatchSearchOntologies(String str) {
		searchOntologiesPanel.dispatchSearchOntologies(str);
	}
}
