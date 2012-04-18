package org.mmisw.orrportal.gwt.client.util.table.ontab;

import java.util.Comparator;
import java.util.List;

import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrportal.gwt.client.util.table.IOntologyTable;
import org.mmisw.orrportal.gwt.client.util.table.IQuickInfo;

import com.google.gwt.user.client.ui.ClickListener;


abstract class BaseOntologyTable implements IOntologyTable {
	private static String _mark(String mk) { 
		return "<sup><font color=\"red\" size=\"-2\">" +mk+ "</font></sup>";
	}
	protected static final String TESTING_ONT_MARK = _mark("(T)");
	protected static final String TESTING_ONT_TOOLTIP = "A testing ontology";

	protected static final String INTERNAL_ONT_MARK = _mark("(int)");
	protected static final String INTERNAL_ONT_TOOLTIP = "An internal ontology";

	
	protected IQuickInfo quickInfo;
	
	protected boolean isVersionsTable;
	

	protected List<RegisteredOntologyInfo> ontologyInfos;
	
	protected boolean isAdmin = false;

	
	/** given by the user */
	protected ClickListener clickListenerToHyperlinks;
	
	
	protected boolean includeVersionInLinks = false;
	
	
	// #209: list of ontologies ordered by time of registration; most recent first
	protected String sortColumn = "version";
	protected int sortFactor = -1;    // -1=down   +1:up
	
	protected Comparator<RegisteredOntologyInfo> cmp = new Comparator<RegisteredOntologyInfo>() {
		public int compare(RegisteredOntologyInfo o1, RegisteredOntologyInfo o2) {
			String s1, s2;
			if ( sortColumn.equalsIgnoreCase("version") ) {
				s1 = _getVersion(o1);
				s2 = _getVersion(o2);
			}
			else if ( sortColumn.equalsIgnoreCase("name") ) {
				s1 = _getName(o1);
				s2 = _getName(o2);
			}
			else if ( sortColumn.equalsIgnoreCase("author") ) {
				s1 = _getAuthor(o1);
				s2 = _getAuthor(o2);
			}
			else if ( sortColumn.equalsIgnoreCase("uri") ) {
				s1 = _getUri(o1);
				s2 = _getUri(o2);
			}
			else if ( sortColumn.equalsIgnoreCase("submitter") ) {
				s1 = _getUsername(o1);
				s2 = _getUsername(o2);
			}
			else {
				s1 = _getName(o1);
				s2 = _getName(o2);
			}
			
			return sortFactor * s1.compareToIgnoreCase(s2);
		}
	};

	
	/**
	 * 
	 * @param quickInfo
	 * @param isVersionsTable
	 */
	protected BaseOntologyTable(IQuickInfo quickInfo, boolean isVersionsTable) {
		super();
		this.quickInfo = quickInfo;
		this.isVersionsTable = isVersionsTable;
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


	
	/////////////////////////////////////////////////////////
	// methods to get values for the columns
	
	protected static String _getName(RegisteredOntologyInfo oi) {
		return oi.getDisplayLabel();
	}
	protected static String _getUri(RegisteredOntologyInfo oi) {
		return oi.getUri();
	}
	protected static String _getVersion(RegisteredOntologyInfo oi) {
		return oi.getVersionNumber();
	}
	protected static String _getUsername(RegisteredOntologyInfo oi) {
		return oi.getUsername();
	}
	protected static String _getAuthor(RegisteredOntologyInfo oi) {
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
