package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * A utility Panel providing "All" and "None" (and perhaps others later) buttons.
 * 
 * @author Carlos Rueda
 */
abstract class SelectAllNonePanel extends HorizontalPanel {
	
	SelectAllNonePanel() {
		super();
		
		Label label = new Label("Select: ");
		
		PushButton all = new PushButton("All", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(true);
			}
		});
		DOM.setElementAttribute(all.getElement(), "id", "my-button-id");
		PushButton none = new PushButton("None", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(false);
			}
		});
		DOM.setElementAttribute(none.getElement(), "id", "my-button-id");
		
		add(label);
		add(all);
		add(none);
	}

	/** Called when one of the buttons is clicked to notified the selection desored */
	abstract void updateAllNone(boolean selected);
	
}
