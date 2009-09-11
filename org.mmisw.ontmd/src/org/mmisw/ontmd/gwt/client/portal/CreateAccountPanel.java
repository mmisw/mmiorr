package org.mmisw.ontmd.gwt.client.portal;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for creating a user account.
 * 
 * @author Carlos Rueda
 */
public class CreateAccountPanel extends VerticalPanel {
	
	private PushButton createButton = new PushButton("Create", new ClickListener() {
		public void onClick(Widget sender) {
			_doCreateAccount();
		}
	});
	
	private final VerticalPanel resultsPanel = new VerticalPanel();
	
	
	/**
	 * Creates a field with a choose feature.
	 * @param attr
	 * @param cl
	 */
	public CreateAccountPanel() {
		
		super.setSpacing(5);
		
		add(new HTML("<h2>Create account</h2>"));
		
		HorizontalPanel hp = new HorizontalPanel();
		hp.setVerticalAlignment(ALIGN_MIDDLE);
		hp.setSpacing(5);
		
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(hp);
	    add(decPanel);

		hp.add(new Label("TODO:"));
		
		
		
		hp.add(createButton);
		
		resultsPanel.setBorderWidth(1);
		add(resultsPanel);
		
		new Timer() {
			@Override
			public void run() {
//				suggestBox.setFocus(true);
			}
		}.schedule(300);
	}

	
	private void _doCreateAccount() {
		

	}
	
	private void enable(boolean enabled) {
//		textBox.setReadOnly(!enabled);  
		createButton.setEnabled(enabled);
	}

}
