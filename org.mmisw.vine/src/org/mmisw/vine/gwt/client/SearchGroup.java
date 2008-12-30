package org.mmisw.vine.gwt.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SearchGroup extends VerticalPanel {
	
	private ResultsForm resultsForm;
	
	private MultiWordSuggestOracle oracle;
	private CheckBox cb;
	private SuggestBox box;

	SearchGroup(ResultsForm resultsForm) {
		super();
		this.resultsForm = resultsForm;
		
		HorizontalPanel hp0 = new HorizontalPanel();
		add(hp0);
		hp0.setSpacing(10);
		hp0.add(new HTML("Search for:"));
		cb = new CheckBox("REGEX");
		cb.setTitle("Check this to apply a regular expression search");
//-		hp0.add(cb);
		
//-		HorizontalPanel hp = new HorizontalPanel();
//-		add(hp);
		
		oracle = new MultiWordSuggestOracle();  
		
		// TODO: update the oracle as the user enters new search strings	
		oracle.add("Cat");
		oracle.add("Dog");
		oracle.add("Horse");
		oracle.add("Canary");
		   
		box = new SuggestBox(oracle);
		box.setWidth("250px");
//-		hp.add(box);
		hp0.add(box);
		
		PushButton b = new PushButton(Main.images.search().createImage());
		box.addChangeListener(new ChangeListener() {
			public void onChange(Widget sender) {
				search();
			}
		});
		b.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				search();
			}
		});
		
//-		hp.add(b);
		hp0.add(b);

		hp0.add(cb);
	}

	private void search() {
		final String text = box.getText().trim();
		
		AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
			public void onFailure(Throwable thr) {
				RootPanel.get().add(new HTML(thr.toString()));
			}

			public void onSuccess(List<String> terms) {
				Main.log("search: retrieved " +terms.size()+ " terms");
				if ( text.length() > 0 ) {
					oracle.add(text);
				}
				// TODO Auto-generated method stub
				
				resultsForm.updateTerms(terms);
			}
		};
		
		// FIXME: not on workingUris but on my selected URIs
		Main.vineService.search(text, Main.workingUris, callback);
		
	}
}
