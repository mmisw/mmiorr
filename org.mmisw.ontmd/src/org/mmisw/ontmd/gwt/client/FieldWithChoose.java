package org.mmisw.ontmd.gwt.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.vocabulary.AttrDef;
import org.mmisw.ontmd.gwt.client.vocabulary.Option;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestionEvent;
import com.google.gwt.user.client.ui.SuggestionHandler;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Carlos Rueda
 */
public class FieldWithChoose  extends HorizontalPanel {
	AttrDef attr;
	TextBoxBase textBox;
	PushButton chooseButton;
	ChangeListener cl;
	
	FieldWithChoose(AttrDef attr, ChangeListener cl) {
		this.attr = attr;
		this.cl = cl;
		int nl = 1;    /// attr.getNumberOfLines() is ignored
		textBox = Util.createTextBoxBase(nl, "400", cl);

		add(textBox);
		
		chooseButton = new PushButton("Choose", new ClickListener() {
			public void onClick(Widget sender) {
				choose();
			}
		});
		
		add(chooseButton);
	}

	/** nothing done here */
	protected void optionSelected(Option option) {
	}

	
	/**
	 * dispatches the selection of an option.
	 */
	private void choose() {
		
		final MyDialog waitPopup = new MyDialog(Util.createHtml("Getting options ...", 12),
				false);   // No "Close" button
		waitPopup.setText("Please wait");
		waitPopup.center();
		waitPopup.show();

		String optionsVocab = attr.getOptionsVocabulary();
		if ( optionsVocab != null ) {
			Main.refreshOptions(attr, new AsyncCallback<AttrDef>() {
				public void onFailure(Throwable thr) {
					String error = thr.toString();
					while ( ( thr = thr.getCause()) != null ) {
						error += "\n" + thr.toString();
					}
					waitPopup.hide();
					Window.alert(error);
				}

				public void onSuccess(AttrDef result) {
					dispatchOptions(result.getOptions(), waitPopup);
				}
			});
		}
		else {
			dispatchOptions(attr.getOptions(), waitPopup);
		}
	}
	
	private void dispatchOptions(final List<Option> options, final MyDialog waitPopup) {
		Main.log("Dispatching options");
		
		final String width = "500px";
		
		final ListBox listBox = Util.createListBox(options, cl);
		listBox.setWidth(width);
		
		VerticalPanel vp = new VerticalPanel();
		
		final MyDialog popup = new MyDialog(vp);
		
		listBox.setVisibleItemCount(Math.min(options.size(), 12));

		listBox.addChangeListener(new ChangeListener () {
			public void onChange(Widget sender) {
				String value = listBox.getValue(listBox.getSelectedIndex());
				textBox.setText(value);
				
				Option option = options.get(listBox.getSelectedIndex());
				optionSelected(option);
				popup.hide();
			}
		});
		
		/////////////////////////////////////////////////////////
		// Use a SuggestBox wirh a MultiWordSuggestOracle.
		//
		// A map from a suggestion to its corresponding Option:
		final Map<String,Option> suggestions = new HashMap<String,Option>();
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("/ :-"); 
		for ( Option option : options ) {
			String suggestion = option.getName()+ " - " +option.getUri();
			suggestions.put(suggestion, option);
			oracle.add(suggestion);

		}
		final SuggestBox suggestBox = new SuggestBox(oracle);
		suggestBox.setWidth(width);
		suggestBox.addEventHandler(new SuggestionHandler() {
			public void onSuggestionSelected(SuggestionEvent event) {
				String suggestion = event.getSelectedSuggestion().getReplacementString();
				Option option = suggestions.get(suggestion);
				textBox.setText(option.getName());
				optionSelected(option);
				popup.hide();
			}
		});
		////////////////////////////////////////////////////////////
		
		vp.add(suggestBox);
		vp.add(listBox);
		
		waitPopup.hide();
		
		// use a timer to request for focus in the suggest-box:
		new Timer() {
			public void run() {
				suggestBox.setFocus(true);
			}
		}.schedule(700);
		    
		popup.setText("Select " +attr.getLabel());
		popup.center();
		popup.show();

	}

	void enable(boolean enabled) {
		textBox.setEnabled(enabled);
//		lb.setEnabled(enabled);
		chooseButton.setEnabled(enabled);
	}

	public void setValue(String value) {
		textBox.setText(value);
//		lb.setSelectedIndex(0);
	}
	
	public String getValue() {
		return textBox.getText();
	}
}
