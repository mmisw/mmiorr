package org.mmisw.voc2rdf.gwt.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Captures login info and starts the upload to the registry.
 * 
 * @author Carlos Rueda
 */
public class UserPanel extends VerticalPanel {

	private MainPanel mainPanel;
	private CellPanel container = new VerticalPanel();
	
	private PushButton uploadButton;
	
	private Map<String, Widget> widgets = new HashMap<String, Widget>();
	
	
	UserPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		DecoratorPanel decPanel = new DecoratorPanel();
	    decPanel.setWidget(container);
	    add(decPanel);

	    container.add(createForm());
	}

	private Widget createForm() {
		FlexTable panel = new FlexTable();
		panel.setCellSpacing(5);
		
		int row = 0;
		
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, new Label("Your account in the MMI Registry"));
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;

		
		String[] attrNames =  { "userId", "userPassword" };
		String[] attrLabels = { "Username:", "Password:" };
		
		for ( int i = 0; i < attrNames.length; i++ ) {
			String attrName = attrNames[i];
			String attrLabel = attrLabels[i];
			
			Widget widget;
			final TextBox tb = "userPassword".equals(attrName) ? new PasswordTextBox() : new TextBox();
			tb.setName(attrName );
			tb.setWidth("200");
			
			widget = tb;
				
			widgets.put(attrName, widget);
				
			Label lbl = new Label(attrLabel);
			panel.setWidget(row, 0, lbl);
			panel.setWidget(row, 1, widget);
			panel.getFlexCellFormatter().setAlignment(row, 0, 
					HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			panel.getFlexCellFormatter().setAlignment(row, 1, 
					HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE
			);
			row++;
		}
		
		CellPanel buttons = createButtons();
		panel.getFlexCellFormatter().setColSpan(row, 0, 2);
		panel.setWidget(row, 0, buttons);
		panel.getFlexCellFormatter().setAlignment(row, 0, 
				HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE
		);
		row++;
		
		return panel;
	}
	
	private CellPanel createButtons() {
		CellPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		
		uploadButton = new PushButton("Upload", new ClickListener() {
			public void onClick(Widget sender) {
				upload();
			}
		});
		panel.add(uploadButton);

		return panel;
	}
	
	String putValues(Map<String, String> values) {
		for ( String attrName : widgets.keySet() ) {
			Widget widget = widgets.get(attrName);
			String value = null;
			if ( widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) widget).getText();
			}
			
			if ( value == null || value.trim().length() == 0 ) {
				return attrName+ "\n   A value is required.";
			}
			values.put(attrName, value.trim());
		}
		return null;
	}

	private void upload() {
		Map<String, String> values = new HashMap<String, String>();
		putValues(values);
		for ( String attrName : widgets.keySet() ) {
			Widget widget = widgets.get(attrName);
			String value = null;
			if ( widget instanceof TextBoxBase ) {
				value = ((TextBoxBase) widget).getText();
			}
			
			if ( value == null || value.trim().length() == 0 ) {
				Window.alert(attrName+ "\n   A value is required.");
				return;
			}
			values.put(attrName, value.trim());
		}
		mainPanel.doUpload(values);
	}


}
