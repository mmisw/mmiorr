package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.util.Util;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for searching terms. TODO implement
 * @author Carlos Rueda
 */
public class SearchTermsPanel extends VerticalPanel {
	
	private TextBoxBase textBox;
	private PushButton searchButton;
	
	private final VerticalPanel resultsPanel = new VerticalPanel();
	
	
	/**
	 * Creates a field with a choose feature.
	 * @param attr
	 * @param cl
	 */
	public SearchTermsPanel() {
		int nl = 1;    /// attr.getNumberOfLines() is ignored
		textBox = Util.createTextBoxBase(nl, "200", null);
		textBox.addKeyboardListener(new KeyboardListenerAdapter() {
			public void onKeyUp(Widget sender, char keyCode, int modifiers) {
				if ( keyCode == KEY_ENTER ) {
					_doSearch();
				}
			}
		});

		
		HorizontalPanel hp = new HorizontalPanel();
		add(hp);
		
		hp.add(textBox);
		
		searchButton = new PushButton("Search", new ClickListener() {
			public void onClick(Widget sender) {
				_doSearch();
			}
		});
		
		hp.add(searchButton);
		
		resultsPanel.setBorderWidth(1);
		add(resultsPanel);
	}

	
	/**
	 * dispatches the selection of an option.
	 */
	private void _doSearch() {
		
		enable(false);
		resultsPanel.clear();
		resultsPanel.add(new HTML("SEARCHING ..."));
		
		
		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET,
				"http://mmisw.org/ont?listall");
		try {
			requestBuilder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					resultsPanel.clear();
					resultsPanel.add(new Label("ERROR: " +exception.getMessage()));
				}

				public void onResponseReceived(Request request, Response response) {
					resultsPanel.clear();
					resultsPanel.add(new Label(response.getText()));

				}
			});
		}
		catch (RequestException ex) {
			resultsPanel.clear();
			resultsPanel.add(new Label("RequestException: " +ex.getMessage()));
		}
	}
	
	private void enable(boolean enabled) {
		textBox.setReadOnly(!enabled);
//		lb.setEnabled(enabled);
		searchButton.setEnabled(enabled);
	}

	public void setValue(String value) {
		textBox.setText(value);
//		lb.setSelectedIndex(0);
	}
	
	public String getSearchString() {
		return textBox.getText();
	}

}
