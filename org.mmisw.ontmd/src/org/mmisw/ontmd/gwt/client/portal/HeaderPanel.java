package org.mmisw.ontmd.gwt.client.portal;

import org.mmisw.ontmd.gwt.client.Main;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;

/**
 * Portal header panel.
 * 
 * @author Carlos Rueda
 */
public class HeaderPanel extends FlexTable {

	
	HeaderPanel(LoginControlPanel loginControlPanel) {
		super();
		
		FlexTable flexPanel = this;
		flexPanel.setWidth("100%");
//		flexPanel.setBorderWidth(1);
		int row = 0;
		
		flexPanel.setWidget(row, 0, Main.images.mmior().createImage());
		flexPanel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
		);

		flexPanel.setWidget(row, 1, loginControlPanel);
		flexPanel.getFlexCellFormatter().setAlignment(row, 1, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_TOP
		);

	}
	

}
