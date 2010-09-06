package org.mmisw.orrportal.gwt.client.vine.util;

import org.mmisw.orrportal.gwt.client.vine.VineMain;

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
 * tootip text to be displayed. KEY_ENTER and KEY_ESCAPE make the popup disappear.
 * 
 * @author Carlos Rueda
 */
public class TLabel extends HorizontalPanel {
	
	/** HTML snippet for the required indicator */
	public static final String requiredHtml = "<font color=\"red\">*</font>";
	
	private HTML label = new HTML();
	
	// Used if a tooltip is actually associated:
	
	private Image ttIcon;
	private Grid popupWidget = new Grid(1,1);
	private DecoratedPopupPanel popup;
	private ClickListener clickListener;


	/**
	 * Creates a TLabel with the given text and no tool tip.
	 * @param text
	 */
	public TLabel(String text) {
		this(text, null);
	}
	
	/**
	 * Creates a TLabel with the given label text and associated tool tip. 
	 * @param text
	 * @param tooltip
	 */
	public TLabel(String text, String tooltip) {
		this(text, false, tooltip);
	}
	
	
	/**
	 * Creates a TLabel with the given label text and associated tooltip. 
	 * @param text
	 * @param required true to add an indicator that the associated field is required.
	 * @param tooltip
	 */
	public TLabel(String text, boolean required, String tooltip) {
		setSpacing(4);
		if ( text.trim().length() > 0 && ! text.endsWith(":") ) {
			text += ":";
		}
		label.setHTML(text + (required ? requiredHtml : ""));
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
	private TLabel setTooltip(String tooltip) {
		if ( ttIcon == null ) {
			ttIcon = VineMain.images.question12().createImage();
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
		}
		else {
			popup.clear();
		}
		HTML widget = new HTML(tooltip);
		popupWidget.setWidget(0, 0, widget);
		popupWidget.setWidth("100%");
		popup.setWidget(popupWidget);
		
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

		this.clear();
		this.add(ttIcon);
		this.add(label);
		
		ttIcon.addClickListener(clickListener);
		label.addClickListener(clickListener);
		
		return this;
	}
}
