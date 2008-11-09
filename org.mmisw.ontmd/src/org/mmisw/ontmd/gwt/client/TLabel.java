package org.mmisw.ontmd.gwt.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;


/**
 * A label with a manual tooltip handling: User will click an icon for the
 * tootip text to be displayed. KEY_ENTER, KEY_ESCAPE, and click make the popup disappear.
 * 
 * @author Carlos Rueda
 */
class TLabel extends HorizontalPanel {
	
	/** HTML snipped for the required indicator */
	static final String requiredHtml = "<font color=\"red\">*</font>";
	
	private HTML label = new HTML();
	
	// Used if a tooltip is actually associated:
	
	private Image ttIcon;
	private DecoratedPopupPanel popup;
	private ClickListener clickListener;


	/**
	 * Creates a TLabel with the given text and no tool tip.
	 * @param text
	 */
	TLabel(String text) {
		this(text, null);
	}
	
	/**
	 * Creates a TLabel with the given label text and associated tool tip. 
	 * @param text
	 * @param tooltip
	 */
	TLabel(String text, String tooltip) {
		this(text, false, tooltip);
	}
	
	
	/**
	 * Creates a TLabel with the given label text and associated tool tip. 
	 * @param text
	 * @param required true to add an indicator tha the associated field is required.
	 * @param tooltip
	 */
	TLabel(String text, boolean required, String tooltip) {
		setSpacing(4);
		if ( !text.endsWith(":") ) {
			text += ":";
		}
		label.setHTML(text + (required ? requiredHtml : ""));
//		this.setWidget(0, 1, label);
//		this.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
		this.add(label);
		if ( tooltip != null && tooltip.length() > 0 ) {
			setTooltip(tooltip);
		}
	}
	
	
	/**
	 * Associates a tooltip to this label.
	 * @param tooltip The tool tip.
	 * @return this object.
	 */
	TLabel setTooltip(String tooltip) {
		if ( ttIcon == null ) {
			ttIcon = Main.images.question12().createImage();
		}
		
		if ( popup == null ) {
			popup = new DecoratedPopupPanel(true) {
				public boolean onKeyUpPreview(char key, int modifiers) {
					if ( key == KeyboardListener.KEY_ESCAPE
					||  key == KeyboardListener.KEY_ENTER ) {
						hide();
						return false;
					}
				    return true;
				  }
			};
//		    simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
//		    popup.setWidth("400px");
		}
		else {
			popup.clear();
		}
		HTML widget = new HTML(tooltip);
		Grid grid = new Grid(1,1);
		grid.setWidget(0, 0, widget);
		grid.setBorderWidth(1);  // just to improve appearance in firefox
		grid.setCellPadding(10);
		grid.setWidth("100%");
		popup.setWidget(grid);
		widget.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				popup.hide();
			}
		});
		

		if ( clickListener == null ) {
			clickListener = new ClickListener() {
				public void onClick(Widget sender) {
					int left = sender.getAbsoluteLeft() + 10;
					int top = sender.getAbsoluteTop() + 20;
					popup.setPopupPosition(left, top);
					popup.show();
				}
			};
		}
		else {
			ttIcon.removeClickListener(clickListener);
			label.removeClickListener(clickListener);
		}

//		this.setWidget(0, 0, ttIcon);
//		this.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
		this.clear();
		this.add(ttIcon);
		this.add(label);
		
		ttIcon.addClickListener(clickListener);
		label.addClickListener(clickListener);
		
		return this;
	}
	

}
