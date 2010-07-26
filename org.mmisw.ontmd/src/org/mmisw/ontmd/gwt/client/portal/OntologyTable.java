package org.mmisw.ontmd.gwt.client.portal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.ontmd.gwt.client.util.TooltipIcon;
import org.mmisw.ontmd.gwt.client.util.Util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class OntologyTable extends FlexTable {
	private static String _mark(String mk) { 
		return "<sup><font color=\"red\" size=\"-2\">" +mk+ "</font></sup>";
	}
	private static final String TESTING_ONT_MARK = _mark("(T)");
	private static final String TESTING_ONT_TOOLTIP = "A testing ontology";

	private static final String INTERNAL_ONT_MARK = _mark("(int)");
	private static final String INTERNAL_ONT_TOOLTIP = "An internal ontology";
	
// TODO Use utility ViewTable 
	
	
	static interface IQuickInfo {
		/**
		 * 
		 * @param name  Used to show a label (in particular, for numbering)
		 * @param oi
		 * @param includeVersionInLinks
		 * @param includeVersionsMenu
		 * @return
		 */
		Widget getWidget(String name, RegisteredOntologyInfo oi, boolean includeVersionInLinks, boolean includeVersionsMenu);
	}
	
	private IQuickInfo quickInfo;
	
	private boolean isVersionsTable;
	

	private List<RegisteredOntologyInfo> ontologyInfos;
	
	private boolean isAdmin = false;
	
	private final FlexTable flexPanel = this;
	
	
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
				hp2.add(Main.images.tridown().createImage());
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

	// #209: list of ontologies ordered by time of registration; most recent first
	private String sortColumn = "version";
	private int sortFactor = -1;    // -1=down   +1:up
	
	private Comparator<RegisteredOntologyInfo> cmp = new Comparator<RegisteredOntologyInfo>() {
		public int compare(RegisteredOntologyInfo o1, RegisteredOntologyInfo o2) {
			String s1 = o1.getDisplayLabel();
			String s2 = o2.getDisplayLabel();
			if ( sortColumn.equalsIgnoreCase("name") ) {
				s1 = o1.getDisplayLabel();
				s2 = o2.getDisplayLabel();
			}
			else if ( sortColumn.equalsIgnoreCase("author") ) {
				s1 = o1.getContactName();
				s2 = o2.getContactName();
			}
			else if ( sortColumn.equalsIgnoreCase("uri") ) {
				s1 = o1.getUri();
				s2 = o2.getUri();
			}
			else if ( sortColumn.equalsIgnoreCase("version") ) {
				s1 = o1.getVersionNumber();
				s2 = o2.getVersionNumber();
			}
			else if ( sortColumn.equalsIgnoreCase("submitter") ) {
				s1 = o1.getUsername();
				s2 = o2.getUsername();
			}
			else {
				s1 = o1.getDisplayLabel();
				s2 = o2.getDisplayLabel();
			}
			
			return sortFactor * s1.compareToIgnoreCase(s2);
		}
	};

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

	
	/** given by the user */
	private ClickListener clickListenerToHyperlinks;
	
	
	private boolean includeVersionInLinks = false;
	
	
	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	OntologyTable(IQuickInfo quickInfo, boolean isVersionsTable) {
		super();
		this.quickInfo = quickInfo;
		this.isVersionsTable = isVersionsTable;
		
		flexPanel.setBorderWidth(1);
		flexPanel.setCellPadding(3);
		flexPanel.setWidth("100%");
		flexPanel.setStylePrimaryName("OntologyTable");
		
		prepareHeader();
	}
	
	/**
	 * By default, the version is not included in the hyperlinks.
	 * 
	 * @param includeVersionInLinks true to include version in the hyperlinks.
	 */
	public void setIncludeVersionInLinks(boolean includeVersionInLinks) {
		this.includeVersionInLinks = includeVersionInLinks;
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


	/**
	 * For subsequent creation of the entries in the table, the given listener will be
	 * associated to the corresponding hyperlinks. So, call this before {@link #setOntologyInfos(List, LoginResult)}.
	 * @param clickListenerToHyperlinks
	 */
	public void addClickListenerToHyperlinks(ClickListener clickListenerToHyperlinks) {
		this.clickListenerToHyperlinks = clickListenerToHyperlinks;
	}

	public void setQuickInfo(IQuickInfo quickInfo) {
		this.quickInfo = quickInfo;
	}

	public void clear() {
		super.clear();
		while ( getRowCount() > 0 ) {
			removeRow(0);
		}
	}
	
	void showProgress() {
		flexPanel.clear();
		prepareHeader();
		int row = 1;
		flexPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
		flexPanel.setWidget(row, 0, new HTML("<i>one moment...</i>"));
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

	}

	private void prepareHeader() {
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
	}
	
	/**
	 * Sets the ontologies to be displayed in the table.
	 * 
	 * @param ontologyInfos
	 * @param loginResult
	 */
	void setOntologyInfos(final List<RegisteredOntologyInfo> ontologyInfos, LoginResult loginResult) {
		this.ontologyInfos = ontologyInfos;
		this.isAdmin = loginResult != null && loginResult.isAdministrator();
		
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				Collections.sort(ontologyInfos, cmp);
				update();
			}
		});
	}
	
	private void update() {
		
		flexPanel.clear();
		prepareHeader();
		int row = 1;
		
		for ( RegisteredOntologyInfo oi : ontologyInfos ) {
			flexPanel.getRowFormatter().setStylePrimaryName(row, "OntologyTable-row");
			
			String name = _getName(oi);
			String uri = _getUri(oi);
			String author = _getAuthor(oi);
			String version = _getVersion(oi);
			
			String tooltip = uri;
			String historyToken = uri;
			
			if ( includeVersionInLinks ) {
				historyToken += "?version=" +version;
				tooltip += "   \nversion: " +version;
			}
			
			Widget nameWidget;
			
			Hyperlink nameLink = new Hyperlink(name, historyToken);
			Hyperlink uriLink = new Hyperlink(uri, historyToken);
			
			boolean isTesting = Util.isTestingOntology(oi);
			boolean isInternal = isTesting ? false : Util.isInternalOntology(oi);
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

			if ( clickListenerToHyperlinks != null ) {
				nameLink.addClickListener(clickListenerToHyperlinks);
				// issue #257:"version selection window remains open"
				// the listener was not added to the uri. Fixed.
				uriLink.addClickListener(clickListenerToHyperlinks);
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
	
	/////////////////////////////////////////////////////////
	// methods to get values for the columns
	
	private static String _getName(RegisteredOntologyInfo oi) {
		return oi.getDisplayLabel();
	}
	private static String _getUri(RegisteredOntologyInfo oi) {
		return oi.getUri();
	}
	private static String _getVersion(RegisteredOntologyInfo oi) {
		return oi.getVersionNumber();
	}
	private static String _getUsername(RegisteredOntologyInfo oi) {
		return oi.getUsername();
	}
	private static String _getAuthor(RegisteredOntologyInfo oi) {
//		Re. issue #236 "Author column should show Content Creator"
//		NOTE: a possibility would be to use the OntologyMetadata object associated with
//		the RegisteredOntologyInfo, something like:
//		   return oi.getOntologyMetadata().getOriginalValues().get("http://mmisw.org/ont/mmi/20081020/ontologyMetadata/hasContentCreator");
//		BUT such metadata is not available at this point.
//		So, here we just continue to return the contactName value.
//
		String author = oi.getContactName();
		return author;
	}
}
