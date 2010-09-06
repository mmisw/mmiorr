package org.mmisw.orrportal.gwt.client.vine.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * A utility Panel for selection of things. 
 * It includes buttons "All" and "None", as well as client-desired buttons.
 * 
 * @author Carlos Rueda
 */
public class SelectAllNonePanel extends HorizontalPanel {
	
	/**
	 * Creates a Panel with "All" and "None" buttons, along with the given "client" buttons.
	 * 
	 * @param btns string for additional buttons. When  the user click one of these buttons,
	 *        {@link #clientButtonClicked(String)} is called with the name of the button.
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

		if ( btns.length > 0 ) {
			// put some separation:
			Widget filler = new HTML("&nbsp;");
			add(filler);
			
			// add the "client" buttons:
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
	}

	/**
	 * Called when one of the All/None buttons is clicked to notify the selection desired.
	 * Nothing is done is this class. 
	 */
	protected void updateAllNone(boolean selected) {
	}
	
	/** 
	 * Called when one of the client buttons is clicked.
	 * Nothing is done is this class. 
	 */
	protected void clientButtonClicked(String str) {
	}
	
}
