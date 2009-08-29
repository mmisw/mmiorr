package org.mmisw.ontmd.gwt.client.vine.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * A utility Panel providing "All" and "None", and provided buttons.
 * 
 * @author Carlos Rueda
 */
public class SelectAllNonePanel extends HorizontalPanel {
	
	/**
	 * @param btns string for additional buttons
	 */
	public SelectAllNonePanel(String... btns) {
		super();
		
		Label label = new Label("Select: ");
		add(label);
		
		PushButton all = new PushButton("All", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(true);
			}
		});
		DOM.setElementAttribute(all.getElement(), "id", "my-button-id");
		add(all);

		PushButton none = new PushButton("None", new ClickListener() {
			public void onClick(Widget sender) {
				updateAllNone(false);
			}
		});
		DOM.setElementAttribute(none.getElement(), "id", "my-button-id");
		add(none);

		for ( final String str : btns ) {
			PushButton pb = new PushButton(str, new ClickListener() {
				public void onClick(Widget sender) {
					clientButtonClicked(str);
				}
			});
			DOM.setElementAttribute(pb.getElement(), "id", "my-button-id");
			add(pb);
		}
	}

	/** Called when one of the buttons is clicked to notified the selection desored */
	protected void updateAllNone(boolean selected) {
	}
	
	/** Called when one of the client buttons is clicked */
	protected void clientButtonClicked(String str) {
	}
	
}
