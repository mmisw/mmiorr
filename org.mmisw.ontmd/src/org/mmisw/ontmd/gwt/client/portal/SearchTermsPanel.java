package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.iserver.gwt.client.rpc.SparqlQueryInfo;
import org.mmisw.iserver.gwt.client.rpc.SparqlQueryResult;
import org.mmisw.ontmd.gwt.client.Orr;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for searching terms. Includes link to SPARQL page.
 * 
 * TODO eventually include general SPARQL dispatch here.
 * 
 * @author Carlos Rueda
 */
public class SearchTermsPanel extends VerticalPanel {
	
	// these are static to "remember" values
	private static final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("|");
	private static final TextBox textBox = new TextBox();
	private static final SuggestBox suggestBox = new SuggestBox(oracle, textBox);

	private PushButton searchButton;
	
	
	private static class ResultsPanel {
		private final VerticalPanel panel = new VerticalPanel();

		Widget getWidget() {
			return panel;
		}

		void searching() {
			panel.setBorderWidth(0);
			panel.clear();
			panel.add(new HTML("<img src=\"" +GWT.getModuleBaseURL()+ "images/loading.gif\"> " +
					"<i>searching ...</i>"));
		}

		void error(String error) {
			panel.setBorderWidth(0);
			panel.clear();
			panel.add(new Label("ERROR: " +error));
		}

		void setHtml(String result) {
//			panel.setBorderWidth(1);
			panel.clear();
			panel.add(new HTML(result));
		}
		
	}
	
	private ResultsPanel resultsPanel = new ResultsPanel();
	
	
	/**
	 * Creates a field with a choose feature.
	 * @param attr
	 * @param cl
	 */
	public SearchTermsPanel() {
		
		super.setSpacing(5);
		
		textBox.setWidth("250px");

		textBox.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if ( searchButton.isEnabled() && keyCode == KEY_ENTER ) {
					_doSearch();
				}
			}
		});
		
		add(new HTML("<h2>Keyword Search</h2>"));
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(ALIGN_MIDDLE);
		hp.setSpacing(5);
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(hp);
	    add(decPanel);

		hp.add(new Label("Search terms containing:"));
		hp.add(suggestBox);
		
		searchButton = new PushButton(Orr.images.search().createImage(), new ClickListener() {
			public void onClick(Widget sender) {
				_doSearch();
			}
		});
		
		hp.add(searchButton);
		hp.add(new Label("Use OR to separate alternative keywords"));
		
		add(resultsPanel.getWidget());
		
		new Timer() {
			@Override
			public void run() {
				suggestBox.setFocus(true);
			}
		}.schedule(300);
	}

	
	private void _doSearch() {
		
		final String searchString = getSearchString();
		if ( searchString.length() == 0) {
			return;
		}
		
		final String keywords = searchString.replaceAll("\\s+(o|O)(r|R)\\s+", "|");
		
		// TODO some paging mechanism
		String queryString = "SELECT DISTINCT ?subject ?predicate ?object " +
				"WHERE { ?subject ?predicate ?object. " +
				"FILTER regex(?object, \"" +keywords+ "\", \"i\" ) } " +
				"ORDER BY ?subject";
		
		SparqlQueryInfo query = new SparqlQueryInfo();
		query.setQuery(queryString);
		query.setFormat("html-frag");
		
		enable(false);
		resultsPanel.searching();
		
		AsyncCallback<SparqlQueryResult> callback = new AsyncCallback<SparqlQueryResult>() {
			public void onFailure(Throwable exception) {
				enable(true);
				String error = exception.getMessage();
				Orr.log("Search failure: " + error);
				resultsPanel.error(error);
			}

			public void onSuccess(SparqlQueryResult result) {
				enable(true);
				if ( result.getError() != null ) {
					String error = result.getError();
					Orr.log("Search error: " + error);
					resultsPanel.error(error);	
				}
				else {
					String[] kwArray = keywords.split("\\|");
					if ( kwArray.length > 1 ) {
						for ( String kw : kwArray ) {
							oracle.add(kw);
						}
					}
					oracle.add(searchString);

					resultsPanel.setHtml(result.getResult());
				}
			}
		};
		Orr.log("Searching. query: " +query.getQuery());
		Orr.service.runSparqlQuery(query, callback);
	}
	
	private void enable(boolean enabled) {
		textBox.setReadOnly(!enabled);  
		searchButton.setEnabled(enabled);
	}

	private String getSearchString() {
		return suggestBox.getText().trim();
	}

}
