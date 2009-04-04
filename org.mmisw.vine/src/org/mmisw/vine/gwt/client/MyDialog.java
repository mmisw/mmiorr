package org.mmisw.vine.gwt.client;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * Helps to display a panel in a dialog box.
 *  * (a verbatim copy of the same class from ontmd project)
 * 
 * 
 * @author Carlos Rueda
 */
public class MyDialog extends DialogBox {
	
	private DockPanel dockPanel = new DockPanel();
	private CellPanel buttons = createButtons();
	private HorizontalPanel hp = new HorizontalPanel();
	
	private PushButton closeButton;
	private TextArea ta;

	
	MyDialog(Widget contents) {
		this(contents, true);
	}
	
	MyDialog(Widget contents, boolean modal) {
		super(false, modal);
		setAnimationEnabled(true);
		Grid grid = new Grid(1,1);
		grid.setWidget(0, 0, dockPanel);
		setWidget(grid);
		
		hp.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.add(buttons);

		if ( contents != null ) {
			dockPanel.add(contents, DockPanel.CENTER);
		}
		dockPanel.add(hp, DockPanel.SOUTH);
		
		dockPanel.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_RIGHT);
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

	public boolean onKeyUpPreview(char key, int modifiers) {
		if ( key == KeyboardListener.KEY_ESCAPE
		||  key == KeyboardListener.KEY_ENTER ) {
			hide();
			return false;
		}
	    return true;
	  }
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);

		closeButton = new PushButton("Close", new ClickListener() {
			public void onClick(Widget sender) {
				MyDialog.this.hide();
			}
		});
		panel.add(closeButton);

		return panel;
	}
	
	@Override
	public void show() {
		if ( closeButton != null ) {
			closeButton.setFocus(true);
		}
		else if ( ta != null ) {
			ta.setFocus(true);
		}
		super.show();
	}
}

