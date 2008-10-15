package org.mmisw.voc2rdf.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class MyDialog extends DialogBox {
	
	MyDialog(Panel contents) {
		super(false, true);
		setAnimationEnabled(true);
		
		DockPanel panel = new DockPanel();
		setWidget(panel);

		panel.add(contents, DockPanel.CENTER);
		
		CellPanel buttons = createButtons();
		HorizontalPanel hp = new HorizontalPanel();
		hp.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(buttons);
		panel.add(hp, DockPanel.SOUTH);
	}
	
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		PushButton closeButton = new PushButton("Close", new ClickListener() {
			public void onClick(Widget sender) {
				MyDialog.this.hide();
			}
		});
		panel.add(closeButton);

		return panel;
	}

}
