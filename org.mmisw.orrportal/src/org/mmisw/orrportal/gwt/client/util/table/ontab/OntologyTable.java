package org.mmisw.orrportal.gwt.client.util.table.ontab;

import java.util.Collections;
import java.util.List;

import com.google.gwt.user.client.ui.*;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.Orr;
import org.mmisw.orrportal.gwt.client.util.OrrUtil;
import org.mmisw.orrportal.gwt.client.util.TooltipIcon;
import org.mmisw.orrportal.gwt.client.util.table.IQuickInfo;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class OntologyTable extends BaseOntologyTable {
	
	private final FlexTable flexPanel;
	
	
	/**
	 * Column header. Dispatches the sorting of the table.
	 * See Issue #44: "want sorting of columns in Browse view"
	 */
	private class ColHeader {
		
		private final HorizontalPanel hp1 = new HorizontalPanel();
		private FocusPanel focusPanel;
		
		ColHeader(String colLabel) {
			this(colLabel, null);
		}
		
		ColHeader(final String colLabel, String tooltip) {
			HorizontalPanel hp2 = new HorizontalPanel();
			hp2.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			focusPanel = new FocusPanel(hp2);

			if ( tooltip != null && tooltip.length() > 0 ) {
				Image ttIcon = new TooltipIcon(tooltip).getIcon();
				hp1.add(ttIcon);
			}
			
			hp1.add(focusPanel);
			
			if ( colLabel.length() > 0 ) {
				String labelText = "<b>" +colLabel+ "</b>&nbsp;";
				Widget labelWidget = new HTML(labelText);
				hp2.add(labelWidget);

				focusPanel.addClickListener(new ClickListener() {
					public void onClick(Widget sender) {
						_dispatchColumnHeader(colLabel);
					}
				});
				hp2.add(Orr.images.tridown().createImage());
			}
			else {
				hp2.add(new Label(""));
			}
		}

		private void _dispatchColumnHeader(final String colName) {
			Widget ww = focusPanel;
			int left = ww.getAbsoluteLeft();
			int top = ww.getAbsoluteTop() + ww.getOffsetHeight();

		    MenuBar menu = new MenuBar(true);
//		    menu.setStylePrimaryName("PopupMenu");
		    final PopupPanel menuPopup = new PopupPanel(true);
		    
		    menu.addItem(new MenuItem("Sort up", new Command() {
				public void execute() {
					sortByColumn(colName, false);
					menuPopup.hide();
				}
		    }));
		    menu.addItem(new MenuItem("Sort down", new Command() {
				public void execute() {
					sortByColumn(colName, true);
					menuPopup.hide();
				}
		    }));
		    
		    menuPopup.setWidget(menu);
		    menuPopup.setPopupPosition(left, top);
			menuPopup.show();
		}

		Widget getWidget() {
			return hp1;
		}
	}


	private ColHeader quickInfoHeaderHtml = new ColHeader("");
	
	private ColHeader nameHeaderHtml = new ColHeader("Name",
			"The one-line descriptive title for the ontology."
	);
	
	private ColHeader ontologyUriHeaderHtml = new ColHeader("URI",
			"The Uniform Resource Identifier given to the ontology."
	);
	
	// Issue #236
	private ColHeader authorHeaderHtml = new ColHeader("Author",
			"This column shows the value of the 'Content Creator' metadata " +
			"field for ontologies registered on or after 2010-07-26. " +
			"(For previous submissions, the value shown may correspond to the " +
			"'Ontology Creator' field.)"
	);
	
	private ColHeader versionHeaderHtml = new ColHeader("Version",
			"The version of the ontology."
	);
	
	private ColHeader submitterHeaderHtml = new ColHeader("Submitter",
			"Account name of the user that registered the ontology."
	);

	/**
	 * Performs sorting of the table entries in a deferred command.
	 */
	private void sortByColumn(String colName, boolean down) {
		sortFactor = down ? -1 : 1;
		sortColumn = colName;

		showProgress();
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Collections.sort(ontologyInfos, cmp);
				update();
			}
		});
	}

	
	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	public OntologyTable(IQuickInfo quickInfo, boolean isVersionsTable) {
		super(quickInfo, isVersionsTable);
		
		flexPanel = new FlexTable();
		flexPanel.setWidth("100%");
		flexPanel.setBorderWidth(1);
		flexPanel.setCellPadding(3);
		flexPanel.setStylePrimaryName("OntologyTable");
		
		prepareHeader();
	}
	
	public Widget getWidget() {
		return flexPanel;
	}

	/**
	 * Set the sort criteria. It will have effect on the next update of the table, which
	 * happens upon a call to {@link #setOntologyInfos(List, LoginResult)}.
	 * 
	 * @param sortColumn  Base column for the sort
	 * @param down        true to sort down.
	 */
	public void setSortColumn(String sortColumn, boolean down) {
		this.sortColumn = sortColumn;
		this.sortFactor = down ? -1 : +1;
	}


	public void clear() {
		flexPanel.clear();
		while ( flexPanel.getRowCount() > 0 ) {
			flexPanel.removeRow(0);
		}
	}
	
	public void showProgress() {
		clear();
		int cols = prepareHeader();
		int row = 1;
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, cols);
		flexPanel.setWidget(row, 0, new HTML("<i>one moment...</i>"));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
	}

	/** returns the number of columns */
	private int prepareHeader() {
		int row = 0;
		
		flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-header");
		
		int col = 0;
		if ( quickInfo != null ) {
			flexPanel.setWidget(row, col, quickInfoHeaderHtml.getWidget());
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
		}
		
		flexPanel.setWidget(row, col, ontologyUriHeaderHtml.getWidget());
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		flexPanel.setWidget(row, col, nameHeaderHtml.getWidget());
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;
		
		flexPanel.setWidget(row, col, authorHeaderHtml.getWidget());
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		flexPanel.setWidget(row, col, versionHeaderHtml.getWidget());
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		if ( isAdmin ) {
			flexPanel.setWidget(row, col, submitterHeaderHtml.getWidget());
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
		}

		row++;
		
		return col + 1;
	}
	
	/**
	 * Sets the ontologies to be displayed in the table.
	 * 
	 * @param ontologyInfos
	 * @param loginResult
	 */
	public void setOntologyInfos(final List<RegisteredOntologyInfo> ontologyInfos, LoginResult loginResult) {
		this.ontologyInfos = ontologyInfos;
		this.isAdmin = loginResult != null && loginResult.isAdministrator();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Collections.sort(ontologyInfos, cmp);
				update();
			}
		});
	}
	
	/**
	 * Performs the update of the table with the given information.
	 */
	private void update() {
		
		clear();
		prepareHeader();
		int row = 1;
		
		for ( RegisteredOntologyInfo oi : ontologyInfos ) {
			flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-row");
			
			String name = _getName(oi);
			String uri = _getUri(oi);
			String author = _getAuthor(oi);
			String version = _getVersion(oi);
			
			String tooltip = uri;
			String link = uri;

			if ( includeVersionInLinks ) {
				link += "?version=" +version;
				tooltip += "   \nversion: " +version;
			}
			
			Widget nameWidget;
			
			Widget nameLink, uriLink;
			if (Orr.rUri != null) {
				if (!Orr.isOntResolvableUri(link)) {
					link = Orr.getPortalBaseInfo().getOntServiceUrl() + "?uri=" + link;
				}
				nameLink = new Anchor(name, link);
				uriLink = new Anchor(uri, link);
			}
			else {
				nameLink = new Hyperlink(name, link);
				uriLink = new Hyperlink(uri, link);
			}
			
			boolean isTesting = OrrUtil.isTestingOntology(oi);
			boolean isInternal = isTesting ? false : OrrUtil.isInternalOntology(oi);
			if ( isTesting || isInternal ) {
				// add a mark
				HorizontalPanel hp = new HorizontalPanel();
				hp.add(nameLink);
				HTML html = new HTML(isTesting ? TESTING_ONT_MARK: INTERNAL_ONT_MARK);
				html.setTitle(isTesting ? TESTING_ONT_TOOLTIP : INTERNAL_ONT_TOOLTIP);
				hp.add(html);
				nameWidget = hp;
			}
			else {
				nameWidget = nameLink;
			}

			nameWidget.setTitle(tooltip);

			if ( clickListenerToHyperlinks != null && nameLink instanceof Hyperlink) {
				((Hyperlink) nameLink).addClickListener(clickListenerToHyperlinks);
				// issue #257:"version selection window remains open"
				// the listener was not added to the uri. Fixed.
				((Hyperlink) uriLink).addClickListener(clickListenerToHyperlinks);
			}
				
			int col = 0;
			if ( quickInfo != null ) {
				String quickInfoName = "" +row;
				Widget widget = quickInfo.getWidget(quickInfoName, oi, includeVersionInLinks, !isVersionsTable);
				flexPanel.setWidget(row, col, widget);
				flexPanel.getFlexCellFormatter().setAlignment(row, col, 
						HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
				);
				col++;
			}
			
			flexPanel.setWidget(row, col, uriLink);
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
				
			flexPanel.setWidget(row, col, nameWidget);
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
			
			flexPanel.setWidget(row, col, new Label(author));
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
				
			flexPanel.setWidget(row, col, new Label(version));
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
			
			if ( isAdmin ) {
				flexPanel.setWidget(row, col, new Label(_getUsername(oi)));
				flexPanel.getFlexCellFormatter().setAlignment(row, col, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
				);
			}


			row++;
		}

	}
	
}
