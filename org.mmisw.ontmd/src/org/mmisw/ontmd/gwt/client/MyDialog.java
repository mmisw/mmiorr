package org.mmisw.ontmd.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Helps to display a panel in a dialog box.
 * 
 * @author Carlos Rueda
 */
public class MyDialog extends DialogBox {
	
	private DockPanel dockPanel = new DockPanel();
	private CellPanel buttons = createButtons();
	private HorizontalPanel hp = new HorizontalPanel();
	
	private TextArea ta;

	
	MyDialog(Widget contents) {
		super(false, true);
		setAnimationEnabled(true);
		setWidget(dockPanel);
		
		hp.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(buttons);

		if ( contents != null ) {
			dockPanel.add(contents, DockPanel.CENTER);
		}
		dockPanel.add(hp, DockPanel.SOUTH);
	}
	
	DockPanel getDockPanel() {
		return dockPanel;
	}
	
	/** convenience method */
	TextArea addTextArea(TextArea ta) {
		if ( ta == null ) {
			ta = new TextArea();
		}
		this.ta = ta;
		ta.setSize("720", "450");
		ta.setReadOnly(true);
		dockPanel.add(ta, DockPanel.CENTER);
		return ta;
	}
	
	TextArea getTextArea() {
		return ta;
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

