package org.mmisw.orrportal.gwt.client.util;

import org.mmisw.orrportal.gwt.client.Orr;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;


/**
 * Tooltip icon: User will click icon for the
 * tootip text to be displayed. KEY_ENTER and KEY_ESCAPE make the popup disappear.
 * 
 * <p>
 * Functionality quickly extracted from {@link TLabel}, which was not adjusted at time of writing.
 * 
 * @author Carlos Rueda
 */
public class TooltipIcon  {
	
	private Image ttIcon;
	private Grid popupWidget = new Grid(1,1);
	private DecoratedPopupPanel popup;
	private ClickListener clickListener;


	/**
	 * Constructor 
	 * @param tooltip
	 */
	public TooltipIcon(String tooltip) {
		ttIcon = Orr.images.question12t().createImage();
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
		HTML widget = new HTML(tooltip);
		popupWidget.setWidget(0, 0, widget);
		popupWidget.setWidth("100%");
		popup.setWidget(popupWidget);
		
		clickListener = new ClickListener() {
			public void onClick(Widget sender) {
				int left = sender.getAbsoluteLeft() + 10;
				int top = sender.getAbsoluteTop() + 20;
				popup.setPopupPosition(left, top);
				popup.show();
			}
		};

		ttIcon.addClickListener(clickListener);
	}

	/** Gets the icon */
	public Image getIcon() {
		return ttIcon;
	}
	
}
