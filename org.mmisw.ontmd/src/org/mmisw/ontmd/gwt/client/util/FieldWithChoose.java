package org.mmisw.ontmd.gwt.client.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ontmd.gwt.client.Main;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.iserver.gwt.client.vocabulary.Option;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ChangeListenerCollection;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
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
public class FieldWithChoose  extends HorizontalPanel implements SourcesChangeEvents {
	AttrDef attr;
	private TextBoxBase textBox;
	PushButton chooseButton;
	ChangeListener cl;
	
	private ChangeListenerCollection changeListeners;
	
	/**
	 * Creates a field with a choose feature.
	 * @param attr
	 * @param cl
	 */
	public FieldWithChoose(AttrDef attr, ChangeListener cl) {
		this(attr, cl, "200px");
	}
	
	/**
	 * Creates a field with a choose feature.
	 * @param attr
	 * @param cl
	 * @param textWidth
	 */
	public FieldWithChoose(AttrDef attr, ChangeListener cl, String textWidth) {
		this.attr = attr;
		this.cl = cl;
		
		addChangeListener(cl);
		
		int nl = 1;    /// attr.getNumberOfLines() is ignored
		textBox = Util.createTextBoxBase(nl, textWidth, cl);
		textBox.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				_onChange();
			}
		});

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

	
	private void _onChange() {
		if (changeListeners != null) {
			changeListeners.fireChange(FieldWithChoose.this);
		}
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
		// make sure no item is selected so we get a change event on the first item (needed for firefox at least):
		// (see issue #139: Can't select AGU as authority abbreviation)
		listBox.setSelectedIndex(-1);

		listBox.addChangeListener(new ChangeListener () {
			public void onChange(Widget sender) {
				String value = listBox.getValue(listBox.getSelectedIndex());
				textBox.setText(value);
				
				Option option = options.get(listBox.getSelectedIndex());
				optionSelected(option);

				_onChange();

				popup.hide();
			}
		});
		
		/////////////////////////////////////////////////////////
		// Use a SuggestBox with a MultiWordSuggestOracle.
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
				
				_onChange();
				
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

	public void enable(boolean enabled) {
		textBox.setReadOnly(!enabled);
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

	public TextBoxBase getTextBox() {
		return textBox;
	}

	public void addChangeListener(ChangeListener listener) {
	    if (changeListeners == null) {
	        changeListeners = new ChangeListenerCollection();
	        sinkEvents(Event.ONCHANGE);
	      }
	      changeListeners.add(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		if (changeListeners != null) {
			changeListeners.remove(listener);
		}
	}
	
}
