package org.mmisw.ontmd.gwt.client.portal;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ontmd.gwt.client.util.Util;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * ontology table.
 * 
 * @author Carlos Rueda
 */
public class OntologyTable extends FlexTable {
	private static final String TESTING_ONT_MARK = "<b><font color=\"red\" size=\"-1\">T</font></b>";
	private static final String TESTING_ONT_TOOLTIP = "A testing ontology";

// TODO Use utility ViewTable 
	
	
	static interface IQuickInfo {
		Widget getWidget(RegisteredOntologyInfo oi, boolean includeVersionInLinks, boolean includeVersionsMenu);
	}
	
	private IQuickInfo quickInfo;
	
	private boolean isVersionsTable;
	

	private List<RegisteredOntologyInfo> ontologyInfos;
	
	private boolean isAdmin = false;
	
	private final FlexTable flexPanel = this;

	private HTML quickInfoHeaderHtml = new HTML("");
	private HTML nameHeaderHtml = new HTML("Name");
	private HTML ontologyUriHeaderHtml = new HTML("URI");
	private HTML authorHeaderHtml = new HTML("Author");
	private HTML versionHeaderHtml = new HTML("Version");
	private HTML submitterHeaderHtml = new HTML("Submitter");
	
	private String sortColumn = "name";
	
	private int sortFactor = 1;
	
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

	private ClickListener columnHeaderClickListener = new ClickListener() {
		public void onClick(Widget sender) {
			String colName = ((HTML) sender).getText().toLowerCase();
			if ( sortColumn.equalsIgnoreCase(colName) ) {
				sortFactor *= -1;
			}
			else {
				sortColumn = colName;
			}

			showProgress();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					Collections.sort(ontologyInfos, cmp);
					update();
				}
			});
		}
	};

	
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
		
		nameHeaderHtml.addClickListener(columnHeaderClickListener);
		ontologyUriHeaderHtml.addClickListener(columnHeaderClickListener);
		authorHeaderHtml.addClickListener(columnHeaderClickListener);
		versionHeaderHtml.addClickListener(columnHeaderClickListener);
		submitterHeaderHtml.addClickListener(columnHeaderClickListener);
		
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
	 * @param sortColumn
	 * @param increasing
	 */
	public void setSortColumn(String sortColumn, boolean increasing) {
		this.sortColumn = sortColumn;
		this.sortFactor = increasing ? 1 : -1;
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
			flexPanel.setWidget(row, col, quickInfoHeaderHtml);
			flexPanel.getFlexCellFormatter().setAlignment(row, col, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			col++;
		}
		
		flexPanel.setWidget(row, col, ontologyUriHeaderHtml);
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		flexPanel.setWidget(row, col, nameHeaderHtml);
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;
		
		flexPanel.setWidget(row, col, authorHeaderHtml);
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		flexPanel.setWidget(row, col, versionHeaderHtml);
		flexPanel.getFlexCellFormatter().setAlignment(row, col, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		col++;

		if ( isAdmin ) {
			flexPanel.setWidget(row, col, submitterHeaderHtml);
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
			
			String name = oi.getDisplayLabel();
			String uri = oi.getUri();
			String author = oi.getContactName();
			String version = oi.getVersionNumber();
			
			String tooltip = uri;
			String historyToken = uri;
			
			if ( includeVersionInLinks ) {
				historyToken += "?version=" +version;
				tooltip += "   \nversion: " +version;
			}
			
			Widget nameWidget;
			
			Hyperlink hlink = new Hyperlink(name, historyToken);
			if ( Util.isTestingOntology(oi) ) {
				// add a mark
				HorizontalPanel hp = new HorizontalPanel();
				hp.add(hlink);
				HTML html = new HTML(TESTING_ONT_MARK);
				html.setTitle(TESTING_ONT_TOOLTIP);
				hp.add(html);
				nameWidget = hp;
			}
			else {
				nameWidget = hlink;
			}

			if ( clickListenerToHyperlinks != null ) {
				hlink.addClickListener(clickListenerToHyperlinks);
			}
				

			nameWidget.setTitle(tooltip);
			
			Widget uriWidget = new Hyperlink(uri, historyToken);
			
			int col = 0;
			if ( quickInfo != null ) {
				flexPanel.setWidget(row, col, quickInfo.getWidget(oi, includeVersionInLinks, !isVersionsTable));
				flexPanel.getFlexCellFormatter().setAlignment(row, col, 
						HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
				);
				col++;
			}
			
			flexPanel.setWidget(row, col, uriWidget);
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
				flexPanel.setWidget(row, col, new Label(oi.getUsername()));
				flexPanel.getFlexCellFormatter().setAlignment(row, col, 
						HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
				);
			}


			row++;
		}

	}

}
