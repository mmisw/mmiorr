package org.mmisw.ontmd.gwt.client.util;

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
 * 
 * @author Carlos Rueda
 */
public class MyDialog extends DialogBox {
	
	private DockPanel dockPanel = new DockPanel();
	private HorizontalPanel buttonsPanel;
	private HorizontalPanel hp = new HorizontalPanel();
	
	private PushButton closeButton;
	private TextArea ta;

	
	public MyDialog(Widget contents) {
		this(contents, true);
	}
	
	public MyDialog(Widget contents, boolean buttons) {
		super(false, true);
		setAnimationEnabled(true);
		Grid grid = new Grid(1,1);
		grid.setWidget(0, 0, dockPanel);
		setWidget(grid);
		
		if ( buttons ) {
			buttonsPanel = createButtonsPanel();
			hp.setCellHorizontalAlignment(buttonsPanel, HasHorizontalAlignment.ALIGN_RIGHT);
			hp.add(buttonsPanel);
		}

		if ( contents != null ) {
			dockPanel.add(contents, DockPanel.CENTER);
		}
		dockPanel.add(hp, DockPanel.SOUTH);
		
		dockPanel.setCellHorizontalAlignment(hp, HasHorizontalAlignment.ALIGN_RIGHT);
	}
	
	public DockPanel getDockPanel() {
		return dockPanel;
	}
	
	/** convenience method */
	public TextArea addTextArea(TextArea ta) {
		if ( ta == null ) {
			ta = new TextArea();
		}
		this.ta = ta;
		ta.setSize("720", "450");
		ta.setReadOnly(true);
		dockPanel.add(ta, DockPanel.CENTER);
		
		return ta;
	}
	
	public TextArea getTextArea() {
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
	
	private HorizontalPanel createButtonsPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(4);

		closeButton = new PushButton("Close", new ClickListener() {
			public void onClick(Widget sender) {
				MyDialog.this.hide();
			}
		});
		panel.add(closeButton);

		return panel;
	}
	
	public void setCloseButtonText(String text) {
		if ( closeButton != null ) {
			closeButton.setText(text);
		}
	}
	
	public HorizontalPanel getButtonsPanel() {
		return buttonsPanel;
	}
	
	@Override
	public void show() {
		if ( ta != null && ! ta.isReadOnly() ) {
			ta.setFocus(true);
		}
		else if ( closeButton != null ) {
			closeButton.setFocus(true);
		}
		else if ( ta != null ) {
			ta.setFocus(true);
		}
		super.show();
	}
}

